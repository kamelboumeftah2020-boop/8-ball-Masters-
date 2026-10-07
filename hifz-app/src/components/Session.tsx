import React, { useState } from 'react';
import { ScrollView, Pressable, View } from 'react-native';
import { useTheme, radius, space, font } from '../theme';
import { useSettings } from '../store/settings';
import { Ayah, rate } from '../db/queries';
import { Btn, Txt } from './ui';
import { arNum, SURAHS } from '../lib/surahs';
import { playAyah, reciterFor, stopAudio } from '../lib/audio';

// الحالات: 0 مخفية، 1 تلميح (أول كلمة)، 2 ظاهرة
export function Session({ ayahs, kind, onDone }: { ayahs: Ayah[]; kind: 'hifz' | 'review'; onDone: () => void }) {
  const t = useTheme();
  const { fontSize, riwaya } = useSettings();
  const [lv, setLv] = useState<Record<string, number>>({});
  const key = (a: Ayah) => `${a.surah}:${a.ayah}`;
  const allShown = ayahs.every((a) => lv[key(a)] === 2);
  const step = (a: Ayah) => setLv((p) => ({ ...p, [key(a)]: Math.min(2, (p[key(a)] ?? 0) + 1) }));
  const showAll = () => setLv(Object.fromEntries(ayahs.map((a) => [key(a), 2])));
  const finish = async (r: 0 | 1 | 2) => { stopAudio(); await rate(riwaya, ayahs, r, kind); onDone(); };
  const listen = (a: Ayah) => playAyah(a.surah, a.ayah, reciterFor(riwaya).id).catch(() => {});

  return (
    <View style={{ flex: 1 }}>
      <ScrollView contentContainerStyle={{ padding: space.md, gap: space.md, paddingBottom: 140 }}>
        <Txt muted style={{ textAlign: 'center' }}>اقرأ الآية من حفظك، ثم المس البطاقة لتتحقق · اضغط مطولًا للاستماع</Txt>
        {ayahs.map((a) => {
          const l = lv[key(a)] ?? 0;
          const first = a.text.split(' ')[0];
          return (
            <Pressable key={key(a)} onPress={() => step(a)} onLongPress={() => listen(a)}
              style={{ backgroundColor: t.card, borderRadius: radius.md, padding: space.md, borderWidth: 1, borderColor: l === 2 ? t.primary : t.border, borderStyle: l === 0 ? 'dashed' : 'solid' }}>
              <Txt size={12} color={t.gold} bold>{SURAHS[a.surah - 1]} · {arNum(a.ayah)}</Txt>
              <Txt style={{ fontFamily: font.quran, fontSize, lineHeight: fontSize * 1.9, marginTop: 8, textAlign: 'center' }}>
                {l === 0 ? '﴿ ● ● ● ● ● ﴾' : l === 1 ? `${first} …` : a.text}
              </Txt>
            </Pressable>
          );
        })}
      </ScrollView>
      <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: space.md, backgroundColor: t.bg, borderTopWidth: 1, borderColor: t.border, gap: 10 }}>
        {!allShown ? (
          <Btn kind="soft" label="إظهار الكل" onPress={showAll} />
        ) : (
          <>
            <Txt muted style={{ textAlign: 'center' }}>كيف كان تسميعك؟</Txt>
            <View style={{ flexDirection: 'row', gap: 8 }}>
              {([[0, 'ضعيف', t.weak], [1, 'متوسط', t.mid], [2, 'متقن', t.good]] as const).map(([r, l, c]) => (
                <Pressable key={r} onPress={() => finish(r as 0 | 1 | 2)} style={{ flex: 1, backgroundColor: c, borderRadius: radius.md, paddingVertical: 14, alignItems: 'center' }}>
                  <Txt bold color="#fff">{l}</Txt>
                </Pressable>
              ))}
            </View>
          </>
        )}
      </View>
    </View>
  );
}
