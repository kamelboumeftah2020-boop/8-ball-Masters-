import React from 'react';
import { Pressable, Text, View, StyleSheet, ViewStyle } from 'react-native';
import { useTheme, radius, space, font } from '../theme';

export function Txt({ children, size = 15, bold, muted, color, style, numberOfLines }: any) {
  const t = useTheme();
  return (
    <Text numberOfLines={numberOfLines} style={[{ fontFamily: bold ? font.bold : font.ui, fontSize: size, color: color ?? (muted ? t.muted : t.text), textAlign: 'right', writingDirection: 'rtl' }, style]}>
      {children}
    </Text>
  );
}

export function Btn({ label, onPress, kind = 'primary', style, disabled }: { label: string; onPress: () => void; kind?: 'primary' | 'soft' | 'ghost'; style?: ViewStyle; disabled?: boolean }) {
  const t = useTheme();
  const bg = kind === 'primary' ? t.primary : kind === 'soft' ? t.soft : 'transparent';
  const fg = kind === 'primary' ? t.onPrimary : t.primary;
  return (
    <Pressable onPress={onPress} disabled={disabled}
      style={({ pressed }) => [{ backgroundColor: bg, borderRadius: radius.md, paddingVertical: 14, paddingHorizontal: 20, alignItems: 'center', opacity: disabled ? 0.4 : pressed ? 0.85 : 1, borderWidth: kind === 'ghost' ? 1 : 0, borderColor: t.border }, style]}>
      <Txt bold color={fg} size={16}>{label}</Txt>
    </Pressable>
  );
}

export function Card({ children, style }: { children: React.ReactNode; style?: ViewStyle }) {
  const t = useTheme();
  return <View style={[{ backgroundColor: t.card, borderRadius: radius.md, padding: space.md, borderWidth: StyleSheet.hairlineWidth, borderColor: t.border }, style]}>{children}</View>;
}

export function Segmented<T extends string>({ value, options, onChange }: { value: T; options: { v: T; label: string }[]; onChange: (v: T) => void }) {
  const t = useTheme();
  return (
    <View style={{ flexDirection: 'row', backgroundColor: t.soft, borderRadius: radius.lg, padding: 4 }}>
      {options.map((o) => (
        <Pressable key={o.v} onPress={() => onChange(o.v)}
          style={{ flex: 1, paddingVertical: 10, borderRadius: radius.lg, backgroundColor: value === o.v ? t.primary : 'transparent', alignItems: 'center' }}>
          <Txt bold color={value === o.v ? t.onPrimary : t.primary}>{o.label}</Txt>
        </Pressable>
      ))}
    </View>
  );
}

export function Stepper({ value, min, max, onChange, label }: { value: number; min: number; max: number; onChange: (n: number) => void; label: string }) {
  const t = useTheme();
  const b = (txt: string, d: number) => (
    <Pressable onPress={() => onChange(Math.min(max, Math.max(min, value + d)))}
      style={{ width: 44, height: 44, borderRadius: 22, backgroundColor: t.soft, alignItems: 'center', justifyContent: 'center' }}>
      <Txt bold size={22} color={t.primary} style={{ textAlign: 'center' }}>{txt}</Txt>
    </Pressable>
  );
  return (
    <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
      <Txt muted>{label}</Txt>
      <View style={{ flexDirection: 'row', alignItems: 'center', gap: 14 }}>
        {b('+', 1)}<Txt bold size={20} style={{ minWidth: 36, textAlign: 'center' }}>{value}</Txt>{b('−', -1)}
      </View>
    </View>
  );
}

export function Empty({ title, body }: { title: string; body: string }) {
  return (
    <View style={{ padding: space.lg, gap: 8, alignItems: 'center' }}>
      <Txt bold size={18} style={{ textAlign: 'center' }}>{title}</Txt>
      <Txt muted style={{ textAlign: 'center', lineHeight: 26 }}>{body}</Txt>
    </View>
  );
}
