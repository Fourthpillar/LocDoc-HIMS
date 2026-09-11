Client-agnostic agent spec for turning a client's legacy report exports into a real,
validated Flyway seed migration. Not tied to any one client - "Aayurdhara" was simply
the client whose reports happened to be in `temp/` the first time this was run.

## Trigger

The user drops one or more legacy report exports (PDF, mostly) into `temp/` at the repo
root and asks to populate the database from them. Reports are typically per-module data
dumps from the client's previous system: supplier lists, medicine/stock lists, purchase
orders, GRNs (purchases), sales, sales returns, patient lists, etc. - whatever the client
happened to export. Don't assume a fixed set of six reports; read whatever is there.

## Step 0 - identify the client and the reports

1. Open each file in `temp/` and read its letterhead/title block to get the client's
   real name (e.g. "AAYURDHARA HOSPITAL"). All reports in one run should share the same
   client; if they don't, stop and ask.
2. Derive a folder-safe client name from it (e.g. `Aayurdhara`) - this is `{Client}`
   throughout the rest of this doc.
3. For each file, identify what entity/table it maps to from its title and columns, not
   its filename. Don't assume a report is complete or itemized just because of its name -
   verify by reading actual rows (a "Purchases" report is often a financial header list
   with zero line items; a "Purchase Orders" report often has items but no pricing).

## Step 1 - know the schema before writing any INSERT

Read the owning module's `CREATE TABLE` migration(s) for every target table (column
names, types, `NOT NULL`, `UNIQUE`, FK targets) before generating a single row. Don't
guess columns from memory. If a report's module isn't obvious (e.g. patient data belongs
to `outpatient`, not `pharmacy`), check `CLAUDE.md`'s module table.

## Step 2 - pick the version number

This app numbers Flyway migrations in per-module blocks of 100 (`common`=V1xx,
`pharmacy`=V2xx, `outpatient`=V3xx, next module=V4xx - see `CLAUDE.md`). A client seed
migration belongs to the module(s) it touches and gets the next unused number in that
block. Scan the module's existing `V2xx__*.sql` / `V3xx__*.sql` files to find it - don't
hardcode a number from a previous run.

## Step 3 - extract data honestly, never invent financial figures

This is the part most likely to go wrong at speed. Rules, learned the hard way:

- **Read every page.** For large PDFs, check page count first (a `pypdf`/`PyPDF2` one-
  liner is enough if available; install it if not) and budget accordingly - don't assume
  page 1's density holds for the whole document, and don't silently truncate to save
  effort. If the true scope is large (100+ pages of transactions), **ask the user** how
  much to cover before spending the effort - don't decide unilaterally.
- **Prefer a report's raw/unstructured text extraction over its rendered-table view when
  both are available.** Narrow table columns in PDF renderers routinely truncate long
  values (bill numbers, codes) to the visible width; the underlying text stream usually
  has the full value. Cross-check a few rows between the two views before trusting either
  one at scale - this exact bug (truncated GRN bill numbers) slipped through once already.
- **Every NOT NULL column needs a real source.** If a required column (e.g. a line item's
  `rate`) has no value anywhere in the report that contains it, look for that same real
  quantity in a *different* report about the same entity (e.g. a stock/inventory report's
  current rate for a medicine that also appears, unpriced, on a purchase order). Using
  that is a documented backfill, not a fabrication - say so in the migration's header
  comment. If no report has the value at all, **do not invent one** - either skip that
  row/line (and say how many you skipped and why) or ask the user how they want it
  handled. Never make up a rate, tax percent, or amount to make a row insertable.
- **A report with only header/financial totals and no line items is still worth seeding
  at the header level** if the target table's schema doesn't require child rows to exist
  (check the FK/NOT NULL constraints from Step 1) - just leave the child table empty for
  those records and say so in the comment, rather than fabricating plausible-looking line
  items to fill it.
- Ask the user (a short set of concrete options, not an open-ended question) whenever a
  gap like the above forces a real tradeoff. Don't silently downgrade scope or silently
  fabricate - both were live mistakes to avoid here.

## Step 4 - generate, don't hand-type, at any real scale

Once past roughly 30-50 rows, hand-typing INSERT statements is where transcription errors
live. Instead:

1. Transcribe report rows into a few structured intermediate JSON files, in your
   scratchpad, not the repo.
2. Write a small generation script (Python is fine) that reads that JSON, does any
   cross-report matching/lookups (e.g. matching a purchase order's medicine name against
   the stock report to backfill a rate), and emits the final SQL using
   `INSERT ... SELECT ... FROM <parent> WHERE <business-key match>` for every row that
   needs a foreign key to an autoincrement id generated earlier in the same script - don't
   hardcode ids.
3. Print summary stats from the script (rows kept per table, rows/items skipped and why)
   so scope decisions are visible before the SQL is finalized.

## Step 5 - validate before calling it done

Concatenate every migration from V1 through the new file, in version order, and run it
against a throwaway file-based H2 database using the H2 jar already in the Gradle cache
(`org.h2.tools.RunScript`) - `find ~/.gradle/caches -iname 'h2-*.jar'` finds it. A clean
exit code proves the FKs/constraints all resolve. Then run a few sanity queries: row
counts per table, and at least one aggregate (e.g. a financial total) cross-checked
against the source report's own printed grand total, to catch a mis-transcribed row
before it ships. Small rounding-scale drift (roughly 1-2%) across hundreds of hand-read
rows is a known, acceptable margin - don't chase it row-by-row; a large drift means a real
mistake and is worth finding.

## Step 6 - where the output goes

- The generated migration: `<module>/src/main/resources/db/scripts/<module>/{Client}/V{n}__seed_{client}_reference_data.sql`
  (lower-cased client name in the filename, `{Client}` folder name as read from the
  report letterhead). Multiple modules may each need their own file + version number if
  the client's reports span modules (e.g. pharmacy + outpatient).
- The migration's header comment must say, plainly, what's directly from the source
  reports vs backfilled from another report vs intentionally left empty, so a future
  reader doesn't mistake an approximation for ground truth.
- Also generate a small standalone HTML overview page named with the client,
  `app/src/main/resources/client-reference-data/{Client}-overview.html` (e.g.
  `client-reference-data/Aayurdhara-overview.html`), styled like
  `app/src/main/resources/db-scripts-overview.html` but scoped to this client only. This
  is centralized in the `app` module - the one module that carries no SQL of its own - so
  every client gets exactly **one** overview page even when their reports span multiple
  modules (e.g. pharmacy + outpatient), instead of a fragmented page per module folder.
  It should cover: which source reports were read (file name + what it mapped to), a
  table listing every migration generated for this client with its owning module and
  version, a table of what was seeded per target table (row counts), and the same
  real-vs-backfilled-vs-skipped disclosure as each migration's header comment (including
  *why* anything was skipped, e.g. "no rate anywhere in the reports"). This is the
  client-specific counterpart to the shared schema catalog - write it so a future
  maintainer opening just this page understands the data's provenance without reading
  the SQL first.
- **Do not** add this client's data to `CLAUDE.md`'s version ledger or to
  `app/src/main/resources/db-scripts-overview.html`. Those describe the app's own schema
  catalog, which every install shares; a specific client's historical business data
  belongs in the migration's owning module folder, and its overview page in
  `client-reference-data/` - never in the shared catalog files themselves.
- Clean up scratch JSON/scripts used to generate the SQL - they don't belong in the repo.

## Non-goals

- This agent doesn't touch the frontend, backend Java code, or existing migrations.
- It doesn't invent transactional history (sales, returns, etc.) beyond what a report
  actually contains, even to make demo data "look complete."
- It isn't a one-shot template to fill in - re-read Steps 0-3 fresh for each client,
  since report formats, available columns, and gaps vary client to client.
