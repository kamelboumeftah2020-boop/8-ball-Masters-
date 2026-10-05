import { useState } from "react";
import { formatClock } from "../lib/format";
import { usePlayer, usePlayerTime } from "../store/player";

export function SeekBar() {
  const { seek } = usePlayer();
  const { position, duration, buffered } = usePlayerTime();
  const [drag, setDrag] = useState<number | null>(null);

  const value = drag ?? position;
  const max = duration || 1;
  const pct = (value / max) * 100;
  const bufPct = (buffered / max) * 100;

  return (
    <div className="seek">
      <input
        type="range"
        min={0}
        max={max}
        step={1}
        value={Math.min(value, max)}
        aria-label="موضع التشغيل"
        aria-valuetext={formatClock(value)}
        style={{ "--pct": `${pct}%`, "--buf": `${bufPct}%` } as React.CSSProperties}
        onChange={(e) => setDrag(Number(e.target.value))}
        onPointerUp={() => {
          if (drag != null) seek(drag);
          setDrag(null);
        }}
        onKeyUp={() => {
          if (drag != null) seek(drag);
          setDrag(null);
        }}
      />
      <div className="seek-times">
        <span>{formatClock(value)}</span>
        <span>-{formatClock(Math.max(0, duration - value))}</span>
      </div>
    </div>
  );
}
