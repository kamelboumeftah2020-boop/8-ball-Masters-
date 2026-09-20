// A fixed-window counter keyed by caller address. It exists so one machine
// cannot open accounts in bulk and squat every good username: names are unique
// and permanent, so mass registration is the cheap way to ruin the namespace.
//
// The window and the clock are injectable so the behaviour can be tested without
// waiting an hour for one to roll over.
export function createRateLimiter({ limit, windowMs, now = Date.now } = {}) {
  const hits = new Map(); // key -> { count, resetAt }

  function sweep(t) {
    for (const [key, entry] of hits) if (entry.resetAt <= t) hits.delete(key);
  }

  return {
    // Records one use and says whether it was allowed. `retryAfterMs` is how long
    // the caller must wait, and is 0 while they are still under the limit.
    take(key) {
      const t = now();
      if (hits.size > 5000) sweep(t);
      let entry = hits.get(key);
      if (!entry || entry.resetAt <= t) {
        entry = { count: 0, resetAt: t + windowMs };
        hits.set(key, entry);
      }
      entry.count += 1;
      const allowed = entry.count <= limit;
      return { allowed, remaining: Math.max(0, limit - entry.count), retryAfterMs: allowed ? 0 : entry.resetAt - t };
    },
    reset(key) { if (key === undefined) hits.clear(); else hits.delete(key); },
  };
}

// The address a request came from, as a limiter key. Behind a reverse proxy
// Express only reports the real client when `trust proxy` is configured, so this
// falls back to the socket address rather than trusting a header on its own.
export function callerKey(req) {
  return req.ip || req.socket?.remoteAddress || 'unknown';
}

// Loopback is the operator's own machine - the dev server, the test suites, a
// single-box deployment's health checks - so it is never throttled.
export function isLoopback(key) {
  return key === '127.0.0.1' || key === '::1' || key === '::ffff:127.0.0.1';
}
