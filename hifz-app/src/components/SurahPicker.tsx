import React from 'react';
import { FlatList, Modal, Pressable, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useTheme, space, radius } from '../theme';
import { Txt } from './ui';
import { SURAHS, arNum, ayahCount } from '../lib/surahs';
import type { Riwaya } from '../db/queries';

export function SurahPicker({ visible, current, onPick, onClose, marks, riwaya = 'hafs' }: {
  visible: boolean; riwaya?: Riwaya; current?: number; onPick: (surah: number) => void; onClose: () => void; marks?: Set<number>;
}) {
  const t = useTheme();
  return (
    <Modal visible={visible} animationType="slide" onRequestClose={onClose}>
      <SafeAreaView style={{ flex: 1, backgroundColor: t.bg }}>
        <View style={{ padding: space.md, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
          <Txt bold size={22}>اختر السورة</Txt>
          <Pressable onPress={onClose} hitSlop={12}><Txt bold color={t.primary}>إغلاق</Txt></Pressable>
        </View>
        <FlatList data={SURAHS} keyExtractor={(_, i) => String(i)} contentContainerStyle={{ padding: space.md, gap: 6 }}
          initialNumToRender={30}
          renderItem={({ item, index }) => (
            <Pressable onPress={() => onPick(index + 1)}
              style={{ padding: 14, borderRadius: radius.md, backgroundColor: index + 1 === current ? t.soft : t.card, flexDirection: 'row', justifyContent: 'space-between' }}>
              <Txt bold>{arNum(index + 1)} · {item}</Txt>
              <Txt muted size={12}>{marks?.has(index + 1) ? '✓ ' : ''}{arNum(ayahCount(index + 1, riwaya))} آية</Txt>
            </Pressable>
          )} />
      </SafeAreaView>
    </Modal>
  );
}
