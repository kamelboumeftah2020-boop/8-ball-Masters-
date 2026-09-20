// The sign-in flow, walked in a real browser: a new name opens an account, the same
// name comes back to it from a clean browser, and a PIN set in settings is asked for.
import { launchBrowser } from './browser.mjs';
const browser = await launchBrowser();
const ctx = await browser.newContext({ viewport: { width: 420, height: 880 } });
const page = await ctx.newPage();
// The negative cases here deliberately provoke 401/404/409 responses, and the
// browser logs every failed fetch as a console error - those are expected, so
// only genuine script errors count.
const EXPECTED = /Failed to load resource.*\b(400|401|403|404|409|429)\b/;
const errs = [];
const watch = (pg, tag) => pg.on('console', m => {
  if (m.type() === 'error' && !EXPECTED.test(m.text())) errs.push(`${tag}: ${m.text()}`);
});
watch(page, 'main');
const name = 'Kamel' + (Date.now() % 10000);
let fails = 0;
const check = (l, ok) => { console.log(`${ok ? 'PASS' : 'FAIL'}  ${l}`); if (!ok) fails++; };

// --- 1. a brand new name opens an account ---
await page.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
await page.waitForTimeout(400);
check('the first screen asks only for a username', await page.isVisible('#username'));
await page.fill('#username', name);
await page.click('#go');
await page.waitForSelector('.avatar-pick', { timeout: 6000 });
check('a new name goes to avatar pick', await page.isVisible('.avatar-pick'));
await page.click('.avatar-pick:nth-child(3)');
await page.click('#create');
await page.waitForTimeout(900);
check('the new account gets the welcome gift', (await page.textContent('body')).includes('🎁'));
await page.click('#go');
await page.waitForTimeout(600);
check('and lands in the lobby', await page.isVisible('[data-play="1"]'));
check('the lobby shows that username', (await page.textContent('body')).includes(name));
check('an idle table says so instead of inventing a crowd', await page.isVisible('.online.empty'));

// --- 2. the same name from a clean browser comes back to the same account ---
const ctx2 = await browser.newContext({ viewport: { width: 420, height: 880 } });
const p2 = await ctx2.newPage();
watch(p2, 'ctx2');
await p2.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
await p2.waitForTimeout(400);
await p2.fill('#username', name);
await p2.click('#go');
await p2.waitForTimeout(1200);
check('a known name signs straight back in, no avatar step', await p2.isVisible('[data-play="1"]'));
check('it is the same account', (await p2.textContent('body')).includes(name));

// --- 3. a name someone already owns cannot be taken ---
const ctx3 = await browser.newContext({ viewport: { width: 420, height: 880 } });
const p3 = await ctx3.newPage();
await p3.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
await p3.waitForTimeout(400);
await p3.fill('#username', 'ab');
await p3.click('#go');
await p3.waitForTimeout(500);
check('a too-short name is rejected on screen', await p3.isVisible('#username'));

// --- 4. setting a PIN in settings makes sign-in ask for it ---
await p2.click('#tb-settings');
await p2.waitForTimeout(500);
check('settings offers an account PIN', await p2.isVisible('#account-pin'));
await p2.click('#account-pin');
await p2.waitForTimeout(400);
await p2.fill('#new-pin', '7391');
await p2.click('#save-pin');
await p2.waitForTimeout(900);
check('the recovery code is shown once, right after the PIN is set', await p2.isVisible('.recovery-code'));
const recoveryCode = (await p2.textContent('.recovery-code')).trim();
check('and it looks like a code you can write down', /^[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$/.test(recoveryCode));
await p2.click('#done-code');
await p2.waitForTimeout(500);
check('the PIN is saved and shown as on', (await p2.textContent('#account-pin')).match(/On|مفعّل|Aktiv|Activado|Activé|Açık/i) !== null);

const ctx4 = await browser.newContext({ viewport: { width: 420, height: 880 } });
const p4 = await ctx4.newPage();
await p4.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
await p4.waitForTimeout(400);
await p4.fill('#username', name);
await p4.click('#go');
await p4.waitForTimeout(800);
check('signing in with that name now asks for the PIN', await p4.isVisible('#pin'));
await p4.fill('#pin', '0000');
await p4.click('#go');
await p4.waitForTimeout(700);
check('a wrong PIN does not get in', await p4.isVisible('#pin'));
await p4.fill('#pin', '7391');
await p4.click('#go');
await p4.waitForTimeout(1200);
check('the right PIN gets in', await p4.isVisible('[data-play="1"]'));

// --- 5. forgetting the PIN is survivable: the recovery code gets you back in ---
const ctx5 = await browser.newContext({ viewport: { width: 420, height: 880 } });
const p5 = await ctx5.newPage();
watch(p5, 'ctx5');
await p5.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
await p5.waitForTimeout(400);
await p5.fill('#username', name);
await p5.click('#go');
await p5.waitForTimeout(800);
check('the PIN screen offers a way out', await p5.isVisible('#forgot'));
await p5.click('#forgot');
await p5.waitForTimeout(400);
await p5.fill('#code', 'AAAA-BBBB-CCCC');
await p5.click('#go');
await p5.waitForTimeout(800);
check('a wrong recovery code does not get in', await p5.isVisible('#code'));
await p5.fill('#code', recoveryCode);
await p5.click('#go');
await p5.waitForTimeout(1500);
check('the real recovery code gets in', await p5.isVisible('[data-play="1"]'));

const ctx6 = await browser.newContext({ viewport: { width: 420, height: 880 } });
const p6 = await ctx6.newPage();
await p6.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
await p6.waitForTimeout(400);
await p6.fill('#username', name);
await p6.click('#go');
await p6.waitForTimeout(1200);
check('recovering cleared the PIN, so the name alone works again', await p6.isVisible('[data-play="1"]'));

console.log(errs.length ? 'console errors:\n' + errs.join('\n') : 'no console errors');
await browser.close();
console.log(fails === 0 && errs.length === 0 ? '\nSIGN-IN OK' : `\n${fails} CHECK(S) FAILED`);
process.exit(fails === 0 && errs.length === 0 ? 0 : 1);
