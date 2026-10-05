# صدى — تطبيق بودكاست

تطبيق ويب تقدّمي (PWA) عصري باللغة العربية يجمع البودكاست المتوفر على الإنترنت في مكان واحد،
مع مشغّل متكامل، تحميل للاستماع بدون إنترنت، ومفضّلة.

## المزايا

- **كل أنواع البودكاست**: أكثر البرامج استماعاً و19 تصنيفاً (مجتمع، أخبار، كوميديا، دين، تعليم، تقنية…)
  من دليل Apple Podcasts، مع اختيار البلد (السعودية، مصر، الجزائر، المغرب، الإمارات… وغيرها).
- **بحث** في البرامج والحلقات، وبحث داخل حلقات أي برنامج مع الترتيب حسب الأحدث أو الأقدم.
- **مشغّل عصري**: مشغّل مصغّر وآخر بملء الشاشة بخلفية مأخوذة من غلاف الحلقة، وتقديم 30 ثانية ورجوع 15،
  وسرعات من 0.75× إلى 2×، ومؤقت نوم، وقائمة تشغيل («التالي»)، ومشاركة، وأزرار التحكم من شاشة القفل
  والسماعات (Media Session). يحفظ موضعك ويكمل من حيث توقفت.
- **التحميل داخل التطبيق**: تُحفظ الحلقات في IndexedDB مع شريط تقدّم، وتعمل بدون إنترنت، وتظهر المساحة
  المستخدمة والمتاحة. يمكن تحميل آخر 5 حلقات من أي برنامج بضغطة.
- **المفضلة**: برامج وحلقات مفضّلة، وسجل استماع، وقسم «تابع الاستماع» في الرئيسية.
- **قابل للتثبيت** على الجوال كتطبيق (PWA) مع Service Worker للعمل دون اتصال.

## التشغيل

```bash
npm install
npm run dev       # بيئة التطوير
npm run build     # نسخة الإنتاج في dist/
npm run preview   # معاينة نسخة الإنتاج
```

لا يحتاج أي مفتاح API: البيانات من واجهة iTunes Search العامة.

## ملاحظة عن التحميل (CORS)

المتصفح يستطيع **تشغيل** أي حلقة، لكن **حفظها** يتطلب أن يسمح خادم البودكاست بذلك (CORS).
بعض المستضيفين (مثل anchor.fm / Spotify for Podcasters) لا يسمحون، فيظهر للمستخدم تنبيه مع رابط لفتح الملف.

لحل ذلك، انشر وسيط التحميل المرفق `proxy/cloudflare-worker.js` على Cloudflare Workers (مجاني)،
ثم ابنِ التطبيق مع عنوانه:

```bash
VITE_DOWNLOAD_PROXY=https://your-worker.workers.dev npm run build
```

يحاول التطبيق التحميل المباشر أولاً، ويلجأ للوسيط فقط عند الحاجة.

## تطبيق Android

المشروع مغلّف بـ [Capacitor](https://capacitorjs.com) ومجلد `android/` جاهز. في نسخة أندرويد:

- **التحميل يتم بشكل أصلي** (`@capacitor/file-transfer`) ويُحفظ في ذاكرة التطبيق، فيعمل مع كل مستضيفي البودكاست
  دون الحاجة لوسيط CORS.
- **التشغيل في الخلفية** عبر خدمة أصلية (`MediaPlaybackService`) مع إشعار وتحكم من شاشة القفل والسماعات
  (تشغيل/إيقاف، رجوع 15، تقديم 30، التالي/السابق).
- زر الرجوع في الجهاز يغلق المشغّل أو يرجع للصفحة السابقة، ومن الرئيسية يصغّر التطبيق إذا كان يشغّل.
- مشاركة عبر قائمة المشاركة الأصلية، وأيقونة وشاشة بداية خاصة بالتطبيق.

### بناء الـ APK

يتطلب JDK 21 و Android SDK (أو Android Studio):

```bash
npm install
npm run build
npx cap sync android
cd android && ./gradlew assembleDebug
# الملف: android/app/build/outputs/apk/debug/app-debug.apk
```

أو افتح المشروع في Android Studio بـ `npx cap open android`.

كل push إلى GitHub يبني الـ APK تلقائياً (GitHub Actions ← «Android APK» ← Artifacts).

### للنشر على Google Play

أنشئ مفتاح توقيع ثم ابنِ نسخة release:

```bash
keytool -genkey -v -keystore sada.keystore -alias sada -keyalg RSA -keysize 2048 -validity 10000
cd android && ./gradlew bundleRelease   # بعد إعداد signingConfigs في app/build.gradle
```

لتغيير الأيقونة: استبدل الصور في `assets/` ثم شغّل `npx capacitor-assets generate --android`.

## البنية

```
src/
  lib/         api.ts (iTunes)، db.ts (IndexedDB)، genres.ts، format.ts
  store/       library.tsx (المفضلة، التحميلات، السجل)، player.tsx (المشغّل)
  components/  المشغّل المصغّر والكامل، بطاقات البرامج، صف الحلقة، زر التحميل…
  pages/       الرئيسية، استكشاف، التصنيف/الأكثر استماعاً، صفحة البرنامج، مكتبتي، التحميلات
public/        manifest، الأيقونة، Service Worker
  native/      ربط الميزات الأصلية (تحميل، خدمة التشغيل في الخلفية)
android/       مشروع أندرويد (Capacitor) + MediaPlaybackService
proxy/         وسيط تحميل اختياري لنسخة الويب (Cloudflare Worker)
```
