import React from 'react';
import { Tabs } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useTheme, font } from '../../src/theme';

const icon = (name: any) => ({ color, size }: any) => <Ionicons name={name} color={color} size={size} />;

export default function TabsLayout() {
  const t = useTheme();
  return (
    <Tabs screenOptions={{
      headerShown: false, tabBarActiveTintColor: t.primary, tabBarInactiveTintColor: t.muted,
      tabBarStyle: { backgroundColor: t.card, borderTopColor: t.border, height: 64, paddingBottom: 8, paddingTop: 6 },
      tabBarLabelStyle: { fontFamily: font.ui, fontSize: 12 },
      sceneStyle: { backgroundColor: t.bg },
    }}>
      <Tabs.Screen name="index" options={{ title: 'الرئيسية', tabBarIcon: icon('home-outline') }} />
      <Tabs.Screen name="mushaf" options={{ title: 'المصحف', tabBarIcon: icon('book-outline') }} />
      <Tabs.Screen name="hifz" options={{ title: 'الحفظ', tabBarIcon: icon('mic-outline') }} />
      <Tabs.Screen name="review" options={{ title: 'المراجعة', tabBarIcon: icon('repeat-outline') }} />
      <Tabs.Screen name="profile" options={{ title: 'حسابي', tabBarIcon: icon('person-outline') }} />
    </Tabs>
  );
}
