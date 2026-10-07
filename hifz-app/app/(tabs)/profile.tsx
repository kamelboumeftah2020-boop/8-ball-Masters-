import React, { useCallback, useState } from 'react';
import { Pressable, Platform, ScrollView, Switch, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect, useRouter } from 'expo-router';
import * as Notifications from 'expo-notifications';
import { useTheme, space, radius } from '../../src/theme';
import { Btn, Card, Segmented, Stepper, Txt } from '../../src/components/ui';
import { useSettings } from '../../src/store/settings';
import { stats, allBookmarks } from '../../src/db/queries';
import { arNum, SURAHS } from '../../src/lib/surahs';

async function scheduleDaily(hour: number | null) {
  await Notifications.cancelAllScheduledNotificationsAsync();
  if (hour === null) return;
  const perm = await Notifications.requestPermissionsAsync();
  if (!perm.granted) return;
  if (Platform.OS === 'android')
    await Notifications.setNotificationChannelAsync('daily', { name: 'تذكير الحفظ', importance: Notifications.AndroidImportance.DEFAULT });
  await Notifications.scheduleNotificationAsync({
    content: { title: 'وقت الحفظ', body: 'ورد اليوم ينتظرك، ولو بآيات قليلة.' },
    trigger: { type: Notifications.SchedulableTriggerInputTypes.DAILY, hour, minute: 0, channelId: 'daily' },
  });
}

export default function Profile() {
  const t = useTheme();
  const { riwaya, dark, fontSize, reminderHour, set } = useSettings();
  const router = useRouter();
  const [s, setS] = useState({ memorized: 0, learning: 0, total: 0, streak: 0 } as any);
  const [marks, setMarks] = useState<{ surah: number; ayah: number; text: string | null }[]>([]);
  useFocusEffect(useCallback(() => { stats(riwaya).then(setS); allBookmarks(riwaya).then(setMarks); }, [riwaya]));

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <ScrollView contentContainerStyle={{ padding: space.md, gap: space.md }}>
        <Txt bold size={26}>حسابي</Txt>
        <Card style={{ gap: 6 }}>
          <Txt muted>الإحصائيات · {riwaya === 'hafs' ? 'حفص' : 'ورش'}</Txt>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginTop: 8 }}>
            {[['متقن', s.memorized], ['قيد الحفظ', s.learning], ['أيام متتالية', s.streak]].map(([l, v]) => (
              <View key={l as string} style={{ alignItems: 'center', flex: 1 }}>
                <Txt bold size={26} style={{ textAlign: 'center' }}>{arNum(v as number)}</Txt>
                <Txt muted size={12} style={{ textAlign: 'center' }}>{l}</Txt>
              </View>
            ))}
          </View>
        </Card>

        <Card style={{ gap: 8 }}>
          <Txt bold>العلامات المرجعية</Txt>
          {marks.length === 0 ? <Txt muted size={13}>اضغط على آية في المصحف ثم على أيقونة العلامة لحفظها هنا.</Txt> :
            marks.map((m) => (
              <Pressable key={`${m.surah}:${m.ayah}`} onPress={() => router.push(`/surah/${m.surah}`)}
                style={{ backgroundColor: t.soft, borderRadius: radius.sm, padding: 10 }}>
                <Txt bold size={13} color={t.gold}>{SURAHS[m.surah - 1]} · {arNum(m.ayah)}</Txt>
                {m.text ? <Txt size={13} numberOfLines={1}>{m.text}</Txt> : null}
              </Pressable>
            ))}
        </Card>

        <Card style={{ gap: space.md }}>
          <Txt bold>الرواية</Txt>
          <Segmented value={riwaya} onChange={(v) => set({ riwaya: v })} options={[{ v: 'hafs', label: 'حفص' }, { v: 'warsh', label: 'ورش' }]} />
          <Txt bold>المظهر</Txt>
          <Segmented value={dark === null ? 'auto' : dark ? 'dark' : 'light'}
            onChange={(v) => set({ dark: v === 'auto' ? null : v === 'dark' })}
            options={[{ v: 'auto', label: 'تلقائي' }, { v: 'light', label: 'فاتح' }, { v: 'dark', label: 'داكن' }]} />
          <Stepper label="حجم خط القرآن" value={fontSize} min={22} max={48} onChange={(n) => set({ fontSize: n })} />
        </Card>

        <Card style={{ gap: space.md }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
            <Txt bold>تذكير يومي</Txt>
            <Switch value={reminderHour !== null} trackColor={{ true: t.primary }}
              onValueChange={async (on) => { const h = on ? 20 : null; await set({ reminderHour: h }); await scheduleDaily(h); }} />
          </View>
          {reminderHour !== null && (
            <Stepper label="الساعة" value={reminderHour} min={0} max={23} onChange={async (h) => { await set({ reminderHour: h }); await scheduleDaily(h); }} />
          )}
        </Card>
        <Txt muted size={12} style={{ textAlign: 'center' }}>النص: fawazahmed0/quran-api · الصوت: everyayah.com · التفسير الميسّر: spa5k/tafsir_api</Txt>
      </ScrollView>
    </SafeAreaView>
  );
}
