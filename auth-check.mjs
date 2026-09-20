// Sign-in is the username alone. This checks that names are genuinely unique, that
// a known name returns you to your own account, and that a PIN is what stops
// someone else signing in as you.
const BASE = 'http://localhost:3000';

async function api(method, path, body, token) {
  const headers = {};
  if (body) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(BASE + '/api' + path, {
    method,
    headers: Object.keys(headers).length ? headers : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  return { status: res.status, body: await res.json().catch(() => null) };
}

let fails = 0;
const check = (label, ok) => { console.log(`${ok ? 'PASS' : 'FAIL'}  ${label}`); if (!ok) fails++; };

const name = 'Player' + (Date.now() % 100000);

// --- a fresh name opens an account ---
const fresh = await api('POST', '/auth/check', { username: name });
check('an unused username is reported as free', fresh.body.exists === false);

const signup = await api('POST', '/auth/signup', { username: name, avatarId: 2 });
check('signing up with it works', signup.status === 200 && !!signup.body.token);
check('the account carries that username', signup.body.user.nickname === name);
const token = signup.body.token;

// --- nobody else can have that name ---
const dupe = await api('POST', '/auth/signup', { username: name, avatarId: 0 });
check('the same username cannot be taken twice', dupe.status === 409);
const dupeCase = await api('POST', '/auth/signup', { username: name.toUpperCase(), avatarId: 0 });
check('uniqueness ignores capitalisation', dupeCase.status === 409);
const spaced = await api('POST', '/auth/check', { username: `  ${name}  ` });
check('surrounding spaces do not make a different name', spaced.body.exists === true);

// --- coming back with the same name returns your own account ---
await api('POST', '/shop/buy-cue', { cueId: 2 }, token);   // leave a trace on the account
const back = await api('POST', '/auth/login', { username: name });
check('signing in with a known name works', back.status === 200);
check('it is the same account, not a new one', back.body.user.id === signup.body.user.id);
check('progress is still there', back.body.user.ownedCues.includes(2));
check('signing in issues a working token',
  (await api('GET', '/session', null, back.body.token)).status === 200);

const unknown = await api('POST', '/auth/login', { username: 'Nobody' + Date.now() });
check('an unknown username cannot sign in', unknown.status === 404);

const tooShort = await api('POST', '/auth/signup', { username: 'ab' });
check('a too-short username is refused', tooShort.status === 400);

// --- without a PIN the username alone is enough, which is the tradeoff ---
const impostor = await api('POST', '/auth/login', { username: name });
check('with no PIN set, the name alone signs you in', impostor.status === 200);
check('signing in again retires the previous token',
  (await api('GET', '/session', null, back.body.token)).status === 401);

// --- setting a PIN closes that ---
const set = await api('PUT', '/auth/pin', { pin: '4821' }, impostor.body.token);
check('a PIN can be set', set.status === 200 && set.body.hasPin === true);

const probe = await api('POST', '/auth/check', { username: name });
check('the sign-in screen is told a PIN is needed', probe.body.needsPin === true);

const noPin = await api('POST', '/auth/login', { username: name });
check('the name alone no longer signs you in', noPin.status === 401);
const wrongPin = await api('POST', '/auth/login', { username: name, pin: '0000' });
check('a wrong PIN is refused', wrongPin.status === 401);
const rightPin = await api('POST', '/auth/login', { username: name, pin: '4821' });
check('the right PIN signs you in', rightPin.status === 200);

const changeNoAuth = await api('PUT', '/auth/pin', { pin: '1111' }, rightPin.body.token);
check('changing a PIN needs the current one', changeNoAuth.status === 401);
const changed = await api('PUT', '/auth/pin', { pin: '1111', currentPin: '4821' }, rightPin.body.token);
check('the PIN can be changed with the current one', changed.status === 200);

const cleared = await api('PUT', '/auth/pin', { pin: '', currentPin: '1111' }, rightPin.body.token);
check('the PIN can be removed', cleared.status === 200 && cleared.body.hasPin === false);
const nameOnly = await api('POST', '/auth/login', { username: name });
check('after removing it the name is enough again', nameOnly.status === 200);

const badPin = await api('PUT', '/auth/pin', { pin: '12' }, nameOnly.body.token);
check('a too-short PIN is refused', badPin.status === 400);

// --- a 4-digit PIN is 10,000 guesses, so guessing has to get expensive ---
const lockName = 'Lock' + (Date.now() % 100000);
const lockAcct = await api('POST', '/auth/signup', { username: lockName });
await api('PUT', '/auth/pin', { pin: '2468' }, lockAcct.body.token);

let sawLockout = false, lastTries = 99;
for (let i = 0; i < 6; i++) {
  const r = await api('POST', '/auth/login', { username: lockName, pin: '0001' });
  if (r.status === 429) { sawLockout = true; check('the lockout says how long to wait', r.body.retryAfterMs > 0); break; }
  if (typeof r.body.triesLeft === 'number') {
    if (r.body.triesLeft >= lastTries) check('each miss costs a try', false);
    lastTries = r.body.triesLeft;
  }
}
check('repeated wrong PINs lock the account', sawLockout);
check('the countdown is reported to the player', lastTries === 0);
const lockedOutRight = await api('POST', '/auth/login', { username: lockName, pin: '2468' });
check('even the right PIN is refused while locked', lockedOutRight.status === 429);

// --- a PIN you cannot reset is a way to lose an account for good ---
const recName = 'Rec' + (Date.now() % 100000);
const recAcct = await api('POST', '/auth/signup', { username: recName });
const withPin = await api('PUT', '/auth/pin', { pin: '9753' }, recAcct.body.token);
const code = withPin.body.recoveryCode;
check('setting a PIN hands back a recovery code', /^[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$/.test(code || ''));
check('the code avoids characters that look alike', !/[OI01]/.test(code || ''));

const badCode = await api('POST', '/auth/recover', { username: recName, code: 'AAAA-BBBB-CCCC' });
check('a wrong recovery code is refused', badCode.status === 401);

// lock the PIN first: the person holding the code must still get back in
for (let i = 0; i < 6; i++) await api('POST', '/auth/login', { username: recName, pin: '0000' });
check('the PIN is now locked', (await api('POST', '/auth/login', { username: recName, pin: '9753' })).status === 429);

const recovered = await api('POST', '/auth/recover', { username: recName, code });
check('the recovery code still works while the PIN is locked', recovered.status === 200);
check('recovery returns your own account', recovered.body.user.nickname === recName);
check('recovery clears the PIN', recovered.body.user.hasPin === false);
const backIn = await api('POST', '/auth/login', { username: recName });
check('and the name alone signs you in again', backIn.status === 200);
check('the used code cannot be used twice',
  (await api('POST', '/auth/recover', { username: recName, code })).status === 401);

const reissue = await api('PUT', '/auth/pin', { pin: '1357' }, backIn.body.token);
check('a new PIN comes with a new code', !!reissue.body.recoveryCode && reissue.body.recoveryCode !== code);
const extra = await api('POST', '/auth/recovery-code', { currentPin: '1357' }, backIn.body.token);
check('a fresh code can be requested with the current PIN', !!extra.body.recoveryCode);
check('requesting one needs the PIN',
  (await api('POST', '/auth/recovery-code', { currentPin: '0000' }, backIn.body.token)).status === 401);
check('the older code is retired by the new one',
  (await api('POST', '/auth/recover', { username: recName, code: reissue.body.recoveryCode })).status === 401);

// --- names are unique and permanent, so bulk registration has to be capped ---
const { createRateLimiter, isLoopback } = await import('./server/util/rateLimit.js');
let clock = 0;
const limiter = createRateLimiter({ limit: 3, windowMs: 1000, now: () => clock });
check('uses under the limit are allowed', [0, 1, 2].every(() => limiter.take('a').allowed));
const blocked = limiter.take('a');
check('the one over the limit is refused', blocked.allowed === false);
check('it says how long to wait', blocked.retryAfterMs === 1000);
check('another address is unaffected', limiter.take('b').allowed === true);
clock = 1001;
check('the window rolls over', limiter.take('a').allowed === true);
check('the operator\'s own machine is never throttled', isLoopback('127.0.0.1') && isLoopback('::1'));

console.log(fails === 0 ? '\nAUTH OK' : `\n${fails} CHECK(S) FAILED`);
process.exit(fails === 0 ? 0 : 1);
