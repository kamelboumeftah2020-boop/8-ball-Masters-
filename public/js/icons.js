// مجموعة أيقونات SVG موحّدة (بدل الإيموجي) — خطوط بسماكة ثابتة وتأخذ لون النص
const P = {
  play: '<path d="M7 4.5v15l12.5-7.5z" fill="currentColor" stroke="none"/>',
  trophy: '<path d="M7 4h10v5a5 5 0 0 1-10 0z"/><path d="M7 6H4v1.5A3.5 3.5 0 0 0 7.5 11M17 6h3v1.5a3.5 3.5 0 0 1-3.5 3.5"/><path d="M12 14v3.5M8.5 20.5h7M9.5 17.5h5v3h-5z"/>',
  globe: '<circle cx="12" cy="12" r="8.5"/><path d="M3.5 12h17M12 3.5c2.6 2.6 3.6 5.4 3.6 8.5s-1 5.9-3.6 8.5M12 3.5C9.4 6.1 8.4 8.9 8.4 12s1 5.9 3.6 8.5"/>',
  shirt: '<path d="M8.5 3.5 4 6l1.8 4.2 2.2-.9V20.5h8V9.3l2.2.9L20 6l-4.5-2.5a3.5 3.5 0 0 1-7 0z"/>',
  book: '<path d="M4 5.5A2 2 0 0 1 6 3.5h13v15H6a2 2 0 0 0-2 2z"/><path d="M4 20.5a2 2 0 0 1 2-2M8.5 8h6M8.5 11.5h4"/>',
  gear: '<circle cx="12" cy="12" r="3.2"/><path d="M12 2.8v2.4M12 18.8v2.4M21.2 12h-2.4M5.2 12H2.8M18.5 5.5l-1.7 1.7M7.2 16.8l-1.7 1.7M18.5 18.5l-1.7-1.7M7.2 7.2 5.5 5.5"/><circle cx="12" cy="12" r="6.6"/>',
  coin: '<circle cx="12" cy="12" r="8.5" fill="currentColor" fill-opacity=".18"/><path d="M12 7.5v9M14.6 9.3c-.5-.9-1.5-1.3-2.6-1.3-1.5 0-2.6.8-2.6 1.9 0 2.6 5.4 1.4 5.4 4.1 0 1.1-1.2 2-2.8 2-1.2 0-2.3-.5-2.8-1.4"/>',
  star: '<path d="m12 3.5 2.6 5.4 5.9.8-4.3 4.1 1 5.8L12 16.8l-5.2 2.8 1-5.8-4.3-4.1 5.9-.8z" fill="currentColor" stroke="none"/>',
  back: '<path d="M9 5.5 15.5 12 9 18.5" />',
  next: '<path d="M15 5.5 8.5 12 15 18.5" />',
  check: '<path d="m5 12.5 4.5 4.5L19 7.5"/>',
  home: '<path d="M4 11 12 4l8 7M6.5 9.5v10h11v-10"/><path d="M10 19.5v-5h4v5"/>',
  pause: '<path d="M8.5 5v14M15.5 5v14"/>',
  camera: '<rect x="3.5" y="7" width="12" height="10" rx="2"/><path d="m15.5 10.5 5-3v9l-5-3z"/>',
  sound: '<path d="M4 9.5h3.5L12 5.5v13l-4.5-4H4z"/><path d="M15.5 9a4.2 4.2 0 0 1 0 6M18 6.5a7.6 7.6 0 0 1 0 11"/>',
  mute: '<path d="M4 9.5h3.5L12 5.5v13l-4.5-4H4z"/><path d="m16 9.5 5 5M21 9.5l-5 5"/>',
  full: '<path d="M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5"/>',
  menu: '<path d="M4.5 7h15M4.5 12h15M4.5 17h15"/>',
  exit: '<path d="M14 4.5H6.5v15H14"/><path d="M10.5 12h10M17 8.5l3.5 3.5-3.5 3.5"/>',
  stop: '<rect x="6" y="6" width="12" height="12" rx="2"/>',
  refresh: '<path d="M19.5 12a7.5 7.5 0 1 1-2.2-5.3"/><path d="M19.5 4.5v4h-4"/>',
  link: '<path d="M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7L11.5 6.8"/><path d="M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1.5-1.5"/>',
  eye: '<path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z"/><circle cx="12" cy="12" r="3"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  user: '<circle cx="12" cy="8.5" r="3.8"/><path d="M4.5 20.5a7.5 7.5 0 0 1 15 0"/>',
  stadium: '<ellipse cx="12" cy="12.5" rx="9" ry="5"/><path d="M3 12.5v3c0 2.8 4 5 9 5s9-2.2 9-5v-3"/><path d="M12 7.5v10"/>',
  clock: '<circle cx="12" cy="12" r="8.5"/><path d="M12 7.5V12l3 2"/>',
  gauge: '<path d="M4 16a8 8 0 1 1 16 0"/><path d="m12 16 4-5"/>',
  ball: '<circle cx="12" cy="12" r="8.5"/><path d="m12 8 3.3 2.4-1.3 3.9h-4L8.7 10.4zM12 3.5V8M15.3 10.4l4.2-1.6M14 14.3l2.6 3.7M10 14.3 7.4 18M8.7 10.4 4.5 8.8"/>',
  shoot: '<circle cx="15" cy="12" r="5.5"/><path d="m15 9.6 2.2 1.6-.8 2.6h-2.8l-.8-2.6zM2.5 8.5h5M1.5 12h5.5M2.5 15.5h5"/>',
  pass: '<path d="M3.5 12h14M13 6.5l5.5 5.5-5.5 5.5"/>',
  lob: '<path d="M3.5 18c3-9 11-11.5 16-5.5"/><path d="m20.5 8.5-.8 4.5-4.4-1"/>',
  press: '<path d="M12 3.5 5 6.5v5c0 4.3 3 7.6 7 9 4-1.4 7-4.7 7-9v-5z"/><path d="m9 12 2.2 2.2L15.5 10"/>',
  switch: '<path d="M4.5 9.5h13l-3.5-3.5M19.5 14.5h-13l3.5 3.5"/>',
  sprint: '<path d="M13.5 3 6 13.5h5.5L10.5 21 18 10.5h-5.5z"/>',
  glove: '<path d="M8 20.5v-4.7L5.2 12a1.6 1.6 0 0 1 2.5-2l1.8 2V5.5a1.5 1.5 0 0 1 3 0v4.5V4.5a1.5 1.5 0 0 1 3 0V10V6a1.5 1.5 0 0 1 3 0v8.5c0 3.4-2.3 6-5.5 6z"/>',
  spark: '<path d="M12 3v5M12 16v5M3 12h5M16 12h5M6 6l3 3M15 15l3 3M18 6l-3 3M9 15l-3 3"/>',
  medal: '<circle cx="12" cy="14.5" r="5.5"/><path d="m8.5 10-3-6.5h4L12 9l2.5-5.5h4l-3 6.5"/><path d="m12 12 .9 1.8 2 .3-1.4 1.4.3 2-1.8-.9-1.8.9.3-2-1.4-1.4 2-.3z" fill="currentColor" stroke="none"/>',
  chat: '<path d="M4.5 5.5h15v10H10l-4.5 4v-4h-1z"/>',
  bolt: '<path d="M13.5 3 6 13.5h5.5L10.5 21 18 10.5h-5.5z" fill="currentColor" stroke="none"/>',
  lock: '<rect x="5.5" y="10.5" width="13" height="10" rx="2"/><path d="M8.5 10.5V8a3.5 3.5 0 0 1 7 0v2.5"/>',
};

export function icon(name, cls = '') {
  const p = P[name] || P.ball;
  return `<svg class="ic ${cls}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${p}</svg>`;
}

// يملأ كل عنصر يحمل data-ic بأيقونته (يُستدعى مرة عند الإقلاع)
export function hydrateIcons(root = document) {
  root.querySelectorAll('[data-ic]').forEach((el) => {
    if (el.dataset.icDone) return;
    el.dataset.icDone = '1';
    el.insertAdjacentHTML('afterbegin', icon(el.dataset.ic));
  });
}

// شعار اللعبة (درع + كرة)
export function logoSVG(size = 56) {
  return `<svg class="logo-svg" width="${size}" height="${size}" viewBox="0 0 64 64" aria-hidden="true">
    <defs>
      <linearGradient id="lg-a" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#d9ff4a"/><stop offset="1" stop-color="#1fd8ff"/></linearGradient>
      <linearGradient id="lg-b" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#16203a"/><stop offset="1" stop-color="#070b17"/></linearGradient>
    </defs>
    <path d="M32 3 56 11v19c0 15-10 25-24 31C18 55 8 45 8 30V11z" fill="url(#lg-a)"/>
    <path d="M32 8.5 51 15v15c0 12-8 20.5-19 25.5C21 50.5 13 42 13 30V15z" fill="url(#lg-b)"/>
    <circle cx="32" cy="30" r="12.5" fill="#fff"/>
    <path d="m32 23.5 6.2 4.5-2.4 7.3h-7.6L25.8 28z" fill="#0b1220"/>
    <path d="M32 17.5v6M38.2 28l5.7-2M35.8 35.3l3.6 4.9M28.2 35.3l-3.6 4.9M25.8 28l-5.7-2" stroke="#0b1220" stroke-width="2" stroke-linecap="round"/>
    <text x="32" y="56" text-anchor="middle" font-family="Changa, sans-serif" font-weight="800" font-size="8" fill="#d9ff4a">5V5</text>
  </svg>`;
}
