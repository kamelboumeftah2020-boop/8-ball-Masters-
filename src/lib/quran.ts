import type { Episode } from "./types";

/**
 * Quran recitations, bundled so the section works offline.
 * Audio: mp3quran.net (CORS-enabled CDN, one MP3 per surah).
 */

export interface Reciter {
  id: string;
  name: string;
  rewaya: string;
  server: string;
}

export const RECITERS: Reciter[] = [
  {
    id: "abdullah-khalaf",
    name: "عبدالله الخلف",
    rewaya: "حفص عن عاصم · مرتّل",
    server: "https://cdn.mp3quran.net/audio/abdullah-khalaf/r1/",
  },
];

/** Surah names in mushaf order (index 0 = Al-Fatiha). */
export const SURAH_NAMES: string[] = ["الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس", "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه", "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم", "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر", "فصلت", "الشورى", "الزخرف", "الدّخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق", "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة", "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج", "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس", "التكوير", "الإنفطار", "المطففين", "الإنشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد", "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات", "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص", "الفلق", "الناس"];

/** "1" = Meccan, "0" = Medinan, per surah. */
const MAKKIA = "100001100111011111111010111111110111111111111100011111010000000000111111111011111111111111111111100111111111101111";

export const isMakki = (n: number) => MAKKIA[n - 1] === "1";

export const quranPodcastId = (r: Reciter) => `quran-${r.id}`;

/** Bundled cover, resolved to an absolute URL for the media notification. */
export const quranArtwork = () => new URL("quran-cover.png", window.location.href).href;

export function surahEpisode(r: Reciter, n: number): Episode {
  const num = String(n).padStart(3, "0");
  return {
    id: `quran:${r.id}:${num}`,
    podcastId: quranPodcastId(r),
    podcastTitle: `القرآن الكريم · ${r.name}`,
    title: `سورة ${SURAH_NAMES[n - 1]}`,
    description: `${isMakki(n) ? "مكية" : "مدنية"} · السورة رقم ${n} · ${r.rewaya}`,
    audioUrl: `${r.server}${num}.mp3`,
    artwork: quranArtwork(),
    releaseDate: "",
    durationMs: 0,
    fileExtension: "mp3",
    mediaType: "audio",
  };
}

export const allSurahs = (r: Reciter) => SURAH_NAMES.map((_, i) => surahEpisode(r, i + 1));

export const reciterById = (id: string) => RECITERS.find((r) => r.id === id);
