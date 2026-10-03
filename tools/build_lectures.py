#!/usr/bin/env python3
"""يبني ملف data/lectures.json للمواعظ الصوتية.

المصادر:
- IslamHouse (دار الإسلام): محاضرات وخطب كبار العلماء وأئمة الحرمين.
- أرشيف الإنترنت: مواعظ الشيخ خالد الراشد.

التشغيل: python3 tools/build_lectures.py
"""
import json
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor

IH = 'https://api3.islamhouse.com/v3/paV29H2gm56kvLPy/main/audios/ar/ar/{}/50/json'
MAX_PARTS = 6  # نتجنب السلاسل العلمية الطويلة، فالمقصود هنا المواعظ

# (المعرّف، الاسم المعروض، الاسم في IslamHouse، المجموعة)
SCHOLARS = [
    ('baz', 'عبد العزيز بن باز', 'عبد العزيز بن باز', 'ulama'),
    ('uthaymeen', 'محمد بن صالح العثيمين', 'محمد بن صالح العثيمين', 'ulama'),
    ('fawzan', 'صالح بن فوزان الفوزان', 'صالح بن فوزان الفوزان', 'ulama'),
    ('salehalshaikh', 'صالح بن عبد العزيز آل الشيخ', 'صالح بن عبد العزيز آل الشيخ', 'ulama'),
    ('badr', 'عبد الرزاق البدر', 'عبد الرزاق بن عبد المحسن البدر', 'ulama'),
    ('khudair', 'عبد الكريم الخضير', 'عبد الكريم بن عبد الله الخضير', 'ulama'),
    ('qahtani', 'سعيد بن وهف القحطاني', 'سعيد بن علي بن وهف القحطاني', 'duroos'),
    ('shinqiti', 'محمد المختار الشنقيطي', 'محمد بن محمد المختار الشنقيطي', 'duroos'),
    ('sudais', 'عبد الرحمن السديس', 'عبد الرحمن بن عبد العزيز السديس', 'haram'),
    ('shuraim', 'سعود الشريم', 'سعود بن إبراهيم الشريم', 'haram'),
    ('budair', 'صلاح البدير', 'صلاح بن محمد البدير', 'haram'),
    ('hudhaifi', 'علي الحذيفي', 'علي بن عبد الرحمن الحذيفي', 'haram'),
    ('qasim', 'عبد المحسن القاسم', 'عبد المحسن بن محمد القاسم', 'haram'),
    ('thubaiti', 'عبد الباري الثبيتي', 'عبد الباري الثبيتي', 'haram'),
    ('khayyat', 'أسامة خياط', 'أسامة بن عبد الله خياط', 'haram'),
    ('humaid', 'صالح بن حميد', 'صالح بن عبد الله بن حميد', 'haram'),
    ('husainalshaikh', 'حسين آل الشيخ', 'حسين بن عبد العزيز آل الشيخ', 'haram'),
]

# مواعظ الشيخ خالد الراشد من أرشيف الإنترنت
# استبعدنا ما يغلب عليه الطابع السياسي أو الحماسي، وما لم يتضح مضمونه من عنوانه، والمقاطع غير الوعظية.
RASHED_EXCLUDE = {
    'اضحك مع الشيخ خالد الراشد', 'البنيان المرصوص', 'الطاغوت الاكبر', 'امة المليار', 'يا أمة محمد',
    'تدنيس القرآن', 'و كفيناك المستهزئين', 'وا معتصماه', 'قاتل ومقتول', 'و لا تهنوا و لا تحزنوا',
    'ولا تهنوا ولا تحزنوا', 'نشرة الاخبار', 'رأيت النبي يبكي', 'قعيد أحيا الأمة', 'قصة بطل',
    'نعم هذا البطل', 'لم يتجاوز عددهم العشرين', 'إنهم فتية آمنوا بربهم', 'رجال صدقوا', 'ابشروا لقد جاء النصر',
}


def get(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'nur-app-builder'})
    with urllib.request.urlopen(req, timeout=90) as r:
        return json.load(r)


def islamhouse():
    pages = get(IH.format(1))['links']['pages_number']
    with ThreadPoolExecutor(8) as ex:
        data = list(ex.map(lambda p: get(IH.format(p))['data'], range(1, pages + 1)))
    items = {x['id']: x for page in data for x in page}
    out = {}
    for sid, _, ih_name, _ in SCHOLARS:
        lst = []
        for x in sorted(items.values(), key=lambda x: -x['add_date']):
            if not any(a['title'] == ih_name for a in x['prepared_by']):
                continue
            mp3 = [a['url'] for a in sorted(x['attachments'], key=lambda a: a['order']) if a['extension_type'] == 'MP3']
            if not mp3 or len(mp3) > MAX_PARTS:
                continue
            lst.append({'t': x['title'].strip(), 'u': mp3, 'id': x['id']})
        out[sid] = lst
    return out


def archive_files(identifier):
    meta = get(f'https://archive.org/metadata/{identifier}')
    files = [f for f in meta['files'] if f['name'].lower().endswith('.mp3') and f.get('source') == 'original']
    base = f'https://archive.org/download/{identifier}/'
    return [(f['name'], base + urllib.parse.quote(f['name']), float(f.get('length') or 0)) for f in files]


def rashed():
    lst, seen = [], set()

    def norm(t):
        return t.replace('ى', 'ي').replace('أ', 'ا').replace('إ', 'ا').replace('ـ', '').replace(' ', '').replace('؟', '').strip()

    def add(title, url, length):
        title = ' '.join(title.split())
        if title in RASHED_EXCLUDE or norm(title) in seen:
            return
        seen.add(norm(title))
        lst.append({'t': title, 'u': [url], 'd': round(length)})

    # المجموعة الرئيسية (عناوين عربية واضحة)
    for name, url, length in archive_files('khaled-alrashed-lectures_202602'):
        add(name[:-4], url, length)
    # إضافات غير موجودة في المجموعة الرئيسية
    for name, url, length in archive_files('Khaled-Alrashed-Lectures'):
        add(name[:-4].replace('مميز-', '').replace('-', ' '), url, length)
    return sorted(lst, key=lambda x: x['t'])


def main():
    items = islamhouse()
    items['rashed'] = rashed()
    speakers = [{'id': 'rashed', 'name': 'خالد الراشد', 'group': 'mawaiz', 'src': 'archive.org'}]
    speakers += [{'id': s, 'name': n, 'group': g, 'src': 'islamhouse.com'} for s, n, _, g in SCHOLARS]
    for s in speakers:
        s['count'] = len(items[s['id']])
    data = {
        'groups': [
            {'id': 'mawaiz', 'name': 'مواعظ مؤثرة'},
            {'id': 'ulama', 'name': 'كبار العلماء'},
            {'id': 'haram', 'name': 'خطب الحرمين'},
            {'id': 'duroos', 'name': 'دروس ومواعظ'},
        ],
        'speakers': speakers,
        'items': items,
    }
    with open('data/lectures.json', 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, separators=(',', ':'))
    for s in speakers:
        print(s['count'], s['name'])


if __name__ == '__main__':
    main()
