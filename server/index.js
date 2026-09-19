import express from 'express';
import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { Server } from 'socket.io';

import { api } from './routes/api.js';
import { admin } from './routes/admin.js';
import { initSockets } from './sockets.js';
import { allUsers, persist } from './store.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PORT = process.env.PORT || 3000;

const app = express();
app.use(express.json());

app.use('/api', api);
app.use('/api/admin', admin);
app.use('/admin', express.static(path.join(__dirname, '..', 'admin')));
app.use(express.static(path.join(__dirname, '..', 'public')));

app.get('/invite/:code', (req, res) => {
  res.redirect(`/?invite=${req.params.code}`);
});

const server = http.createServer(app);
const io = new Server(server, { cors: { origin: '*' } });
initSockets(io);

// Stand-in for a real push-notification service (no FCM/APNs credentials in this
// environment): every day at ~21:00 server time we drop a personalized message into
// each active user's in-app notification inbox, matching the spec's "9pm re-engagement"
// idea. A production build would swap this for actual push delivery.
let lastNotifyDay = null;
setInterval(() => {
  const now = new Date();
  if (now.getHours() !== 21 || lastNotifyDay === now.toDateString()) return;
  lastNotifyDay = now.toDateString();
  for (const user of allUsers()) {
    if (user.banned) continue;
    const idleDays = (Date.now() - user.lastSeenAt) / (24 * 60 * 60 * 1000);
    if (idleDays < 0.5) continue;
    const msg = user.coins > 0
      ? `Your ${user.coins.toLocaleString()} coins are waiting! Jump back in now.`
      : `A rival just climbed past you on the leaderboard. Go take your rank back!`;
    user.notifications = user.notifications || [];
    user.notifications.push({ id: `${Date.now()}`, text: msg, at: Date.now() });
  }
  persist();
}, 60 * 1000);

server.listen(PORT, () => {
  console.log(`8 Ball Masters server listening on http://localhost:${PORT}`);
  console.log(`Admin dashboard at http://localhost:${PORT}/admin (token via ADMIN_TOKEN, default admin123)`);
});
