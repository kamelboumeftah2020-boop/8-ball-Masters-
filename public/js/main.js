import { api, getToken, setToken } from './api.js';
import { state, setUser } from './state.js';
import { loadLang, preferredLang } from './i18n.js';
import { registerScreens, navigate, currentScreen, setScreenChangeHandler } from './router.js';
import { connectSocket, identify, on as onSocket, emit as socketEmit } from './net/socket.js';
import { toast, openModal, closeModal } from './ui.js';
import { t } from './i18n.js';
import * as audio from './engine/audio.js';

import * as onboarding from './screens/onboarding.js';
import * as home from './screens/home.js';
import * as search from './screens/search.js';
import * as game from './screens/game.js';
import * as result from './screens/result.js';
import * as shop from './screens/shop.js';
import * as leaderboard from './screens/leaderboard.js';
import * as profile from './screens/profile.js';
import * as friends from './screens/friends.js';
import * as settings from './screens/settings.js';
import * as banned from './screens/banned.js';

registerScreens({
  onboarding: onboarding.render,
  home: home.render,
  search: search.render,
  game: game.render,
  result: result.render,
  shop: shop.render,
  leaderboard: leaderboard.render,
  profile: profile.render,
  friends: friends.render,
  settings: settings.render,
  banned: banned.render,
});

async function loadCatalogs() {
  const [cues, avatars, tables] = await Promise.all([
    api.get('/cues'), api.get('/avatars'), api.get('/tables'),
  ]);
  state.cues = cues.cues;
  state.avatars = avatars.avatars;
  state.tables = tables.tables;
}

async function boot() {
  const invite = new URLSearchParams(location.search).get('invite');

  await loadLang(preferredLang());
  try { await loadCatalogs(); } catch (e) { console.error('catalog load failed', e); }

  if (getToken()) {
    try {
      const { user } = await api.get('/session');
      setUser(user);
      audio.setEnabled(user.settings.sound);
      await loadLang(user.settings.lang);
      connectSocket();
      identify();
      navigate('home');
      return;
    } catch (e) {
      if (e.status === 403) {
        navigate('banned', { reason: e.data?.reason });
        return;
      }
      // 401 means the stored token is stale - start fresh
      if (e.status === 401) setToken(null);
    }
  }
  navigate('onboarding', { invite });
}

// Browsers keep audio muted until the page has been touched, so unlock on the
// first interaction and give every tappable control a click.
document.addEventListener('pointerdown', () => audio.unlock(), { once: true });
document.addEventListener('click', (e) => {
  if (e.target.closest('.btn, .icon-btn, .tab-btn, .play-btn, .fine-btn, .avatar-pick, [data-nav]')) {
    audio.uiTap();
  }
});

// Background music belongs in the menus; during a match the table sounds carry it.
const SILENT_SCREENS = new Set(['game', 'search']);
setScreenChangeHandler((name) => {
  if (SILENT_SCREENS.has(name)) audio.stopMusic();
  else audio.startMusic();
});

// Any match that starts - from the queue, a rematch or a friend's challenge -
// takes you to the table from wherever you are. Handled once, here, so no screen
// can navigate twice for the same event.
const toTable = (matchData) => {
  if (currentScreen()?.name === 'game') return;
  closeModal();
  navigate('game', { matchData });
};
onSocket('match_start', toTable);
// Reconnecting into a match you are still in beats the old behaviour, where
// dropping out for any reason meant losing on the clock.
onSocket('match_resume', toTable);

// A friend's challenge arrives as an invitation you can accept or turn down.
onSocket('challenge_invite', (inv) => {
  if (currentScreen()?.name === 'game') return;   // don't interrupt a live match
  audio.coin();
  openModal(`
    <h3>${t('challengeFrom', { name: inv.fromNickname })}</h3>
    <p class="muted">${inv.tableName} · ${t('friendlyMatch')}</p>
    <button class="btn gold block" id="ch-accept">${t('accept')}</button>
    <button class="btn ghost block" id="ch-decline">${t('decline')}</button>
  `, {
    onMount: (root) => {
      const respond = (accept) => {
        closeModal();
        socketEmit('challenge_respond', { challengeId: inv.challengeId, accept });
      };
      root.querySelector('#ch-accept').onclick = () => respond(true);
      root.querySelector('#ch-decline').onclick = () => respond(false);
    },
  });
});
onSocket('challenge_sent', ({ nickname }) => toast(t('challengeSent', { name: nickname })));
onSocket('challenge_declined', ({ reason, nickname }) => {
  toast(t(reason === 'expired' ? 'challengeExpired' : 'challengeDeclined', { name: nickname }));
});

connectSocket();
boot().catch((e) => {
  console.error(e);
  toast('Failed to load. Please refresh.');
});
