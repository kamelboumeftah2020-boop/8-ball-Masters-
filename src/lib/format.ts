const dateFmt = new Intl.DateTimeFormat("ar-u-nu-latn", { day: "numeric", month: "short", year: "numeric" });

export function formatDate(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const days = Math.floor((Date.now() - d.getTime()) / 86_400_000);
  if (days <= 0) return "اليوم";
  if (days === 1) return "أمس";
  if (days < 7) return `منذ ${days} أيام`;
  return dateFmt.format(d);
}

/** Human duration: "45 د" / "1 س 20 د". */
export function formatDuration(ms: number): string {
  if (!ms) return "";
  const totalMin = Math.round(ms / 60_000);
  if (totalMin < 1) return "أقل من دقيقة";
  const h = Math.floor(totalMin / 60);
  const m = totalMin % 60;
  return h ? `${h} س${m ? ` ${m} د` : ""}` : `${m} د`;
}

/** Clock time for the player: 4:05 / 1:02:09. */
export function formatClock(sec: number): string {
  if (!Number.isFinite(sec) || sec < 0) sec = 0;
  const s = Math.floor(sec % 60);
  const m = Math.floor((sec / 60) % 60);
  const h = Math.floor(sec / 3600);
  const pad = (n: number) => String(n).padStart(2, "0");
  return h ? `${h}:${pad(m)}:${pad(s)}` : `${m}:${pad(s)}`;
}

export function formatBytes(bytes: number): string {
  if (!bytes) return "0 م.ب";
  const mb = bytes / 1024 / 1024;
  return mb >= 1024 ? `${(mb / 1024).toFixed(1)} ج.ب` : `${mb.toFixed(1)} م.ب`;
}
