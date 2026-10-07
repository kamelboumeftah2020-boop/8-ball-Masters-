# حفظ — ورش وحفص (تطبيق أندرويد)

تطبيق لحفظ القرآن ومراجعته بروايتي **حفص** و**ورش**، خفيف الحجم: النص والصوت يُجلبان من الانترنت ويمكن تحميلهما للعمل بدون اتصال.

## المميزات
- **المصحف**: 114 سورة، بحث، نسبة الحفظ لكل سورة، البسملة، علامات مرجعية.
- **الاستماع**: مشاري العفاسي (حفص) وياسين الجزائري (ورش)، تشغيل متتابع من أي آية مع تكرار كل آية ١/٣/٥/١٠ مرات.
- **التفسير الميسّر** لكل آية (يُخزَّن بعد أول فتح).
- **الحفظ**: اختيار مقطع، الاستماع له، ثم تسميع بآيات مخفية (تلميح بأول كلمة ← إظهار) وتقييم.
- **المراجعة الذكية** (تكرار متباعد SM-2) + أيام متتالية + إحصائيات.
- **بدون انترنت**: تحميل نص المصحف (2MB) وصوت أي سورة أو جزء عمّ كاملًا.
- تذكير يومي، وضع داكن، حجم خط قابل للتعديل، واجهة عربية RTL.

## التشغيل للتطوير
    npm install
    npx expo start

## بناء ملف APK

### محليًا (بدون حساب Expo)
يحتاج Android SDK و JDK 17:

    npm install
    npx expo prebuild -p android --clean
    cd android && ./gradlew assembleRelease -PreactNativeArchitectures=armeabi-v7a,arm64-v8a

الملف الناتج: `android/app/build/outputs/apk/release/app-release.apk`

### عبر GitHub Actions
كل push إلى `main` يبني الـ APK تلقائيًا (أو شغّله يدويًا من تبويب Actions ← Build APK).
حمّل الملف من قسم **Artifacts** في صفحة التشغيل. لا يحتاج أي مفاتيح أو حساب.

### عبر EAS (اختياري)
    npm i -g eas-cli && eas login
    eas build -p android --profile preview

## ملاحظات
- اتجاه RTL يُفعَّل بعد أول تشغيل: أغلق التطبيق وافتحه مرة ثانية.
- الـ APK الحالي موقَّع بمفتاح التطوير الافتراضي (مناسب للتثبيت المباشر). للنشر على Google Play أنشئ مفتاح توقيع خاص.

## المصادر
- النص: [fawazahmed0/quran-api](https://github.com/fawazahmed0/quran-api) (`ara-quranuthmanihaf`، `ara-quranwarsh`)
- الصوت: [everyayah.com](https://everyayah.com)
- التفسير الميسّر: [spa5k/tafsir_api](https://github.com/spa5k/tafsir_api)
