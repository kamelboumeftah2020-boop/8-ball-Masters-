// The leaderboard screen has always shown a Friday countdown and prize amounts.
// This checks the job behind them: who gets paid, how much, and that the season
// actually rolls over.
import { db, createAccount, allUsers } from './server/store.js';
import { payoutAndReset, nextResetAt, runWeeklyResetIfDue } from './server/game/weekly.js';
import { weeklyPrizes } from './server/data/stars.js';

let fails = 0;
function check(label, ok) {
  console.log(`${ok ? 'PASS' : 'FAIL'}  ${label}`);
  if (!ok) fails++;
}

// --- the schedule itself ---
const wed = Date.UTC(2026, 0, 7, 12, 0, 0);          // a Wednesday
check('next reset from a Wednesday is the coming Friday 00:00 UTC',
  new Date(nextResetAt(wed)).toUTCString() === 'Fri, 09 Jan 2026 00:00:00 GMT');

const friMorning = Date.UTC(2026, 0, 9, 0, 30, 0);   // just after a reset
check('next reset from Friday morning is the following Friday',
  new Date(nextResetAt(friMorning)).toUTCString() === 'Fri, 16 Jan 2026 00:00:00 GMT');

// --- the payout ---
const stamp = Date.now();
const players = [];
for (let i = 0; i < 5; i++) {
  const u = createAccount({ nickname: `Weekly${stamp}_${i}`, avatarId: 0, country: 'INT' });
  u.starId = 3;                     // all in the Gold tier
  u.weeklyCoins = (5 - i) * 1000;   // #0 highest, #4 lowest
  u.coins = 0;
  players.push(u);
}
// somebody in a different tier, to prove tiers are paid independently
const other = createAccount({ nickname: `WeeklyOther${stamp}`, avatarId: 0, country: 'INT' });
other.starId = 7;
other.weeklyCoins = 50;
other.coins = 0;

const prizes = weeklyPrizes(3);
const paid = payoutAndReset(Date.now());

check('the top three of the tier are paid', players[0].coins === prizes.top1
  && players[1].coins === prizes.top2 && players[2].coins === prizes.top3);
check('fourth place and below get nothing', players[3].coins === 0 && players[4].coins === 0);
check('a higher tier pays a bigger first prize', weeklyPrizes(7).top1 > prizes.top1);
check('the leader of another tier is paid from their own tier',
  other.coins === weeklyPrizes(7).top1);
check('prizes count toward career coins', players[0].careerCoinsWon >= prizes.top1);
check('winners are told they won', players[0].notifications.some(n => n.text.includes('#1')));
check('every weekly total is back to zero', allUsers().every(u => u.weeklyCoins === 0));
check('the payout reports what it did', paid.some(r => r.rank === 1 && r.amount === prizes.top1));

// --- it must not pay twice for the same season ---
const again = runWeeklyResetIfDue(Date.now());
check('running again in the same season pays nothing', again === null);
check('the season boundary was recorded', db.lastWeeklyReset > 0);

// clean up the accounts this test made
for (const u of [...players, other]) {
  delete db.nicknames[u.nickname.toLowerCase()];
  delete db.users[u.id];
}

console.log(fails === 0 ? '\nWEEKLY OK' : `\n${fails} CHECK(S) FAILED`);
process.exit(fails === 0 ? 0 : 1);
