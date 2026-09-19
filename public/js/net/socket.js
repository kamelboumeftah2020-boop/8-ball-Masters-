import { state } from '../state.js';
import { getToken } from '../api.js';

export const bus = new EventTarget();

let socket = null;

export function connectSocket() {
  if (socket) return socket;
  socket = window.io();
  state.socket = socket;

  socket.on('connect', () => {
    if (getToken()) socket.emit('identify', { token: getToken() });
  });

  const forward = (event) => socket.on(event, (payload) => {
    bus.dispatchEvent(new CustomEvent(event, { detail: payload }));
  });

  [
    'queue_joined', 'queue_error', 'match_start', 'match_tick', 'match_end',
    'rescue_result', 'double_result', 'opponent_emoji', 'challenge_error',
    'table_sync', 'auth_error', 'match_resume',
  ].forEach(forward);

  return socket;
}

export function identify() {
  if (socket && getToken()) socket.emit('identify', { token: getToken() });
}

export function on(event, handler) {
  const wrapped = (e) => handler(e.detail);
  bus.addEventListener(event, wrapped);
  return () => bus.removeEventListener(event, wrapped);
}

export function emit(event, payload) {
  socket?.emit(event, payload);
}
