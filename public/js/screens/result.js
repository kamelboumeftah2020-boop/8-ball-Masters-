import { api } from '../api.js';
import { state, setUser } from '../state.js';
import { t } from '../i18n.js';
import { navigate, onLeave } from '../router.js';
import { on, emit } from '../net/socket.js';
import { toast, formatCoins } from '../ui.js';
import * as audio from '../engine/audio.js';

export async function render(root, { payload }) {
  const r = payload.you;
  let bonusApplied = false;

  try {
    const { user } = await api.get(`/me/${state.user.id}`);
    setUser(user);
  } catch {}

  if (r.won) audio.win(); else audio.lose();
  if (r.leveledUp) setTimeout(() => audio.levelUp(), 700);

  draw();

  function draw() {
    root.innerHTML = `
      <div class="result-screen ${r.won ? 'win-bg' : ''}">
        <div class="result-title ${r.won ? 'win' : 'lose'}">${r.won ? '🏆 ' + t('youWin') : t('youLose')}</div>
        ${!r.won && r.nearMiss ? `
          <div class="card" style="max-width:320px;">
            <div class="gold-text" style="font-weight:800;">${t('nearMissTitle')}</div>
            <p style="font-size:16px;margin:8px 0 0;">${t('nearMissDesc')}</p>
          </div>` : ''}
        <div class="result-stats">
          <div class="stat">
            <div class="val" style="color:${r.coinsDelta >= 0 ? 'var(--gold)' : 'var(--red)'}">
              ${r.coinsDelta >= 0 ? '+' : ''}${formatCoins(r.coinsDelta)}
            </div>
            <div class="muted" style="font-size:14px;margin-top:4px;">🪙 ${r.coinsDelta >= 0 ? t('coinsWon') : t('coinsLost')}</div>
          </div>
          <div class="stat">
            <div class="val">+${r.xpGained}</div>
            <div class="muted" style="font-size:14px;margin-top:4px;">${t('xpGained')}</div>
          </div>
        </div>
        ${r.won && r.streak > 1 ? `<div class="muted" style="font-size:15px;font-weight:700;">🔥 ${t('streakLabel')}: ${r.streak} ${r.streak % 4 === 0 ? '· ' + t('streakBonus') : ''}</div>` : ''}
        ${r.leveledUp ? `<div class="gold-text" style="font-weight:800;">⭐ ${t('newLevel')} ${r.newLevel}</div>` : ''}
        <div class="result-actions">
          ${r.won && !bonusApplied ? `<button class="btn gold block" id="double-ad">📺 ${t('watchAdDouble')}</button>` : ''}
          ${!r.won && r.canRevenge ? `<button class="btn danger block" id="revenge">🔥 ${t('revengeTitle')} — ${t('revengeDesc')}</button>` : ''}
          ${!r.won ? lossBoxHtml() : ''}
          <button class="btn ghost block" id="share">📤 ${t('shareClip')}</button>
          <button class="btn primary block" id="home">${t('back')}</button>
        </div>
      </div>`;

    root.querySelector('#home').onclick = () => navigate('home');
    root.querySelector('#share').onclick = shareClip;
    root.querySelector('#double-ad')?.addEventListener('click', watchDoubleAd);
    root.querySelector('#revenge')?.addEventListener('click', requestRevenge);
    root.querySelector('#claim-lossbox')?.addEventListener('click', claimLossBox);
  }

  function lossBoxHtml() {
    const box = state.user.lossBoxes?.filter(b => !b.opened).slice(-1)[0];
    if (!box) return '';
    const ready = Date.now() >= box.availableAt;
    return `
      <div class="card">
        <div class="gold-text" style="font-weight:800;">📦 ${t('lossBoxTitle')}</div>
        <p style="font-size:15px;margin:8px 0 12px;" class="muted">${t('lossBoxDesc')}</p>
        <button class="btn sm ${ready ? 'gold' : 'ghost'} block" id="claim-lossbox" ${ready ? '' : 'disabled'} data-box="${box.id}">
          ${ready ? t('claim') : t('opensIn') + ' ' + minsLeft(box.availableAt)}
        </button>
      </div>`;
  }

  function minsLeft(ts) {
    const m = Math.max(0, Math.round((ts - Date.now()) / 60000));
    return m >= 60 ? `${Math.floor(m / 60)}h ${m % 60}m` : `${m}m`;
  }

  async function claimLossBox() {
    const box = state.user.lossBoxes.filter(b => !b.opened).slice(-1)[0];
    try {
      const { user, reward } = await api.post('/lossboxes/claim', { boxId: box.id });
      setUser(user);
      audio.coin();
      toast(`+${reward} 🪙`);
      draw();
    } catch { toast('Not ready yet'); }
  }

  function watchDoubleAd() {
    playFakeAd(() => emit('watch_ad_double', { matchId: payload.matchId }));
  }

  const offDouble = on('double_result', async ({ bonus }) => {
    if (bonus) {
      bonusApplied = true;
      toast(`+${bonus} 🪙 (2x!)`);
      const { user } = await api.get(`/me/${state.user.id}`);
      setUser(user);
    }
    draw();
  });

  function requestRevenge() {
    emit('revenge', { matchId: payload.matchId });
    toast('Rematch requested...');
  }
  const offErr = on('queue_error', (e) => {
    const map = { insufficient_coins: t('insufficientCoins'), opponent_offline: 'Opponent is offline', opponent_insufficient_coins: 'Opponent can\'t afford the rematch' };
    toast(map[e.error] || 'Could not start rematch');
  });

  onLeave(() => { offDouble(); offErr(); });

  function shareClip() {
    if (state.lastClip && state.lastClip.size > 0) {
      const url = URL.createObjectURL(state.lastClip);
      const a = document.createElement('a');
      a.href = url;
      a.download = '8ballmasters-clip.webm';
      a.click();
      setTimeout(() => URL.revokeObjectURL(url), 10000);
      toast('Clip saved — share it on TikTok / WhatsApp!');
      return;
    }
    shareResultCard();
  }

  // Fallback for browsers without MediaRecorder: a still result card.
  function shareResultCard() {
    const canvas = document.createElement('canvas');
    canvas.width = 600; canvas.height = 800;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#0b0b0f'; ctx.fillRect(0, 0, 600, 800);
    ctx.fillStyle = '#ffd447'; ctx.font = 'bold 40px sans-serif'; ctx.textAlign = 'center';
    ctx.fillText('8 Ball Masters', 300, 100);
    ctx.fillStyle = r.won ? '#ffd447' : '#9a97a8';
    ctx.font = 'bold 60px sans-serif';
    ctx.fillText(r.won ? t('youWin') : t('youLose'), 300, 220);
    ctx.fillStyle = '#f2f0ea'; ctx.font = '28px sans-serif';
    ctx.fillText(`${state.user.nickname}`, 300, 300);
    ctx.fillText(`${r.coinsDelta >= 0 ? '+' : ''}${r.coinsDelta} coins`, 300, 350);
    ctx.fillText(`${payload.table?.name || ''}`, 300, 400);
    const url = canvas.toDataURL('image/png');
    const a = document.createElement('a');
    a.href = url; a.download = '8ballmasters-result.png';
    a.click();
    toast('Clip saved — share it on TikTok / WhatsApp!');
  }
}

function playFakeAd(onDone) {
  const root = document.getElementById('modal-root');
  root.innerHTML = `<div class="modal-backdrop"><div class="modal-box"><h3>📺 Ad</h3>
    <div class="progress-bar"><div class="fill" id="ad-fill2" style="width:0%"></div></div></div></div>`;
  const fill = document.getElementById('ad-fill2');
  let p = 0;
  const iv = setInterval(() => {
    p += 20;
    fill.style.width = `${Math.min(100, p)}%`;
    if (p >= 100) { clearInterval(iv); root.innerHTML = ''; onDone(); }
  }, 300);
}
