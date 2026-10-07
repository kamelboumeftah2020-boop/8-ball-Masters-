import * as SQLite from 'expo-sqlite';
import { SCHEMA } from './schema';

let _init: Promise<SQLite.SQLiteDatabase> | null = null;

export function getDb() {
  if (!_init) {
    _init = (async () => {
      const db = await SQLite.openDatabaseAsync('hifz.db');
      await db.execAsync('PRAGMA journal_mode = WAL;');
      await db.execAsync(SCHEMA);
      return db;
    })();
    _init.catch(() => { _init = null; });
  }
  return _init;
}
