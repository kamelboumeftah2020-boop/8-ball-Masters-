import React, { memo, useMemo } from 'react';
import { I18nManager, Pressable, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTheme, font, quranFont } from '../theme';
import { SURAHS, arNum, ayahCount } from '../lib/surahs';
import { BASMALA, LINES, MAyah, getMushaf, hizbLabel, pageBlocks } from '../lib/mushaf';
import type { Riwaya } from '../db/queries';

export const TOP_BAR = 54;
export const BOTTOM_BAR = 50;
// صف من اليمين إلى اليسار مهما كانت حالة RTL في النظام
const row = I18nManager.isRTL ? 'row' : 'row-reverse';

type Props = {
  riwaya: Riwaya; page: number; width: number; height: number;
  selected?: string | null; playing?: string | null; bookmarked?: boolean;
  onAyah: (a: MAyah) => void; onBookmark: (page: number) => void; onSurahPress: () => void;
};

export const keyOf = (s: number, a: number) => `${s}:${a}`;

function MushafPageInner({ riwaya, page, width, height, selected, playing, bookmarked, onAyah, onBookmark, onSurahPress }: Props) {
  const t = useTheme();
  const blocks = useMemo(() => pageBlocks(riwaya, page), [riwaya, page]);
  const first = getMushaf(riwaya).pages[page]?.[0];
  const opening = page <= 2; // الفاتحة وأول البقرة: نص قصير في وسط الصفحة
  const padX = opening ? width * 0.13 : 14;
  const areaW = width - padX * 2;
  const areaH = height - TOP_BAR - BOTTOM_BAR;
  const lineH = areaH / LINES;
  const fs = Math.min((opening ? width - 28 : areaW) * 0.0575, lineH * 0.6, 30);
  const qf = quranFont(riwaya);

  // في الصفحتين الأوليين نوسّط الكتل عموديًا
  const usedFrom = Math.min(...blocks.map((b) => b.line));
  const usedTo = Math.max(...blocks.map((b) => (b.kind === 'text' ? b.line + b.lines - 1 : b.line)));
  const shift = opening ? ((LINES - (usedTo - usedFrom + 1)) / 2 - (usedFrom - 1)) * lineH : 0;
  const top = (line: number) => (line - 1) * lineH + shift;

  return (
    <View style={{ width, height, backgroundColor: t.page }}>
      <View style={{ height: TOP_BAR, flexDirection: row, alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16 }}>
        <Pressable onPress={onSurahPress} hitSlop={8}
          style={{ borderWidth: 1.5, borderColor: t.frame, backgroundColor: t.frameSoft, borderRadius: 22, paddingHorizontal: 18, paddingVertical: 4 }}>
          <Text style={{ fontFamily: font.bold, fontSize: 16, color: t.ink }}>سورة {first ? SURAHS[first.surah - 1] : ''}</Text>
        </Pressable>
        <Pressable onPress={() => onBookmark(page)} hitSlop={12}>
          <Ionicons name={bookmarked ? 'bookmark' : 'bookmark-outline'} size={30} color={t.frame} />
        </Pressable>
      </View>

      <View style={{ height: areaH, marginHorizontal: padX }}>
        {blocks.map((b, i) => {
          if (b.kind === 'title') return (
            <View key={i} style={{ position: 'absolute', top: top(b.line) + lineH * 0.08, height: lineH * 0.84, left: -padX + 14, right: -padX + 14, borderWidth: 1.5, borderColor: t.frame, borderRadius: 6, padding: 2 }}>
              <View style={{ flex: 1, borderWidth: 0.8, borderColor: t.frame, borderRadius: 4, backgroundColor: t.frameSoft, flexDirection: row, alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 14 }}>
                <Text style={{ fontFamily: font.ui, fontSize: 11, color: t.frame }}>{arNum(b.surah)}</Text>
                <Text style={{ fontFamily: font.bold, fontSize: Math.min(18, lineH * 0.42), color: t.ink }}>سورة {SURAHS[b.surah - 1]}</Text>
                <Text style={{ fontFamily: font.ui, fontSize: 11, color: t.frame }}>{arNum(ayahCount(b.surah, riwaya))} آية</Text>
              </View>
            </View>
          );
          if (b.kind === 'basmala') return (
            <Text key={i} style={{ position: 'absolute', top: top(b.line), left: 0, right: 0, height: lineH, lineHeight: lineH, fontFamily: qf, fontSize: fs, color: t.ink, textAlign: 'center' }}>
              {BASMALA[riwaya]}
            </Text>
          );
          return (
            <Text key={i} adjustsFontSizeToFit minimumFontScale={0.6} numberOfLines={b.lines}
              style={{ position: 'absolute', top: top(b.line), left: 0, right: 0, height: b.lines * lineH, lineHeight: lineH, fontFamily: qf, fontSize: fs, color: t.ink, textAlign: opening ? 'center' : 'justify', writingDirection: 'rtl' }}>
              {b.ayahs.map((a) => {
                const k = keyOf(a.surah, a.ayah);
                const bg = k === playing ? t.play : k === selected ? t.hl : undefined;
                return <Text key={k} onPress={() => onAyah(a)} style={bg ? { backgroundColor: bg } : undefined}>{a.text} </Text>;
              })}
            </Text>
          );
        })}
      </View>

      <View style={{ height: BOTTOM_BAR, flexDirection: row, alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 20 }}>
        <Text style={{ fontFamily: font.bold, fontSize: 14, color: t.ink }}>الجزء {arNum(first?.juz ?? 1)}</Text>
        <View style={{ borderWidth: 1.5, borderColor: t.frame, backgroundColor: t.frameSoft, borderRadius: 20, paddingHorizontal: 22, paddingVertical: 2 }}>
          <Text style={{ fontFamily: font.bold, fontSize: 15, color: t.ink }}>{arNum(page)}</Text>
        </View>
        <Text style={{ fontFamily: font.bold, fontSize: 14, color: t.ink }}>{hizbLabel(first?.quarter ?? 1, arNum)}</Text>
      </View>
    </View>
  );
}

export const MushafPage = memo(MushafPageInner);
