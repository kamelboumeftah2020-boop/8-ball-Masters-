import { useCallback, useEffect, useRef, useState } from "react";
import type { Query } from "./api";
import { getCached, putCached } from "./db";
import { useRefreshTick } from "./refresh";

interface State<T> {
  data?: T;
  error?: unknown;
  loading: boolean;
  /** True while a background refresh is running over data already on screen. */
  refreshing: boolean;
}

/**
 * Stale-while-revalidate data hook:
 *  1. shows the last saved result immediately (works offline),
 *  2. fetches the latest from the network,
 *  3. re-fetches silently whenever the app-wide refresh signal ticks.
 * Errors are only surfaced when there is nothing to show.
 */
export function useQuery<T>(make: () => Query<T>, deps: unknown[]) {
  const [state, setState] = useState<State<T>>({ loading: true, refreshing: false });
  const [nonce, setNonce] = useState(0);
  const tick = useRefreshTick();
  const queryRef = useRef<Query<T> | null>(null);
  const hasData = useRef(false);

  // New query (route/params changed or retry): cached copy first, then network.
  useEffect(() => {
    let alive = true;
    const q = make();
    queryRef.current = q;
    hasData.current = false;
    setState({ loading: true, refreshing: false });

    if (q.persist) {
      getCached<T>(q.key).then((cached) => {
        if (alive && cached !== undefined && !hasData.current) {
          hasData.current = true;
          setState({ data: cached, loading: false, refreshing: true });
        }
      });
    }
    q.run(nonce > 0).then(
      (data) => {
        if (!alive) return;
        hasData.current = true;
        setState({ data, loading: false, refreshing: false });
        if (q.persist) putCached(q.key, data);
      },
      (error) => {
        if (!alive) return;
        setState((s) => (s.data !== undefined ? { ...s, loading: false, refreshing: false } : { error, loading: false, refreshing: false }));
      }
    );
    return () => {
      alive = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, nonce]);

  // Automatic refresh: keep what's on screen, swap in new data when it arrives.
  useEffect(() => {
    if (tick === 0) return;
    const q = queryRef.current;
    if (!q) return;
    let alive = true;
    setState((s) => ({ ...s, refreshing: true }));
    q.run(true).then(
      (data) => {
        if (!alive || queryRef.current !== q) return;
        hasData.current = true;
        setState({ data, loading: false, refreshing: false });
        if (q.persist) putCached(q.key, data);
      },
      () => alive && setState((s) => ({ ...s, refreshing: false }))
    );
    return () => {
      alive = false;
    };
  }, [tick]);

  const retry = useCallback(() => setNonce((n) => n + 1), []);
  return { ...state, retry };
}
