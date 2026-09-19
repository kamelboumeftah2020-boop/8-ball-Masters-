import { api } from './api.js';

const RTL_LANGS = new Set(['ar']);
let dict = {};
let currentLang = 'en';

export async function loadLang(lang) {
  currentLang = lang;
  const res = await fetch(`/api/i18n/${lang}`);
  dict = await res.json();
  document.documentElement.lang = lang;
  document.documentElement.dir = RTL_LANGS.has(lang) ? 'rtl' : 'ltr';
  try { localStorage.setItem('bm_lang', lang); } catch {}
}

export function t(key, vars) {
  let str = dict[key] ?? key;
  if (vars) {
    for (const [k, v] of Object.entries(vars)) str = str.replaceAll(`{${k}}`, v);
  }
  return str;
}

export function currentLanguage() { return currentLang; }

export function preferredLang() {
  try { return localStorage.getItem('bm_lang') || 'en'; } catch { return 'en'; }
}
