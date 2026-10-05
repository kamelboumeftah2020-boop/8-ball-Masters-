import type { Episode } from "./types";

// Downloaded audio lives in IndexedDB so it survives reloads and works offline.

export interface StoredDownload {
  id: string;
  episode: Episode;
  /** Browser builds keep the audio itself in IndexedDB… */
  blob?: Blob;
  /** …the Android app stores it as a file and keeps only its path here. */
  path?: string;
  size: number;
  savedAt: number;
}

const DB_NAME = "sada";
const STORE = "downloads";

let dbPromise: Promise<IDBDatabase> | null = null;

function open(): Promise<IDBDatabase> {
  dbPromise ??= new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, 1);
    req.onupgradeneeded = () => req.result.createObjectStore(STORE, { keyPath: "id" });
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
  return dbPromise;
}

async function tx<T>(mode: IDBTransactionMode, fn: (s: IDBObjectStore) => IDBRequest<T>): Promise<T> {
  const db = await open();
  return new Promise((resolve, reject) => {
    const req = fn(db.transaction(STORE, mode).objectStore(STORE));
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

export const putDownload = (d: StoredDownload) => tx("readwrite", (s) => s.put(d));
export const getDownload = (id: string) => tx<StoredDownload | undefined>("readonly", (s) => s.get(id));
export const deleteDownload = (id: string) => tx("readwrite", (s) => s.delete(id));
export const allDownloads = () => tx<StoredDownload[]>("readonly", (s) => s.getAll());
