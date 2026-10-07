import React, { useCallback, useState } from 'react';
import { View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from 'expo-router';
import { Session } from '../../src/components/Session';
import { Empty, Txt } from '../../src/components/ui';
import { space } from '../../src/theme';
import { useSettings } from '../../src/store/settings';
import { Ayah, dueAyahs } from '../../src/db/queries';

export default function Review() {
  const riwaya = useSettings((s) => s.riwaya);
  const [batch, setBatch] = useState<Ayah[] | null>(null);
  const [n, setN] = useState(0);
  const load = useCallback(() => { dueAyahs(riwaya, 6).then(setBatch); }, [riwaya]);
  useFocusEffect(load);

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['top']}>
      <View style={{ padding: space.md }}><Txt bold size={26}>المراجعة</Txt></View>
      {batch && batch.length > 0
        ? <Session key={n} ayahs={batch} kind="review" onDone={() => { setN(n + 1); load(); }} />
        : <Empty title="أحسنت، لا مراجعة الآن" body="ستظهر هنا الآيات عندما يحين موعد مراجعتها." />}
    </SafeAreaView>
  );
}
