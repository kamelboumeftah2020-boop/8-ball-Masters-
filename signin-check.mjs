// The sign-in flow, walked in a real browser: a new name opens an account, the same
// name comes back to it from a clean browser, and a PIN set in settings is asked for.
import { chromium } from 'playwright';
const browser = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome', args: ['--no-sandbox'] });
const ctx = await browser.newContext({ viewport: { width: 420, height: 880 } });
const page = await ctx.newPage();
const errs = [];
page.on('console', m => { if (m.type() === 'error') errs.push(m.text()); });
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

// --- 2. the same name from a clean browser comes back to the same account ---
const ctx2 = await browser.newContext({ viewport: { width: 420, height: 880 } });
const p2 = await ctx2.newPage();
p2.on('console', m => { if (m.type() === 'error') errs.push('ctx2: ' + m.text()); });
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
await p2.waitForTimeout(700);
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

console.log(errs.length ? 'console errors:\n' + errs.join('\n') : 'no console errors');
await browser.close();
console.log(fails === 0 && errs.length === 0 ? '\nSIGN-IN OK' : `\n${fails} CHECK(S) FAILED`);
process.exit(fails === 0 && errs.length === 0 ? 0 : 1);
