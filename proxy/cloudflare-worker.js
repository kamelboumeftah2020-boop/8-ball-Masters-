// Download proxy for Sada (صدى).
//
// Some podcast hosts don't send CORS headers, so a browser can stream their
// audio but can't save it for offline listening. Deploy this as a Cloudflare
// Worker (free tier is fine), then build the app with:
//   VITE_DOWNLOAD_PROXY=https://<your-worker>.workers.dev npm run build
//
// Only audio responses are relayed, so the worker can't be used as a general
// open proxy for web pages.

const ALLOWED_ORIGINS = ["*"]; // e.g. ["https://your-app.example"]

export default {
  async fetch(request) {
    const origin = request.headers.get("Origin") || "*";
    const allowOrigin = ALLOWED_ORIGINS.includes("*") || ALLOWED_ORIGINS.includes(origin) ? origin : "null";
    const cors = {
      "Access-Control-Allow-Origin": allowOrigin,
      "Access-Control-Allow-Methods": "GET, OPTIONS",
      "Access-Control-Allow-Headers": "Range",
      "Access-Control-Expose-Headers": "Content-Length, Content-Type, Content-Range",
    };
    if (request.method === "OPTIONS") return new Response(null, { headers: cors });
    if (request.method !== "GET") return new Response("Method not allowed", { status: 405, headers: cors });

    const target = new URL(request.url).searchParams.get("url");
    if (!target || !/^https?:\/\//i.test(target)) return new Response("Missing ?url=", { status: 400, headers: cors });

    const range = request.headers.get("Range");
    const upstream = await fetch(target, {
      redirect: "follow",
      headers: { "User-Agent": "SadaPodcastProxy/1.0", ...(range ? { Range: range } : {}) },
    });

    const type = upstream.headers.get("Content-Type") || "";
    const looksLikeAudio = /^(audio|video)\//.test(type) || type === "application/octet-stream" || /\.(mp3|m4a|aac|ogg|opus|wav)(\?|$)/i.test(target);
    if (!looksLikeAudio) return new Response("Not an audio file", { status: 415, headers: cors });

    const headers = new Headers(cors);
    for (const h of ["Content-Type", "Content-Length", "Content-Range", "Accept-Ranges"]) {
      const v = upstream.headers.get(h);
      if (v) headers.set(h, v);
    }
    return new Response(upstream.body, { status: upstream.status, headers });
  },
};
