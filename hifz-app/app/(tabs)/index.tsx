import React, { useCallback, useState } from 'react';
import { ScrollView, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect, useRouter } from 'expo-router';
import { useTheme, space, radius } from '../../src/theme';
import { Btn, Card, Segmented, Txt, Empty } from '../../src/components/ui';
import { useSettings } from '../../src/store/settings';
import { OfflineManager } from '../../src/components/OfflineManager';
import { stats } from '../../src/db/queries';
import { arNum } from '../../src/lib/surahs';

export default function Home() {
  const t = useTheme(); const router = useRouter();
  const { riwaya, set } = useSettings();
  const [s, setS] = useState({ due: 0, memorized: 0, learning: 0, total: 0, todayCount: 0, streak: 0 });
  const refresh = useCallback(() => { stats(riwaya).then(setS); }, [riwaya]);
  useFocusEffect(refresh);

  const pct = s.total ? Math.round((s.memorized / s.total) * 100) : 0;
  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <ScrollView contentContainerStyle={{ padding: space.md, gap: space.md }}>
        <View style={{ gap: 4, paddingTop: space.sm }}>
          <Txt muted>السلام عليكم</Txt>
          <Txt bold size={28}>ورد اليوم</Txt>
        </View>
        <Segmented value={riwaya} onChange={(v) => set({ riwaya: v })} options={[{ v: 'hafs', label: 'رواية حفص' }, { v: 'warsh', label: 'رواية ورش' }]} />

        {s.total === 0 ? (
          <Card><Empty title="جاري تجهيز المصحف" body={'يتم تحميل نص هذه الرواية من الانترنت (مرة واحدة فقط). إن لم يكتمل، اضغط زر التحميل في الأسفل.'} /></Card>
        ) : (
          <>
            <View style={{ backgroundColor: t.primary, borderRadius: radius.lg, padding: space.lg, gap: 14 }}>
              <Txt color={t.onPrimary} style={{ opacity: 0.8 }}>المحفوظ المتقن</Txt>
              <Txt bold size={44} color={t.onPrimary}>{arNum(s.memorized)} <Txt size={16} color={t.onPrimary}>من {arNum(s.total)} آية</Txt></Txt>
              <View style={{ height: 8, backgroundColor: 'rgba(255,255,255,0.25)', borderRadius: 4, overflow: 'hidden', flexDirection: 'row-reverse' }}>
                <View style={{ width: `${pct}%`, backgroundColor: t.gold === '#A8802F' ? '#E9CF8E' : t.gold, borderRadius: 4 }} />
              </View>
            </View>

            <View style={{ flexDirection: 'row', gap: space.sm }}>
              {[['للمراجعة اليوم', s.due], ['أيام متتالية', s.streak], ['آيات اليوم', s.todayCount]].map(([l, v]) => (
                <Card key={l as string} style={{ flex: 1, alignItems: 'center', gap: 2 }}>
                  <Txt bold size={26} style={{ textAlign: 'center' }}>{arNum(v as number)}</Txt>
                  <Txt muted size={12} style={{ textAlign: 'center' }}>{l}</Txt>
                </Card>
              ))}
            </View>

            <Btn label={s.due > 0 ? `ابدأ المراجعة (${arNum(s.due)})` : 'لا مراجعة اليوم'} disabled={s.due === 0} onPress={() => router.push('/review')} />
            <Btn kind="soft" label="حفظ جديد" onPress={() => router.push('/hifz')} />
          </>
        )}
        <OfflineManager riwaya={riwaya} onTextReady={refresh} />
      </ScrollView>
    </SafeAreaView>
  );
}
