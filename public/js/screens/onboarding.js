import { api } from '../api.js';
import { state, setUser } from '../state.js';
import { t, loadLang } from '../i18n.js';
import { navigate } from '../router.js';
import { connectSocket, identify } from '../net/socket.js';
import { toast, avatarHtml } from '../ui.js';

export function render(root, params = {}) {
  let step = 'login';
  let nickname = '';
  let avatarId = 0;
  let inviteInfo = null;

  if (params.invite) {
    api.get(`/invite/${params.invite}`).then((r) => { inviteInfo = r; }).catch(() => {});
  }

  function draw() {
    if (step === 'login') return drawLogin();
    if (step === 'nickname') return drawNickname();
    if (step === 'avatar') return drawAvatar();
  }

  function drawLogin() {
    root.innerHTML = `
      <div class="onboarding">
        <div class="logo-ball"><div class="inner"><div class="badge">8</div></div></div>
        <div>
          <div class="app-title">${t('appName')}</div>
          <div class="app-tagline">${t('tagline')}</div>
        </div>
        <div class="onboarding-buttons">
          <button class="btn block" id="fb">📘 ${t('continueFacebook')}</button>
          <button class="btn block" id="gg">🔴 ${t('continueGoogle')}</button>
          <button class="btn primary block" id="guest">${t('continueGuest')}</button>
        </div>
      </div>`;
    root.querySelector('#fb').onclick = () => { toast('Facebook login demo — continuing as guest'); step = 'nickname'; draw(); };
    root.querySelector('#gg').onclick = () => { toast('Google login demo — continuing as guest'); step = 'nickname'; draw(); };
    root.querySelector('#guest').onclick = () => { step = 'nickname'; draw(); };
  }

  function drawNickname() {
    root.innerHTML = `
      <div class="onboarding">
        <div class="logo-ball" style="width:74px;height:74px;"><div class="inner"><div class="badge" style="width:34px;height:34px;font-size:16px;">8</div></div></div>
        <h2>${t('enterNickname')}</h2>
        <input class="text-input" id="nick" maxlength="18" placeholder="${t('nicknamePlaceholder')}" value="${nickname}" />
        <button class="btn primary block" id="next" style="max-width:320px;">${t('confirm')}</button>
      </div>`;
    const input = root.querySelector('#nick');
    input.focus();
    root.querySelector('#next').onclick = () => {
      const val = input.value.trim();
      if (val.length < 3) { toast('Nickname too short'); return; }
      nickname = val;
      step = 'avatar';
      draw();
    };
  }

  function drawAvatar() {
    const starters = state.avatars.filter(a => a.currency === 'free').slice(0, 6);
    root.innerHTML = `
      <div class="onboarding">
        <h2>${t('chooseAvatar')}</h2>
        <div class="avatar-grid">
          ${starters.map(a => `
            <button class="avatar-pick ${a.id === avatarId ? 'selected' : ''}" data-id="${a.id}"
              style="background:linear-gradient(135deg, ${a.gradient[0]}, ${a.gradient[1]})">${a.emoji}</button>
          `).join('')}
        </div>
        <button class="btn primary block" id="create" style="max-width:320px;">${t('claim')}</button>
      </div>`;
    root.querySelectorAll('.avatar-pick').forEach(btn => {
      btn.onclick = () => { avatarId = Number(btn.dataset.id); drawAvatar(); };
    });
    root.querySelector('#create').onclick = createAccount;
  }

  async function createAccount() {
    try {
      const { user } = await api.post('/auth/guest', { nickname, avatarId, country: 'INT' });
      setUser(user);
      await loadLang(user.settings.lang);
      connectSocket();
      identify();
      if (inviteInfo?.fromUserId) {
        api.post('/friends/add', { userId: user.id, friendId: inviteInfo.fromUserId }).catch(() => {});
      }
      showGift();
    } catch (e) {
      if (e.status === 409) toast(t('idCopied') && 'Nickname already taken');
      else toast('Something went wrong, try again');
    }
  }

  function showGift() {
    root.innerHTML = `
      <div class="onboarding">
        <div style="font-size:54px;">🎁</div>
        <h2 class="gold-text">${t('startGiftTitle')}</h2>
        <p class="muted">${t('startGiftDesc')}</p>
        <div class="card" style="max-width:320px;">
          <p style="font-size:13px;">${t('tutorialText')}</p>
        </div>
        <button class="btn primary block" id="go" style="max-width:320px;">${t('gotIt')}</button>
      </div>`;
    root.querySelector('#go').onclick = () => navigate('home');
  }

  draw();
}
