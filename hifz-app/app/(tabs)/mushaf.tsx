import React, { useCallback, useState } from 'react';
import { FlatList, Pressable, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect, useRouter } from 'expo-router';
import { useTheme, space, radius, font } from '../../src/theme';
import { Txt, Empty } from '../../src/components/ui';
import { SURAHS, arNum, ayahCount } from '../../src/lib/surahs';
import { useSettings } from '../../src/store/settings';
import { surahProgress } from '../../src/db/queries';

export default function Mushaf() {
  const t = useTheme(); const router = useRouter();
  const riwaya = useSettings((s) => s.riwaya);
  const [q, setQ] = useState('');
  const [prog, setProg] = useState<Record<number, number>>({});
  useFocusEffect(useCallback(() => { surahProgress(riwaya).then(setProg); }, [riwaya]));

  const data = SURAHS.map((n, i) => ({ n, id: i + 1 })).filter((x) => x.n.includes(q.trim()) || String(x.id) === q.trim());
  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <View style={{ padding: space.md, gap: 12 }}>
        <Txt bold size={26}>المصحف · {riwaya === 'hafs' ? 'حفص' : 'ورش'}</Txt>
        <TextInput value={q} onChangeText={setQ} placeholder="ابحث عن سورة" placeholderTextColor={t.muted}
          style={{ backgroundColor: t.card, borderRadius: radius.md, paddingHorizontal: 16, paddingVertical: 12, fontFamily: font.ui, textAlign: 'right', color: t.text, borderWidth: 1, borderColor: t.border }} />
      </View>
      <FlatList data={data} keyExtractor={(x) => String(x.id)} contentContainerStyle={{ paddingHorizontal: space.md, paddingBottom: 24, gap: 8 }}
        ListEmptyComponent={<Empty title="لا توجد نتائج" body="جرّب اسمًا آخر للسورة." />}
        renderItem={({ item }) => {
          const c = ayahCount(item.id); const p = prog[item.id] ?? 0;
          return (
            <Pressable onPress={() => router.push(`/surah/${item.id}`)}
              style={{ flexDirection: 'row', alignItems: 'center', gap: 14, backgroundColor: t.card, borderRadius: radius.md, padding: 14, borderWidth: 1, borderColor: t.border }}>
              <View style={{ width: 38, height: 38, borderRadius: 19, backgroundColor: t.soft, alignItems: 'center', justifyContent: 'center' }}>
                <Txt bold size={13} color={t.primary} style={{ textAlign: 'center' }}>{arNum(item.id)}</Txt>
              </View>
              <View style={{ flex: 1 }}>
                <Txt bold size={17}>{item.n}</Txt>
                <Txt muted size={12}>{arNum(c)} آية</Txt>
              </View>
              {c > 0 && p > 0 && <Txt size={12} color={t.gold} bold>{arNum(Math.round((p / c) * 100))}٪</Txt>}
            </Pressable>
          );
        }} />
    </SafeAreaView>
  );
}
