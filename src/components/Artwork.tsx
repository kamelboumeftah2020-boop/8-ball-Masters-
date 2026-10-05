import { useState } from "react";
import { IconMic } from "./Icons";

export function Artwork({ src, alt, className = "" }: { src?: string; alt: string; className?: string }) {
  const [failed, setFailed] = useState(false);
  return (
    <div className={`artwork ${className}`}>
      {src && !failed ? (
        <img src={src} alt={alt} loading="lazy" decoding="async" onError={() => setFailed(true)} />
      ) : (
        <div className="artwork-fallback"><IconMic size={28} /></div>
      )}
    </div>
  );
}
