# 8 Ball Masters

A fast, competitive, portrait-only online billiards game. Two players race — on their
**own separate table** — to pot 7 numbered balls before their clock hits `00.00`.
Built from the full GDD (`8 BALL MASTERS V4.1 FINAL` + the V0.5 vertical-layout addendum).

```
npm install
npm start
# open http://localhost:3000
# admin dashboard: http://localhost:3000/admin  (token: set ADMIN_TOKEN, or copy the random one the server prints at startup)
```

No build step. Plain ES modules on both client and server, Socket.IO for realtime,
a JSON-file store for persistence (`server/data/db.json`, created on first run).

## How the match works

Each player only ever sees **their own table** (this is in the spec — it's not a shared
physics table). That means the two clients never need to sync ball positions with each
other; only the timer and pot-count are networked, which keeps the whole thing simple
and cheat-resistant:

- The **server is the sole authority** (per the spec's anti-cheat section). It deals
  the ball layout, runs the same simulation the client draws, ticks both clocks, and
  decides what was potted. A client can only report *how it struck the ball* — angle,
  power and spin — never what it scored. Because the physics is frame-rate
  independent, the server's replay of a shot lands on exactly the state the player
  sees, so this costs nothing in feel.
- Matchmaking queues are **100% isolated per table** (10 separate queues), exactly as
  specified. If nobody else is searching within ~5s, a bot opponent fills in so the game
  is always playable solo for testing/demoing.
- Win condition: pot all 7 balls first, or your opponent's timer reaches `00.00` first.
- Scratching the cue ball has no time penalty — it just respawns (per spec).

## Controls

Everything is reachable with one thumb, in portrait:

- **Aim** — drag anywhere on the cloth. The aim does *not* jump to your finger; it
  rotates by how far your finger swings around the cue ball, so fine placement is
  possible. The strip under the table scrubs the angle degree-by-degree (its rate
  follows the player's aim-sensitivity setting) and the ◀ ▶ buttons nudge it, with
  hold-to-repeat.
- **Shoot** — pull straight back from behind the cue ball to draw the stick, then
  release. The side slider (shaped like a cue) does the same thing for anyone who
  prefers a slider. There is no separate "hit" button.
- **Spin** — drag the red dot on the little cue ball to pick your contact point.
  Top spin follows through after contact, back spin draws the cue ball back, side
  spin kicks off the cushion. Double-tap the ball to re-centre.
- The on-table aim guide shows a ghost cue ball at the predicted contact point plus
  the object ball's predicted direction.

Sound and music are synthesised at runtime with the Web Audio API — there are no
audio files to ship. Ball clicks are pitched and shaped from the actual impact
speed, cushions thud lower and softer, and there are separate cues for potting,
the strike, the last five seconds, coins and the win/lose stings. A quiet chord
bed plays in the menus and steps aside during a match. It all follows the
player's sound setting.

## The look

One palette drives every screen, defined as CSS custom properties at the top of
`public/css/style.css`:

- A violet night background with three slow-drifting colour blobs behind
  everything, so no screen is a flat dark rectangle.
- Cyan, magenta, violet, lime and amber as the accents. Amber is reserved for
  coins and prizes, where it means money; everything else rotates by hue - the
  five nav tabs each own a colour, the ten tables each own a colour (that colour
  is also the piping, pocket rims and diamonds on that table in-game), and the
  coin packages step through the palette as they get bigger.
- Glass surfaces: translucent panels with a blur behind them, a light hairline
  border and a highlight along the top edge.
- The one thing deliberately left realistic is the table itself - cloth, wood,
  pockets and balls are drawn to look like a real table, not a neon one.

Changing the whole scheme means editing the tokens in `:root`; nothing else
hard-codes a colour except the table felts in `server/data/tables.js`.

## Signing in

There is no password and no third-party login: a player is their username.

- Names are unique. Capitalisation and stray spaces don't make a new name, so
  `Kamel`, `kamel` and `  Kamel  ` are all the same player.
- A name nobody has taken opens a new account (pick an avatar, collect the gift).
- A name you already own signs you straight back into it, with your coins, cues
  and stats intact.

A username is public — it is on every leaderboard — so on its own it is not a
secret. Anyone who types your name gets your account. Settings → Account PIN
closes that: with a PIN set, the name alone is refused and sign-in asks for the
PIN. It is optional because the spec asks for sign-in to be the username and
nothing else; it is there because without it a name is not a credential.

PINs are stored only as a salted SHA-256 hash and compared in constant time, and
signing in issues a fresh session token that retires the previous one.

A four-digit PIN is only 10,000 guesses, so guessing is made expensive: after
five misses each further miss locks the account for longer (30s, 2m, 10m, then
an hour), and the counters live on the account so restarting the server does not
clear them. A correct PIN resets them.

Because a PIN nobody can reset is a way to lose an account for good, setting one
also mints a **recovery code** — twelve characters from a 32-symbol alphabet with
no look-alikes (no O/0, no I/1), shown once and stored only as a hash. Entering it
on the sign-in screen clears the PIN, signs you in, and burns the code. It has its
own lockout, so an attacker hammering the PIN cannot lock out the person who
actually holds the code.

Two more limits guard the things that get an account taken away. Five reports
temp-ban an account and three temp-bans ban it for good, so the report button
would be a weapon if one person could pull it five times: only the first report
one player files against a target counts, nobody can report themselves, and each
player gets five reports a day. And the admin dashboard bans and unbans accounts,
so it never ships with a token anyone could guess - without `ADMIN_TOKEN` the
server mints a random one at startup and prints it once.

Names are unique and permanent, so bulk registration is the cheap
way to ruin the namespace. One address may open 20 accounts an hour
(`SIGNUP_LIMIT_PER_HOUR`); loopback is exempt, so the dev server and the test
suites are never throttled.

## Tests

- `npm run test:physics` — fires 200 shots across the power range and asserts no
  ball tunnels through another, none escape the cushions, none end up stuck
  overlapping, and roll times stay sane for a 20-second clock.
- `npm run test:pvp` — drives two real socket clients through a full match: it
  works out potting shots with its own copy of the physics, sends only the shot
  parameters, and asserts the server independently credits those exact pots. Also
  covers both win conditions, the coin settlement, account security, and a set of
  cheat attempts. (Start the server first.)
- `npm run test:ui` — drives a real browser: captures the layout the server sends,
  computes a potting shot, aims and strikes it with genuine pointer input, and
  checks the pot comes back from the server. (Start the server first.)
- `npm run test:weekly` — covers the Friday season rollover: the schedule, who is
  paid, that fourth place gets nothing, that star tiers pay independently, and
  that a season never pays out twice.
- `npm run test:auth` — covers username sign-in: that a name is unique (ignoring
  case and stray spaces), that a known name returns you to your own account with
  your progress, that an unknown one cannot sign in, the whole optional-PIN
  behaviour, the guessing lockout, recovery codes (issued once, usable once, and
  still usable while the PIN is locked), the signup rate limiter, the report-abuse
  guards, and that the admin dashboard refuses a guessable token.
  (Start the server first.)
- `npm run test:signin` — walks the sign-in screen itself in a real browser: a new
  name opens an account, the same name from a clean browser comes back to it, a PIN
  set in settings is then asked for, and the recovery code shown when that PIN was
  set gets a locked-out player back in. (Start the server first.)

All six run on every push and pull request via `.github/workflows/ci.yml`.

## What's implemented from the GDD

| Section | Status |
|---|---|
| Onboarding (username sign-in with unique names, 6 starter avatars, 5,000 coin + cue gift, 10s tutorial) | ✅ |
| Home lobby (top bar, 10 tables, real online counts, bottom nav) | ✅ |
| Per-table isolated matchmaking + searching animation | ✅ |
| Vertical 8%/72%/20% gameplay layout, one-thumb controls (see below) | ✅ |
| Hand-rolled 2D billiard physics (cushions, ball-ball collisions, 6 pockets) | ✅ |
| Timer (20s start, +10s/ball, red+shake <10s, server authoritative) | ✅ |
| XP / level curve (1–10,000), career titles (Beginner → Godfather of Billiards) | ✅ |
| 10-star global league system, weekly reset (Fridays 00:00 UTC), scaled prizes, local + friends leaderboards | ✅ |
| Shop: 5 coin packages w/ first-purchase x2, 32 cues w/ real stat bonuses, 52 avatars | ✅ |
| Profile: ID + copy, stats, title, career coins, add friend / report | ✅ |
| Friends: add by ID, online status, challenge invites you accept or decline (stakes-free) | ✅ |
| Settings: 6 languages (ar/fr/en/de/es/tr) incl. Arabic RTL, vibration, sound, aim sensitivity, report, privacy/TOS | ✅ |
| Daily Box (24h), 3 daily missions/day | ✅ |
| Retention hooks: Revenge (2x stake rematch), Near-Miss message + cue upsell, 2h loss box, win-streak (4th win = 2x), shareable result-card image, friend invite links, emoji reactions in-match | ✅ |
| Admin dashboard: player list (active/inactive/banned), reports, ban/unban, analytics | ✅ |
| Anti-cheat: server deals the layout, simulates every shot, and owns time, coins and queue length | ✅ |
| Session tokens (a public profile ID can't act on an account) | ✅ |
| PIN guessing lockout, one-time recovery codes, signup rate limit | ✅ |
| Report abuse guards: unique reporters only, daily cap, no self-reports | ✅ |
| Admin dashboard token is random per run unless `ADMIN_TOKEN` is set | ✅ |
| Reconnect into a match after dropping out | ✅ |
| Auto temp-ban at 5 reports, permanent ban after 3 temp-bans | ✅ |

## Honest limitations (things that need real infrastructure this environment doesn't have)

These are all wired up end-to-end with clear, working demo behavior, but would need
real third-party credentials/services in production:

- **Real payments** — the coin shop is a genuine server-authoritative economy, but
  "buying" a package is an instant demo grant, not a real payment gateway charge.
- **Video ads** — rescue/double-prize/interstitial "ads" are simulated with a progress
  bar (no ad network SDK). The reward logic they trigger is real.
- **Push notifications** — there's no FCM/APNs wired up, so the spec's "9pm reminder"
  is implemented as a server-side scheduled job that populates each user's in-app
  notification inbox rather than a native push.
- **Storage** — persistence is a JSON file written atomically (temp file + rename, so
  a crash mid-write can't corrupt it) and flushed on shutdown. Fine for development
  and small scale; a real deployment wants a proper database.
- **10-second gameplay clip** — the table is recorded with MediaRecorder and the last
  ~12 seconds are offered as a real `.webm` after a match. Browsers without
  MediaRecorder fall back to a shareable result-card PNG.
- **A player base** — the lobby's online count is the real number of people queued
  or playing on that table, which on a fresh install is zero. Matchmaking waits
  ~5s for a human and then fills the seat with a bot, so a solo install is always
  playable — but most matches will be against a bot until real players show up.
- **50/30+ unique art assets** — cues/avatars are data-driven (name, category, rarity,
  stat bonuses) and rendered with CSS gradients + emoji rather than bespoke art.

## Project layout

```
server/
  index.js            Express + Socket.IO entrypoint, static hosting, 9pm notification job
  sockets.js           Realtime handlers: queue, match events, presence, revenge, challenges
  store.js             JSON-file persisted state (users, invites, reports, match log)
  game/
    matchQueue.js       Per-table isolated matchmaking queues
    matchEngine.js       Server-authoritative match loop: timer, pot/scratch, settlement
  routes/
    api.js               REST: auth, profile, shop, leaderboard, friends, missions, i18n...
    admin.js              Token-protected admin REST API
  data/                  Tables, cues, avatars, titles, star leagues, missions, i18n JSON
  util/                  Leveling curve, DTOs, ID generation

public/
  index.html, css/style.css
  js/
    main.js, router.js, state.js, i18n.js, api.js, ui.js
    net/socket.js         Thin Socket.IO client + event bus
    engine/                physics.js (billiards sim), render.js (canvas), controls.js (aim/power input)
    screens/                One module per screen (onboarding, home, search, game, result, shop, leaderboard, profile, friends, settings)

admin/index.html        Standalone admin dashboard (token auth)
```
