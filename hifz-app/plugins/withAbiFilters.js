// يقصر الـ APK على معالجات ARM (كل هواتف أندرويد الحقيقية) لتقليل الحجم إلى النصف تقريبًا
const { withAppBuildGradle } = require('expo/config-plugins');

module.exports = function withAbiFilters(config, abis = ['armeabi-v7a', 'arm64-v8a']) {
  return withAppBuildGradle(config, (cfg) => {
    if (!cfg.modResults.contents.includes('abiFilters')) {
      cfg.modResults.contents = cfg.modResults.contents.replace(
        /defaultConfig\s*\{/,
        `defaultConfig {\n        ndk { abiFilters ${abis.map((a) => `"${a}"`).join(', ')} }`);
    }
    return cfg;
  });
};
