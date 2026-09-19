import { api } from '../api.js';
import { state, setUser } from '../state.js';
import { t } from '../i18n.js';
import { navigate } from '../router.js';
import { toast, formatCoins } from '../ui.js';
import { topbarHtml, wireTopbar, bottomNavHtml, wireBottomNav } from './chrome.js';

const STYLE_ICONS = {
  'old-wood': '🪵', 'street-coffee': '☕', 'french-neon': '🗼', 'pure-gold': '🏅',
  'dark-techno': '🌃', 'japanese-neon': '🎌', casino: '🎰', royal: '👑',
  skyscrapers: '🏙️', 'flying-throne': '🐐',
};

export async function render(root) {
  draw();
  try {
    const { tables } = await api.get(`/tables?userId=${state.user.id}`);
    state.tables = tables;
    draw();
  } catch (e) { console.error(e); }

  function draw() {
    const u = state.user;
    root.innerHTML = `
      <div class="screen">
        ${topbarHtml()}
        <div class="scroll">
          <div class="section-title">${t('tables')}</div>
          <div class="row between" style="margin:0 14px 10px;">
            <button class="btn sm gold" id="daily-box">📦 ${t('dailyBox')}</button>
            <button class="btn sm ghost" id="missions-btn">🎯 ${t('missions')}</button>
          </div>
          ${state.tables.map(tableCard).join('')}
          <div style="height:16px;"></div>
        </div>
        ${bottomNavHtml('home')}
      </div>`;
    wireTopbar(root);
    wireBottomNav(root);
    root.querySelectorAll('[data-play]').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = Number(btn.dataset.play);
        const table = state.tables.find(x => x.id === id);
        if (table.locked) { toast(t('unlockAtLevel', { lvl: table.unlockLvl })); return; }
        if (u.coins < table.entry) { toast(t('insufficientCoins')); return; }
        navigate('search', { tableId: id });
      });
    });
    root.querySelector('#daily-box').onclick = claimDailyBox;
    root.querySelector('#missions-btn').onclick = () => openMissions();
  }

  function tableCard(table) {
    const g = table.colors;
    return `
      <div class="table-card ${table.locked ? 'locked' : ''}" style="background:linear-gradient(135deg, ${g.felt}, ${g.rail}); border-color:${g.accent}55;">
        <div class="thumb" style="background:${g.accent};">${STYLE_ICONS[table.style] || '🎱'}</div>
        <div class="meta">
          <div class="name">${table.name}</div>
          <div class="sub">${t('entryFee')}: 🪙 ${formatCoins(table.entry)} · XP ${formatCoins(table.xp)}</div>
          <div class="online">● ${table.onlineCount.toLocaleString()} ${t('online')}</div>
        </div>
        ${table.locked
          ? `<div class="lock-badge">🔒 ${t('unlockAtLevel', { lvl: table.unlockLvl })}</div>`
          : `<button class="btn sm gold" data-play="${table.id}">${t('playNow')}</button>`}
      </div>`;
  }

  async function claimDailyBox() {
    try {
      const { user, reward } = await api.post('/dailybox/claim', { userId: state.user.id });
      setUser(user);
      toast(`+${reward} 🪙`);
      draw();
    } catch (e) {
      if (e.status === 429) {
        const mins = Math.max(1, Math.round((e.data.availableAt - Date.now()) / 60000));
        toast(`${t('opensIn')} ${mins}m`);
      }
    }
  }

  function openMissions() {
    import('./missionsModal.js').then(m => m.openMissionsModal(draw));
  }
}
