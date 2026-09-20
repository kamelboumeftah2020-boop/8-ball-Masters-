import fs from 'node:fs';
import { chromium } from 'playwright';

// This container ships a Chromium at a fixed path; CI and a normal checkout let
// Playwright resolve its own. Passing an undefined executablePath means "use
// whatever Playwright found", so one call covers both.
const BUNDLED = '/opt/pw-browsers/chromium-1194/chrome-linux/chrome';

export function launchBrowser(options = {}) {
  const executablePath = process.env.CHROMIUM_PATH
    || (fs.existsSync(BUNDLED) ? BUNDLED : undefined);
  return chromium.launch({ executablePath, args: ['--no-sandbox'], ...options });
}
