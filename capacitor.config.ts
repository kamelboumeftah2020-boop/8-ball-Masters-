import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "com.sada.podcasts",
  appName: "صدى",
  webDir: "dist",
  android: {
    backgroundColor: "#0b0b14",
  },
  plugins: {
    SystemBars: {
      insetsHandling: "css",
      initialViewportFitValueHint: "cover",
      style: "DARK",
    },
    SplashScreen: {
      launchShowDuration: 800,
      backgroundColor: "#0b0b14",
      showSpinner: false,
    },
  },
};

export default config;
