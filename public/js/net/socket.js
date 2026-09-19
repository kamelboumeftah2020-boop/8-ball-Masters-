import { state } from '../state.js';

export const bus = new EventTarget();

let socket = null;

export function connectSocket() {
  if (socket) return socket;
  socket = window.io();
  state.socket = socket;

  socket.on('connect', () => {
    if (state.user) socket.emit('identify', { userId: state.user.id });
  });

  const forward = (event) => socket.on(event, (payload) => {
    bus.dispatchEvent(new CustomEvent(event, { detail: payload }));
  });

  [
    'queue_joined', 'queue_error', 'match_start', 'match_tick', 'match_end',
    'rescue_result', 'double_result', 'opponent_emoji', 'challenge_error',
  ].forEach(forward);

  return socket;
}

export function identify() {
  if (socket && state.user) socket.emit('identify', { userId: state.user.id });
}

export function on(event, handler) {
  const wrapped = (e) => handler(e.detail);
  bus.addEventListener(event, wrapped);
  return () => bus.removeEventListener(event, wrapped);
}

export function emit(event, payload) {
  socket?.emit(event, payload);
}
