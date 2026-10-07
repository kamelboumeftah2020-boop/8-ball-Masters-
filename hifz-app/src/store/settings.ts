import { create } from 'zustand';
import { getDb } from '../db';
import type { Riwaya } from '../db/queries';

type Persist = { riwaya: Riwaya; dark: boolean | null; fontSize: number; reminderHour: number | null; lastPage: Record<Riwaya, number> };
type State = Persist & {
  ready: boolean;
  load: () => Promise<void>;
  set: (p: Partial<Persist>) => Promise<void>;
};
export const useSettings = create<State>((set, get) => ({
  riwaya: 'hafs', dark: null, fontSize: 30, reminderHour: null, lastPage: { hafs: 1, warsh: 1 }, ready: false,
  load: async () => {
    const db = await getDb();
    const row = await db.getFirstAsync<{ value: string }>("SELECT value FROM settings WHERE key='app'");
    set({ ...(row ? JSON.parse(row.value) : {}), ready: true });
  },
  set: async (p) => {
    set(p);
    const { riwaya, dark, fontSize, reminderHour, lastPage } = get();
    const db = await getDb();
    await db.runAsync("INSERT OR REPLACE INTO settings (key,value) VALUES ('app',?)", [JSON.stringify({ riwaya, dark, fontSize, reminderHour, lastPage })]);
  },
}));
