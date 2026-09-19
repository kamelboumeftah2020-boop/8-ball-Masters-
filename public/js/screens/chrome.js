import { state } from '../state.js';
import { t } from '../i18n.js';
import { navigate } from '../router.js';
import { formatCoins, avatarHtml } from '../ui.js';

const STAR_EMOJI = ['⚪', '🥈', '🥉', '🟡', '⬜', '🔴', '🟢', '🔷', '🟣', '🟠', '⬛'];

export function topbarHtml() {
  const u = state.user;
  if (!u) return '';
  return `
    <div class="topbar">
      <button class="row gap avatar-btn" id="tb-profile">
        ${avatarHtml(u.avatarId, 'sm')}
        <div style="text-align:start;">
          <div style="font-size:17px;font-weight:800;">${u.nickname}</div>
          <div style="font-size:13px;margin-top:3px;" class="muted">${STAR_EMOJI[u.starId] || '⚪'} <span class="badge-lvl">LVL ${u.level}</span></div>
        </div>
      </button>
      <div class="row gap">
        <div class="pill coins">🪙 ${formatCoins(u.coins)}</div>
        <button class="icon-btn" id="tb-add-coins">+</button>
        <button class="icon-btn" id="tb-settings">⚙️</button>
      </div>
    </div>`;
}

export function wireTopbar(root) {
  root.querySelector('#tb-profile')?.addEventListener('click', () => navigate('profile'));
  root.querySelector('#tb-add-coins')?.addEventListener('click', () => navigate('shop', { tab: 'coins' }));
  root.querySelector('#tb-settings')?.addEventListener('click', () => navigate('settings'));
}

const NAV_ITEMS = [
  { key: 'home', icon: '🎱', labelKey: 'tables' },
  { key: 'shop', icon: '🛒', labelKey: 'shop' },
  { key: 'leaderboard', icon: '🏆', labelKey: 'leaderboard' },
  { key: 'friends', icon: '👥', labelKey: 'friends' },
  { key: 'profile', icon: '🙍', labelKey: 'profile' },
];

export function bottomNavHtml(active) {
  return `<div class="bottomnav">
    ${NAV_ITEMS.map(item => `
      <button class="${item.key === active ? 'active' : ''}" data-nav="${item.key}">
        <span class="ic">${item.icon}</span>
        <span>${t(item.labelKey)}</span>
      </button>
    `).join('')}
  </div>`;
}

export function wireBottomNav(root) {
  root.querySelectorAll('[data-nav]').forEach(btn => {
    btn.addEventListener('click', () => navigate(btn.dataset.nav));
  });
}
