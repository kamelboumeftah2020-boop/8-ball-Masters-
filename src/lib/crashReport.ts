/**
 * Last-resort crash screen: if something fails outside React's error boundaries
 * (or React itself unmounts), show the error instead of an empty page so users
 * can report it.
 */
export function showCrash(error: unknown, where: string) {
  const err = error instanceof Error ? error : new Error(String(error));
  console.error(`[${where}]`, err);
  if (document.getElementById("crash-report")) return;
  const box = document.createElement("div");
  box.id = "crash-report";
  box.setAttribute("dir", "rtl");
  box.style.cssText =
    "position:fixed;inset:0;z-index:9999;background:#0b0b14;color:#f4f4f8;padding:48px 20px;overflow:auto;font:14px/1.6 system-ui,sans-serif";
  const h = document.createElement("h2");
  h.textContent = "حدث خطأ في التطبيق";
  const p = document.createElement("p");
  p.textContent = "صوّر هذه الشاشة وأرسلها للمطوّر.";
  const pre = document.createElement("pre");
  pre.dir = "ltr";
  pre.style.cssText = "white-space:pre-wrap;word-break:break-word;font-size:12px;background:#1b1b2a;padding:12px;border-radius:12px";
  pre.textContent = `${where}: ${err.message}\n\n${(err.stack || "").split("\n").slice(0, 8).join("\n")}\n\n${navigator.userAgent}\n${location.href}`;
  const btn = document.createElement("button");
  btn.textContent = "إعادة تشغيل";
  btn.style.cssText = "margin-top:16px;padding:10px 20px;border-radius:999px;border:0;background:#8b5cf6;color:#fff;font:inherit";
  btn.onclick = () => {
    location.hash = "#/";
    location.reload();
  };
  box.append(h, p, pre, btn);
  document.body.appendChild(box);
}

export function installCrashHandlers() {
  window.addEventListener("error", (e) => showCrash(e.error ?? e.message, "window.onerror"));
}
