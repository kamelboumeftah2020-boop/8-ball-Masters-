import { useRef, useState, type ReactNode } from "react";
import { Link, useNavigate } from "react-router-dom";
import { BackButton } from "../components/BackButton";
import { IconPlus } from "../components/Icons";
import { Spinner } from "../components/States";
import { loadFeed } from "../lib/api";
import { exportBackup, exportOpml, parseOpml, resolveOpml, restoreBackup } from "../lib/backup";
import { formatBytes } from "../lib/format";
import { COUNTRIES } from "../lib/genres";
import { getThemePref, setThemePref, type ThemePref } from "../lib/theme";
import { APP_VERSION } from "../lib/version";
import { EpisodeChecker, isNative } from "../native";
import { useLibrary } from "../store/library";
import { usePlayer } from "../store/player";
import { useAds } from "../store/ads";
import { useSubscriptions } from "../store/subscriptions";

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="settings-section">
      <h2>{title}</h2>
      <div className="subs-card">{children}</div>
    </section>
  );
}

function Toggle({ title, hint, checked, onChange }: { title: string; hint?: string; checked: boolean; onChange: (v: boolean) => void }) {
  return (
    <label className="subs-row toggle">
      <span>
        <strong>{title}</strong>
        {hint && <small>{hint}</small>}
      </span>
      <input type="checkbox" checked={checked} onChange={(e) => onChange(e.target.checked)} />
    </label>
  );
}

const THEMES: { id: ThemePref; label: string }[] = [
  { id: "system", label: "تلقائي" },
  { id: "dark", label: "داكن" },
  { id: "light", label: "فاتح" },
];

export function Settings() {
  const lib = useLibrary();
  const player = usePlayer();
  const subs = useSubscriptions();
  const ads = useAds();
  const navigate = useNavigate();
  const [theme, setTheme] = useState(getThemePref());
  const [feedUrl, setFeedUrl] = useState("");
  const [busy, setBusy] = useState<string | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  const fileInput = useRef<HTMLInputElement>(null);

  const totalDownloads = Object.values(lib.downloads).reduce((s, d) => s + d.size, 0);

  const run = async (label: string, fn: () => Promise<string | void>) => {
    setBusy(label);
    setMsg(null);
    try {
      const m = await fn();
      if (m) setMsg(m);
    } catch (e) {
      setMsg(`⚠️ ${e instanceof Error ? e.message : String(e)}`);
    } finally {
      setBusy(null);
    }
  };

  const addFeed = () =>
    run("feed", async () => {
      let url = feedUrl.trim();
      if (!url) return;
      if (!/^https?:\/\//i.test(url)) url = `https://${url}`;
      const { podcast } = await loadFeed(url);
      setFeedUrl("");
      navigate(`/podcast/${podcast.id}`, { state: { podcast } });
    });

  const onImport = (file: File) =>
    run("import", async () => {
      const text = await file.text();
      if (text.trimStart().startsWith("{")) {
        const n = restoreBackup(text);
        setTimeout(() => window.location.reload(), 1200);
        return `✓ تمت استعادة النسخة الاحتياطية (${n} عنصر). جارٍ إعادة التشغيل…`;
      }
      const entries = parseOpml(text);
      if (!entries.length) return "لم يُعثر على برامج في الملف";
      const podcasts = await resolveOpml(entries, lib.country, (d) => setBusy(`import:${d}/${entries.length}`));
      lib.upsertFavPodcasts(podcasts);
      return `✓ تمت إضافة ${podcasts.length} برنامج إلى المفضلة`;
    });

  return (
    <div className="page settings-page">
      <header className="page-head with-back">
        <BackButton />
        <h1>الإعدادات</h1>
      </header>

      {msg && <div className="settings-msg">{msg}</div>}

      <Section title="المظهر">
        <div className="subs-row">
          <span><strong>السمة</strong></span>
          <div className="segmented" role="radiogroup">
            {THEMES.map((t) => (
              <button
                key={t.id}
                role="radio"
                aria-checked={theme === t.id}
                className={theme === t.id ? "active" : ""}
                onClick={() => {
                  setTheme(t.id);
                  setThemePref(t.id);
                }}
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>
        <label className="subs-row">
          <span><strong>بلد المحتوى</strong><small>يحدد قوائم «الأكثر استماعاً»</small></span>
          <select className="settings-select" value={lib.country} onChange={(e) => lib.setCountry(e.target.value)}>
            {COUNTRIES.map((c) => <option key={c.code} value={c.code}>{c.flag} {c.name}</option>)}
          </select>
        </label>
      </Section>

      {ads.available && (
        <Section title="الإعلانات">
          <button
            className="subs-row action"
            disabled={!ads.rewardedReady || !!busy}
            onClick={() => run("reward", async () => {
              const ok = await ads.watchForAdFree();
              return ok ? "✓ استمتع بساعة بدون إعلانات" : "لم يكتمل الإعلان، حاول مرة أخرى";
            })}
          >
            <span>
              <strong>ساعة بدون إعلانات</strong>
              <small>
                {ads.adFreeUntil
                  ? `مفعّلة حتى ${new Date(ads.adFreeUntil).toLocaleTimeString("ar-u-nu-latn", { hour: "2-digit", minute: "2-digit" })} · شاهد إعلاناً لإضافة ساعة`
                  : ads.rewardedReady
                    ? "شاهد إعلاناً قصيراً واستمع ساعة كاملة بدون إعلانات"
                    : "جارٍ تجهيز الإعلان…"}
              </small>
            </span>
            {busy === "reward" ? <Spinner small /> : <span className="chev">‹</span>}
          </button>
        </Section>
      )}

      <Section title="الاستماع">
        <Toggle title="تقوية الصوت" hint="يرفع الأصوات الخافتة ويوازن الصوت" checked={player.fx.boost} onChange={(v) => player.setFx({ ...player.fx, boost: v })} />
        <Toggle title="تسريع فترات الصمت" hint="يتجاوز السكتات الطويلة ليوفّر وقتك" checked={player.fx.trimSilence} onChange={(v) => player.setFx({ ...player.fx, trimSilence: v })} />
        <p className="muted small-note row-note">
          تعمل على الحلقات المحمّلة وتلاوات القرآن.
          {player.timeSaved >= 60 && ` وفّرت حتى الآن ${Math.round(player.timeSaved / 60)} دقيقة.`}
        </p>
      </Section>

      <Section title="التحديث والتنبيهات">
        <Toggle title="تحميل الحلقات الجديدة تلقائياً" hint="من برامجك المفضلة، للاستماع بدون إنترنت" checked={subs.autoDownload} onChange={subs.setAutoDownload} />
        {isNative && (
          <button className="subs-row action" onClick={() => run("notif", async () => {
            const r = await EpisodeChecker.requestPermission();
            return r.granted ? "✓ الإشعارات مفعّلة" : "الإشعارات معطّلة — فعّلها من إعدادات الجوال";
          })}>
            <span><strong>إشعارات الحلقات الجديدة</strong><small>تنبيه عند نزول حلقة من برامجك المفضلة</small></span>
            <span className="chev">‹</span>
          </button>
        )}
      </Section>

      <Section title="إضافة بودكاست برابط RSS">
        <div className="subs-row feed-row">
          <input
            type="url"
            dir="ltr"
            inputMode="url"
            placeholder="https://example.com/feed.xml"
            value={feedUrl}
            onChange={(e) => setFeedUrl(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && addFeed()}
          />
          <button className="btn primary small" onClick={addFeed} disabled={!feedUrl.trim() || busy === "feed"}>
            {busy === "feed" ? <Spinner small /> : <IconPlus size={16} />} إضافة
          </button>
        </div>
        <p className="muted small-note row-note">لأي بودكاست غير موجود في الدليل، أو بودكاست خاص.</p>
      </Section>

      <Section title="النسخ الاحتياطي">
        <button className="subs-row action" disabled={!!busy || !lib.favPodcasts.length} onClick={() => run("opml", async () => {
          const n = await exportOpml(lib.favPodcasts, lib.country);
          return `✓ تم تصدير ${n} برنامج`;
        })}>
          <span><strong>تصدير المفضلة (OPML)</strong><small>ملف تفهمه كل تطبيقات البودكاست</small></span>
          {busy === "opml" ? <Spinner small /> : <span className="chev">‹</span>}
        </button>
        <button className="subs-row action" disabled={!!busy} onClick={() => run("backup", async () => {
          await exportBackup();
        })}>
          <span><strong>نسخة احتياطية كاملة</strong><small>المفضلة، التقدّم، السجل والإعدادات</small></span>
          <span className="chev">‹</span>
        </button>
        <button className="subs-row action" disabled={!!busy} onClick={() => fileInput.current?.click()}>
          <span>
            <strong>استيراد</strong>
            <small>{busy?.startsWith("import") ? `جارٍ الاستيراد ${busy.split(":")[1] ?? ""}` : "ملف OPML من تطبيق آخر أو نسخة احتياطية من صدى"}</small>
          </span>
          {busy?.startsWith("import") ? <Spinner small /> : <span className="chev">‹</span>}
        </button>
        <input
          ref={fileInput}
          type="file"
          accept=".opml,.xml,.json,text/xml,application/json,text/x-opml"
          hidden
          onChange={(e) => {
            const f = e.target.files?.[0];
            e.target.value = "";
            if (f) onImport(f);
          }}
        />
      </Section>

      <Section title="حول التطبيق">
        <div className="subs-row"><span><strong>الإصدار</strong></span><span className="muted">{APP_VERSION}</span></div>
        <div className="subs-row"><span><strong>التحميلات</strong></span><span className="muted">{formatBytes(totalDownloads)}</span></div>
        <Link to="/privacy" className="subs-row action">
          <span><strong>سياسة الخصوصية</strong></span>
          <span className="chev">‹</span>
        </Link>
        <p className="muted small-note row-note">
          بيانات البودكاست من دليل Apple Podcasts وخلاصات RSS الخاصة بكل برنامج. تلاوات القرآن من mp3quran.net.
        </p>
      </Section>
    </div>
  );
}
