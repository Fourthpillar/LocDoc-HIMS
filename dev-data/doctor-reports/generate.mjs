// Generates seed.sql: 30 days (2026-08-12 -> 2026-09-10) of Doctor Reports test data for dr.regtest
// (doctor 97, user 130) at Default Facility (1), written straight into the DEV H2 database.
//
//   node generate.mjs                      -> writes seed.sql (deterministic: same output every run)
//   java -cp h2-2.2.224.jar org.h2.tools.RunScript -url "jdbc:h2:file:<repo>/data/lockdocdb;AUTO_SERVER=TRUE" -user sa -script seed.sql
//
// seed.sql runs cleanup.sql's deletes first, so re-running replaces the seed instead of doubling it, and the whole
// thing is one transaction. This is dev data, not a Flyway migration - it references accounts that only exist
// in this dev database. Today is left untouched so the doctor's live status and queue aren't disturbed.
//
// Feeds every Doctor Reports tab:
//   Punctuality  - weekly sessions, per-day status updates (on time / late / none), a holiday, a leave day and a
//                  cancelled session; ratings of the range's consultations
//   Consultation - completed OP visits with consultation notes, consultation bills (+ payments), ratings
//   Medicines    - completed prescriptions with realistic lines
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));

const FACILITY = 1;
const DOCTOR = 97;
const DOCTOR_USER = 130; // dr.regtest
const RECEPTION_USER = 162; // test_reception1
const FIRST_DAY = '2026-08-12';
const LAST_DAY = '2026-09-10';
/** Microsecond marker on every seeded timestamp - how cleanup.sql finds rows with no document number. */
const TAG = '000777';

// ----------------------------------------------------------------- helpers

let state = 20260911;
function rand() {
  state = (state + 0x6d2b79f5) | 0;
  let t = Math.imul(state ^ (state >>> 15), 1 | state);
  t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
  return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
}
const int = (a, b) => a + Math.floor(rand() * (b - a + 1));
const pick = (arr) => arr[Math.floor(rand() * arr.length)];
const chance = (p) => rand() < p;
const weighted = (entries) => {
  let r = rand() * entries.reduce((s, [, w]) => s + w, 0);
  for (const [value, w] of entries) if ((r -= w) < 0) return value;
  return entries[entries.length - 1][0];
};

const pad = (n, w = 2) => String(n).padStart(w, '0');
const sql = (v) =>
  v === null || v === undefined ? 'NULL' : typeof v === 'number' ? String(v) : typeof v === 'boolean' ? (v ? 'TRUE' : 'FALSE') : `'${String(v).replace(/'/g, "''")}'`;
const minutesOf = (hhmm) => {
  const [h, m] = hhmm.split(':').map(Number);
  return h * 60 + m;
};
/** A local timestamp literal on `date` at `minutes` past midnight, carrying the seed marker. */
const ts = (date, minutes) => {
  const m = Math.max(0, Math.min(23 * 60 + 59, Math.round(minutes)));
  return `TIMESTAMP '${date} ${pad(Math.floor(m / 60))}:${pad(m % 60)}:${pad(int(0, 59))}.${TAG}'`;
};
const WEEKDAYS = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
const weekdayOf = (date) => WEEKDAYS[new Date(`${date}T00:00:00Z`).getUTCDay()];
const addDays = (date, n) => {
  const d = new Date(`${date}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() + n);
  return d.toISOString().slice(0, 10);
};
const daysBetween = (a, b) => Math.round((new Date(`${b}T00:00:00Z`) - new Date(`${a}T00:00:00Z`)) / 86_400_000);

// ----------------------------------------------------------------- sessions

// dr.regtest's existing active sessions at facility 1 that fall inside the range (read from the dev DB).
const EXISTING_SESSIONS = [
  { id: 34, weekday: 'FRIDAY', start: '09:00', end: '12:00', from: '2026-01-01', kind: 'OPD' },
  { id: 33, weekday: 'THURSDAY', start: '16:00', end: '19:00', from: '2026-01-01', kind: 'OPD' },
  { id: 65, weekday: 'THURSDAY', start: '10:00', end: '11:00', from: '2026-01-01', kind: 'ROUNDS' },
  { id: 66, weekday: 'THURSDAY', start: '00:30', end: '01:00', from: '2026-01-01', kind: 'EARLY' },
  { id: 131, weekday: 'MONDAY', start: '12:45', end: '14:45', from: '2026-09-07', kind: 'OPD' },
];
// Existing SESSION_MOVE exceptions - the report counts these sessions as blocked.
const EXISTING_BLOCKED = new Set(['2026-09-07|131', '2026-09-10|65', '2026-09-10|66']);

// A regular morning OPD on the other weekdays, so most days of the range have a session to be punctual for.
// Tuesday ends the day before the doctor's own "New Live Test Session" (Tue 09:00, from 2026-09-10) starts.
const NEW_SESSIONS = [
  { weekday: 'MONDAY', start: '09:00', end: '12:00', from: '2026-08-01', to: null },
  { weekday: 'TUESDAY', start: '09:00', end: '12:00', from: '2026-08-01', to: '2026-09-09' },
  { weekday: 'WEDNESDAY', start: '09:00', end: '12:00', from: '2026-08-01', to: null },
  { weekday: 'SATURDAY', start: '09:00', end: '12:00', from: '2026-08-01', to: null },
].map((s) => ({ ...s, kind: 'OPD', name: 'Morning OPD', capacity: 20 }));

const EXCEPTIONS = [
  { date: '2026-08-15', type: 'HOLIDAY', scheduleId: null, reason: 'Independence Day' },
  { date: '2026-08-20', type: 'SESSION_CANCEL', scheduleId: 33, reason: 'Hospital CME in the evening' },
  { date: '2026-08-26', type: 'LEAVE', scheduleId: null, reason: 'Personal leave' },
];
for (const e of EXCEPTIONS) {
  if (e.scheduleId === 33 && weekdayOf(e.date) !== 'THURSDAY') throw new Error(`${e.date} is not a Thursday`);
}

function sessionsOn(date) {
  const weekday = weekdayOf(date);
  const all = [
    ...EXISTING_SESSIONS.map((s) => ({ ...s, key: s.id })),
    ...NEW_SESSIONS.map((s, i) => ({ ...s, key: `new${i}` })),
  ];
  return all
    .filter((s) => s.weekday === weekday && s.from <= date && (!s.to || s.to >= date))
    .filter((s) => !EXISTING_BLOCKED.has(`${date}|${s.key}`))
    .filter((s) => !EXCEPTIONS.some((e) => e.date === date && (e.scheduleId === null || e.scheduleId === s.key)))
    .sort((a, b) => minutesOf(a.start) - minutesOf(b.start));
}

// ----------------------------------------------------------------- patients

const FIRST_NAMES = {
  MALE: ['Aarav', 'Vihaan', 'Arjun', 'Reyansh', 'Sai', 'Krishna', 'Ishaan', 'Rohan', 'Kiran', 'Ravi', 'Suresh', 'Mahesh', 'Venkat', 'Srinivas', 'Rajesh', 'Anil', 'Prakash', 'Naveen', 'Harish', 'Ramesh'],
  FEMALE: ['Ananya', 'Diya', 'Saanvi', 'Aadhya', 'Ira', 'Meera', 'Kavya', 'Lakshmi', 'Priya', 'Divya', 'Swathi', 'Sravani', 'Padma', 'Sunitha', 'Radha', 'Deepika', 'Keerthi', 'Harika', 'Bhavani', 'Latha'],
};
const SURNAMES = ['Reddy', 'Rao', 'Sharma', 'Naidu', 'Kumar', 'Varma', 'Goud', 'Chowdary', 'Iyer', 'Patel', 'Yadav', 'Shetty', 'Menon', 'Das', 'Pillai'];
const AREAS = ['Kukatpally, Hyderabad', 'Miyapur, Hyderabad', 'Ameerpet, Hyderabad', 'Madhapur, Hyderabad', 'Begumpet, Hyderabad', 'Secunderabad', 'LB Nagar, Hyderabad', 'Kondapur, Hyderabad'];

const patients = Array.from({ length: 72 }, (_, i) => {
  const gender = chance(0.5) ? 'MALE' : 'FEMALE';
  const band = weighted([['CHILD', 45], ['ADULT', 30], ['SENIOR', 25]]);
  const age = band === 'CHILD' ? int(1, 14) : band === 'ADULT' ? int(18, 39) : int(40, 72);
  return {
    mrn: `PT-SEED-${pad(i + 1, 4)}`,
    name: `${pick(FIRST_NAMES[gender])} ${pick(SURNAMES)}`,
    gender,
    band,
    age,
    dob: `${2026 - age}-${pad(int(1, 12))}-${pad(int(1, 28))}`,
    phone: `90000${pad(i + 1, 5)}`,
    address: pick(AREAS),
    allergies: chance(0.06) ? pick(['Penicillin', 'Sulfa drugs', 'NSAIDs']) : null,
    firstVisit: null,
    lastVisitDate: null,
    profile: null,
  };
});

// ----------------------------------------------------------------- clinical profiles

const med = (name, generic, strength, dosage, route, frequency, duration, quantity) => ({
  name, generic, strength, dosage, route, frequency: frequency === 'TID' && chance(0.12) ? 'TDS' : frequency, duration, quantity,
});

const PROFILES = {
  URTI: {
    complaint: () => pick(['Fever and cold for 2 days', 'Running nose and sneezing since 3 days', 'Fever with body pains since yesterday', 'Cough and cold for 4 days']),
    findings: 'Throat congested, chest clear, no respiratory distress',
    diagnosis: 'Acute upper respiratory tract infection',
    plan: 'Symptomatic treatment, plenty of oral fluids, steam inhalation',
    followUp: 'Review if fever persists beyond 3 days',
    lines: (child) => [
      child
        ? med('DOLO 120 SYP', 'Paracetamol', '120mg/5ml', '5 ml', 'Oral', 'TID', '3 days', 1)
        : chance(0.6)
          ? med('DOLO 650', 'Paracetamol', '650mg', '1 tab', 'Oral', 'TID', '3 days', 9)
          : med('Paracetamol', 'Paracetamol', '500mg', '1 tab', 'Oral', 'TID', '3 days', 9),
      chance(0.7) && (child ? med('SINAREST AF', 'Paracetamol + Phenylephrine + Chlorpheniramine', null, '5 ml', 'Oral', 'BD', '5 days', 1) : med('Cetirizine', 'Cetirizine', '10mg', '1 tab', 'Oral', 'OD', '5 days', 5)),
      chance(0.3) && med('LIMCEE', 'Vitamin C', '500mg', child ? '1/2 tab' : '1 tab', 'Oral', 'OD', '10 days', child ? 5 : 10),
    ],
  },
  THROAT: {
    complaint: () => pick(['Sore throat and fever for 3 days', 'Painful swallowing since 2 days', 'Productive cough with fever for 5 days']),
    findings: 'Tonsils enlarged and inflamed, tender cervical nodes',
    diagnosis: () => pick(['Acute tonsillopharyngitis', 'Acute bronchitis']),
    plan: 'Antibiotic course to be completed, warm saline gargles',
    followUp: 'Review after 5 days',
    lines: (child) => [
      child
        ? med('Azithromycin syrup', 'Azithromycin', '200mg/5ml', '5 ml', 'Oral', 'OD', '3 days', 1)
        : chance(0.65)
          ? med('AZEE 500 TAB', 'Azithromycin', '500mg', '1 tab', 'Oral', 'OD', '3 days', 3)
          : med('AUGMENTIN 625 DUO', 'Amoxicillin + Clavulanate', '625mg', '1 tab', 'Oral', 'BD', '5 days', 10),
      child ? med('DOLO 120 SYP', 'Paracetamol', '120mg/5ml', '5 ml', 'Oral', 'SOS', '3 days', 1) : med('DOLO 650', 'Paracetamol', '650mg', '1 tab', 'Oral', 'SOS', '3 days', 6),
      !child && chance(0.5) && med('Betadine gargle', 'Povidone iodine', '2%', '10 ml', 'Gargle', 'TID', '5 days', 1),
    ],
  },
  GASTRO: {
    complaint: () => pick(['Loose stools 4-5 times since yesterday', 'Vomiting and stomach pain since morning', 'Acidity and upper abdominal pain for a week']),
    findings: 'Mild dehydration, abdomen soft, diffuse tenderness',
    diagnosis: () => pick(['Acute gastroenteritis', 'Acid peptic disease']),
    plan: 'Oral rehydration, bland diet, avoid outside food',
    followUp: 'Review if stools are bloody or vomiting persists',
    lines: (child) => [
      med('ORS sachet', 'Oral rehydration salts', null, '1 sachet in 1 L water', 'Oral', 'After each loose stool', '3 days', 6),
      child ? med('ENTEROGERMINA', 'Bacillus clausii', '2 billion', '5 ml', 'Oral', 'BD', '5 days', 10) : med('PAN L', 'Pantoprazole + Levosulpiride', '40mg', '1 cap', 'Oral', 'OD before breakfast', '5 days', 5),
      child ? chance(0.6) && med('Zinc syrup', 'Zinc sulphate', '20mg/5ml', '5 ml', 'Oral', 'OD', '14 days', 1) : chance(0.4) && med('METROGYL 400MG', 'Metronidazole', '400mg', '1 tab', 'Oral', 'TID', '5 days', 15),
      child ? chance(0.4) && med('CYCLOPAM', 'Dicyclomine + Simethicone', null, '5 ml', 'Oral', 'SOS', '3 days', 1) : chance(0.4) && med('Ondansetron', 'Ondansetron', '4mg', '1 tab', 'Oral', 'SOS', '2 days', 4),
    ],
  },
  PAIN: {
    complaint: () => pick(['Low back pain for 2 weeks', 'Knee pain while climbing stairs', 'Neck pain and stiffness for 5 days']),
    findings: 'Paraspinal tenderness, no neurological deficit',
    diagnosis: () => pick(['Mechanical low back pain', 'Osteoarthritis knee', 'Cervical spondylosis']),
    plan: 'Analgesics, posture correction, physiotherapy exercises',
    followUp: 'Review after 2 weeks',
    lines: () => [
      chance(0.7) ? med('ZERODOL', 'Aceclofenac', '100mg', '1 tab', 'Oral', 'BD', '5 days', 10) : med('IBUGESIC PLUS', 'Ibuprofen + Paracetamol', '400/325mg', '1 tab', 'Oral', 'TID', '3 days', 9),
      chance(0.8) && med('PAN L', 'Pantoprazole + Levosulpiride', '40mg', '1 cap', 'Oral', 'OD before breakfast', '5 days', 5),
      chance(0.4) && med('CALCIGEN D3', 'Calcium + Vitamin D3', null, '1 cap', 'Oral', 'OD', '30 days', 30),
      chance(0.35) && med('Diclofenac gel', 'Diclofenac', '1%', 'Apply locally', 'Topical', 'TID', '7 days', 1),
    ],
  },
  HTN: {
    complaint: () => pick(['Routine BP check-up', 'Occasional headache, on BP medicines', 'Follow-up for hypertension']),
    findings: () => `BP ${int(128, 158)}/${int(82, 98)} mmHg, pulse regular`,
    diagnosis: 'Essential hypertension',
    plan: 'Continue medicines, low-salt diet, 30 minutes walk daily',
    followUp: 'Review after 1 month with lipid profile',
    lines: () => [
      med('TELMIKIND 40', 'Telmisartan', '40mg', '1 tab', 'Oral', 'OD', '30 days', 30),
      chance(0.5) && med('AMLOKIND 5', 'Amlodipine', '5mg', '1 tab', 'Oral', 'OD', '30 days', 30),
      chance(0.45) && med('ATORVA 10MG', 'Atorvastatin', '10mg', '1 tab', 'Oral', 'OD at night', '30 days', 30),
    ],
  },
  ASTHMA: {
    complaint: () => pick(['Wheezing and breathlessness at night', 'Recurrent cough with wheeze for a week']),
    findings: 'Bilateral wheeze, SpO2 96% on room air',
    diagnosis: 'Acute exacerbation of bronchial asthma',
    plan: 'Inhaler technique demonstrated, avoid dust and cold drinks',
    followUp: 'Review after 2 weeks',
    lines: (child) => [
      med('Salbutamol inhaler', 'Salbutamol', '100mcg', '2 puffs', 'Inhalation', 'SOS', '30 days', 1),
      child ? med('LEVOCET M KID TAB', 'Levocetirizine + Montelukast', '2.5/4mg', '1 tab', 'Oral', 'OD at night', '14 days', 14) : med('Montelukast', 'Montelukast', '10mg', '1 tab', 'Oral', 'OD at night', '14 days', 14),
      chance(0.4) && med('WYSOLONE 10MG', 'Prednisolone', '10mg', '1 tab', 'Oral', 'OD', '5 days', 5),
    ],
  },
  NUTRITION: {
    complaint: () => pick(['Tiredness and poor appetite', 'Pale and easily fatigued for a month']),
    findings: 'Pallor present, no organomegaly',
    diagnosis: () => pick(['Iron deficiency anaemia', 'Nutritional deficiency']),
    plan: 'Iron-rich diet, supplements, CBC after 1 month',
    followUp: 'Review after 1 month with CBC',
    lines: (child) => [
      med('FOLVITE', 'Folic acid', '5mg', '1 tab', 'Oral', 'OD', '30 days', 30),
      chance(0.6) && med('ZINCOVIT', 'Multivitamin + Zinc', null, '1 tab', 'Oral', 'OD', '30 days', 30),
      child
        ? med('Ferrous ascorbate syrup', 'Ferrous ascorbate + Folic acid', '30mg/5ml', '5 ml', 'Oral', 'OD', '30 days', 1)
        : chance(0.5) && med('Vitamin D3 60K', 'Cholecalciferol', '60000 IU', '1 sachet', 'Oral', 'Once weekly', '8 weeks', 8),
    ],
  },
  SKIN: {
    complaint: () => pick(['Itchy rash on arms for a week', 'Infected wound on the leg', 'Red itchy patches since 5 days']),
    findings: 'Erythematous lesions with mild discharge, no fever',
    diagnosis: () => pick(['Impetigo', 'Allergic dermatitis', 'Infected abrasion']),
    plan: 'Keep the area clean and dry, avoid scratching',
    followUp: 'Review after 1 week',
    lines: (child) => [
      chance(0.6) ? med('BETADINE OINTMENT', 'Povidone iodine', '5%', 'Apply locally', 'Topical', 'BD', '7 days', 1) : med('Fusidic acid cream', 'Fusidic acid', '2%', 'Apply locally', 'Topical', 'TID', '7 days', 1),
      chance(0.6) && (child ? med('Cetirizine syrup', 'Cetirizine', '5mg/5ml', '5 ml', 'Oral', 'OD', '5 days', 1) : med('Cetirizine', 'Cetirizine', '10mg', '1 tab', 'Oral', 'OD', '5 days', 5)),
      !child && chance(0.3) && med('TAXIM-O 200', 'Cefixime', '200mg', '1 tab', 'Oral', 'BD', '5 days', 10),
    ],
  },
};

const profileFor = (p) =>
  p.band === 'CHILD'
    ? weighted([['URTI', 40], ['THROAT', 15], ['GASTRO', 20], ['ASTHMA', 10], ['NUTRITION', 8], ['SKIN', 7]])
    : p.band === 'ADULT'
      ? weighted([['URTI', 30], ['THROAT', 15], ['GASTRO', 20], ['PAIN', 20], ['SKIN', 10], ['NUTRITION', 5]])
      : weighted([['HTN', 32], ['PAIN', 25], ['URTI', 13], ['GASTRO', 15], ['THROAT', 10], ['NUTRITION', 5]]);
const text = (v) => (typeof v === 'function' ? v() : v);

// ----------------------------------------------------------------- simulate the 30 days

const visits = [];
const statuses = [];
const outcomes = { ON_TIME: 0, LATE: 0, NO_STATUS: 0 };

for (let date = FIRST_DAY; date <= LAST_DAY; date = addDays(date, 1)) {
  const sessions = sessionsOn(date);
  if (sessions.length === 0) continue;

  const firstStart = minutesOf(sessions[0].start);
  const outcome = weighted([['ON_TIME', 66], ['LATE', 22], ['NO_STATUS', 12]]);
  outcomes[outcome]++;
  const arrival = outcome === 'LATE' ? firstStart + int(4, 35) : firstStart - int(3, 15);
  const recordStatus = (minutes, status, source = 'SELF') => statuses.push({ date, minutes, status, source });

  if (outcome !== 'NO_STATUS') {
    if (arrival - 60 >= 0) {
      recordStatus(arrival - int(40, 60), 'AVAILABLE');
      recordStatus(arrival - int(12, 30), 'IN_TRANSIT');
    }
    recordStatus(arrival, 'AT_FACILITY', chance(0.15) ? 'RECEPTION' : 'SELF');
  }

  let doctorFree = outcome === 'NO_STATUS' ? firstStart + int(0, 10) : Math.max(arrival, firstStart);
  let lastEnd = doctorFree;
  let firstConsult = null;

  for (const s of sessions) {
    const start = minutesOf(s.start);
    const end = minutesOf(s.end);
    if (s.kind === 'EARLY') {
      if (outcome !== 'NO_STATUS') recordStatus(Math.max(arrival, start) + int(1, 5), 'IN_ROUNDS');
      doctorFree = Math.max(doctorFree, end);
      continue;
    }
    if (s.kind === 'ROUNDS' && outcome !== 'NO_STATUS') recordStatus(start + int(0, 5), 'IN_ROUNDS');

    const count = s.kind === 'ROUNDS' ? int(1, 3) : s.id === 131 ? int(2, 4) : int(4, 9);
    let patientArrival = start - int(5, 20);
    doctorFree = Math.max(doctorFree, start);
    for (let i = 0; i < count; i++) {
      patientArrival += int(6, 22);
      const consultStart = Math.max(patientArrival + int(3, 10), doctorFree);
      const consultEnd = consultStart + int(7, 16);
      if (consultEnd > end + 40) break;
      doctorFree = consultEnd + int(1, 3);
      lastEnd = consultEnd;
      firstConsult ??= consultStart;

      // Returning patient seen 3-25 days ago -> follow-up; otherwise someone new (or anyone, once all have visited).
      const returning = patients.filter((p) => p.lastVisitDate && daysBetween(p.lastVisitDate, date) >= 3 && daysBetween(p.lastVisitDate, date) <= 25);
      const unseen = patients.filter((p) => !p.lastVisitDate);
      const isReturn = returning.length > 0 && (chance(0.32) || unseen.length === 0);
      const patient = isReturn ? pick(returning) : unseen.length ? pick(unseen) : pick(patients);
      const child = patient.band === 'CHILD';
      const daysSinceLast = patient.lastVisitDate ? daysBetween(patient.lastVisitDate, date) : null;

      let visitType;
      if (isReturn) visitType = chance(0.9) ? 'FOLLOW_UP' : 'SECOND_OPINION';
      else visitType = child && chance(0.1) ? 'VACCINATION' : 'NEW';
      if (!isReturn || !patient.profile || chance(0.3)) patient.profile = profileFor(patient);

      patient.firstVisit ??= { date, minutes: patientArrival };
      patient.lastVisitDate = date;

      visits.push({ date, arrival: patientArrival, consultStart, consultEnd, patient, child, visitType, profile: patient.profile, daysSinceLast });
    }
  }

  if (outcome !== 'NO_STATUS' && firstConsult !== null) {
    recordStatus(firstConsult, 'IN_CONSULTATION');
    recordStatus(lastEnd + int(5, 20), 'DAY_COMPLETE');
  }
}

// ----------------------------------------------------------------- emit SQL

const out = [];
const emit = (s) => out.push(s);

emit(`-- GENERATED by generate.mjs - do not edit by hand. Dev database only.`);
emit(`-- ${FIRST_DAY} .. ${LAST_DAY}: Doctor Reports test data for dr.regtest (doctor ${DOCTOR}) at facility ${FACILITY}.`);
emit(`SET AUTOCOMMIT FALSE;`);
emit(fs.readFileSync(path.join(HERE, 'cleanup.sql'), 'utf8').replace(/^--.*$/gm, '').trim());
emit('');

const opVisitId = (n) => `(SELECT id FROM op_visits WHERE facility_id = ${FACILITY} AND op_no = '${n}')`;

for (const s of NEW_SESSIONS) {
  emit(
    `INSERT INTO doctor_schedules (doctor_id, facility_id, weekday, session_name, start_time, end_time, capacity, overbook_allowance, effective_from, effective_to, active, created_date) VALUES (${DOCTOR}, ${FACILITY}, ${sql(s.weekday)}, ${sql(s.name)}, TIME '${s.start}:00', TIME '${s.end}:00', ${s.capacity}, 2, DATE '${s.from}', ${s.to ? `DATE '${s.to}'` : 'NULL'}, TRUE, TIMESTAMP '2026-07-30 18:30:00.${TAG}');`
  );
}
for (const e of EXCEPTIONS) {
  emit(
    `INSERT INTO schedule_exceptions (doctor_id, facility_id, doctor_schedule_id, exception_date, exception_type, reason, created_by_user_id, created_date) VALUES (${DOCTOR}, ${FACILITY}, ${e.scheduleId ?? 'NULL'}, DATE '${e.date}', ${sql(e.type)}, ${sql(e.reason)}, ${DOCTOR_USER}, ${ts(addDays(e.date, -int(3, 9)), int(18 * 60, 22 * 60))});`
  );
}

const seenPatients = patients.filter((p) => p.firstVisit);
for (const p of seenPatients) {
  const created = ts(p.firstVisit.date, p.firstVisit.minutes - 3);
  emit(
    `INSERT INTO patients (mrn, full_name, phone, gender, date_of_birth, address, active, created_date, updated_date, facility_id, allergies) VALUES (${sql(p.mrn)}, ${sql(p.name)}, ${sql(p.phone)}, ${sql(p.gender)}, DATE '${p.dob}', ${sql(p.address)}, TRUE, ${created}, ${created}, ${FACILITY}, ${sql(p.allergies)});`
  );
}

const counts = { visits: 0, notes: 0, prescriptions: 0, lines: 0, bills: 0, payments: 0, ratings: 0 };
visits.forEach((v, i) => {
  const opNo = `OP-SEED-${pad(i + 1, 5)}`;
  const billNo = `BILL-SEED-${pad(i + 1, 5)}`;
  const p = v.patient;
  const profile = PROFILES[v.profile];
  const adultWeight = p.gender === 'MALE' ? int(58, 88) : int(48, 76);
  const weight = v.child ? Math.round((8 + p.age * 2.6 + rand() * 3) * 10) / 10 : adultWeight;
  const height = v.child ? Math.round(75 + p.age * 6.2 + rand() * 6) : p.gender === 'MALE' ? int(160, 182) : int(148, 168);
  const feverish = v.profile === 'URTI' || v.profile === 'THROAT';
  const temp = feverish ? Math.round((99 + rand() * 3.2) * 10) / 10 : Math.round((97.6 + rand() * 1.2) * 10) / 10;
  const bp = v.child ? null : `${int(112, 150)}/${int(72, 94)}`;

  emit(
    `INSERT INTO op_visits (op_no, facility_id, patient_id, doctor_id, org_type, visit_type, weight_kg, height_cm, temperature_f, bp, status, mlc_flag, arrived_ts, consult_start_ts, consult_end_ts) VALUES (${sql(opNo)}, ${FACILITY}, (SELECT id FROM patients WHERE facility_id = ${FACILITY} AND mrn = ${sql(p.mrn)}), ${DOCTOR}, 'DIRECT', ${sql(v.visitType)}, ${weight}, ${height}, ${temp}, ${sql(bp)}, 'COMPLETED', FALSE, ${ts(v.date, v.arrival)}, ${ts(v.date, v.consultStart)}, ${ts(v.date, v.consultEnd)});`
  );
  counts.visits++;

  // Consultation bill: new visits at the approved day rate, follow-ups cheaper, and free within a week.
  const fee = v.visitType === 'FOLLOW_UP' ? (v.daysSinceLast !== null && v.daysSinceLast <= 7 ? 0 : 300) : 500;
  const unpaid = fee > 0 && chance(0.04);
  const paid = unpaid ? 0 : fee;
  emit(
    `INSERT INTO bills (facility_id, patient_id, bill_no, encounter_type, encounter_id, gross, discount, tax, net, paid, due, status, created_by_user_id, created_date, refund_status) VALUES (${FACILITY}, (SELECT id FROM patients WHERE facility_id = ${FACILITY} AND mrn = ${sql(p.mrn)}), ${sql(billNo)}, 'CONSULTATION', ${opVisitId(opNo)}, ${fee}.00, 0.00, 0.00, ${fee}.00, ${paid}.00, ${fee - paid}.00, ${sql(unpaid ? 'UNPAID' : 'PAID')}, ${RECEPTION_USER}, ${ts(v.date, v.arrival + 2)}, 'NONE');`
  );
  counts.bills++;
  if (paid > 0) {
    emit(
      `INSERT INTO payments (bill_id, facility_id, party_type, payment_type, amount, paid_at, received_by_user_id) VALUES ((SELECT id FROM bills WHERE facility_id = ${FACILITY} AND bill_no = ${sql(billNo)}), ${FACILITY}, 'PATIENT', ${sql(chance(0.6) ? 'CASH' : 'CARD_UPI')}, ${paid}.00, ${ts(v.date, v.arrival + 3)}, ${RECEPTION_USER});`
    );
    counts.payments++;
  }

  const vaccination = v.visitType === 'VACCINATION';
  const noteAt = ts(v.date, v.consultEnd);
  emit(
    `INSERT INTO consultation_notes (op_visit_id, doctor_id, facility_id, chief_complaint, examination_findings, provisional_diagnosis, treatment_plan, follow_up_plan, is_draft, created_date, updated_date) VALUES (${opVisitId(opNo)}, ${DOCTOR}, ${FACILITY}, ${sql(vaccination ? 'Scheduled immunisation' : text(profile.complaint))}, ${sql(vaccination ? 'Afebrile, active and playful' : text(profile.findings))}, ${sql(vaccination ? 'Routine immunisation' : text(profile.diagnosis))}, ${sql(vaccination ? 'Vaccine given as per IAP schedule, paracetamol SOS for fever' : profile.plan)}, ${sql(vaccination ? 'Next dose as per schedule' : profile.followUp)}, FALSE, ${noteAt}, ${noteAt});`
  );
  counts.notes++;

  if (!vaccination && chance(0.9)) {
    const lines = profile.lines(v.child).filter(Boolean);
    emit(
      `INSERT INTO prescriptions (op_visit_id, doctor_id, facility_id, is_draft, created_date, updated_date) VALUES (${opVisitId(opNo)}, ${DOCTOR}, ${FACILITY}, FALSE, ${ts(v.date, v.consultEnd - 1)}, ${ts(v.date, v.consultEnd - 1)});`
    );
    counts.prescriptions++;
    lines.forEach((l, order) => {
      emit(
        `INSERT INTO prescription_lines (prescription_id, line_order, medicine_name, generic_name, strength, dosage, route, frequency, duration, quantity, refill_flag) VALUES ((SELECT id FROM prescriptions WHERE op_visit_id = ${opVisitId(opNo)}), ${order}, ${sql(l.name)}, ${sql(l.generic)}, ${sql(l.strength)}, ${sql(l.dosage)}, ${sql(l.route)}, ${sql(l.frequency)}, ${sql(l.duration)}, ${l.quantity}, FALSE);`
      );
      counts.lines++;
    });
  }

  if (chance(0.55)) {
    const rating = weighted([[5, 46], [4, 33], [3, 12], [2, 6], [1, 3]]);
    const comment =
      rating >= 5 && chance(0.4) ? pick(['Very patient and explained everything clearly', 'Excellent doctor', 'Child was comfortable, thank you'])
        : rating <= 2 && chance(0.7) ? pick(['Waited too long', 'Felt rushed during the consultation'])
          : null;
    const ratedAt = ts(v.date, Math.min(v.consultEnd + int(20, 180), 23 * 60 + 30));
    emit(
      `INSERT INTO consultation_ratings (op_visit_id, doctor_id, facility_id, rating, comment, rated_by_user_id, created_date, updated_date) VALUES (${opVisitId(opNo)}, ${DOCTOR}, ${FACILITY}, ${rating}, ${sql(comment)}, ${RECEPTION_USER}, ${ratedAt}, ${ratedAt});`
    );
    counts.ratings++;
  }
});

for (const s of statuses.sort((a, b) => (a.date === b.date ? a.minutes - b.minutes : a.date < b.date ? -1 : 1))) {
  emit(
    `INSERT INTO doctor_statuses (doctor_id, facility_id, session_date, status, source, set_by_user_id, created_date) VALUES (${DOCTOR}, ${FACILITY}, DATE '${s.date}', ${sql(s.status)}, ${sql(s.source)}, ${s.source === 'RECEPTION' ? RECEPTION_USER : DOCTOR_USER}, ${ts(s.date, s.minutes)});`
  );
}

emit('COMMIT;');
fs.writeFileSync(path.join(HERE, 'seed.sql'), out.join('\n') + '\n');

console.log(JSON.stringify({ range: `${FIRST_DAY}..${LAST_DAY}`, patients: seenPatients.length, ...counts, statuses: statuses.length, sessionDays: outcomes, newSchedules: NEW_SESSIONS.length, exceptions: EXCEPTIONS.length }, null, 2));
