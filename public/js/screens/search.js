import { state } from '../state.js';
import { t } from '../i18n.js';
import { navigate, onLeave } from '../router.js';
import { on, emit } from '../net/socket.js';
import { toast, formatCoins } from '../ui.js';

export function render(root, { tableId }) {
  const table = state.tables.find(x => x.id === tableId);
  root.innerHTML = `
    <div class="search-screen">
      <div class="radar">
        <div class="ring"></div><div class="ring"></div><div class="ring"></div>
        <div class="avatar lg search-orb" style="--orb:${table.colors.accent}; background:radial-gradient(circle at 34% 26%, color-mix(in srgb, var(--orb) 72%, white), var(--orb) 58%, #1a0d3a 100%)">🎱</div>
      </div>
      <div class="center">
        <h2>${t('searchingOpponent')}</h2>
        <p class="muted">${table.name} · 🪙 ${formatCoins(table.entry)}</p>
      </div>
      <button class="btn ghost" id="cancel">${t('cancel')}</button>
    </div>`;

  emit('join_queue', { tableId });

  const offErr = on('queue_error', (payload) => {
    toast(payload.error === 'insufficient_coins' ? t('insufficientCoins') : 'Table locked');
    navigate('home');
  });

  root.querySelector('#cancel').onclick = () => {
    emit('leave_queue', { tableId });
    navigate('home');
  };

  onLeave(() => { offErr(); });
}
