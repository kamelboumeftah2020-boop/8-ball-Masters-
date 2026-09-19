import { api } from '../api.js';
import { state, setUser } from '../state.js';
import { t } from '../i18n.js';
import { openModal, closeModal, toast } from '../ui.js';
import * as audio from '../engine/audio.js';

export async function openMissionsModal(onChange) {
  const { missions } = await api.get(`/missions/${state.user.id}`);
  render(missions.list);

  function render(list) {
    openModal(`
      <h3>${t('missions')}</h3>
      <div class="stack gap" style="text-align:start;">
        ${list.map(m => `
          <div>
            <div class="row between" style="font-size:16px;font-weight:700;margin-bottom:8px;">
              <span>${t(m.descKey)}</span>
              <span class="gold-text">${m.progress}/${m.target}</span>
            </div>
            <div class="progress-bar"><div class="fill" style="width:${Math.min(100, m.progress / m.target * 100)}%"></div></div>
            <div class="row between" style="margin-top:6px;">
              <span class="muted" style="font-size:15px;font-weight:700;">🪙 ${m.reward}</span>
              <button class="btn sm ${m.claimed ? 'ghost' : 'gold'}" data-claim="${m.key}"
                ${m.claimed || m.progress < m.target ? 'disabled' : ''}>
                ${m.claimed ? t('owned') : t('claim')}
              </button>
            </div>
          </div>
        `).join('<div style="height:2px;background:var(--line);"></div>')}
      </div>
      <button class="btn ghost block" id="close-missions">${t('back')}</button>
    `, {
      onMount: (root) => {
        root.querySelector('#close-missions').onclick = closeModal;
        root.querySelectorAll('[data-claim]').forEach(btn => {
          btn.onclick = async () => {
            try {
              const { user, reward } = await api.post('/missions/claim', { userId: state.user.id, missionKey: btn.dataset.claim });
              setUser(user);
              audio.coin();
              toast(`+${reward} 🪙`);
              const fresh = await api.get(`/missions/${state.user.id}`);
              render(fresh.missions.list);
              onChange?.();
            } catch (e) { toast('Could not claim'); }
          };
        });
      },
    });
  }
}
