const BASE = '/api';

// The session token is the credential for every account action. The player's
// 8-digit ID is public (it is printed on their profile), so it is never enough.
let token = null;
try { token = localStorage.getItem('bm_token'); } catch {}

export function setToken(value) {
  token = value || null;
  try {
    if (token) localStorage.setItem('bm_token', token);
    else localStorage.removeItem('bm_token');
  } catch {}
}

export function getToken() { return token; }

async function request(method, url, body) {
  const headers = {};
  if (body) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(BASE + url, {
    method,
    headers: Object.keys(headers).length ? headers : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  let data = null;
  try { data = await res.json(); } catch { /* no body */ }
  if (!res.ok) {
    const err = new Error(data?.error || `HTTP ${res.status}`);
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

export const api = {
  get: (url) => request('GET', url),
  post: (url, body) => request('POST', url, body),
  put: (url, body) => request('PUT', url, body),
};
