import React, { useEffect, useState } from 'react';
import { ActivityIndicator, Alert, Pressable, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useLocalSearchParams } from 'expo-router';
import { useTheme, space, radius } from '../../src/theme';
import { Btn, Card, Stepper, Txt } from '../../src/components/ui';
import { Session } from '../../src/components/Session';
import { SurahPicker } from '../../src/components/SurahPicker';
import { SURAHS, arNum, ayahCount } from '../../src/lib/surahs';
import { useSettings } from '../../src/store/settings';
import { Ayah, rangeAyahs } from '../../src/db/queries';
import { fetchAndCacheQuran } from '../../src/lib/quran-online';
import { playRange, reciterFor, stopAudio } from '../../src/lib/audio';

export default function Hifz() {
  const t = useTheme();
  const params = useLocalSearchParams<{ surah?: string }>();
  const riwaya = useSettings((s) => s.riwaya);
  const [surah, setSurah] = useState(1);
  const [from, setFrom] = useState(1);
  const [count, setCount] = useState(3);
  const [pick, setPick] = useState(false);
  const [busy, setBusy] = useState(false);
  const [listening, setListening] = useState<number | null>(null);
  const [run, setRun] = useState<Ayah[] | null>(null);

  useEffect(() => { if (params.surah) { setSurah(Number(params.surah)); setFrom(1); } }, [params.surah]);
  useEffect(() => () => { stopAudio(); }, []);

  const max = ayahCount(surah);
  const to = Math.min(max, from + count - 1);

  const start = async () => {
    setBusy(true);
    let rows = await rangeAyahs(riwaya, surah, from, to);
    if (!rows.length) {
      try { await fetchAndCacheQuran(riwaya); rows = await rangeAyahs(riwaya, surah, from, to); }
      catch { Alert.alert('يلزم الانترنت', 'حمّل المصحف مرة واحدة من الصفحة الرئيسية ثم يعمل بدون انترنت.'); }
    }
    setBusy(false);
    stopAudio(); setListening(null);
    if (rows.length) setRun(rows);
  };

  const listen = () => {
    if (listening != null) { stopAudio(); setListening(null); return; }
    playRange(surah, from, reciterFor(riwaya).id, { to, repeat: 3, onAyah: setListening })
      .catch(() => Alert.alert('تعذّر التشغيل', 'تأكد من الانترنت'));
  };

  if (run) return (
    <SafeAreaView style={{ flex: 1 }}>
      <Session ayahs={run} kind="hifz" onDone={() => { setFrom(Math.min(max, from + count)); setRun(null); }} />
    </SafeAreaView>
  );

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <ScrollView contentContainerStyle={{ padding: space.md, gap: space.md }}>
        <Txt bold size={26}>حفظ جديد</Txt>
        <Card style={{ gap: space.md }}>
          <Pressable onPress={() => setPick(true)} style={{ backgroundColor: t.soft, borderRadius: radius.md, padding: 16 }}>
            <Txt muted size={12}>السورة (اضغط للتغيير)</Txt>
            <Txt bold size={22} color={t.primary}>{SURAHS[surah - 1]}</Txt>
          </Pressable>
          <Stepper label="البداية من الآية" value={from} min={1} max={max} onChange={setFrom} />
          <Stepper label="عدد الآيات" value={count} min={1} max={10} onChange={setCount} />
          <Txt muted size={13}>من {arNum(from)} إلى {arNum(to)} · السورة {arNum(max)} آية</Txt>
          <Btn kind="soft" label={listening != null ? `إيقاف (الآية ${arNum(listening)})` : 'استمع للمقطع أولًا (٣ مرات لكل آية)'} onPress={listen} />
          {busy ? <ActivityIndicator color={t.primary} /> : <Btn label="ابدأ التسميع" onPress={start} />}
        </Card>
        <Card style={{ gap: 6 }}>
          <Txt bold>طريقة الحفظ</Txt>
          <Txt muted size={13} style={{ lineHeight: 22 }}>١. استمع للمقطع وردّده مع القارئ.{'\n'}٢. ابدأ التسميع: الآيات مخفية، اقرأها من حفظك ثم المس البطاقة للتحقق.{'\n'}٣. قيّم تسميعك، وسيذكّرك التطبيق بمراجعتها في الوقت المناسب.</Txt>
        </Card>
      </ScrollView>
      <SurahPicker visible={pick} current={surah} onClose={() => setPick(false)} onPick={(s) => { setSurah(s); setFrom(1); setPick(false); }} />
    </SafeAreaView>
  );
}
