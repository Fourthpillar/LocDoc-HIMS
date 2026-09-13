// Generates seed.sql: 30 days of history for every report screen, written straight into
// the DEV H2 database.
//
//   node generate.mjs        -> writes seed.sql (deterministic: same output every run)
//   java -cp h2-2.2.224.jar org.h2.tools.RunScript \
//        -url "jdbc:h2:file:<repo>/data/lockdocdb;AUTO_SERVER=TRUE" -user sa -script seed.sql
//
// Why SQL and not the API: every report is date-ranged, and the API stamps rows with the
// server's clock. There is no way to ask it for last Tuesday, so history has to be
// inserted. Anything that can legitimately be "now" was created through the API instead -
// see ../module-logins/setup.mjs.
//
// seed.sql runs cleanup.sql's deletes first, so re-running replaces the seed rather than
// doubling it, and the whole thing is one transaction. Today is deliberately left alone,
// so the live queue and day list that setup.mjs created stay as they are.
//
// Feeds:
//   Doctor    punctuality (session vs first arrival, per-day status, leave/holiday),
//             consultation count, medicines prescribed
//   OP        registrations, day collection, discounts, cancellations, free reviews,
//             pharmacy conversion, commission (referral/PRO), area-wise consultations
//   Facility  MLC register, audit log
//   Pharmacy  sales register, medicine sales, stock ledger movement
//
// Ids below belong to THIS dev database (see the header of each block). They are the
// accounts ../module-logins/setup.mjs creates, so run that first.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));

const FACILITY = 1;
const DOCTOR = 1; // Dr. Meera Sundaram
const DOCTOR_USER = 5; // doctor1
const RECEPTION_USER = 3; // reception1
const ADMIN_USER = 2; // BOOTSTRAP_ADMIN
const PHARMACIST_USER = 4; // pharmacist1
const STORE = 1; // Main Store

/** The six patients setup.mjs registers, and the areas this script gives them. */
const PATIENTS = [1, 2, 3, 4, 5, 6];

/** Medicines that already exist in the pharmacy catalogue, with a batch each. */
const STOCK = [
  { medicine: 1, batch: 1, name: '1-AL', rate: 5.5 },
  { medicine: 2, batch: 2, name: '1-AL 10MG', rate: 8.0 },
  { medicine: 4, batch: 4, name: 'ACENAC MR', rate: 12.5 },
  { medicine: 6, batch: 6, name: 'AMLOKIND 5', rate: 4.25 },
];

const DAYS = 30;
/** Microsecond marker on every seeded timestamp — how cleanup.sql finds rows with no document number. */
const TAG = '000777';

// ----------------------------------------------------------------- helpers

let state = 20260913;
function rand() {
  state = (state + 0x6d2b79f5) | 0;
  let t = Math.imul(state ^ (state >>> 15), 1 | state);
  t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
  return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
}
const int = (a, b) => a + Math.floor(rand() * (b - a + 1));
const pick = (xs) => xs[Math.floor(rand() * xs.length)];
const chance = (p) => rand() < p;

const q = (s) => `'${String(s).replace(/'/g, "''")}'`;
const ts = (day, time) => `'${day} ${time}.${TAG}'`;
const hhmm = (h, m) => `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:00`;

function isoDay(offsetFromToday) {
  const d = new Date();
  d.setDate(d.getDate() + offsetFromToday);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
const weekdayOf = (day) =>
  ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'][new Date(`${day}T00:00:00`).getDay()];

// Yesterday backwards, so today's live data is untouched.
const DAY_LIST = [];
for (let i = DAYS; i >= 1; i -= 1) DAY_LIST.push(isoDay(-i));

const lines = [];
const say = (s) => lines.push(s);

// ----------------------------------------------------------------- masters
// Area-wise consultations and the commission report have nothing to group by until
// these exist; the facility has none out of the box.

const AREAS = [
  { name: 'T Nagar', city: 'Chennai' },
  { name: 'Adyar', city: 'Chennai' },
  { name: 'Velachery', city: 'Chennai' },
  { name: 'Anna Nagar', city: 'Chennai' },
];
const REFERRERS = ['Dr. Suresh Iyer', 'Dr. Kavitha Menon'];
const PROS = ['Ramesh (PRO)', 'Anitha (PRO)'];

say('-- Masters the grouped reports need. Marked with the 777 microsecond tag.');
AREAS.forEach((a) => {
  say(
    `INSERT INTO areas (facility_id, country, state, city, area_name, active, created_date) ` +
      `VALUES (${FACILITY}, 'India', 'Tamil Nadu', ${q(a.city)}, ${q(a.name)}, TRUE, ${ts(DAY_LIST[0], '08:00:00')});`,
  );
});
REFERRERS.forEach((name, i) => {
  say(
    `INSERT INTO referral_doctors (facility_id, name, contact, registration_no, active, created_date) ` +
      `VALUES (${FACILITY}, ${q(name)}, '98400${11000 + i}', ${q(`TSMC-REF-${100 + i}`)}, TRUE, ${ts(DAY_LIST[0], '08:00:00')});`,
  );
});
PROS.forEach((name, i) => {
  say(
    `INSERT INTO pros (facility_id, name, contact, active, created_date) ` +
      `VALUES (${FACILITY}, ${q(name)}, '98400${12000 + i}', TRUE, ${ts(DAY_LIST[0], '08:00:00')});`,
  );
});

// What each party is actually paid. Without a basis the commission report still lists
// the parties and their billed totals, but every commission reads zero - the number the
// screen exists for. Matched to the party by NAME, which is how the service joins them.
say('');
say('-- Commission basis per party, so the commission column is not all zeroes.');
REFERRERS.forEach((name) => {
  say(
    `INSERT INTO commission_basis (facility_id, party_type, party_name, basis, value_amount, applies_to, active, created_date) ` +
      `VALUES (${FACILITY}, 'REFERRAL_DOCTOR', ${q(name)}, 'PERCENT', 10, 'CONSULTATION', TRUE, ${ts(DAY_LIST[0], '08:00:00')});`,
  );
});
PROS.forEach((name) => {
  say(
    `INSERT INTO commission_basis (facility_id, party_type, party_name, basis, value_amount, applies_to, active, created_date) ` +
      `VALUES (${FACILITY}, 'PRO', ${q(name)}, 'FIXED', 50, 'CONSULTATION', TRUE, ${ts(DAY_LIST[0], '08:00:00')});`,
  );
});

// Spread the existing patients across areas, so area-wise has more than one bar.
say('');
say('-- Patients get an area, so the area-wise report has something to group by.');
PATIENTS.forEach((patientId, i) => {
  say(
    `UPDATE patients SET area_id = (SELECT id FROM areas WHERE facility_id = ${FACILITY} AND area_name = ${q(AREAS[i % AREAS.length].name)}) ` +
      `WHERE id = ${patientId};`,
  );
});

// ----------------------------------------------------------------- sessions
// Punctuality compares a session's start against the doctor's first arrival that day, so
// the range needs sessions that actually ran, plus the exceptions that explain the gaps.

say('');
say('-- A weekly session on every weekday of the range, so punctuality has sessions to measure.');
// When the day's FIRST session starts, per weekday. Punctuality measures arrival against
// exactly that, so a status stamped at 09:05 on a day whose only session is the 17:00
// evening clinic reads as eight hours early — and the average delay goes negative.
// Monday/Wednesday/Saturday mornings and the Friday evening clinic come from
// ../module-logins/setup.mjs; Tuesday/Thursday are seeded above.
const FIRST_SESSION_HOUR = { MONDAY: 9, TUESDAY: 9, WEDNESDAY: 9, THURSDAY: 9, FRIDAY: 17, SATURDAY: 9 };
say(
  `INSERT INTO doctor_schedules (doctor_id, facility_id, weekday, session_name, start_time, end_time, capacity, overbook_allowance, effective_from, effective_to, active, created_date) ` +
    `SELECT ${DOCTOR}, ${FACILITY}, w, 'Report Seed OPD', '09:00:00', '13:00:00', 20, 2, '${DAY_LIST[0]}', NULL, TRUE, ${ts(DAY_LIST[0], '07:00:00')} ` +
    `FROM (SELECT 'TUESDAY' AS w UNION ALL SELECT 'THURSDAY') days;`,
);

// ----------------------------------------------------------------- the days

/** The last paid consultation per patient — what a later free review is counted against. */
const lastPaidVisitByPatient = {};
let visitSeq = 0;
let billSeq = 0;
let regSeq = 0;
let invoiceSeq = 0;

const COMPLAINTS = ['Fever and body ache', 'Persistent cough', 'Follow-up for diabetes', 'Headache', 'Back pain', 'Acidity'];
const DIAGNOSES = ['Viral fever', 'Upper respiratory infection', 'Type 2 diabetes - stable', 'Tension headache', 'Lumbar strain', 'Gastritis'];
const MEDS = [
  { name: 'Paracetamol 500mg', generic: 'Paracetamol', strength: '500mg', match: 1 },
  { name: 'Amoxicillin 500mg', generic: 'Amoxicillin', strength: '500mg', match: 2 },
  { name: 'Metformin 500mg', generic: 'Metformin', strength: '500mg', match: null },
  { name: 'Pantoprazole 40mg', generic: 'Pantoprazole', strength: '40mg', match: 4 },
  { name: 'Cetirizine 10mg', generic: 'Cetirizine', strength: '10mg', match: 6 },
  { name: 'Diclofenac gel', generic: 'Diclofenac', strength: '1%', match: null },
];

say('');
say('-- 30 days of visits, bills, payments, prescriptions, ratings and pharmacy sales.');

for (const day of DAY_LIST) {
  const weekday = weekdayOf(day);
  if (weekday === 'SUNDAY') continue; // the clinic is shut

  // One day in the range is leave, one is a holiday — punctuality shows both as
  // "no session" rather than as lateness, which is the distinction worth testing.
  const isLeave = day === DAY_LIST[8];
  const isHoliday = day === DAY_LIST[17];
  if (isLeave || isHoliday) {
    say(
      `INSERT INTO schedule_exceptions (doctor_id, facility_id, doctor_schedule_id, exception_date, exception_type, reason, created_by_user_id, created_date) ` +
        `VALUES (${DOCTOR}, ${FACILITY}, NULL, '${day}', ${isLeave ? "'LEAVE'" : "'HOLIDAY'"}, ${isLeave ? q('Personal leave') : q('Public holiday')}, ` +
        `${isLeave ? DOCTOR_USER : ADMIN_USER}, ${ts(day, '07:30:00')});`,
    );
    continue;
  }

  // How late the doctor was that day: mostly on time, sometimes late, occasionally
  // no status set at all — the three cases the punctuality report distinguishes.
  const statusRoll = rand();
  const lateMinutes = statusRoll < 0.55 ? int(-10, 0) : statusRoll < 0.85 ? int(5, 40) : null;
  const firstHour = FIRST_SESSION_HOUR[weekday] ?? 9;
  if (lateMinutes !== null) {
    const arriveMinute = lateMinutes;
    const h = firstHour + Math.floor(arriveMinute / 60);
    const m = ((arriveMinute % 60) + 60) % 60;
    say(
      `INSERT INTO doctor_statuses (doctor_id, facility_id, session_date, status, source, set_by_user_id, created_date) ` +
        `VALUES (${DOCTOR}, ${FACILITY}, '${day}', 'AT_FACILITY', 'SELF', ${DOCTOR_USER}, ${ts(day, hhmm(h, m))});`,
    );
  }

  const visitsToday = int(3, 7);
  for (let v = 0; v < visitsToday; v += 1) {
    visitSeq += 1;
    const patient = pick(PATIENTS);
    const opNo = `OP-RPT-${String(visitSeq).padStart(5, '0')}`;
    const arriveHour = firstHour + Math.floor(v / 2);
    const arriveMin = int(0, 55);
    const arrived = hhmm(arriveHour, arriveMin);
    const startMin = arriveMin + int(5, 25);
    const consultStart = hhmm(arriveHour + Math.floor(startMin / 60), startMin % 60);
    const endMin = startMin + int(8, 30);
    const consultEnd = hhmm(arriveHour + Math.floor(endMin / 60), endMin % 60);

    // Roughly one visit in twelve is a medico-legal case, so the MLC register is not empty.
    const isMlc = chance(0.08);
    // A quarter carry a referral or a PRO, which is what the commission report pays on.
    const referral = chance(0.18);
    const pro = !referral && chance(0.12);
    const visitType = chance(0.4) ? 'FOLLOW_UP' : 'NEW';

    say(
      `INSERT INTO op_visits (op_no, facility_id, patient_id, doctor_id, org_type, visit_type, weight_kg, height_cm, temperature_f, bp, status, mlc_flag, ` +
        `${isMlc ? 'mlc_police_station, mlc_number, ' : ''}${referral ? 'referral_doctor_id, ' : ''}${pro ? 'pro_id, ' : ''}arrived_ts, consult_start_ts, consult_end_ts) ` +
        `VALUES (${q(opNo)}, ${FACILITY}, ${patient}, ${DOCTOR}, 'DIRECT', ${q(visitType)}, ${int(52, 88)}, ${int(150, 182)}, ${(97 + rand() * 3).toFixed(1)}, ` +
        `${q(`${int(110, 140)}/${int(70, 90)}`)}, 'COMPLETED', ${isMlc ? 'TRUE' : 'FALSE'}, ` +
        `${isMlc ? `${q('Adyar Police Station')}, ${q(`MLC/${visitSeq}/2026`)}, ` : ''}` +
        `${referral ? `(SELECT MIN(id) FROM referral_doctors WHERE facility_id = ${FACILITY}), ` : ''}` +
        `${pro ? `(SELECT MIN(id) FROM pros WHERE facility_id = ${FACILITY}), ` : ''}` +
        `${ts(day, arrived)}, ${ts(day, consultStart)}, ${ts(day, consultEnd)});`,
    );

    // The consultation bill. Some are free reviews (₹0) — the free-review report reads
    // exactly those, and the day-collection report must not count them as revenue.
    const isFreeReview = visitType === 'FOLLOW_UP' && Boolean(lastPaidVisitByPatient[patient]) && chance(0.35);
    const gross = isFreeReview ? 0 : pick([450, 450, 450, 650]);
    const discountAmount = !isFreeReview && chance(0.12) ? Math.round(gross * 0.1) : 0;
    const net = gross - discountAmount;
    const paid = net === 0 ? 0 : chance(0.85) ? net : Math.round(net / 2);
    const due = net - paid;
    billSeq += 1;
    const billNo = `BILL-RPT-${String(billSeq).padStart(5, '0')}`;
    say(
      `INSERT INTO bills (facility_id, patient_id, bill_no, encounter_type, encounter_id, gross, discount, tax, net, paid, due, status, created_by_user_id, created_date, refund_status) ` +
        `VALUES (${FACILITY}, ${patient}, ${q(billNo)}, 'CONSULTATION', (SELECT id FROM op_visits WHERE op_no = ${q(opNo)}), ${gross}, ${discountAmount}, 0, ${net}, ${paid}, ${due}, ` +
        `${q(due === 0 ? 'PAID' : paid === 0 ? 'UNPAID' : 'PARTIALLY_PAID')}, ${RECEPTION_USER}, ${ts(day, consultStart)}, 'NONE');`,
    );
    if (paid > 0) {
      say(
        `INSERT INTO payments (bill_id, facility_id, party_type, payment_type, amount, paid_at, received_by_user_id) ` +
          `VALUES ((SELECT id FROM bills WHERE bill_no = ${q(billNo)}), ${FACILITY}, 'PATIENT', ${q(pick(['CASH', 'CASH', 'UPI', 'CARD']))}, ${paid}, ${ts(day, consultEnd)}, ${RECEPTION_USER});`,
      );
    }
    if (discountAmount > 0) {
      say(
        `INSERT INTO discounts (bill_id, facility_id, discount_kind, discount_value, amount, reason, status, requested_by_user_id, approved_by_user_id, approved_at, created_date) ` +
          `VALUES ((SELECT id FROM bills WHERE bill_no = ${q(billNo)}), ${FACILITY}, 'PERCENT', 10, ${discountAmount}, ${q(pick(['Staff relative', 'Camp patient', 'Goodwill']))}, ` +
          `'APPROVED', ${RECEPTION_USER}, ${ADMIN_USER}, ${ts(day, consultEnd)}, ${ts(day, consultStart)});`,
      );
    }

    // A free review is only a free review because it points at the paid consultation it
    // was counted against; the report reads those links, not the zero on the bill.
    if (isFreeReview && lastPaidVisitByPatient[patient]) {
      say(
        `INSERT INTO free_review_links (patient_id, doctor_id, facility_id, op_visit_id, original_op_visit_id, created_date) ` +
          `VALUES (${patient}, ${DOCTOR}, ${FACILITY}, (SELECT id FROM op_visits WHERE op_no = ${q(opNo)}), ` +
          `(SELECT id FROM op_visits WHERE op_no = ${q(lastPaidVisitByPatient[patient])}), ${ts(day, consultStart)});`,
      );
    }
    if (!isFreeReview) {
      lastPaidVisitByPatient[patient] = opNo;
    }

    // The clinical record: a note, a prescription, and a rating on most visits.
    say(
      `INSERT INTO consultation_notes (op_visit_id, doctor_id, facility_id, chief_complaint, examination_findings, provisional_diagnosis, treatment_plan, follow_up_plan, is_draft, created_date, updated_date) ` +
        `VALUES ((SELECT id FROM op_visits WHERE op_no = ${q(opNo)}), ${DOCTOR}, ${FACILITY}, ${q(pick(COMPLAINTS))}, ${q('Vitals stable, systemic examination unremarkable')}, ` +
        `${q(pick(DIAGNOSES))}, ${q('Symptomatic treatment, hydration, rest')}, ${q(chance(0.5) ? 'Review after 5 days' : 'Review if symptoms persist')}, FALSE, ${ts(day, consultEnd)}, ${ts(day, consultEnd)});`,
    );

    const lineCount = int(1, 3);
    say(
      `INSERT INTO prescriptions (op_visit_id, doctor_id, facility_id, is_draft, created_date, updated_date) ` +
        `VALUES ((SELECT id FROM op_visits WHERE op_no = ${q(opNo)}), ${DOCTOR}, ${FACILITY}, FALSE, ${ts(day, consultEnd)}, ${ts(day, consultEnd)});`,
    );
    for (let l = 0; l < lineCount; l += 1) {
      const med = pick(MEDS);
      say(
        `INSERT INTO prescription_lines (prescription_id, line_order, medicine_name, generic_name, strength, dosage, route, frequency, duration, quantity, refill_flag, matched_pharmacy_medicine_id) ` +
          `VALUES ((SELECT p.id FROM prescriptions p JOIN op_visits v ON v.id = p.op_visit_id WHERE v.op_no = ${q(opNo)}), ${l + 1}, ${q(med.name)}, ${q(med.generic)}, ${q(med.strength)}, ` +
          `${q('1 tablet')}, 'ORAL', ${q(pick(['OD', 'BD', 'TDS']))}, ${q(pick(['3 days', '5 days', '7 days']))}, ${int(3, 15)}, FALSE, ${med.match ?? 'NULL'});`,
      );
    }
    if (chance(0.6)) {
      say(
        `INSERT INTO consultation_ratings (op_visit_id, doctor_id, facility_id, rating, comment, rated_by_user_id, created_date, updated_date) ` +
          `VALUES ((SELECT id FROM op_visits WHERE op_no = ${q(opNo)}), ${DOCTOR}, ${FACILITY}, ${pick([5, 5, 4, 4, 3])}, ` +
          `${q(pick(['Very patient', 'Explained clearly', 'Short wait', 'Good consultation']))}, ${RECEPTION_USER}, ${ts(day, consultEnd)}, ${ts(day, consultEnd)});`,
      );
    }

    // Roughly half walk across to the pharmacy afterwards — that ratio is the whole
    // point of the OP → pharmacy conversion report.
    if (chance(0.5)) {
      invoiceSeq += 1;
      const item = pick(STOCK);
      const qty = int(5, 20);
      const amount = Number((item.rate * qty).toFixed(2));
      const invoiceNo = `SI-RPT-${String(invoiceSeq).padStart(5, '0')}`;
      say(
        `INSERT INTO sales_invoices (invoice_number, patient_id, sale_date, payment_mode, subtotal, tax_amount, discount_amount, total_amount, amount_paid, balance_due, status, created_by, created_date, round_off_amount, facility_id, store_id) ` +
          `VALUES (${q(invoiceNo)}, ${patient}, '${day}', ${q(pick(['CASH', 'UPI']))}, ${amount}, 0, 0, ${amount}, ${amount}, 0, 'POSTED', ${PHARMACIST_USER}, ${ts(day, consultEnd)}, 0, ${FACILITY}, ${STORE});`,
      );
      say(
        `INSERT INTO sales_invoice_items (sales_invoice_id, medicine_id, medicine_batch_id, qty, rate, tax_percent, discount_amount, amount, created_date) ` +
          `VALUES ((SELECT id FROM sales_invoices WHERE invoice_number = ${q(invoiceNo)}), ${item.medicine}, ${item.batch}, ${qty}, ${item.rate}, 0, 0, ${amount}, ${ts(day, consultEnd)});`,
      );
      // The ledger is what the stock reports read; balance is indicative dev data, not a
      // recomputation of the real running balance.
      say(
        `INSERT INTO stock_ledger_entries (medicine_id, medicine_batch_id, txn_type, qty_in, qty_out, balance_qty, reference_type, reference_id, reference_number, txn_date, created_date, facility_id, store_id) ` +
          `VALUES (${item.medicine}, ${item.batch}, 'SALE', 0, ${qty}, ` +
          `(SELECT quantity_on_hand FROM medicine_batches WHERE id = ${item.batch}), 'SALES_INVOICE', ` +
          `(SELECT id FROM sales_invoices WHERE invoice_number = ${q(invoiceNo)}), ${q(invoiceNo)}, '${day}', ${ts(day, consultEnd)}, ${FACILITY}, ${STORE});`,
      );
    }
  }

  // A registration or two most days, so the registrations report has a real curve.
  if (chance(0.5)) {
    regSeq += 1;
    const patient = pick(PATIENTS);
    const regNo = `REG-RPT-${String(regSeq).padStart(5, '0')}`;
    const isRepeat = regSeq % 3 === 0;
    const fee = isRepeat ? 150 : 300;
    billSeq += 1;
    const billNo = `BILL-RPT-${String(billSeq).padStart(5, '0')}`;
    say(
      `INSERT INTO patient_registrations (patient_id, facility_id, registration_no, is_re_registration, expiry_date, registered_at, registered_by_user_id) ` +
        `VALUES (${patient}, ${FACILITY}, ${q(regNo)}, ${isRepeat ? 'TRUE' : 'FALSE'}, DATEADD('DAY', 365, DATE '${day}'), ${ts(day, '09:05:00')}, ${RECEPTION_USER});`,
    );
    say(
      `INSERT INTO bills (facility_id, patient_id, bill_no, encounter_type, encounter_id, gross, discount, tax, net, paid, due, status, created_by_user_id, created_date, refund_status) ` +
        `VALUES (${FACILITY}, ${patient}, ${q(billNo)}, 'REGISTRATION', (SELECT id FROM patient_registrations WHERE registration_no = ${q(regNo)}), ${fee}, 0, 0, ${fee}, ${fee}, 0, 'PAID', ${RECEPTION_USER}, ${ts(day, '09:05:00')}, 'NONE');`,
    );
    say(
      `INSERT INTO payments (bill_id, facility_id, party_type, payment_type, amount, paid_at, received_by_user_id) ` +
        `VALUES ((SELECT id FROM bills WHERE bill_no = ${q(billNo)}), ${FACILITY}, 'PATIENT', 'CASH', ${fee}, ${ts(day, '09:06:00')}, ${RECEPTION_USER});`,
    );
  }

  // The odd cancellation, approved by the admin — the cancellations report is about
  // who asked, why, and who allowed it.
  if (chance(0.15)) {
    say(
      `INSERT INTO cancellations (facility_id, entity_type, entity_id, reason, status, requested_by_user_id, approved_by_user_id, approved_at, created_date) ` +
        `VALUES (${FACILITY}, 'APPOINTMENT', ${int(1, 6)}, ${q(pick(['Patient could not come', 'Doctor unavailable', 'Rescheduled by patient']))}, 'APPROVED', ` +
        `${RECEPTION_USER}, ${ADMIN_USER}, ${ts(day, '12:00:00')}, ${ts(day, '11:30:00')});`,
    );
  }

  // Something for the audit log most days.
  if (chance(0.4)) {
    say(
      `INSERT INTO audit_log (facility_id, actor_user_id, action, entity_type, entity_id, before_data, after_data, occurred_at) ` +
        `VALUES (${FACILITY}, ${ADMIN_USER}, ${q(pick(['DISCOUNT_APPROVE', 'CANCELLATION_APPROVE', 'CONSULTATION_RATE_APPROVE']))}, ` +
        `${q(pick(['Discount', 'Cancellation', 'ConsultationRate']))}, ${int(1, 20)}, 'PENDING', 'APPROVED', ${ts(day, '12:05:00')});`,
    );
  }
}

// --------------------------------------------------------------- purchases
// Three receipts spread across the range, two of them still owing, so the purchase
// register has rows and the payables aging report has something aging.

say('');
say('-- Backdated goods receipts for the purchase register and payables aging.');
[2, 12, 24].forEach((daysAgo, i) => {
  const day = isoDay(-daysAgo);
  const grnNo = `GRN-RPT-${String(i + 1).padStart(5, '0')}`;
  const item = STOCK[i % STOCK.length];
  const qty = 200 + i * 50;
  const rate = 3.2;
  const amount = Number((qty * rate).toFixed(2));
  // The oldest is settled; the newer two are still owed, which is what makes the
  // aging report show a spread rather than one bucket.
  const paid = i === 2 ? amount : i === 1 ? Number((amount / 2).toFixed(2)) : 0;
  const poNo = `PO-RPT-${String(i + 1).padStart(5, '0')}`;
  say(
    `INSERT INTO purchase_orders (po_number, supplier_id, order_date, expected_delivery_date, status, created_by, approved_by, approved_date, created_date, facility_id, store_id) ` +
      `VALUES (${q(poNo)}, ${i + 1}, '${day}', DATEADD('DAY', 2, DATE '${day}'), 'RECEIVED', ${PHARMACIST_USER}, ${ADMIN_USER}, ${ts(day, '10:00:00')}, ${ts(day, '09:30:00')}, ${FACILITY}, ${STORE});`,
  );
  say(
    `INSERT INTO purchases (grn_number, purchase_order_id, supplier_id, purchase_date, supplier_invoice_number, supplier_invoice_date, status, total_amount, created_by, created_date, amount_paid, balance_due, due_date, facility_id, store_id) ` +
      `VALUES (${q(grnNo)}, (SELECT id FROM purchase_orders WHERE po_number = ${q(poNo)}), ${i + 1}, '${day}', ${q(`SINV-${1000 + i}`)}, '${day}', 'POSTED', ${amount}, ${PHARMACIST_USER}, ${ts(day, '11:00:00')}, ${paid}, ${(amount - paid).toFixed(2)}, ` +
      `DATEADD('DAY', 30, DATE '${day}'), ${FACILITY}, ${STORE});`,
  );
  say(
    `INSERT INTO purchase_items (purchase_id, medicine_id, batch_no, expiry_date, received_qty, free_qty, rate, tax_percent, mrp, sale_rate, amount, created_date) ` +
      `VALUES ((SELECT id FROM purchases WHERE grn_number = ${q(grnNo)}), ${item.medicine}, ${q(`B-RPT-${i + 1}`)}, DATEADD('DAY', 540, DATE '${day}'), ${qty}, 10, ${rate}, 0, 6, ${item.rate}, ${amount}, ${ts(day, '11:00:00')});`,
  );
});

// --------------------------------------------------------------- returns
// Two returns against seeded sales — a customer bringing medicine back is ordinary, and
// the returns register should not be the one screen with nothing in it.

say('');
say('-- Sales returns against two of the seeded invoices.');
[1, 2].forEach((n) => {
  const invoiceNo = `SI-RPT-${String(n).padStart(5, '0')}`;
  const returnNo = `SR-RPT-${String(n).padStart(5, '0')}`;
  const day = isoDay(-(20 - n * 5));
  say(
    `INSERT INTO sales_returns (return_number, sales_invoice_id, return_date, reason, total_amount, status, created_by, created_date, facility_id) ` +
      `SELECT ${q(returnNo)}, si.id, '${day}', ${q(n === 1 ? 'Wrong strength dispensed' : 'Patient reaction - returned unopened')}, ` +
      `ROUND(si.total_amount / 2, 2), 'POSTED', ${PHARMACIST_USER}, ${ts(day, '16:00:00')}, ${FACILITY} ` +
      `FROM sales_invoices si WHERE si.invoice_number = ${q(invoiceNo)};`,
  );
  say(
    `INSERT INTO sales_return_items (sales_return_id, sales_invoice_item_id, medicine_id, medicine_batch_id, qty, rate, amount, created_date) ` +
      `SELECT (SELECT id FROM sales_returns WHERE return_number = ${q(returnNo)}), sii.id, sii.medicine_id, sii.medicine_batch_id, ` +
      `GREATEST(1, sii.qty / 2), sii.rate, ROUND(sii.amount / 2, 2), ${ts(day, '16:00:00')} ` +
      `FROM sales_invoice_items sii JOIN sales_invoices si ON si.id = sii.sales_invoice_id WHERE si.invoice_number = ${q(invoiceNo)};`,
  );
});

// ----------------------------------------------------------- today's status
// Punctuality measures the doctor's FIRST status of the day against the session start, so
// a status set late in the evening (setup.mjs runs whenever it runs) reads as arriving
// hours late. This adds an early, seed-owned status for today so the first arrival is a
// sensible one.
//
// It INSERTS rather than rewriting the app's row. An earlier version updated that row's
// timestamp - which stamped the seed's 777 marker onto a row the app created, and the next
// run's cleanup then deleted it. The doctor was left with no status at all while having
// consulted patients, showing "Unavailable" to the whole facility. Never tag a row this
// script did not write.
const todayFirstHour = FIRST_SESSION_HOUR[weekdayOf(isoDay(0))] ?? 9;
if (weekdayOf(isoDay(0)) !== 'SUNDAY') {
  say('');
  say("-- An early status for today, so punctuality's first-arrival is not an evening one.");
  say(
    `INSERT INTO doctor_statuses (doctor_id, facility_id, session_date, status, source, set_by_user_id, created_date) ` +
      `VALUES (${DOCTOR}, ${FACILITY}, DATE '${isoDay(0)}', 'AT_FACILITY', 'SELF', ${DOCTOR_USER}, ${ts(isoDay(0), hhmm(todayFirstHour, 5))});`,
  );
}

// ----------------------------------------------------------------- output

const header = `-- Generated by generate.mjs - DEV DATA ONLY, never a Flyway migration.
-- ${DAY_LIST[0]} .. ${DAY_LIST[DAY_LIST.length - 1]} (${DAYS} days, today excluded).
-- Re-runnable: the cleanup below removes the previous seed first.
SET AUTOCOMMIT FALSE;
`;

const cleanup = fs.readFileSync(path.join(HERE, 'cleanup.sql'), 'utf8')
  .split('\n')
  .filter((l) => l.trim() && !l.trim().startsWith('--'))
  .join('\n');

const sql = [header, '-- ---- cleanup (so re-running replaces rather than doubles) ----', cleanup, '', ...lines, '', 'COMMIT;', ''].join('\n');
fs.writeFileSync(path.join(HERE, 'seed.sql'), sql);

console.log(`seed.sql written: ${lines.length} statements, ${DAY_LIST[0]} .. ${DAY_LIST[DAY_LIST.length - 1]}`);
console.log(`  visits ${visitSeq}, bills ${billSeq}, registrations ${regSeq}, pharmacy sales ${invoiceSeq}`);
