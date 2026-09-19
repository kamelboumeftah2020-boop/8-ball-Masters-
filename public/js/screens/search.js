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
        <div class="avatar lg" style="background:linear-gradient(135deg, ${table.colors.rail}, ${table.colors.felt})">🎱</div>
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
