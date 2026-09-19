import { api, setToken } from '../api.js';
import { state, setUser } from '../state.js';
import { t, loadLang } from '../i18n.js';
import { navigate } from '../router.js';
import { connectSocket, identify } from '../net/socket.js';
import { toast } from '../ui.js';
import * as audio from '../engine/audio.js';

// Sign-in is a username and nothing else. Names are unique, so one name is one
// player: a name nobody has taken opens a new account, a name that exists signs
// back into it (asking for the PIN if that account set one).
export function render(root, params = {}) {
  let step = 'username';
  let username = '';
  let avatarId = 0;
  let inviteInfo = null;

  if (params.invite) {
    api.get(`/invite/${params.invite}`).then((r) => { inviteInfo = r; }).catch(() => {});
  }

  function draw() {
    if (step === 'username') return drawUsername();
    if (step === 'pin') return drawPin();
    if (step === 'avatar') return drawAvatar();
  }

  function logoHtml(small) {
    const size = small ? 'style="width:74px;height:74px;"' : '';
    const badge = small ? 'style="width:34px;height:34px;font-size:16px;"' : '';
    return `<div class="logo-ball" ${size}><div class="inner"><div class="badge" ${badge}>8</div></div></div>`;
  }

  function drawUsername() {
    root.innerHTML = `
      <div class="onboarding">
        ${logoHtml(false)}
        <div>
          <div class="app-title">${t('appName')}</div>
          <div class="app-tagline">${t('tagline')}</div>
        </div>
        <input class="text-input" id="username" maxlength="18" autocomplete="username"
               placeholder="${t('usernamePlaceholder')}" value="${username}" />
        <div class="muted" style="font-size:14px;max-width:340px;">${t('usernameHint')}</div>
        <button class="btn primary block" id="go" style="max-width:360px;">${t('continueBtn')}</button>
      </div>`;

    const input = root.querySelector('#username');
    input.focus();
    const submit = () => continueWithName(input.value);
    root.querySelector('#go').onclick = submit;
    input.onkeydown = (e) => { if (e.key === 'Enter') submit(); };
  }

  async function continueWithName(raw) {
    const name = String(raw || '').trim().replace(/\s+/g, ' ');
    if (name.length < 3) { toast(t('usernameTooShort')); return; }
    username = name;
    try {
      const { exists, needsPin } = await api.post('/auth/check', { username: name });
      if (!exists) { step = 'avatar'; draw(); return; }
      if (needsPin) { step = 'pin'; draw(); return; }
      await signIn(null);
    } catch {
      toast(t('somethingWrong'));
    }
  }

  function drawPin() {
    root.innerHTML = `
      <div class="onboarding">
        ${logoHtml(true)}
        <h2>${t('welcomeBack', { name: username })}</h2>
        <p class="muted">${t('enterPin')}</p>
        <input class="text-input" id="pin" type="password" inputmode="numeric"
               maxlength="8" autocomplete="current-password" placeholder="••••" />
        <button class="btn primary block" id="go" style="max-width:360px;">${t('continueBtn')}</button>
        <button class="btn ghost block" id="back" style="max-width:360px;">${t('back')}</button>
      </div>`;
    const input = root.querySelector('#pin');
    input.focus();
    const submit = () => signIn(input.value);
    root.querySelector('#go').onclick = submit;
    input.onkeydown = (e) => { if (e.key === 'Enter') submit(); };
    root.querySelector('#back').onclick = () => { step = 'username'; draw(); };
  }

  async function signIn(pin) {
    try {
      const { user, token } = await api.post('/auth/login', { username, pin });
      setToken(token);
      await enterGame(user);
      navigate('home');
    } catch (e) {
      if (e.status === 401) toast(t('wrongPin'));
      else if (e.status === 403) navigate('banned', { reason: e.data?.reason });
      else toast(t('somethingWrong'));
    }
  }

  function drawAvatar() {
    const starters = state.avatars.filter(a => a.currency === 'free').slice(0, 6);
    root.innerHTML = `
      <div class="onboarding">
        <h2>${t('chooseAvatar')}</h2>
        <p class="muted">${t('newAccountFor', { name: username })}</p>
        <div class="avatar-grid">
          ${starters.map(a => `
            <button class="avatar-pick ${a.id === avatarId ? 'selected' : ''}" data-id="${a.id}"
              style="background:linear-gradient(135deg, ${a.gradient[0]}, ${a.gradient[1]})">${a.emoji}</button>
          `).join('')}
        </div>
        <button class="btn primary block" id="create" style="max-width:360px;">${t('claim')}</button>
        <button class="btn ghost block" id="back" style="max-width:360px;">${t('back')}</button>
      </div>`;
    root.querySelectorAll('.avatar-pick').forEach(btn => {
      btn.onclick = () => { avatarId = Number(btn.dataset.id); drawAvatar(); };
    });
    root.querySelector('#create').onclick = createAccount;
    root.querySelector('#back').onclick = () => { step = 'username'; draw(); };
  }

  async function createAccount() {
    try {
      const { user, token } = await api.post('/auth/signup', { username, avatarId, country: 'INT' });
      setToken(token);
      await enterGame(user);
      if (inviteInfo?.fromUserId) {
        api.post('/friends/add', { friendId: inviteInfo.fromUserId }).catch(() => {});
      }
      showGift();
    } catch (e) {
      if (e.status === 409) { toast(t('usernameTaken')); step = 'username'; draw(); }
      else toast(t('somethingWrong'));
    }
  }

  async function enterGame(user) {
    setUser(user);
    audio.setEnabled(user.settings.sound);
    audio.unlock();
    await loadLang(user.settings.lang);
    connectSocket();
    identify();
  }

  function showGift() {
    root.innerHTML = `
      <div class="onboarding">
        <div style="font-size:78px;">🎁</div>
        <h2 class="gold-text">${t('startGiftTitle')}</h2>
        <p class="muted">${t('startGiftDesc')}</p>
        <div class="card" style="max-width:340px;">
          <p style="font-size:17px;line-height:1.5;margin:0;">${t('tutorialText')}</p>
        </div>
        <button class="btn primary block" id="go" style="max-width:360px;">${t('gotIt')}</button>
      </div>`;
    root.querySelector('#go').onclick = () => navigate('home');
  }

  draw();
}
