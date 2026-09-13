/**
 * One login per module, and enough data behind each to actually use it.
 *
 * Everything goes through the running app's own REST API, not SQL. That is
 * deliberate: the accounts end up with properly hashed passwords and real role
 * grants, the data ends up with real document numbers and bills, and the script
 * doubles as an end-to-end test of the flows it walks — a failure here is a
 * failure a user would have hit.
 *
 * The one thing it cannot do through the API is facility-type, which no endpoint
 * exposes: run facility-type.sql once first (see its header).
 *
 * Idempotent by name. Every create is attempted, and an existing username, patient
 * or document is reported as "exists" rather than aborting the run.
 *
 *   node setup.mjs
 */
const API = process.env.LOCDOC_API ?? 'http://localhost:7321/lockdoc';

/** Seeded accounts keep the bootstrap password; new ones get this. */
const SEEDED_PASSWORD = 'FP@Admin#123';
const PASSWORD = 'Locdoc@2026';

const ACCOUNTS = [
  { username: 'reception1', fullName: 'Priya Front Desk', roleCode: 'RECEPTIONIST', module: 'OP / Front desk' },
  { username: 'pharmacist1', fullName: 'Ravi Pharmacy', roleCode: 'PHARMACIST', module: 'Pharmacy' },
];

const DOCTOR = {
  username: 'doctor1',
  password: PASSWORD,
  fullName: 'Dr. Meera Sundaram',
  registrationNumber: 'TSMC-2026-77421',
  specialties: 'General Medicine, Diabetology',
  email: 'meera.sundaram@example.com',
};

const PATIENTS = [
  { fullName: 'Anand Krishnan', gender: 'MALE', dateOfBirth: '1979-04-12', phone: '9840011221', address: 'T Nagar, Chennai', allergies: 'Penicillin' },
  { fullName: 'Fatima Begum', gender: 'FEMALE', dateOfBirth: '1992-11-30', phone: '9840011222', address: 'Adyar, Chennai' },
  { fullName: 'Joseph Mathew', gender: 'MALE', dateOfBirth: '1965-02-08', phone: '9840011223', address: 'Velachery, Chennai' },
  { fullName: 'Sneha Ravi', gender: 'FEMALE', dateOfBirth: '2001-07-19', phone: '9840011224', address: 'Anna Nagar, Chennai', allergies: 'Sulfa drugs' },
  { fullName: 'Bhaskar Rao', gender: 'MALE', dateOfBirth: '1958-09-03', phone: '9840011225', address: 'Mylapore, Chennai' },
  { fullName: 'Lakshmi Narayanan', gender: 'FEMALE', dateOfBirth: '1986-12-22', phone: '9840011226', address: 'Kodambakkam, Chennai' },
];

const WEEKDAYS = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

// ---------------------------------------------------------------- plumbing

async function call(token, method, path, body) {
  const res = await fetch(`${API}${path}`, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await res.text();
  let parsed;
  try {
    parsed = text ? JSON.parse(text) : null;
  } catch {
    parsed = text;
  }
  return { ok: res.ok, status: res.status, body: parsed };
}

async function login(username, password) {
  const res = await call(null, 'POST', '/auth/login', { username, password });
  if (!res.ok) throw new Error(`login failed for ${username}: ${res.status} ${JSON.stringify(res.body)}`);
  return res.body;
}

/** A create whose "already exists" is success, not failure — this script is meant to be re-runnable. */
async function ensure(label, fn) {
  const res = await fn();
  if (res.ok) {
    console.log(`  created  ${label}`);
    return res.body;
  }
  const message = String(res.body?.message ?? res.body ?? '');
  if (res.status === 409 || /already|duplicate|exists/i.test(message)) {
    console.log(`  exists   ${label}`);
    return null;
  }
  console.log(`  FAILED   ${label} -> ${res.status} ${message}`);
  return null;
}

function isoDay(offset = 0) {
  const d = new Date();
  d.setDate(d.getDate() + offset);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

// ---------------------------------------------------------------- the run

const admin = await login('BOOTSTRAP_ADMIN', SEEDED_PASSWORD);
const superAdmin = await login('FP_USER', SEEDED_PASSWORD);
console.log(`Facility ${admin.facilityId}, signed in as BOOTSTRAP_ADMIN.\n`);

// 1. Module logins --------------------------------------------------------
console.log('Accounts');
for (const account of ACCOUNTS) {
  await ensure(`${account.username} (${account.roleCode})`, () =>
    call(admin.token, 'POST', '/facility/users', {
      username: account.username,
      password: PASSWORD,
      fullName: account.fullName,
      roleCode: account.roleCode,
    }),
  );
}

// A doctor is not a facility user: they register themselves, a Super Admin verifies the
// identity, and only then can a facility map them (§8.1). Walking that real three-step
// flow is the only way to end up with a usable doctor login.
await ensure(`${DOCTOR.username} (DOCTOR, self-registered)`, () => call(null, 'POST', '/platform/doctors/register', DOCTOR));

const doctorsPage = await call(superAdmin.token, 'GET', `/platform/doctors?page=0&size=100`);
const platformDoctor = (doctorsPage.body?.content ?? []).find((d) => d.registrationNumber === DOCTOR.registrationNumber);
const doctorId = platformDoctor?.id ?? null;
if (platformDoctor && platformDoctor.verificationStatus === 'PENDING') {
  await ensure(`${DOCTOR.username} verified by Super Admin`, () =>
    call(superAdmin.token, 'POST', `/platform/doctors/${platformDoctor.id}/verify`),
  );
} else if (platformDoctor) {
  console.log(`  exists   ${DOCTOR.username} already ${platformDoctor.verificationStatus.toLowerCase()}`);
}

// 2. The doctor's relationship with this facility -------------------------
console.log('\nDoctor setup');
let doctorToken = null;
if (doctorId) {
  doctorToken = (await login(DOCTOR.username, PASSWORD)).token;

  const mine = await call(doctorToken, 'GET', '/doctor/facility-mappings');
  let mapping = (mine.body ?? []).find((m) => ['REQUESTED', 'ACCEPTED'].includes(m.status));
  if (!mapping) {
    const requested = await ensure('facility requested by the doctor', () =>
      call(doctorToken, 'POST', '/doctor/facility-mappings', {
        facilityId: admin.facilityId,
        relationshipType: 'VISITS_ONLY',
        consultationHours: [
          { weekday: 'MONDAY', startTime: '09:00', endTime: '13:00' },
          { weekday: 'WEDNESDAY', startTime: '09:00', endTime: '13:00' },
          { weekday: 'FRIDAY', startTime: '17:00', endTime: '20:00' },
        ],
      }),
    );
    mapping = requested;
  } else {
    console.log('  exists   doctor already mapped to this facility');
  }
  if (mapping && mapping.status === 'REQUESTED') {
    await ensure('request approved by the facility', () =>
      call(admin.token, 'POST', `/facility/doctor-mappings/${mapping.id}/approve`),
    );
  }

  // A fee is proposed by the doctor and approved by the facility — neither side does both.
  const rates = await call(doctorToken, 'GET', `/doctor/consultation-rates?facilityId=${admin.facilityId}`);
  if ((rates.body ?? []).length === 0) {
    for (const [dayNight, amount] of [['DAY', 450], ['NIGHT', 650]]) {
      const proposed = await ensure(`fee proposed (${dayNight} ₹${amount})`, () =>
        call(doctorToken, 'POST', '/doctor/consultation-rates', {
          facilityId: admin.facilityId,
          orgType: 'DIRECT',
          dayNightIndicator: dayNight,
          totalAmount: amount,
          hospitalPercent: 40,
        }),
      );
      if (proposed?.id) {
        await ensure(`fee approved (${dayNight})`, () => call(admin.token, 'POST', `/facility/consultation-rates/${proposed.id}/approve`));
      }
    }
  } else {
    console.log('  exists   consultation fees already proposed');
  }

  // Sessions are the facility's decision (capacity, overbooking, effective dates), which
  // is why they are created here and not from the hours the doctor stated.
  const schedules = await call(admin.token, 'GET', '/op/doctor-schedules');
  if ((schedules.body ?? []).filter((s) => s.doctorId === doctorId).length === 0) {
    const today = WEEKDAYS[new Date().getDay()];
    const sessions = [
      ['MONDAY', 'Morning OPD', '09:00', '13:00', 20],
      ['WEDNESDAY', 'Morning OPD', '09:00', '13:00', 20],
      ['FRIDAY', 'Evening OPD', '17:00', '20:00', 15],
    ];
    // Always give today a session, or the day list and booking screens are empty on first look.
    if (!sessions.some(([weekday]) => weekday === today)) {
      sessions.push([today, 'Today Clinic', '09:00', '20:00', 25]);
    }
    for (const [weekday, sessionName, startTime, endTime, capacity] of sessions) {
      await ensure(`session ${weekday} ${sessionName} ${startTime}-${endTime}`, () =>
        call(admin.token, 'POST', '/op/doctor-schedules', {
          doctorId,
          facilityId: admin.facilityId,
          weekday,
          sessionName,
          startTime,
          endTime,
          capacity,
          overbookAllowance: 2,
          effectiveFrom: isoDay(-30),
        }),
      );
    }
  } else {
    console.log('  exists   doctor sessions already defined');
  }

  await ensure('free-review policy (15 days / 2 visits)', () =>
    call(admin.token, 'PUT', `/facility/free-review-policies/${doctorId}`, { maxDays: 15, maxVisits: 2 }),
  );
  await ensure("today's status set to At facility", () =>
    call(doctorToken, 'POST', '/doctor/status', { facilityId: admin.facilityId, status: 'AT_FACILITY' }),
  );
}

// 3. What it costs to be a patient here ------------------------------------
console.log('\nOP setup');
await ensure('registration fee (₹300 first, ₹150 again, valid 365 days)', () =>
  call(admin.token, 'PUT', '/op/registration-fee-config', { firstFee: 300, reRegistrationFee: 150, validityDays: 365 }),
);

// 4. OP data: patients, registrations, appointments, visits, bills ---------
const recep = await login('reception1', PASSWORD);
const patientIds = [];
for (const patient of PATIENTS) {
  const found = await call(recep.token, 'GET', `/op/patients?search=${encodeURIComponent(patient.phone)}&size=5`);
  const already = (found.body?.content ?? [])[0];
  if (already) {
    console.log(`  exists   ${patient.fullName} (${already.mrn})`);
    patientIds.push(already.id);
    continue;
  }
  const created = await ensure(patient.fullName, () => call(recep.token, 'POST', '/op/patients', patient));
  if (created?.id) patientIds.push(created.id);
}

// Register four of six, so the desk has both "walks straight through" and "has to
// register first" cases to look at.
for (const id of patientIds.slice(0, 4)) {
  const latest = await call(recep.token, 'GET', `/op/registrations/latest?patientId=${id}`);
  if (latest.body) {
    console.log(`  exists   registration for patient ${id}`);
    continue;
  }
  await ensure(`registration for patient ${id}`, () => call(recep.token, 'POST', '/op/registrations', { patientId: id }));
}

const sessionsToday = await call(recep.token, 'GET', '/op/doctor-schedules');
const weekday = WEEKDAYS[new Date().getDay()];
const session = (sessionsToday.body ?? []).find((s) => s.active && s.weekday === weekday && (!doctorId || s.doctorId === doctorId));

if (!session) {
  console.log('  SKIPPED  no session runs today, so no appointments were booked');
} else {
  const existing = await call(recep.token, 'GET', '/op/appointments/today');
  if ((existing.body ?? []).length > 0) {
    console.log(`  exists   ${existing.body.length} appointment(s) already booked today`);
  } else {
    const slots = ['09:15', '09:30', '09:45', '10:00', '10:15', '10:30'];
    const channels = ['FRONT_DESK', 'PHONE', 'APP', 'FRONT_DESK', 'PHONE', 'APP'];
    const purposes = ['CONSULTATION', 'CONSULTATION', 'CONSULTATION', 'PROCEDURE', 'CONSULTATION', 'CONSULTATION'];
    const booked = [];
    for (let i = 0; i < patientIds.length; i += 1) {
      const appointment = await ensure(`appointment ${slots[i]} (${purposes[i].toLowerCase()}, ${channels[i].toLowerCase()})`, () =>
        call(recep.token, 'POST', '/op/appointments', {
          patientId: patientIds[i],
          doctorScheduleId: session.id,
          appointmentTs: `${isoDay(0)}T${slots[i]}:00`,
          purpose: purposes[i],
          channel: channels[i],
        }),
      );
      if (appointment?.id) booked.push({ ...appointment, purpose: purposes[i] });
    }

    // The first three arrive: one waiting, one with the doctor, one finished and rated —
    // so the day list has a row in each of its three columns.
    for (let i = 0; i < Math.min(3, booked.length); i += 1) {
      const appointment = booked[i];
      const visit = await ensure(`check-in for ${appointment.patientName}`, () =>
        call(recep.token, 'POST', '/op/visits', {
          patientId: appointment.patientId,
          doctorId: appointment.doctorId,
          appointmentId: appointment.id,
        }),
      );
      if (!visit?.id) continue;
      if (appointment.purpose === 'CONSULTATION') {
        await ensure(`consultation billed for ${appointment.patientName}`, () =>
          call(recep.token, 'POST', `/op/visits/${visit.id}/bill-consultation`),
        );
      }
      if (i >= 1) {
        await ensure(`${appointment.patientName} with the doctor`, () => call(recep.token, 'POST', `/op/visits/${visit.id}/start-consultation`));
      }
      if (i === 2) {
        await ensure(`${appointment.patientName} finished`, () => call(recep.token, 'POST', `/op/visits/${visit.id}/complete`));
        await ensure(`${appointment.patientName} rated the visit`, () =>
          call(recep.token, 'POST', `/op/visits/${visit.id}/rating`, { rating: 5, comment: 'Explained everything clearly' }),
        );
      }
    }
  }
}

// Collect some of what is owed and leave the rest — Billing & Collections is only worth
// looking at when it has both.
const due = await call(recep.token, 'GET', '/op/bills/due');
const dueBills = (due.body ?? []).filter((b) => b.due > 0);
for (const bill of dueBills.slice(0, 2)) {
  await ensure(`payment collected on ${bill.billNo} (₹${bill.due})`, () =>
    call(recep.token, 'POST', `/op/bills/${bill.id}/payments`, { amount: bill.due, paymentType: 'CASH' }),
  );
}
console.log(`  left     ${Math.max(dueBills.length - 2, 0)} bill(s) outstanding for Billing & Collections`);

// 5. Pharmacy data: stock in through a real PO → GRN, then a counter sale ---
console.log('\nPharmacy data');
const pharma = await login('pharmacist1', PASSWORD);

const suppliers = await call(pharma.token, 'GET', '/pharmacy/suppliers?size=5');
const supplier = (suppliers.body?.content ?? suppliers.body ?? [])[0];
const medicinesPage = await call(pharma.token, 'GET', '/pharmacy/medicines?page=0&size=5');
const medicines = (medicinesPage.body?.content ?? []).slice(0, 5);

if (!supplier || medicines.length === 0) {
  console.log('  SKIPPED  no supplier or medicine master data to buy against');
} else {
  const purchases = await call(pharma.token, 'GET', '/pharmacy/purchases?size=5');
  if ((purchases.body?.content ?? []).length > 0) {
    console.log('  exists   stock already received against a GRN');
  } else {
    const po = await ensure(`purchase order to ${supplier.name}`, () =>
      call(pharma.token, 'POST', '/pharmacy/purchase-orders', {
        supplierId: supplier.id,
        orderDate: isoDay(0),
        expectedDeliveryDate: isoDay(2),
        items: medicines.map((m) => ({ medicineId: m.id, orderedQty: 500, rate: 3.2, taxPercent: m.taxPercent ?? 12 })),
      }),
    );
    if (po?.id) {
      await ensure(`${po.poNumber ?? 'PO'} approved`, () => call(admin.token, 'POST', `/pharmacy/purchase-orders/${po.id}/approve`));
      const grn = await ensure(`goods received against ${po.poNumber ?? 'the PO'}`, () =>
        call(pharma.token, 'POST', '/pharmacy/purchases', {
          purchaseOrderId: po.id,
          supplierId: supplier.id,
          purchaseDate: isoDay(0),
          supplierInvoiceNumber: `INV-${isoDay(0).replace(/-/g, '')}`,
          supplierInvoiceDate: isoDay(0),
          amountPaid: 0,
          dueDate: isoDay(30),
          items: medicines.map((m, i) => ({
            medicineId: m.id,
            batchNo: `B${isoDay(0).replace(/-/g, '')}${i + 1}`,
            expiryDate: isoDay(540),
            receivedQty: 500,
            freeQty: 10,
            rate: 3.2,
            taxPercent: m.taxPercent ?? 12,
            mrp: 6,
            saleRate: 5.5,
          })),
        }),
      );

      // One counter sale, so the sales register and stock ledger are not empty.
      if (grn?.id && patientIds.length > 0) {
        const batches = await call(pharma.token, 'GET', `/pharmacy/inventory/${medicines[0].id}/batches`);
        const batch = (batches.body ?? [])[0];
        if (batch) {
          await ensure(`counter sale of ${medicines[0].name}`, () =>
            call(pharma.token, 'POST', '/pharmacy/sales', {
              patientId: patientIds[0],
              saleDate: isoDay(0),
              paymentMode: 'CASH',
              amountPaid: 55,
              items: [{ medicineId: medicines[0].id, medicineBatchId: batch.id, qty: 10, rate: 5.5, taxPercent: medicines[0].taxPercent ?? 12 }],
            }),
          );
        }
      }
    }
  }
}

console.log('\n--- Logins ---');
console.log(`  FP_USER          ${SEEDED_PASSWORD}   Super Admin (platform, no facility)`);
console.log(`  BOOTSTRAP_ADMIN  ${SEEDED_PASSWORD}   Hospital/Clinic Admin - masters, approvals, reports`);
console.log(`  reception1       ${PASSWORD}      OP / front desk`);
console.log(`  pharmacist1      ${PASSWORD}      Pharmacy`);
console.log(`  doctor1          ${PASSWORD}      Doctor`);
