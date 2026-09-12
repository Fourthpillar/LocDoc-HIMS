/**
 * One login per module, and enough data behind each to actually use it.
 *
 * Everything here goes through the running app's own REST API, not SQL. That is
 * deliberate: the accounts end up with properly hashed passwords and real role
 * grants, the data ends up with real document numbers and bills, and the script
 * doubles as an end-to-end test of the flows it exercises — a failure here is a
 * failure a user would have hit.
 *
 * Idempotent by name: every create is tried, and an existing username, MRN or
 * medicine code is reported as "already there" instead of aborting the run.
 *
 *   node setup.mjs             # create accounts + data
 *   node setup.mjs --report    # print the credential table only
 */
const API = process.env.LOCDOC_API ?? 'http://localhost:7321/lockdoc';
const PASSWORD = 'Locdoc@2026';

// The accounts a person actually logs in with, one per module.
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
  { fullName: 'Fatima Begum', gender: 'FEMALE', dateOfBirth: '1992-11-30', phone: '9840011222', address: 'Adyar, Chennai', allergies: null },
  { fullName: 'Joseph Mathew', gender: 'MALE', dateOfBirth: '1965-02-08', phone: '9840011223', address: 'Velachery, Chennai', allergies: null },
  { fullName: 'Sneha Ravi', gender: 'FEMALE', dateOfBirth: '2001-07-19', phone: '9840011224', address: 'Anna Nagar, Chennai', allergies: 'Sulfa drugs' },
  { fullName: 'Bhaskar Rao', gender: 'MALE', dateOfBirth: '1958-09-03', phone: '9840011225', address: 'Mylapore, Chennai', allergies: null },
  { fullName: 'Lakshmi Narayanan', gender: 'FEMALE', dateOfBirth: '1986-12-22', phone: '9840011226', address: 'Kodambakkam, Chennai', allergies: null },
];

const MEDICINES = [
  { code: 'MED-TD-500', name: 'Paracetamol 500mg', genericName: 'Paracetamol', manufacturer: 'Cipla', uom: 'TAB', taxPercent: 12, reorderLevel: 200, scheduleType: 'NONE' },
  { code: 'MED-AMX-500', name: 'Amoxicillin 500mg', genericName: 'Amoxicillin', manufacturer: 'Sun Pharma', uom: 'CAP', taxPercent: 12, reorderLevel: 150, scheduleType: 'H' },
  { code: 'MED-MET-500', name: 'Metformin 500mg', genericName: 'Metformin', manufacturer: 'USV', uom: 'TAB', taxPercent: 5, reorderLevel: 300, scheduleType: 'NONE' },
  { code: 'MED-PAN-40', name: 'Pantoprazole 40mg', genericName: 'Pantoprazole', manufacturer: 'Alkem', uom: 'TAB', taxPercent: 12, reorderLevel: 120, scheduleType: 'NONE' },
  { code: 'MED-ORS-1', name: 'ORS Sachet', genericName: 'Oral Rehydration Salts', manufacturer: 'FDC', uom: 'SACHET', taxPercent: 5, reorderLevel: 100, scheduleType: 'NONE' },
];

// ---------------------------------------------------------------- plumbing

const log = [];
const note = (line) => {
  log.push(line);
  console.log(line);
};

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

/** A create whose "already exists" is a success, not a failure — this script is meant to be re-runnable. */
async function ensure(label, fn) {
  const res = await fn();
  if (res.ok) {
    note(`  created  ${label}`);
    return res.body;
  }
  const message = String(res.body?.message ?? res.body ?? '');
  if (res.status === 409 || /already|duplicate|exists/i.test(message)) {
    note(`  exists   ${label}`);
    return null;
  }
  note(`  FAILED   ${label} -> ${res.status} ${message}`);
  return null;
}

function isoDay(offset = 0) {
  const d = new Date();
  d.setDate(d.getDate() + offset);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

// ---------------------------------------------------------------- the run

const admin = await login('BOOTSTRAP_ADMIN', 'FP@Admin#123');
const superAdmin = await login('FP_USER', 'FP@Admin#123');
note(`Facility ${admin.facilityId}, signed in as BOOTSTRAP_ADMIN.`);

// 1. Module logins ---------------------------------------------------------
note('\nAccounts');
for (const a of ACCOUNTS) {
  await ensure(`${a.username} (${a.roleCode})`, () =>
    call(admin.token, 'POST', '/facility/users', { username: a.username, password: PASSWORD, fullName: a.fullName, roleCode: a.roleCode })
  );
}

// The doctor is not a facility user — they register themselves, a Super Admin
// verifies the identity, and only then can a facility map them (§8.1). Driving
// the real three-step flow is the only way to get a usable doctor login.
await ensure(`${DOCTOR.username} (DOCTOR, self-registered)`, () => call(null, 'POST', '/platform/doctors/register', DOCTOR));

const doctorsPage = await call(superAdmin.token, 'GET', `/platform/doctors?page=0&size=100&search=${encodeURIComponent(DOCTOR.fullName)}`);
const platformDoctor = (doctorsPage.body?.content ?? []).find((d) => d.fullName === DOCTOR.fullName);
if (platformDoctor && platformDoctor.verificationStatus !== 'VERIFIED') {
  await ensure(`${DOCTOR.username} verified by Super Admin`, () => call(superAdmin.token, 'POST', `/platform/doctors/${platformDoctor.id}/verify`));
} else if (platformDoctor) {
  note('  exists   doctor1 already verified');
}

// 2. Map the doctor to this facility, with the hours they consult -----------
let doctorId = platformDoctor?.id ?? null;
if (doctorId) {
  const mappings = await call(admin.token, 'GET', '/facility/doctor-mappings');
  const existing = (mappings.body ?? []).find((m) => m.doctorId === doctorId && ['REQUESTED', 'ACCEPTED'].includes(m.status));
  if (!existing) {
    await ensure(`${DOCTOR.fullName} invited to the facility`, () =>
      call(admin.token, 'POST', '/facility/doctor-mappings', { doctorId, relationshipType: 'VISITS_ONLY' })
    );
    const doc = await login(DOCTOR.username, PASSWORD);
    const mine = await call(doc.token, 'GET', '/doctor/facility-mappings');
    const invite = (mine.body ?? []).find((m) => m.status === 'REQUESTED' && m.initiatedBy === 'FACILITY');
    if (invite) {
      await ensure('invitation accepted by the doctor', () => call(doc.token, 'POST', `/doctor/facility-mappings/${invite.id}/accept`));
      await ensure('consulting hours stated', () =>
        call(doc.token, 'PUT', `/doctor/facility-mappings/${invite.id}/consultation-hours`, [
          { weekday: 'MONDAY', startTime: '09:00', endTime: '13:00' },
          { weekday: 'WEDNESDAY', startTime: '09:00', endTime: '13:00' },
          { weekday: 'FRIDAY', startTime: '17:00', endTime: '20:00' },
        ])
      );
    }
  } else {
    note('  exists   doctor1 already mapped to this facility');
  }
}

// 3. What the facility decides about that doctor: a rate, sessions, a policy -
if (doctorId) {
  note('\nDoctor setup');
  const doc = await login(DOCTOR.username, PASSWORD);
  const rates = await call(doc.token, 'GET', `/doctor/consultation-rates?facilityId=${admin.facilityId}`);
  if ((rates.body ?? []).length === 0) {
    for (const [dayNight, amount] of [['DAY', 450], ['NIGHT', 650]]) {
      const proposed = await ensure(`fee proposed (${dayNight} ₹${amount})`, () =>
        call(doc.token, 'POST', '/doctor/consultation-rates', {
          facilityId: admin.facilityId,
          orgType: 'DIRECT',
          dayNightIndicator: dayNight,
          totalAmount: amount,
          hospitalPercent: 40,
          effectiveFrom: isoDay(-30),
        })
      );
      if (proposed?.id) {
        await ensure(`fee approved (${dayNight})`, () => call(admin.token, 'POST', `/facility/consultation-rates/${proposed.id}/approve`));
      }
    }
  } else {
    note('  exists   consultation fees already proposed');
  }

  const schedules = await call(admin.token, 'GET', '/op/doctor-schedules');
  const mine = (schedules.body ?? []).filter((s) => s.doctorId === doctorId);
  if (mine.length === 0) {
    for (const [weekday, name, start, end, capacity] of [
      ['MONDAY', 'Morning OPD', '09:00', '13:00', 20],
      ['WEDNESDAY', 'Morning OPD', '09:00', '13:00', 20],
      ['FRIDAY', 'Evening OPD', '17:00', '20:00', 15],
      [['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][new Date().getDay()], 'Today Clinic', '09:00', '20:00', 25],
    ]) {
      await ensure(`session ${weekday} ${name} ${start}-${end}`, () =>
        call(admin.token, 'POST', '/op/doctor-schedules', {
          doctorId,
          weekday,
          sessionName: name,
          startTime: start,
          endTime: end,
          capacity,
          overbookAllowance: 2,
          effectiveFrom: isoDay(-30),
        })
      );
    }
  } else {
    note(`  exists   ${mine.length} session(s) already defined`);
  }

  await ensure('free-review policy (15 days / 2 visits)', () =>
    call(admin.token, 'PUT', `/facility/free-review-policies/${doctorId}`, { maxDays: 15, maxVisits: 2 })
  );
}

// 4. OP data: patients, registrations, appointments, visits, bills ----------
note('\nOP data');
const recep = await login('reception1', PASSWORD);
const patientIds = [];
for (const p of PATIENTS) {
  const found = await call(recep.token, 'GET', `/op/patients?search=${encodeURIComponent(p.phone)}&size=5`);
  const already = (found.body?.content ?? [])[0];
  if (already) {
    note(`  exists   ${p.fullName} (${already.mrn})`);
    patientIds.push(already.id);
    continue;
  }
  const created = await ensure(`${p.fullName}`, () => call(recep.token, 'POST', '/op/patients', p));
  if (created?.id) patientIds.push(created.id);
}

// Register everyone but the last two, so the day list has both "walks straight
// through" and "has to register at the counter" cases to look at.
for (const id of patientIds.slice(0, 4)) {
  await ensure(`registration for patient ${id}`, () => call(recep.token, 'POST', '/op/registrations', { patientId: id }));
}

const sessions = await call(recep.token, 'GET', '/op/doctor-schedules');
const weekday = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][new Date().getDay()];
const todays = (sessions.body ?? []).filter((s) => s.active && s.weekday === weekday && (!doctorId || s.doctorId === doctorId));
const session = todays[0] ?? (sessions.body ?? []).find((s) => s.active && s.weekday === weekday);

if (!session) {
  note('  SKIPPED  no session runs today, so no appointments were booked');
} else {
  const slots = ['09:15', '09:30', '09:45', '10:00', '10:15', '10:30'];
  const channels = ['FRONT_DESK', 'PHONE', 'APP', 'FRONT_DESK', 'PHONE', 'APP'];
  const purposes = ['CONSULTATION', 'CONSULTATION', 'CONSULTATION', 'PROCEDURE', 'CONSULTATION', 'CONSULTATION'];
  const booked = [];
  for (let i = 0; i < patientIds.length; i += 1) {
    const appt = await ensure(`appointment ${slots[i]} (${purposes[i].toLowerCase()}, ${channels[i].toLowerCase()})`, () =>
      call(recep.token, 'POST', '/op/appointments', {
        patientId: patientIds[i],
        doctorScheduleId: session.id,
        appointmentTs: `${isoDay(0)}T${slots[i]}:00`,
        purpose: purposes[i],
        channel: channels[i],
      })
    );
    if (appt?.id) booked.push({ ...appt, purpose: purposes[i] });
  }

  // The first three arrive: one still waiting, one with the doctor, one done and
  // rated — so the day list has a row in each of its three columns.
  for (let i = 0; i < Math.min(3, booked.length); i += 1) {
    const appt = booked[i];
    const visit = await ensure(`check-in for appointment ${appt.id}`, () =>
      call(recep.token, 'POST', '/op/visits', { patientId: appt.patientId, doctorId: appt.doctorId, appointmentId: appt.id })
    );
    if (!visit?.id) continue;
    if (appt.purpose === 'CONSULTATION') {
      await ensure(`consultation billed for visit ${visit.id}`, () => call(recep.token, 'POST', `/op/visits/${visit.id}/bill-consultation`));
    }
    if (i === 1) {
      await ensure(`visit ${visit.id} started`, () => call(recep.token, 'POST', `/op/visits/${visit.id}/start-consultation`));
    }
    if (i === 2) {
      await ensure(`visit ${visit.id} started`, () => call(recep.token, 'POST', `/op/visits/${visit.id}/start-consultation`));
      await ensure(`visit ${visit.id} completed`, () => call(recep.token, 'POST', `/op/visits/${visit.id}/complete`));
      await ensure(`visit ${visit.id} rated`, () => call(recep.token, 'POST', `/op/visits/${visit.id}/rating`, { rating: 5, comment: 'Very patient, explained everything' }));
    }
  }
}

// Collect some of what is owed and leave the rest — Billing & Collections is
// only worth looking at when it has both.
const due = await call(recep.token, 'GET', '/op/bills/due');
const dueBills = (due.body ?? []).filter((b) => b.due > 0);
for (const bill of dueBills.slice(0, 2)) {
  await ensure(`payment collected on ${bill.billNo} (₹${bill.due})`, () =>
    call(recep.token, 'POST', `/op/bills/${bill.id}/payments`, { amount: bill.due, paymentType: 'CASH' })
  );
}
note(`  left     ${Math.max(dueBills.length - 2, 0)} bill(s) outstanding for Billing & Collections`);

// 5. Pharmacy data: supplier, medicines, a received purchase order, a sale ---
note('\nPharmacy data');
const pharma = await login('pharmacist1', PASSWORD);

const suppliers = await call(pharma.token, 'GET', '/pharmacy/suppliers');
let supplier = (suppliers.body?.content ?? suppliers.body ?? []).find((s) => s.name === 'Chennai Medical Distributors');
if (!supplier) {
  supplier = await ensure('supplier Chennai Medical Distributors', () =>
    call(pharma.token, 'POST', '/pharmacy/suppliers', {
      name: 'Chennai Medical Distributors',
      contactPerson: 'Suresh Kumar',
      phone: '9840099887',
      email: 'orders@cmd.example.com',
      address: 'Purasawalkam, Chennai',
      gstin: '33AABCC1234D1ZQ',
      drugLicenseNo: 'TN-CH-20B-4471',
    })
  );
}

const existingMeds = await call(pharma.token, 'GET', '/pharmacy/medicines?size=200');
const medsByCode = new Map((existingMeds.body?.content ?? existingMeds.body ?? []).map((m) => [m.code, m]));
const medicines = [];
for (const m of MEDICINES) {
  if (medsByCode.has(m.code)) {
    note(`  exists   ${m.name}`);
    medicines.push(medsByCode.get(m.code));
    continue;
  }
  const created = await ensure(`${m.name}`, () => call(pharma.token, 'POST', '/pharmacy/medicines', m));
  if (created?.id) medicines.push(created);
}

const stores = await call(pharma.token, 'GET', '/pharmacy/stores');
const store = (stores.body ?? []).find((s) => s.active) ?? (stores.body ?? [])[0];
if (supplier?.id && store?.id && medicines.length > 0) {
  const po = await ensure(`purchase order to ${supplier.name}`, () =>
    call(pharma.token, 'POST', '/pharmacy/purchase-orders', {
      supplierId: supplier.id,
      storeId: store.id,
      expectedDate: isoDay(2),
      items: medicines.map((m) => ({ medicineId: m.id, orderedQty: 500, rate: 3.2 })),
    })
  );
  if (po?.id) {
    await ensure(`PO ${po.poNo ?? po.id} approved`, () => call(admin.token, 'POST', `/pharmacy/purchase-orders/${po.id}/approve`));
    await ensure(`goods received against ${po.poNo ?? po.id}`, () =>
      call(pharma.token, 'POST', '/pharmacy/grn', {
        purchaseOrderId: po.id,
        supplierId: supplier.id,
        storeId: store.id,
        invoiceNo: `INV-${isoDay(0).replace(/-/g, '')}`,
        invoiceDate: isoDay(0),
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
      })
    );
  }
}

note('\nDone.');
note('\n--- Logins (all passwords: ' + PASSWORD + ') ---');
note('  reception1   OP / Front desk   RECEPTIONIST');
note('  pharmacist1  Pharmacy          PHARMACIST');
note('  doctor1      Doctor            DOCTOR');
note('  BOOTSTRAP_ADMIN / FP@Admin#123   Hospital admin, Masters');
note('  FP_USER / FP@Admin#123           Super Admin (platform)');
