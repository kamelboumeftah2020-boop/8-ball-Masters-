import React, { useCallback, useState } from 'react';
import { FlatList, Pressable, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect, useRouter } from 'expo-router';
import { useTheme, space, radius, font } from '../../src/theme';
import { Txt, Empty, Segmented } from '../../src/components/ui';
import { SURAHS, arNum, ayahCount } from '../../src/lib/surahs';
import { getMushaf } from '../../src/lib/mushaf';
import { useSettings } from '../../src/store/settings';
import { surahProgress } from '../../src/db/queries';

type Item = { id: number; title: string; sub: string; page: number; pct?: number };

export default function Mushaf() {
  const t = useTheme(); const router = useRouter();
  const riwaya = useSettings((s) => s.riwaya);
  const lastPage = useSettings((s) => s.lastPage[s.riwaya] ?? 1);
  const [tab, setTab] = useState<'surah' | 'juz'>('surah');
  const [q, setQ] = useState('');
  const [prog, setProg] = useState<Record<number, number>>({});
  useFocusEffect(useCallback(() => { surahProgress(riwaya).then(setProg); }, [riwaya]));

  const m = getMushaf(riwaya);
  const open = (page: number) => router.push(`/page/${page}`);
  const lastSurah = m.pages[lastPage]?.[0]?.surah ?? 1;

  const surahs: Item[] = SURAHS.map((n, i) => {
    const c = ayahCount(i + 1, riwaya);
    return { id: i + 1, title: n, sub: `${arNum(c)} آية · صفحة ${arNum(m.surahPage[i])}`, page: m.surahPage[i], pct: prog[i + 1] ? Math.round((prog[i + 1] / c) * 100) : 0 };
  }).filter((x) => x.title.includes(q.trim()) || String(x.id) === q.trim());
  const juzs: Item[] = m.juzPage.map((p, i) => {
    const a = m.pages[p][0];
    return { id: i + 1, title: `الجزء ${arNum(i + 1)}`, sub: `${SURAHS[a.surah - 1]} · الآية ${arNum(a.ayah)} · صفحة ${arNum(p)}`, page: p };
  });

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <View style={{ padding: space.md, gap: 12 }}>
        <Txt bold size={26}>المصحف · {riwaya === 'hafs' ? 'حفص' : 'ورش'}</Txt>
        <Pressable onPress={() => open(lastPage)} style={{ backgroundColor: t.primary, borderRadius: radius.md, padding: 16, gap: 2 }}>
          <Txt color={t.onPrimary} style={{ opacity: 0.85 }} size={13}>متابعة القراءة</Txt>
          <Txt bold size={20} color={t.onPrimary}>صفحة {arNum(lastPage)} · سورة {SURAHS[lastSurah - 1]}</Txt>
        </Pressable>
        <Segmented value={tab} onChange={setTab} options={[{ v: 'surah', label: 'السور' }, { v: 'juz', label: 'الأجزاء' }]} />
        {tab === 'surah' && (
          <TextInput value={q} onChangeText={setQ} placeholder="ابحث عن سورة" placeholderTextColor={t.muted}
            style={{ backgroundColor: t.card, borderRadius: radius.md, paddingHorizontal: 16, paddingVertical: 10, fontFamily: font.ui, textAlign: 'right', color: t.text, borderWidth: 1, borderColor: t.border }} />
        )}
      </View>
      <FlatList data={tab === 'surah' ? surahs : juzs} keyExtractor={(x) => `${tab}${x.id}`} contentContainerStyle={{ paddingHorizontal: space.md, paddingBottom: 24, gap: 8 }}
        ListEmptyComponent={<Empty title="لا توجد نتائج" body="جرّب اسمًا آخر للسورة." />}
        renderItem={({ item }) => (
          <Pressable onPress={() => open(item.page)}
            style={{ flexDirection: 'row', alignItems: 'center', gap: 14, backgroundColor: t.card, borderRadius: radius.md, padding: 14, borderWidth: 1, borderColor: t.border }}>
            <View style={{ width: 38, height: 38, borderRadius: 19, backgroundColor: t.soft, alignItems: 'center', justifyContent: 'center' }}>
              <Txt bold size={13} color={t.primary} style={{ textAlign: 'center' }}>{arNum(item.id)}</Txt>
            </View>
            <View style={{ flex: 1 }}>
              <Txt bold size={17}>{item.title}</Txt>
              <Txt muted size={12}>{item.sub}</Txt>
            </View>
            {!!item.pct && <Txt size={12} color={t.gold} bold>{arNum(item.pct)}٪</Txt>}
          </Pressable>
        )} />
    </SafeAreaView>
  );
}
