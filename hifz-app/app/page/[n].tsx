import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Alert, BackHandler, I18nManager, Pressable, ScrollView, Text, View, useWindowDimensions } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import PagerView from 'react-native-pager-view';
import { Ionicons } from '@expo/vector-icons';
import { useTheme, palettes, font, radius } from '../../src/theme';
import { MushafPage, keyOf } from '../../src/components/MushafPage';
import { SurahPicker } from '../../src/components/SurahPicker';
import { useSettings } from '../../src/store/settings';
import { PAGE_COUNT, MAyah, getMushaf } from '../../src/lib/mushaf';
import { SURAHS, arNum } from '../../src/lib/surahs';
import { getTafsir } from '../../src/lib/tafasir';
import { playAyah, playRange, reciterFor, stopAudio } from '../../src/lib/audio';
import { bookmarksAll, toggleBookmark } from '../../src/db/queries';

const REPEATS = [1, 3, 5, 10];
const clampPage = (n: number) => Math.min(PAGE_COUNT, Math.max(1, Math.round(n) || 1));
const row = I18nManager.isRTL ? 'row' : 'row-reverse';

export default function Reader() {
  const params = useLocalSearchParams<{ n: string; s?: string; a?: string }>();
  const { width, height } = useWindowDimensions();
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const t = useTheme();
  const riwaya = useSettings((s) => s.riwaya);
  const setSettings = useSettings((s) => s.set);
  const mushaf = getMushaf(riwaya);
  const pager = useRef<PagerView>(null);

  const [page, setPage] = useState(clampPage(Number(params.n)));
  const [sel, setSel] = useState<MAyah | null>(() => (params.s && params.a ? mushaf.find(+params.s, +params.a) ?? null : null));
  const [playing, setPlaying] = useState<{ s: number; a: number } | null>(null);
  const [tafsir, setTafsir] = useState<string | null>(null);
  const [repeat, setRepeat] = useState(1);
  const [marks, setMarks] = useState<Set<string>>(new Set());
  const [pick, setPick] = useState(false);

  const pageH = height - insets.top - insets.bottom;

  useEffect(() => { bookmarksAll(riwaya).then(setMarks); }, [riwaya]);
  useEffect(() => () => { stopAudio(); }, []);

  // حفظ آخر صفحة مقروءة
  useEffect(() => {
    const id = setTimeout(() => {
      const lp = useSettings.getState().lastPage;
      if (lp[riwaya] !== page) setSettings({ lastPage: { ...lp, [riwaya]: page } });
    }, 600);
    return () => clearTimeout(id);
  }, [page, riwaya]);

  // زر الرجوع يغلق اللوحة أولًا
  useEffect(() => {
    if (!sel) return;
    const sub = BackHandler.addEventListener('hardwareBackPress', () => { setSel(null); return true; });
    return () => sub.remove();
  }, [sel]);

  // التفسير يُجلب تلقائيًا عند اختيار آية
  useEffect(() => {
    if (!sel) return;
    let live = true;
    setTafsir(null);
    getTafsir(riwaya, sel.surah, sel.ayah)
      .then((x) => live && setTafsir(x))
      .catch(() => live && setTafsir('التفسير يحتاج اتصالًا بالانترنت في أول مرة، ثم يبقى محفوظًا.'));
    return () => { live = false; };
  }, [sel, riwaya]);

  const goTo = useCallback((p: number) => { pager.current?.setPage(clampPage(p) - 1); setPage(clampPage(p)); }, []);

  // قلب الصفحة تلقائيًا مع التلاوة
  useEffect(() => {
    if (!playing) return;
    const a = mushaf.find(playing.s, playing.a);
    if (a && a.page !== page) goTo(a.page);
  }, [playing]);

  const onAyah = useCallback((a: MAyah) => setSel((cur) => (cur && cur.surah === a.surah && cur.ayah === a.ayah ? null : a)), []);

  const onBookmark = useCallback(async (p: number) => {
    const a = getMushaf(riwaya).pages[p][0];
    await toggleBookmark(riwaya, a.surah, a.ayah);
    setMarks(await bookmarksAll(riwaya));
  }, [riwaya]);

  const onSurahPress = useCallback(() => setPick(true), []);

  const fail = () => { setPlaying(null); Alert.alert('تعذّر التشغيل', 'تأكد من الاتصال بالانترنت أو حمّل صوت السورة من الصفحة الرئيسية'); };
  const listenOne = (a: MAyah) => {
    setPlaying({ s: a.surah, a: a.ayah });
    playAyah(riwaya, a.surah, a.ayah, () => setPlaying(null)).catch(fail);
  };
  const listenFrom = (a: MAyah) => {
    setSel(null);
    playRange(riwaya, a.surah, a.ayah, { repeat, onAyah: (n) => setPlaying(n == null ? null : { s: a.surah, a: n }) }).catch(fail);
  };
  const stop = () => { stopAudio(); setPlaying(null); };
  const markAyah = async (a: MAyah) => { await toggleBookmark(riwaya, a.surah, a.ayah); setMarks(await bookmarksAll(riwaya)); };

  const selKey = sel ? keyOf(sel.surah, sel.ayah) : null;
  const playKey = playing ? keyOf(playing.s, playing.a) : null;
  const pageMarked = (p: number) => { const a = mushaf.pages[p]?.[0]; return !!a && marks.has(keyOf(a.surah, a.ayah)); };

  const children = useMemo(() => Array.from({ length: PAGE_COUNT }, (_, i) => {
    const p = i + 1;
    return (
      <View key={p} collapsable={false} style={{ flex: 1, backgroundColor: t.page }}>
        {Math.abs(p - page) <= 2 && (
          <MushafPage riwaya={riwaya} page={p} width={width} height={pageH} selected={selKey} playing={playKey}
            bookmarked={pageMarked(p)} onAyah={onAyah} onBookmark={onBookmark} onSurahPress={onSurahPress} />
        )}
      </View>
    );
  }), [page, riwaya, width, pageH, selKey, playKey, marks, t]);

  const chip = (label: string, icon: any, onPress: () => void, active = false) => (
    <Pressable onPress={onPress} style={{ flex: 1, alignItems: 'center', gap: 4, paddingVertical: 10, borderRadius: radius.md, backgroundColor: active ? t.primary : t.soft }}>
      <Ionicons name={icon} size={22} color={active ? t.onPrimary : t.primary} />
      <Text style={{ fontFamily: font.bold, fontSize: 12, color: active ? t.onPrimary : t.primary }}>{label}</Text>
    </Pressable>
  );

  return (
    <View style={{ flex: 1, backgroundColor: t.page, paddingTop: insets.top, paddingBottom: insets.bottom }}>
      <StatusBar style={t === palettes.dark ? 'light' : 'dark'} />
      <PagerView ref={pager} style={{ flex: 1 }} initialPage={page - 1} layoutDirection="rtl" offscreenPageLimit={1}
        onPageSelected={(e) => setPage(e.nativeEvent.position + 1)}>
        {children}
      </PagerView>

      {playing && !sel && (
        <View style={{ position: 'absolute', left: 16, right: 16, bottom: insets.bottom + 56, flexDirection: row, alignItems: 'center', gap: 12, backgroundColor: t.primary, borderRadius: radius.lg, paddingHorizontal: 16, paddingVertical: 10, elevation: 6 }}>
          <Ionicons name="volume-high" size={20} color={t.onPrimary} />
          <Text style={{ flex: 1, fontFamily: font.bold, color: t.onPrimary, textAlign: 'right' }}>
            {SURAHS[playing.s - 1]} · الآية {arNum(playing.a)} · {reciterFor(riwaya).name}
          </Text>
          <Pressable onPress={stop} hitSlop={10}><Ionicons name="stop-circle" size={30} color={t.onPrimary} /></Pressable>
        </View>
      )}

      {sel && (
        <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, maxHeight: '55%', backgroundColor: t.card, borderTopLeftRadius: radius.lg, borderTopRightRadius: radius.lg, paddingHorizontal: 16, paddingTop: 10, paddingBottom: insets.bottom + 12, gap: 10, elevation: 16, borderTopWidth: 1, borderColor: t.border }}>
          <View style={{ alignSelf: 'center', width: 42, height: 4, borderRadius: 2, backgroundColor: t.border }} />
          <View style={{ flexDirection: row, alignItems: 'center', justifyContent: 'space-between' }}>
            <Text style={{ fontFamily: font.bold, fontSize: 17, color: t.text }}>سورة {SURAHS[sel.surah - 1]} · الآية {arNum(sel.ayah)}</Text>
            <Pressable onPress={() => setSel(null)} hitSlop={12}><Ionicons name="close" size={24} color={t.muted} /></Pressable>
          </View>
          <View style={{ flexDirection: row, gap: 8 }}>
            {chip(playKey === selKey ? 'إيقاف' : 'استماع', playKey === selKey ? 'stop' : 'play', () => (playKey === selKey ? stop() : listenOne(sel)), playKey === selKey)}
            {chip('من هنا', 'play-forward', () => listenFrom(sel))}
            {chip(marks.has(selKey!) ? 'محفوظة' : 'علامة', marks.has(selKey!) ? 'bookmark' : 'bookmark-outline', () => markAyah(sel))}
            {chip('احفظها', 'school-outline', () => { stop(); router.push({ pathname: '/hifz', params: { surah: String(sel.surah), from: String(sel.ayah) } }); })}
          </View>
          <View style={{ flexDirection: row, alignItems: 'center', gap: 6 }}>
            <Text style={{ fontFamily: font.ui, fontSize: 12, color: t.muted }}>تكرار كل آية عند «من هنا»:</Text>
            {REPEATS.map((r) => (
              <Pressable key={r} onPress={() => setRepeat(r)} style={{ paddingHorizontal: 10, paddingVertical: 3, borderRadius: radius.lg, backgroundColor: repeat === r ? t.primary : t.soft }}>
                <Text style={{ fontFamily: font.bold, fontSize: 12, color: repeat === r ? t.onPrimary : t.primary }}>{arNum(r)}×</Text>
              </Pressable>
            ))}
          </View>
          <Text style={{ fontFamily: font.bold, fontSize: 14, color: t.gold, textAlign: 'right' }}>التفسير الميسّر</Text>
          <ScrollView style={{ flexGrow: 0 }}>
            <Text style={{ fontFamily: font.ui, fontSize: 15, lineHeight: 27, color: t.text, textAlign: 'right', writingDirection: 'rtl' }}>{tafsir ?? 'جاري التحميل…'}</Text>
          </ScrollView>
        </View>
      )}

      <SurahPicker visible={pick} current={mushaf.pages[page]?.[0]?.surah} onClose={() => setPick(false)}
        onPick={(s) => { setPick(false); setSel(null); goTo(mushaf.surahPage[s - 1]); }} />
    </View>
  );
}
