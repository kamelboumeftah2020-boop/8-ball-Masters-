# 8 Ball Masters

A fast, competitive, portrait-only online billiards game. Two players race — on their
**own separate table** — to pot 7 numbered balls before their clock hits `00.00`.
Built from the full GDD (`8 BALL MASTERS V4.1 FINAL` + the V0.5 vertical-layout addendum).

```
npm install
npm start
# open http://localhost:3000
# admin dashboard: http://localhost:3000/admin  (token: admin123, override with ADMIN_TOKEN env var)
```

No build step. Plain ES modules on both client and server, Socket.IO for realtime,
a JSON-file store for persistence (`server/data/db.json`, created on first run).

## How the match works

Each player only ever sees **their own table** (this is in the spec — it's not a shared
physics table). That means the two clients never need to sync ball positions with each
other; only the timer and pot-count are networked, which keeps the whole thing simple
and cheat-resistant:

- The **server is the sole authority for time and coins** (per the spec's anti-cheat
  section). Every 100ms it ticks both players' clocks down and broadcasts the snapshot.
  A client can never report "I have more time" — it can only tell the server "I potted a
  ball", and the server decides whether that's plausible (rate-limited) before crediting
  the +10s bonus.
- Matchmaking queues are **100% isolated per table** (10 separate queues), exactly as
  specified. If nobody else is searching within ~5s, a bot opponent fills in so the game
  is always playable solo for testing/demoing.
- Win condition: pot all 7 balls first, or your opponent's timer reaches `00.00` first.
- Scratching the cue ball has no time penalty — it just respawns (per spec).

## What's implemented from the GDD

| Section | Status |
|---|---|
| Onboarding (guest/FB/Google stub, nickname, 6 starter avatars, 5,000 coin + cue gift, 10s tutorial) | ✅ |
| Home lobby (top bar, 10 tables, online counts, bottom nav) | ✅ |
| Per-table isolated matchmaking + searching animation | ✅ |
| Vertical 8%/72%/20% gameplay layout, aim dial + drag-on-table aiming, vertical power slider, one-thumb shoot-on-release | ✅ |
| Hand-rolled 2D billiard physics (cushions, ball-ball collisions, 6 pockets) | ✅ |
| Timer (20s start, +10s/ball, red+shake <10s, server authoritative) | ✅ |
| XP / level curve (1–10,000), career titles (Beginner → Godfather of Billiards) | ✅ |
| 10-star global league system, weekly reset (Fridays 00:00 UTC), scaled prizes, local + friends leaderboards | ✅ |
| Shop: 5 coin packages w/ first-purchase x2, 32 cues w/ real stat bonuses, 52 avatars | ✅ |
| Profile: ID + copy, stats, title, career coins, add friend / report | ✅ |
| Friends: add by ID, online status, challenge (stakes-free) | ✅ |
| Settings: 6 languages (ar/fr/en/de/es/tr) incl. Arabic RTL, vibration, sound, aim sensitivity, report, privacy/TOS | ✅ |
| Daily Box (24h), 3 daily missions/day | ✅ |
| Retention hooks: Revenge (2x stake rematch), Near-Miss message + cue upsell, 2h loss box, win-streak (4th win = 2x), shareable result-card image, friend invite links, emoji reactions in-match | ✅ |
| Admin dashboard: player list (active/inactive/banned), reports, ban/unban, analytics | ✅ |
| Anti-cheat: server-authoritative time, coins, and queue length | ✅ |
| Auto temp-ban at 5 reports, permanent ban after 3 temp-bans | ✅ |

## Honest limitations (things that need real infrastructure this environment doesn't have)

These are all wired up end-to-end with clear, working demo behavior, but would need
real third-party credentials/services in production:

- **Facebook/Google login** — buttons exist and are wired up, but fall back to guest
  auth (no OAuth app credentials available here).
- **Real payments** — the coin shop is a genuine server-authoritative economy, but
  "buying" a package is an instant demo grant, not a real payment gateway charge.
- **Video ads** — rescue/double-prize/interstitial "ads" are simulated with a progress
  bar (no ad network SDK). The reward logic they trigger is real.
- **Push notifications** — there's no FCM/APNs wired up, so the spec's "9pm reminder"
  is implemented as a server-side scheduled job that populates each user's in-app
  notification inbox rather than a native push.
- **10-second gameplay clip** — recording actual video isn't practical here; instead a
  shareable result-card PNG is generated client-side and downloaded (same "share your
  win" goal, different medium).
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
