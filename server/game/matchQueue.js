// Per-table matchmaking. Each table has a 100% isolated queue - a player only ever
// gets paired against someone searching in the SAME table (spec: "كل طاولة لها
// Matchmaking خاص بيها"). Queue length is tracked server-side only, so clients
// cannot spoof "X players online" or pad their own queue position.
const queues = new Map(); // tableId -> [{ userId, socketId, joinedAt, botTimer }]

export function queueFor(tableId) {
  if (!queues.has(tableId)) queues.set(tableId, []);
  return queues.get(tableId);
}

export function queueLength(tableId) {
  return queueFor(tableId).length;
}

export function enqueue(tableId, entry) {
  const q = queueFor(tableId);
  q.push(entry);
}

export function dequeueByUser(tableId, userId) {
  const q = queueFor(tableId);
  const idx = q.findIndex(e => e.userId === userId);
  if (idx >= 0) {
    const [removed] = q.splice(idx, 1);
    if (removed.botTimer) clearTimeout(removed.botTimer);
    return removed;
  }
  return null;
}

// Try to pop the oldest OTHER waiting player on this table to pair with `userId`.
export function popOpponent(tableId, userId) {
  const q = queueFor(tableId);
  const idx = q.findIndex(e => e.userId !== userId);
  if (idx >= 0) {
    const [opp] = q.splice(idx, 1);
    if (opp.botTimer) clearTimeout(opp.botTimer);
    return opp;
  }
  return null;
}
