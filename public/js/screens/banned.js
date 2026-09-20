import { t } from '../i18n.js';

export function render(root, { reason } = {}) {
  root.innerHTML = `
    <div class="onboarding">
      <div style="font-size:60px;">⛔</div>
      <h2 style="color:var(--red);">${t('banned')}</h2>
      <p class="muted">${reason || t('bannedDesc')}</p>
    </div>`;
}
