import { api } from '../api.js';
import { state, setUser } from '../state.js';
import { t } from '../i18n.js';
import { navigate } from '../router.js';
import { toast, formatCoins, avatarHtml } from '../ui.js';
import { topbarHtml, wireTopbar, bottomNavHtml, wireBottomNav } from './chrome.js';

const TITLE_KEY = {
  beginner: 'titleBeginner', amateur: 'titleAmateur', pro: 'titlePro', elite: 'titleElite',
  champion: 'titleChampion', legend: 'titleLegend', tableKing: 'titleTableKing', godfather: 'titleGodfather',
};

export async function render(root, params = {}) {
  const viewingSelf = !params.userId || params.userId === state.user.id;
  let profile = viewingSelf ? state.user : null;
  if (!viewingSelf) {
    try { profile = (await api.get(`/profile/${params.userId}`)).user; }
    catch { toast('User not found'); navigate('home'); return; }
  }
  draw();

  function draw() {
    root.innerHTML = `
      <div class="screen">
        ${topbarHtml()}
        <div class="scroll">
          <div class="profile-header">
            ${avatarHtml(profile.avatarId, 'lg')}
            <h2>${profile.nickname}</h2>
            <div class="id-row">
              ID: ${profile.id}
              <button class="icon-btn" style="width:26px;height:26px;font-size:11px;" id="copy-id">📋</button>
            </div>
            <div class="row gap">
              <span class="badge-lvl">LVL ${profile.level}</span>
              <span class="star-badge" style="background:${profile.starColor || '#c7cdd6'}33;">⭐</span>
              <span class="gold-text" style="font-size:13px;font-weight:700;">${t(TITLE_KEY[profile.title] || 'titleBeginner')}</span>
            </div>
          </div>
          <div class="stat-grid">
            <div class="stat-box"><div class="v">${profile.totalPlayed}</div><div class="l">${t('played')}</div></div>
            <div class="stat-box"><div class="v" style="color:var(--green);">${profile.totalWins}</div><div class="l">${t('won')}</div></div>
            <div class="stat-box"><div class="v" style="color:var(--red);">${profile.totalLosses}</div><div class="l">${t('lost')}</div></div>
            <div class="stat-box"><div class="v">${profile.winRate}%</div><div class="l">${t('winRate')}</div></div>
          </div>
          <div class="card" style="margin:14px;">
            <div class="row between">
              <span class="muted">${t('careerCoins')}</span>
              <span class="gold-text" style="font-weight:800;">🪙 ${formatCoins(profile.careerCoinsWon)}</span>
            </div>
          </div>
          <div style="padding:0 14px; display:flex; flex-direction:column; gap:10px;">
            ${viewingSelf ? `
              <button class="btn ghost block" id="edit-nick">✏️ ${t('enterNickname')}</button>
              <button class="btn ghost block" id="invite-friend">🔗 ${t('inviteFriend')}</button>
              <button class="btn danger block" id="logout">${t('logout')}</button>
            ` : `
              <button class="btn gold block" id="add-friend">➕ ${t('addFriend')}</button>
              <button class="btn ghost block" id="report">🚩 ${t('report')}</button>
            `}
          </div>
          <div style="height:16px;"></div>
        </div>
        ${bottomNavHtml('profile')}
      </div>`;
    wireTopbar(root);
    wireBottomNav(root);
    root.querySelector('#copy-id').onclick = () => {
      navigator.clipboard?.writeText(profile.id).catch(() => {});
      toast(t('idCopied'));
    };
    if (viewingSelf) {
      root.querySelector('#edit-nick').onclick = editNickname;
      root.querySelector('#invite-friend').onclick = inviteFriend;
      root.querySelector('#logout').onclick = () => {
        try { localStorage.removeItem('bm_userId'); } catch {}
        location.reload();
      };
    } else {
      root.querySelector('#add-friend').onclick = addFriend;
      root.querySelector('#report').onclick = reportUser;
    }
  }

  async function editNickname() {
    const val = prompt(t('enterNickname'), profile.nickname);
    if (!val || val.trim().length < 3) return;
    try {
      const { user } = await api.put('/nickname', { userId: state.user.id, nickname: val.trim() });
      setUser(user);
      profile = user;
      draw();
    } catch (e) {
      toast(e.status === 429 ? 'Nickname can only change every 90 days' : 'Name taken or invalid');
    }
  }

  async function inviteFriend() {
    try {
      const { url } = await api.post('/invite/create', { userId: state.user.id });
      navigator.clipboard?.writeText(url).catch(() => {});
      toast(`${t('copyLink')}: ${url}`);
    } catch { toast('Could not create link'); }
  }

  async function addFriend() {
    try {
      await api.post('/friends/add', { userId: state.user.id, friendId: profile.id });
      toast('Friend added!');
    } catch { toast('Could not add friend'); }
  }

  async function reportUser() {
    try {
      await api.post('/report', { userId: state.user.id, targetId: profile.id, reason: 'reported from profile' });
      toast('Report submitted');
    } catch { toast('Could not report'); }
  }
}
