import { api } from '../api.js';
import { state, setUser } from '../state.js';
import { t, loadLang } from '../i18n.js';
import { navigate } from '../router.js';
import { toast, openModal, closeModal } from '../ui.js';

const LANGS = [
  { code: 'en', label: 'English' }, { code: 'ar', label: 'العربية' }, { code: 'fr', label: 'Français' },
  { code: 'de', label: 'Deutsch' }, { code: 'es', label: 'Español' }, { code: 'tr', label: 'Türkçe' },
];

export function render(root) {
  draw();

  function draw() {
    const s = state.user.settings;
    root.innerHTML = `
      <div class="screen">
        <div class="topbar">
          <button class="icon-btn" id="back">←</button>
          <h3>${t('settings')}</h3>
          <div style="width:36px;"></div>
        </div>
        <div class="scroll">
          <div class="setting-row">
            <span>🌐 ${t('language')}</span>
            <select id="lang-select">
              ${LANGS.map(l => `<option value="${l.code}" ${l.code === s.lang ? 'selected' : ''}>${l.label}</option>`).join('')}
            </select>
          </div>
          <div class="setting-row">
            <span>📳 ${t('vibration')}</span>
            <button class="switch ${s.vibration ? 'on' : ''}" id="vib-toggle"><span class="knob"></span></button>
          </div>
          <div class="setting-row">
            <span>🔊 ${t('sound')}</span>
            <button class="switch ${s.sound ? 'on' : ''}" id="sound-toggle"><span class="knob"></span></button>
          </div>
          <div class="setting-row" style="flex-direction:column;align-items:stretch;gap:8px;">
            <span>🎯 ${t('aimSensitivity')}</span>
            <input type="range" min="0" max="100" value="${s.aimSensitivity}" id="aim-sens" />
          </div>
          <div class="setting-row" id="report-problem" role="button">
            <span>🐞 ${t('reportProblem')}</span><span>›</span>
          </div>
          <div class="setting-row" id="privacy" role="button">
            <span>🔒 ${t('privacyPolicy')}</span><span>›</span>
          </div>
          <div class="setting-row" id="tos" role="button">
            <span>📄 ${t('termsOfService')}</span><span>›</span>
          </div>
        </div>
      </div>`;

    root.querySelector('#back').onclick = () => navigate('profile');
    root.querySelector('#lang-select').onchange = async (e) => {
      const lang = e.target.value;
      await save({ lang });
      await loadLang(lang);
      navigate('settings');
    };
    root.querySelector('#vib-toggle').onclick = () => save({ vibration: !s.vibration }).then(() => draw());
    root.querySelector('#sound-toggle').onclick = () => save({ sound: !s.sound }).then(() => draw());
    root.querySelector('#aim-sens').onchange = (e) => save({ aimSensitivity: Number(e.target.value) });
    root.querySelector('#report-problem').onclick = reportProblem;
    root.querySelector('#privacy').onclick = () => showText(t('privacyPolicy'), PRIVACY_TEXT);
    root.querySelector('#tos').onclick = () => showText(t('termsOfService'), TOS_TEXT);
  }

  async function save(patch) {
    try {
      const { user } = await api.put('/settings', { userId: state.user.id, ...patch });
      setUser(user);
    } catch { toast('Could not save setting'); }
  }

  function reportProblem() {
    openModal(`
      <h3>🐞 ${t('reportProblem')}</h3>
      <textarea id="problem-text" rows="4" style="width:100%;background:var(--card);border:1px solid var(--line);border-radius:10px;color:var(--text);padding:10px;"></textarea>
      <button class="btn gold block" id="send-report">${t('confirm')}</button>
    `, {
      onMount: (m) => {
        m.querySelector('#send-report').onclick = () => { closeModal(); toast('Thanks — your report was sent'); };
      },
    });
  }

  function showText(title, text) {
    openModal(`<h3>${title}</h3><div class="muted" style="font-size:12px;max-height:280px;overflow:auto;text-align:start;white-space:pre-line;">${text}</div>
      <button class="btn ghost block" id="close-text">${t('back')}</button>`, {
      onMount: (m) => { m.querySelector('#close-text').onclick = closeModal; },
    });
  }
}

const PRIVACY_TEXT = `8 Ball Masters collects only what's needed to run your account: your nickname, avatar, gameplay stats and coin balance are stored on our servers. We never sell your data. Guest accounts can be deleted at any time by contacting support.`;
const TOS_TEXT = `By playing 8 Ball Masters you agree to play fair - exploiting bugs, using bots/macros or abusive behavior can result in a temporary or permanent ban. Virtual coins have no real-world cash value and purchases are final.`;
