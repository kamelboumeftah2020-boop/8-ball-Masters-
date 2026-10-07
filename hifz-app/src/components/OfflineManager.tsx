import React, { useCallback, useEffect, useState } from 'react';
import { View, Alert } from 'react-native';
import { useTheme, radius } from '../theme';
import { Txt, Btn, Card } from './ui';
import { SurahPicker } from './SurahPicker';
import { reciterFor, downloadSurahForOffline, downloadedSurahs } from '../lib/audio';
import { SURAHS, JUZ_AMMA, arNum } from '../lib/surahs';
import type { Riwaya } from '../db/queries';

export function OfflineManager({ riwaya }: { riwaya: Riwaya }) {
  const t = useTheme();
  const rec = reciterFor(riwaya);
  const [audioMsg, setAudioMsg] = useState<string | null>(null);
  const [have, setHave] = useState<Set<number>>(new Set());
  const [pick, setPick] = useState(false);

  const refresh = useCallback(() => {
    downloadedSurahs(rec.id).then(setHave);
  }, [riwaya, rec.id]);
  useEffect(refresh, [refresh]);

  const downloadAudio = async (surahs: number[]) => {
    const todo = surahs.filter((s) => !have.has(s));
    if (!todo.length) { Alert.alert('محمّل', 'هذه الصوتيات محمّلة مسبقًا'); return; }
    let failed = 0;
    for (const [i, s] of todo.entries()) {
      try {
        await downloadSurahForOffline(s, rec.id, (d, n) =>
          setAudioMsg(`${SURAHS[s - 1]} ${arNum(d)}/${arNum(n)}${todo.length > 1 ? ` · سورة ${arNum(i + 1)} من ${arNum(todo.length)}` : ''}`));
      } catch { failed++; }
    }
    setAudioMsg(null);
    refresh();
    Alert.alert(failed ? 'اكتمل جزئيًا' : 'تم', failed ? `تعذّر تحميل ${arNum(failed)} سورة، أعد المحاولة لاحقًا` : 'الصوتيات جاهزة بدون انترنت');
  };

  const busy = audioMsg !== null;
  return (
    <Card style={{ gap: 12 }}>
      <Txt bold size={18}>الصوتيات بدون انترنت</Txt>
      <Txt muted size={13}>المصحف مضمّن ويعمل بدون انترنت. التلاوة تُشغَّل من الانترنت، ويمكنك تحميلها هنا للاستماع بدون اتصال.</Txt>

      <View style={{ backgroundColor: t.soft, borderRadius: radius.md, padding: 12, gap: 8 }}>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
          <Txt bold>الصوتيات - {rec.name}</Txt>
          <Txt size={12} color={have.size ? t.good : t.muted}>{arNum(have.size)} / ١١٤ سورة</Txt>
        </View>
        <Txt muted size={12}>{busy ? `جاري التحميل: ${audioMsg}` : 'جزء عمّ ≈ 25MB · البقرة ≈ 60MB'}</Txt>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <Btn kind="ghost" style={{ flex: 1 }} disabled={busy} label="جزء عمّ" onPress={() => downloadAudio(JUZ_AMMA)} />
          <Btn kind="ghost" style={{ flex: 1 }} disabled={busy} label="اختر سورة" onPress={() => setPick(true)} />
        </View>
      </View>
      <SurahPicker visible={pick} marks={have} onClose={() => setPick(false)} onPick={(s) => { setPick(false); downloadAudio([s]); }} />
    </Card>
  );
}
