export interface Genre {
  id: string;
  name: string;
  emoji: string;
  /** Two gradient stops for the genre tile. */
  colors: [string, string];
}

// Apple Podcasts top-level genre ids.
export const GENRES: Genre[] = [
  { id: "1324", name: "المجتمع والثقافة", emoji: "🌍", colors: ["#f59e0b", "#ef4444"] },
  { id: "1489", name: "الأخبار", emoji: "📰", colors: ["#0ea5e9", "#2563eb"] },
  { id: "1303", name: "الكوميديا", emoji: "😂", colors: ["#facc15", "#f97316"] },
  { id: "1314", name: "الدين والروحانيات", emoji: "🕌", colors: ["#10b981", "#047857"] },
  { id: "1304", name: "التعليم", emoji: "🎓", colors: ["#6366f1", "#8b5cf6"] },
  { id: "1321", name: "الأعمال", emoji: "💼", colors: ["#14b8a6", "#0f766e"] },
  { id: "1318", name: "التقنية", emoji: "💻", colors: ["#06b6d4", "#3b82f6"] },
  { id: "1533", name: "العلوم", emoji: "🔬", colors: ["#22d3ee", "#0891b2"] },
  { id: "1512", name: "الصحة واللياقة", emoji: "💪", colors: ["#84cc16", "#16a34a"] },
  { id: "1487", name: "التاريخ", emoji: "🏛️", colors: ["#d97706", "#92400e"] },
  { id: "1488", name: "جرائم حقيقية", emoji: "🔍", colors: ["#dc2626", "#7f1d1d"] },
  { id: "1545", name: "الرياضة", emoji: "⚽", colors: ["#22c55e", "#15803d"] },
  { id: "1309", name: "التلفزيون والسينما", emoji: "🎬", colors: ["#e11d48", "#9f1239"] },
  { id: "1310", name: "الموسيقى", emoji: "🎵", colors: ["#ec4899", "#a21caf"] },
  { id: "1301", name: "الفنون", emoji: "🎨", colors: ["#f472b6", "#c026d3"] },
  { id: "1483", name: "القصص والخيال", emoji: "📖", colors: ["#a78bfa", "#6d28d9"] },
  { id: "1305", name: "الأطفال والعائلة", emoji: "🧸", colors: ["#fb923c", "#f43f5e"] },
  { id: "1502", name: "الترفيه والهوايات", emoji: "🎮", colors: ["#38bdf8", "#7c3aed"] },
  { id: "1511", name: "السياسة والحكومة", emoji: "🏛", colors: ["#64748b", "#334155"] },
];

export const genreById = (id: string) => GENRES.find((g) => g.id === id);

export interface Country {
  code: string;
  name: string;
  flag: string;
}

export const COUNTRIES: Country[] = [
  { code: "sa", name: "السعودية", flag: "🇸🇦" },
  { code: "ae", name: "الإمارات", flag: "🇦🇪" },
  { code: "eg", name: "مصر", flag: "🇪🇬" },
  { code: "dz", name: "الجزائر", flag: "🇩🇿" },
  { code: "ma", name: "المغرب", flag: "🇲🇦" },
  { code: "tn", name: "تونس", flag: "🇹🇳" },
  { code: "kw", name: "الكويت", flag: "🇰🇼" },
  { code: "qa", name: "قطر", flag: "🇶🇦" },
  { code: "bh", name: "البحرين", flag: "🇧🇭" },
  { code: "om", name: "عُمان", flag: "🇴🇲" },
  { code: "jo", name: "الأردن", flag: "🇯🇴" },
  { code: "lb", name: "لبنان", flag: "🇱🇧" },
  { code: "iq", name: "العراق", flag: "🇮🇶" },
  { code: "us", name: "الولايات المتحدة", flag: "🇺🇸" },
  { code: "gb", name: "المملكة المتحدة", flag: "🇬🇧" },
  { code: "fr", name: "فرنسا", flag: "🇫🇷" },
  { code: "de", name: "ألمانيا", flag: "🇩🇪" },
  { code: "ca", name: "كندا", flag: "🇨🇦" },
  { code: "tr", name: "تركيا", flag: "🇹🇷" },
];
