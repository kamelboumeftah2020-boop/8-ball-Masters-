import { api } from '../api.js';
import { state, setUser } from '../state.js';
import { t } from '../i18n.js';
import { toast, formatCoins } from '../ui.js';
import { topbarHtml, wireTopbar, bottomNavHtml, wireBottomNav } from './chrome.js';

const CAT_ICON = { wood: '🪵', flag: '🏁', dragon: '🐉', galaxy: '🌌', laser: '⚡', gold: '🥇', special: '✨' };

function avatarRarity(price) {
  if (price >= 80000) return 'legendary';
  if (price >= 25000) return 'epic';
  if (price >= 8000) return 'rare';
  return 'common';
}

export async function render(root, params = {}) {
  let tab = params.tab || 'coins';
  let packages = [];
  try { packages = (await api.get('/coin-packages')).packages; } catch {}

  draw();

  function draw() {
    root.innerHTML = `
      <div class="screen">
        ${topbarHtml()}
        <div class="shop-tabs">
          <button class="tab-btn ${tab === 'coins' ? 'active' : ''}" data-tab="coins">🪙 ${t('buyCoins')}</button>
          <button class="tab-btn ${tab === 'cues' ? 'active' : ''}" data-tab="cues">🎱 ${t('cues')}</button>
          <button class="tab-btn ${tab === 'avatars' ? 'active' : ''}" data-tab="avatars">🙂 ${t('avatars')}</button>
        </div>
        <div class="scroll">
          ${tab === 'coins' ? coinsHtml() : tab === 'cues' ? gridHtml(state.cues, 'cue') : gridHtml(state.avatars, 'avatar')}
          <div style="height:16px;"></div>
        </div>
        ${bottomNavHtml('shop')}
      </div>`;
    wireTopbar(root);
    wireBottomNav(root);
    root.querySelectorAll('[data-tab]').forEach(b => b.addEventListener('click', () => { tab = b.dataset.tab; draw(); }));
    wireActions();
  }

  function coinsHtml() {
    return packages.map(p => `
      <div class="card pkg-card">
        <div>
          <div class="usd">$${p.usd}</div>
          <div class="coins gold-text">🪙 ${p.coins.toLocaleString()}
            ${!state.user.firstPurchaseDone ? `<span class="pkg-badge">x2 FIRST</span>` : ''}
          </div>
        </div>
        <button class="btn gold" data-buy-coin="${p.id}">${t('buy')}</button>
      </div>
    `).join('');
  }

  function gridHtml(items, kind) {
    const u = state.user;
    return `<div class="shop-grid">${items.map(item => {
      const owned = kind === 'cue' ? u.ownedCues.includes(item.id) : u.ownedAvatars.includes(item.id);
      const equipped = kind === 'cue' ? u.equippedCueId === item.id : u.avatarId === item.id;
      const lvlOk = u.level >= item.unlockLvl;
      const glyph = kind === 'cue' ? (CAT_ICON[item.category] || '🎱') : item.emoji;
      let btnHtml;
      if (equipped) btnHtml = `<button class="btn sm ghost price-btn" disabled>${t('equipped')}</button>`;
      else if (owned) btnHtml = `<button class="btn sm gold price-btn" data-equip="${kind}:${item.id}">${t('equip')}</button>`;
      else if (!lvlOk) btnHtml = `<button class="btn sm ghost price-btn" disabled>LVL ${item.unlockLvl}</button>`;
      else btnHtml = `<button class="btn sm gold price-btn" data-buy="${kind}:${item.id}">🪙 ${item.price === 0 ? t('claim') : formatCoins(item.price)}</button>`;
      const rarity = kind === 'cue' ? item.rarity : avatarRarity(item.price);
      return `
        <div class="item-card rarity-${rarity}" style="${item.gradient ? `background-image:linear-gradient(135deg, ${item.gradient[0]}30, ${item.gradient[1]}30), linear-gradient(160deg, var(--card-2), var(--card));` : ''}">
          <div class="glyph">${glyph}</div>
          <div class="nm">${item.name || ''}</div>
          ${kind === 'cue' ? `<div class="stats">+${item.bonuses.startBonus}s · 🎯+${item.bonuses.aimBonus}% · 💪+${item.bonuses.powerBonus}%</div>` : ''}
          ${btnHtml}
        </div>`;
    }).join('')}</div>`;
  }

  function wireActions() {
    root.querySelectorAll('[data-buy-coin]').forEach(btn => {
      btn.addEventListener('click', async () => {
        try {
          const { user, coinsGranted, firstPurchaseBonus } = await api.post('/shop/buy-coins', { userId: state.user.id, packageId: btn.dataset.buyCoin });
          setUser(user);
          toast(`+${coinsGranted.toLocaleString()} 🪙${firstPurchaseBonus ? ' (x2!)' : ''}`);
          draw();
        } catch { toast('Purchase failed (demo checkout)'); }
      });
    });
    root.querySelectorAll('[data-buy]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const [kind, id] = btn.dataset.buy.split(':');
        try {
          const path = kind === 'cue' ? '/shop/buy-cue' : '/shop/buy-avatar';
          const field = kind === 'cue' ? 'cueId' : 'avatarId';
          const { user } = await api.post(path, { userId: state.user.id, [field]: Number(id) });
          setUser(user);
          toast('Purchased!');
          draw();
        } catch (e) {
          toast(e.data?.error === 'insufficient_coins' ? t('insufficientCoins') : 'Could not buy');
        }
      });
    });
    root.querySelectorAll('[data-equip]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const [kind, id] = btn.dataset.equip.split(':');
        const path = kind === 'cue' ? '/shop/equip-cue' : '/shop/equip-avatar';
        const field = kind === 'cue' ? 'cueId' : 'avatarId';
        const { user } = await api.post(path, { userId: state.user.id, [field]: Number(id) });
        setUser(user);
        draw();
      });
    });
  }
}
