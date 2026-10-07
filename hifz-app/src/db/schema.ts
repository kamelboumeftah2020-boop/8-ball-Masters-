
export const SCHEMA = `
CREATE TABLE IF NOT EXISTS ayahs (
  riwaya TEXT NOT NULL, surah INTEGER NOT NULL, ayah INTEGER NOT NULL, text TEXT NOT NULL,
  PRIMARY KEY (riwaya, surah, ayah)
);
CREATE TABLE IF NOT EXISTS memorization (
  riwaya TEXT NOT NULL, surah INTEGER NOT NULL, ayah INTEGER NOT NULL,
  status TEXT DEFAULT 'learning', ease REAL DEFAULT 2.5, interval_days INTEGER DEFAULT 0,
  next_review TEXT, last_review TEXT, lapses INTEGER DEFAULT 0,
  PRIMARY KEY (riwaya, surah, ayah)
);
CREATE INDEX IF NOT EXISTS idx_mem_next ON memorization (riwaya, next_review);
CREATE TABLE IF NOT EXISTS sessions (
  id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, riwaya TEXT, kind TEXT, ayahs_count INTEGER, rating INTEGER
);
CREATE TABLE IF NOT EXISTS bookmarks (
  riwaya TEXT NOT NULL, surah INTEGER NOT NULL, ayah INTEGER NOT NULL, PRIMARY KEY (riwaya, surah, ayah)
);
CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT);
CREATE TABLE IF NOT EXISTS downloads (
  type TEXT NOT NULL, riwaya TEXT NOT NULL, surah INTEGER, reciter TEXT,
  status TEXT DEFAULT 'pending', progress INTEGER DEFAULT 0,
  PRIMARY KEY (type, riwaya, surah, reciter)
);
CREATE TABLE IF NOT EXISTS tafasir (
  tafsir_id TEXT NOT NULL, surah INTEGER NOT NULL, ayah INTEGER NOT NULL, text TEXT NOT NULL,
  PRIMARY KEY (tafsir_id, surah, ayah)
);
`;
