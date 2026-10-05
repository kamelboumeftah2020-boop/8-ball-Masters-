import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  base: "./",
  // Older Android WebViews (Chrome 80+) must be able to run the bundle.
  build: { target: ["es2019", "chrome80"], cssTarget: "chrome80" },
});
