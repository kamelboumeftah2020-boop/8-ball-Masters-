import { useColorScheme } from 'react-native';
import { useSettings } from './store/settings';

export const palettes = {
  light: {
    bg: '#F2F5F1', card: '#FFFFFF', primary: '#0B5D46', onPrimary: '#FFFFFF',
    soft: '#DDEBE4', gold: '#A8802F', goldSoft: '#F1E7CE',
    text: '#14231E', muted: '#62736C', border: '#DCE4DE',
    good: '#1F9D6B', mid: '#C98A12', weak: '#C4483D',
  },
  dark: {
    bg: '#0A110E', card: '#131D18', primary: '#35B78A', onPrimary: '#04140D',
    soft: '#17342A', gold: '#D4AE5E', goldSoft: '#2A2412',
    text: '#E9F1EC', muted: '#8CA197', border: '#22322B',
    good: '#4CC38A', mid: '#E8B545', weak: '#E56E62',
  },
};
export type Palette = typeof palettes.light;
export const radius = { sm: 10, md: 16, lg: 28 };
export const space = { xs: 4, sm: 8, md: 16, lg: 24, xl: 36 };
export const font = { ui: 'Cairo_600SemiBold', bold: 'Cairo_700Bold', quran: 'AmiriQuran_400Regular' };

export function useTheme(): Palette {
  const sys = useColorScheme();
  const dark = useSettings((s) => s.dark);
  const isDark = dark === null ? sys === 'dark' : dark;
  return isDark ? palettes.dark : palettes.light;
}
