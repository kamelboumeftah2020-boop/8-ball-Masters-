#!/usr/bin/env python3
"""يبني فهرس مكتبة الكتب data/library.json من موقع IslamHouse (دار الإسلام).

نختار كتب العلماء المعروفين بمنهج أهل السنة والجماعة، وكتب قصص الأنبياء والسيرة،
ونصنّفها بحسب عناوينها. الكتب نفسها (PDF) لا تُضمَّن، بل تُحمَّل من داخل التطبيق.

التشغيل: python3 tools/build_library.py [ملف-الكتب-المحمّل.json]
الناتج: {"cats": [[المعرّف، الاسم]...], "b": [[المعرّف، العنوان، المؤلف، التصنيف، الوصف، [[رابط، الحجم بالكيلوبايت، الوصف]...]]...]}
"""
import json, os, re, sys, urllib.request
from concurrent.futures import ThreadPoolExecutor

API = 'https://api3.islamhouse.com/v3/paV29H2gm56kvLPy/main/books/ar/ar/{}/50/json'
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')

AUTHORS = [
    'عبد العزيز بن باز', 'محمد بن صالح العثيمين', 'صالح بن فوزان الفوزان', 'محمد ناصر الدين الألباني',
    'محمد بن عبد الوهاب', 'ابن تيمية', 'ابن القيم', 'ابن كثير', 'ابن رجب الحنبلي', 'النووي', 'عبد الرحمن بن ناصر السعدي',
    'عبد الرزاق بن عبد المحسن البدر', 'عبد المحسن بن حمد العباد', 'صالح بن عبد العزيز آل الشيخ', 'سعيد بن علي بن وهف القحطاني',
    'عبد الكريم بن عبد الله الخضير', 'عبد العزيز بن عبد الله الراجحي', 'حافظ بن أحمد الحكمي', 'محمد الأمين الشنقيطي',
    'أبو بكر جابر الجزائري', 'بكر بن عبد الله أبو زيد', 'عبد الرحمن المعلمي اليماني', 'عبد الرحمن بن ناصر البراك',
    'سعد بن ناصر الشثري', 'محمد بن جميل زينو', 'عمر بن سليمان الأشقر', 'محمد بن إبراهيم الحمد', 'عبد الملك القاسم',
    'عبد المحسن بن محمد القاسم', 'صالح بن عبد الله بن حمد العصيمي', 'عبد الله بن جار الله بن إبراهيم الجار الله',
    'اللجنة العلمية برئاسة الشؤون الدينية بالمسجد الحرام والمسجد النبوي', 'محمد بن إبراهيم التويجري', 'عبد العزيز بن محمد السدحان',
    'عبد الله بن صالح الفوزان', 'محمد بن إبراهيم آل الشيخ', 'عبد الرحمن بن حسن آل الشيخ', 'سليمان بن عبد الله آل الشيخ',
    'ابن أبي العز الحنفي', 'الذهبي', 'ابن قدامة المقدسي', 'ابن حجر العسقلاني', 'محمد بن سليمان التميمي',
]

# (المعرّف، الاسم، كلمات العنوان) — بالترتيب: أول تصنيف يطابق هو المعتمد
CATS = [
    ('anbiya', 'قصص الأنبياء والسيرة', r'الأنبياء|الانبياء|نبي الله|السيرة|سيرة النبي|سيرة الرسول|الرحيق|الشمائل|غزو|الصحابة|الخلفاء|قصص'),
    ('aqida', 'العقيدة والتوحيد', r'عقيد|اعتقاد|الرد على|الجهمية|المعطلة|الرافضة|الفرقة الناجية|الكفر|كفر|العذر بالجهل|مسائل الجاهلية|الدين الخالص|توحيد|الإيمان|الايمان|الشرك|أصول|الأصول|السنة|البدع|بدع|الأسماء والصفات|القضاء والقدر|الولاء|نواقض|كتاب التوحيد'),
    ('quran', 'القرآن وعلومه', r'القرآن|القران|تفسير|سورة|التجويد|آيات|الآيات|المصحف'),
    ('hadith', 'الحديث', r'حديث|أحاديث|الأربعين|الاربعين|بلوغ المرام|عمدة الأحكام|رياض الصالحين|صحيح'),
    ('fiqh', 'الفقه والعبادات', r'فقه|صلاة|الصلاة|الطهارة|الوضوء|زكاة|الزكاة|صيام|الصيام|رمضان|الحج|العمرة|أحكام|احكام|فتاوى|فتاوي|الجنائز|البيوع|المسائل|المناسك|الفرائض|الفرضية|السحر'),
    ('raqaiq', 'الأذكار والرقائق', r'ذكر|أذكار|الأذكار|دعاء|الدعاء|أدعية|حصن|رقائق|القلب|القلوب|التوبة|الموت|الآخرة|القبر|الجنة|النار|الصبر|الإخلاص|الاستغفار|الشكر|الذنوب|المعاصي|الخوف|الرجاء|الزهد|موعظة|مواعظ|تواضع|الكذب|الحياء|الإشاعة|الفتن|الضلالة|التوكل|الحسرة|المصائب|الشتاء|مفتاح'),
    ('usra', 'الأسرة والأخلاق', r'المرأة|المراة|الأسرة|الاسرة|الزواج|الزوج|الأولاد|الأبناء|الطفل|الأطفال|آداب|الآداب|أخلاق|الأخلاق|بر الوالدين|الحجاب|البيت|الجار|الشباب|الوالدين|وصايا|المحادثة|الدخان|المخدرات|الخمور'),
]
OTHER = ('other', 'متنوعة')
# بحوث أكاديمية عن السيرة لا تناسب عامة القرّاء
ACADEMIC = re.compile(r'ترجم|باللغة|دراسة|مصادر|جهود|التقنية|دور علماء|مرويات|أعلام السيرة|القرن|أبعاد|أهمية دراسة|قصص من الإنترنت|قصصي مع')


def fetch_all():
    def page(p):
        for _ in range(3):
            try:
                return json.loads(urllib.request.urlopen(API.format(p), timeout=60).read())['data']
            except Exception:
                pass
        raise RuntimeError(p)
    first = json.loads(urllib.request.urlopen(API.format(1), timeout=60).read())
    n = first['links']['pages_number']
    with ThreadPoolExecutor(12) as ex:
        rest = list(ex.map(page, range(2, n + 1)))
    return first['data'] + [b for r in rest for b in r]


def size_kb(s):
    m = re.match(r'([\d.]+)\s*(KB|MB|GB)', s or '')
    if not m:
        return 0
    return round(float(m.group(1)) * {'KB': 1, 'MB': 1024, 'GB': 1024 ** 2}[m.group(2)])


def short(t, n=220):
    t = re.sub(r'<[^>]+>', ' ', t or '')
    t = re.sub(r'\s+', ' ', t).strip()
    return t if len(t) <= n else t[:n].rsplit(' ', 1)[0] + '…'


def main():
    books = json.load(open(sys.argv[1], encoding='utf-8')) if len(sys.argv) > 1 else fetch_all()
    authors = set(AUTHORS)
    out, seen = [], set()
    for b in books:
        names = [a['title'] for a in b['prepared_by'] if a['kind'] == 'author']
        title = b['title'].strip()
        anbiya = re.search(CATS[0][2], title)
        if not (authors & set(names)) and not (anbiya and names and not ACADEMIC.search(title)):
            continue
        pdfs = [[a['url'], size_kb(a['size']), (a.get('description') or '').strip()]
                for a in sorted(b['attachments'], key=lambda a: a['order']) if a['extension_type'] == 'PDF']
        if not pdfs or title in seen:
            continue
        seen.add(title)
        cat = next((c[0] for c in CATS if re.search(c[2], title)), OTHER[0])
        for p in pdfs:
            if p[2] == title:
                p[2] = ''
        author = next((n for n in names if n in authors), names[0])
        out.append([b['id'], title, author, cat, short(b['description']), pdfs])
    order = {a: i for i, a in enumerate(AUTHORS)}
    out.sort(key=lambda x: (order.get(x[2], len(order)), x[1]))
    cats = [[c[0], c[1]] for c in CATS] + [list(OTHER)]
    json.dump({'cats': cats, 'b': out}, open(os.path.join(ROOT, 'data', 'library.json'), 'w', encoding='utf-8'),
              ensure_ascii=False, separators=(',', ':'))
    from collections import Counter
    print(len(out), Counter(x[3] for x in out))


if __name__ == '__main__':
    main()
