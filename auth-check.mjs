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

console.log(fails === 0 ? '\nAUTH OK' : `\n${fails} CHECK(S) FAILED`);
process.exit(fails === 0 ? 0 : 1);
