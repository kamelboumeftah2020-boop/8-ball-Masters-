import { todayStr } from './surahs';
// rating: 0 ضعيف، 1 متوسط، 2 متقن  (SM-2 مبسطة)
export function nextState(prev: { ease: number; interval: number; lapses: number }, rating: 0 | 1 | 2) {
  let { ease, interval, lapses } = prev;
  if (rating === 0) { lapses += 1; ease = Math.max(1.3, ease - 0.2); interval = 1; }
  else if (rating === 1) { interval = Math.max(1, Math.round(interval * 1.2)); ease = Math.max(1.3, ease - 0.05); }
  else { interval = interval === 0 ? 1 : interval === 1 ? 3 : Math.round(interval * ease); ease += 0.05; }
  const d = new Date(); d.setDate(d.getDate() + interval);
  return { ease, interval, lapses, next: todayStr(d) };
}
