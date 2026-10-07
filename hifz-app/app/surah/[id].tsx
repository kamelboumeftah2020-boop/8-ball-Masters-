import React, { useCallback, useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Alert, FlatList, Pressable, ScrollView, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useLocalSearchParams, useFocusEffect, useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useTheme, space, radius, font } from '../../src/theme';
import { Txt, Btn } from '../../src/components/ui';
import { SURAHS, arNum } from '../../src/lib/surahs';
import { useSettings } from '../../src/store/settings';
import { surahAyahs, bookmarksOf, toggleBookmark, Ayah } from '../../src/db/queries';
import { getTafsir } from '../../src/lib/tafasir';
import { reciterFor, playRange, stopAudio, isSurahAudioDownloaded, downloadSurahForOffline } from '../../src/lib/audio';
import { fetchAndCacheQuran } from '../../src/lib/quran-online';

const REPEATS = [1, 3, 5, 10];
const BASMALA = { hafs: 'بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ', warsh: 'بِسْمِ اِ۬للَّهِ اِ۬لرَّحْمَٰنِ اِ۬لرَّحِيمِ' };

export default function SurahPage() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const surahId = Number(id);
  const t = useTheme();
  const router = useRouter();
  const riwaya = useSettings((s) => s.riwaya);
  const fontSize = useSettings((s) => s.fontSize);
  const rec = reciterFor(riwaya);
  const list = useRef<FlatList<Ayah>>(null);

  const [ayahs, setAyahs] = useState<Ayah[]>([]);
  const [loading, setLoading] = useState(true);
  const [failed, setFailed] = useState(false);
  const [selected, setSelected] = useState<number | null>(null);
  const [playing, setPlaying] = useState<number | null>(null);
  const [repeat, setRepeat] = useState(1);
  const [tafsir, setTafsir] = useState<{ ayah: number; text: string } | null>(null);
  const [marks, setMarks] = useState<Set<number>>(new Set());
  const [offline, setOffline] = useState(false);
  const [dl, setDl] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true); setFailed(false);
    let rows = await surahAyahs(riwaya, surahId);
    if (rows.length === 0) {
      try { await fetchAndCacheQuran(riwaya); rows = await surahAyahs(riwaya, surahId); }
      catch { setFailed(true); }
    }
    setAyahs(rows);
    setMarks(await bookmarksOf(riwaya, surahId));
    setOffline(await isSurahAudioDownloaded(surahId, rec.id));
    setLoading(false);
  }, [riwaya, surahId, rec.id]);

  useFocusEffect(useCallback(() => { load(); return () => { stopAudio(); setPlaying(null); }; }, [load]));

  useEffect(() => {
    if (playing == null) return;
    const i = ayahs.findIndex((a) => a.ayah === playing);
    if (i >= 0) list.current?.scrollToIndex({ index: i, viewPosition: 0.3, animated: true });
  }, [playing]);

  const play = (from: number) => {
    setSelected(from);
    playRange(surahId, from, rec.id, { repeat, onAyah: setPlaying }).catch(() => {
      setPlaying(null);
      Alert.alert('تعذّر التشغيل', 'تأكد من الانترنت أو حمّل صوت السورة');
    });
  };
  const stop = () => { stopAudio(); setPlaying(null); };

  const showTafsir = async (a: number) => {
    setTafsir({ ayah: a, text: '…' });
    try { setTafsir({ ayah: a, text: await getTafsir(surahId, a) }); }
    catch { setTafsir({ ayah: a, text: 'التفسير يحتاج اتصالًا بالانترنت في أول مرة.' }); }
  };

  const mark = async (a: number) => {
    await toggleBookmark(riwaya, surahId, a);
    setMarks(await bookmarksOf(riwaya, surahId));
  };

  const download = async () => {
    try {
      await downloadSurahForOffline(surahId, rec.id, (d, n) => setDl(`${arNum(d)}/${arNum(n)}`));
      setOffline(true);
    } catch { Alert.alert('اكتمل جزئيًا', 'بعض الآيات لم تتحمّل، أعد المحاولة'); }
    setDl(null);
  };

  const iconBtn = (name: any, onPress: () => void, color = t.primary) => (
    <Pressable onPress={onPress} hitSlop={8} style={{ width: 44, height: 44, borderRadius: 22, backgroundColor: t.soft, alignItems: 'center', justifyContent: 'center' }}>
      <Ionicons name={name} size={22} color={color} />
    </Pressable>
  );

  const header = (
    <View style={{ gap: space.sm, paddingBottom: space.sm }}>
      {surahId !== 1 && surahId !== 9 && (
        <Txt style={{ fontFamily: font.quran, fontSize: fontSize * 0.85, textAlign: 'center', color: t.gold }}>{BASMALA[riwaya]}</Txt>
      )}
    </View>
  );

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: t.bg }} edges={['top', 'bottom']}>
      <View style={{ padding: space.md, flexDirection: 'row', alignItems: 'center', gap: 12 }}>
        {iconBtn('arrow-forward', () => router.back())}
        <View style={{ flex: 1 }}>
          <Txt bold size={20}>سورة {SURAHS[surahId - 1]}</Txt>
          <Txt muted size={12}>{riwaya === 'hafs' ? 'رواية حفص' : 'رواية ورش'} · {ayahs.length ? `${arNum(ayahs.length)} آية` : '—'}</Txt>
        </View>
        {iconBtn('school-outline', () => router.push({ pathname: '/hifz', params: { surah: String(surahId) } }))}
      </View>

      <View style={{ paddingHorizontal: space.md, gap: 8 }}>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <Pressable onPress={() => (playing == null ? play(selected ?? 1) : stop())}
            style={{ flex: 1, backgroundColor: t.primary, borderRadius: radius.md, padding: 12, alignItems: 'center', flexDirection: 'row', justifyContent: 'center', gap: 8 }}>
            <Ionicons name={playing == null ? 'play' : 'stop'} size={20} color={t.onPrimary} />
            <View>
              <Txt color={t.onPrimary} bold>{playing == null ? `استماع${selected ? ` من الآية ${arNum(selected)}` : ''}` : `الآية ${arNum(playing)} · إيقاف`}</Txt>
              <Txt color={t.onPrimary} size={11} style={{ opacity: 0.85 }}>{rec.name}</Txt>
            </View>
          </Pressable>
          <Pressable onPress={download} disabled={offline || dl !== null}
            style={{ backgroundColor: t.card, borderRadius: radius.md, paddingHorizontal: 12, borderWidth: 1, borderColor: t.border, alignItems: 'center', justifyContent: 'center' }}>
            <Ionicons name={offline ? 'checkmark-circle' : 'cloud-download-outline'} size={20} color={offline ? t.good : t.primary} />
            <Txt size={10} muted>{offline ? 'محمّل' : dl ?? 'تحميل'}</Txt>
          </Pressable>
        </View>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
          <Txt muted size={12}>تكرار كل آية:</Txt>
          {REPEATS.map((r) => (
            <Pressable key={r} onPress={() => setRepeat(r)}
              style={{ paddingHorizontal: 12, paddingVertical: 6, borderRadius: radius.lg, backgroundColor: repeat === r ? t.primary : t.soft }}>
              <Txt bold size={12} color={repeat === r ? t.onPrimary : t.primary}>{arNum(r)}×</Txt>
            </Pressable>
          ))}
        </View>
      </View>

      {loading ? <ActivityIndicator style={{ marginTop: 40 }} color={t.primary} /> : failed ? (
        <View style={{ margin: space.md, backgroundColor: t.goldSoft, padding: 16, borderRadius: radius.md, gap: 12 }}>
          <Txt>تعذّر جلب نص السورة. تأكد من اتصالك بالانترنت (مرة واحدة فقط، ثم يعمل المصحف بدون انترنت).</Txt>
          <Btn label="إعادة المحاولة" onPress={load} />
        </View>
      ) : (
        <FlatList ref={list} data={ayahs} keyExtractor={(a) => String(a.ayah)} ListHeaderComponent={header}
          contentContainerStyle={{ padding: space.md, gap: 10, paddingBottom: tafsir ? 260 : 40 }}
          onScrollToIndexFailed={({ index }) => setTimeout(() => list.current?.scrollToIndex({ index, animated: true }), 300)}
          renderItem={({ item: a }) => {
            const active = playing === a.ayah, sel = selected === a.ayah;
            return (
              <Pressable onPress={() => setSelected(sel ? null : a.ayah)} onLongPress={() => play(a.ayah)}
                style={{ backgroundColor: active ? t.goldSoft : sel ? t.soft : t.card, borderRadius: radius.md, padding: space.md, borderWidth: 1, borderColor: active ? t.gold : sel ? t.primary : t.border }}>
                <Txt style={{ fontFamily: font.quran, fontSize, lineHeight: fontSize * 1.9, textAlign: 'center' }}>{a.text} ﴿{arNum(a.ayah)}﴾</Txt>
                {sel && (
                  <View style={{ flexDirection: 'row', justifyContent: 'center', gap: 14, marginTop: 10 }}>
                    {iconBtn('play', () => play(a.ayah))}
                    {iconBtn('book-outline', () => showTafsir(a.ayah))}
                    {iconBtn(marks.has(a.ayah) ? 'bookmark' : 'bookmark-outline', () => mark(a.ayah), t.gold)}
                  </View>
                )}
                {!sel && marks.has(a.ayah) && <Ionicons name="bookmark" size={14} color={t.gold} style={{ position: 'absolute', top: 8, left: 8 }} />}
              </Pressable>
            );
          }} />
      )}

      {tafsir && (
        <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, maxHeight: '45%', backgroundColor: t.card, borderTopLeftRadius: radius.lg, borderTopRightRadius: radius.lg, borderTopWidth: 1, borderColor: t.border, padding: space.md, gap: 8 }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
            <Txt bold color={t.gold}>التفسير الميسّر · الآية {arNum(tafsir.ayah)}</Txt>
            <Pressable onPress={() => setTafsir(null)} hitSlop={12}><Ionicons name="close" size={22} color={t.muted} /></Pressable>
          </View>
          <ScrollView><Txt size={15} style={{ lineHeight: 26 }}>{tafsir.text}</Txt></ScrollView>
        </View>
      )}
    </SafeAreaView>
  );
}
