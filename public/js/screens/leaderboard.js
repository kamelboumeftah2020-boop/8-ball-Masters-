import { api } from '../api.js';
import { state } from '../state.js';
import { t } from '../i18n.js';
import { formatCoins, avatarHtml } from '../ui.js';
import { topbarHtml, wireTopbar, bottomNavHtml, wireBottomNav } from './chrome.js';

const STAR_KEY_LABEL = {
  silver: 'starSilver', bronze: 'starBronze', gold: 'starGold', platinum: 'starPlatinum',
  ruby: 'starRuby', emerald: 'starEmerald', diamond: 'starDiamond', legendary: 'starLegendary',
  eternal: 'starEternal', galaxyGodfather: 'starGalaxyGodfather',
};

export async function render(root) {
  let tab = 'global';
  let starId = state.user.starId;
  let stars = [];
  await draw();

  async function draw() {
    root.innerHTML = `
      <div class="screen">
        ${topbarHtml()}
        <div class="lb-tabs">
          <button class="tab-btn ${tab === 'global' ? 'active' : ''}" data-tab="global">${t('global')}</button>
          <button class="tab-btn ${tab === 'local' ? 'active' : ''}" data-tab="local">${t('local')}</button>
          <button class="tab-btn ${tab === 'friends' ? 'active' : ''}" data-tab="friends">${t('friendsTab')}</button>
        </div>
        <div class="scroll" id="lb-content">${loadingHtml()}</div>
        ${bottomNavHtml('leaderboard')}
      </div>`;
    wireTopbar(root);
    wireBottomNav(root);
    root.querySelectorAll('[data-tab]').forEach(b => b.addEventListener('click', () => { tab = b.dataset.tab; draw(); }));
    await loadContent();
  }

  function loadingHtml() { return `<div class="center muted" style="padding:30px;">…</div>`; }

  async function loadContent() {
    const content = document.getElementById('lb-content');
    if (!content) return;
    if (tab === 'global') {
      const data = await api.get(`/leaderboard/global?starId=${starId}`);
      stars = data.stars;
      content.innerHTML = globalHtml(data);
      wireStarChips(content);
    } else if (tab === 'local') {
      const data = await api.get('/leaderboard/local');
      content.innerHTML = rowsHtml(data.rows);
    } else {
      const data = await api.get('/leaderboard/friends');
      content.innerHTML = rowsHtml(data.rows, true);
    }
  }

  function globalHtml(data) {
    const star = stars.find(s => s.id === starId);
    const msLeft = data.resetsAt - Date.now();
    const days = Math.max(0, Math.floor(msLeft / 86400000));
    const hours = Math.max(0, Math.floor((msLeft % 86400000) / 3600000));
    return `
      <div class="row gap" style="overflow-x:auto; padding:10px 14px;">
        ${stars.map(s => `
          <button class="star-badge" data-star="${s.id}" style="background:${s.color}${s.id === starId ? 'ff' : '33'};color:${s.id === starId ? '#111' : 'var(--text)'};white-space:nowrap;">
            ${t(STAR_KEY_LABEL[s.key])}
          </button>`).join('')}
      </div>
      <div class="card" style="margin:0 14px 10px;">
        <div class="row between">
          <span class="muted" style="font-size:15px;font-weight:700;">${t('resetsIn')} ${days}d ${hours}h</span>
        </div>
        <div class="row gap" style="margin-top:8px;">
          <div class="stat-box" style="flex:1;"><div class="v gold-text">🥇 ${formatCoins(data.prizes.top1)}</div><div class="l">${t('prize')} #1</div></div>
          <div class="stat-box" style="flex:1;"><div class="v">🥈 ${formatCoins(data.prizes.top2)}</div><div class="l">${t('prize')} #2</div></div>
          <div class="stat-box" style="flex:1;"><div class="v">🥉 ${formatCoins(data.prizes.top3)}</div><div class="l">${t('prize')} #3</div></div>
        </div>
      </div>
      ${rowsHtml(data.rows.map(r => ({ ...r, value: r.weeklyCoins })))}
    `;
  }

  function wireStarChips(content) {
    content.querySelectorAll('[data-star]').forEach(btn => {
      btn.addEventListener('click', () => { starId = Number(btn.dataset.star); loadContent(); });
    });
  }

  function rowsHtml(rows, isFriends) {
    if (!rows.length) return `<div class="center muted" style="padding:30px;">—</div>`;
    return rows.map(r => `
      <div class="lb-row ${r.rank <= 3 ? 'top' + r.rank : ''} ${r.isMe || r.userId === state.user.id ? 'gold-text' : ''}">
        <div class="rank">${r.rank}</div>
        ${avatarHtml(r.avatarId, 'sm')}
        <div class="nm">${r.nickname}</div>
        <div class="val">🪙 ${formatCoins(r.value ?? r.coins ?? r.weeklyCoins ?? 0)}</div>
      </div>
    `).join('');
  }
}
