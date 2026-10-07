import React, { useEffect, useState } from 'react';
import { I18nManager, View, ActivityIndicator } from 'react-native';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import * as SplashScreen from 'expo-splash-screen';
import * as Notifications from 'expo-notifications';
import { useFonts, Cairo_600SemiBold, Cairo_700Bold } from '@expo-google-fonts/cairo';
import { AmiriQuran_400Regular } from '@expo-google-fonts/amiri-quran';
import { getDb } from '../src/db';
import { useSettings } from '../src/store/settings';
import { useTheme, palettes } from '../src/theme';
import { fetchAndCacheQuran, isQuranCached } from '../src/lib/quran-online';

// RTL يُطبَّق بعد أول إعادة تشغيل للتطبيق
if (!I18nManager.isRTL) { I18nManager.allowRTL(true); I18nManager.forceRTL(true); }

SplashScreen.preventAutoHideAsync().catch(() => {});
Notifications.setNotificationHandler({
  handleNotification: async () => ({ shouldShowAlert: true, shouldPlaySound: true, shouldSetBadge: false }),
});

export default function Root() {
  const [fontsOk, fontErr] = useFonts({ Cairo_600SemiBold, Cairo_700Bold, AmiriQuran_400Regular });
  const load = useSettings((s) => s.load);
  const ready = useSettings((s) => s.ready);
  const riwaya = useSettings((s) => s.riwaya);
  const [dbOk, setDbOk] = useState(false);
  const t = useTheme();

  useEffect(() => { (async () => { await getDb(); await load(); setDbOk(true); })(); }, []);

  // أول تشغيل: نجلب نص الرواية المختارة في الخلفية إن لم يكن محمّلًا
  useEffect(() => {
    if (!dbOk) return;
    isQuranCached(riwaya).then((ok) => { if (!ok) fetchAndCacheQuran(riwaya).catch(() => {}); });
  }, [dbOk, riwaya]);

  const done = (fontsOk || !!fontErr) && ready && dbOk;
  useEffect(() => { if (done) SplashScreen.hideAsync().catch(() => {}); }, [done]);

  if (!done) {
    return <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: t.bg }}><ActivityIndicator color={t.primary} /></View>;
  }
  return (
    <>
      <StatusBar style={t === palettes.dark ? 'light' : 'dark'} />
      <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: t.bg }, animation: 'slide_from_left' }} />
    </>
  );
}
