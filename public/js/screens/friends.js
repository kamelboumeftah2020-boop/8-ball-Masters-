import { api } from '../api.js';
import { state } from '../state.js';
import { t } from '../i18n.js';
import { navigate, onLeave } from '../router.js';
import { toast, avatarHtml } from '../ui.js';
import { on, emit } from '../net/socket.js';
import { topbarHtml, wireTopbar, bottomNavHtml, wireBottomNav } from './chrome.js';

export async function render(root) {
  await draw();

  const offErr = on('challenge_error', () => toast('Friend is not online right now'));
  const offStart = on('match_start', (payload) => navigate('game', { matchData: payload }));
  onLeave(() => { offErr(); offStart(); });

  async function draw() {
    let friends = [];
    try { friends = (await api.get(`/friends/${state.user.id}`)).friends; } catch {}

    root.innerHTML = `
      <div class="screen">
        ${topbarHtml()}
        <div style="padding:12px 14px;">
          <div class="row gap">
            <input class="text-input" id="search-id" placeholder="${t('searchFriendById')}" style="max-width:none;" />
            <button class="btn gold sm" id="search-btn">🔎</button>
          </div>
        </div>
        <div class="scroll">
          ${friends.length ? friends.map(friendRow).join('') : `<div class="center muted" style="padding:30px;">${t('friends')}: 0</div>`}
          <div style="height:16px;"></div>
        </div>
        ${bottomNavHtml('friends')}
      </div>`;
    wireTopbar(root);
    wireBottomNav(root);

    root.querySelector('#search-btn').onclick = doSearch;
    root.querySelectorAll('[data-view]').forEach(b => b.addEventListener('click', () => navigate('profile', { userId: b.dataset.view })));
    root.querySelectorAll('[data-challenge]').forEach(b => b.addEventListener('click', () => {
      emit('challenge_friend', { friendId: b.dataset.challenge, tableId: 1 });
      toast('Challenge sent...');
    }));
  }

  function friendRow(f) {
    return `
      <div class="friend-row">
        <span class="dotstat ${f.online ? 'on' : ''}"></span>
        ${avatarHtml(f.avatarId, 'sm')}
        <div style="flex:1;" data-view="${f.id}">
          <div style="font-weight:800;font-size:17px;">${f.nickname}</div>
          <div class="muted" style="font-size:14px;margin-top:2px;">LVL ${f.level} · ${f.online ? t('online') : ''}</div>
        </div>
        <button class="btn sm gold" data-challenge="${f.id}" ${f.online ? '' : 'disabled'}>${t('challenge')}</button>
      </div>`;
  }

  async function doSearch() {
    const id = root.querySelector('#search-id').value.trim();
    if (!id) return;
    try {
      const { user } = await api.get(`/users/search?id=${encodeURIComponent(id)}`);
      navigate('profile', { userId: user.id });
    } catch { toast('No player with that ID'); }
  }
}
