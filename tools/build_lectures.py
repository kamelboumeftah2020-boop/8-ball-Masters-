#!/usr/bin/env python3
"""يبني ملف data/lectures.json للمواعظ الصوتية.

المصادر:
- IslamHouse (دار الإسلام): محاضرات وخطب كبار العلماء وأئمة الحرمين.
- أرشيف الإنترنت: مواعظ الشيخ خالد الراشد.

التشغيل: python3 tools/build_lectures.py
"""
import json
import re
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

# مواعظ الشيخ خالد الراشد من أرشيف الإنترنت: كل المجموعات المتاحة، مع حذف المكرر.
# الترتيب مهم: المجموعة الأولى هي الموسوعة الكاملة، وما بعدها يضيف ما ليس فيها.
RASHED_COLLECTIONS = [
    'kalrashed', 'khaled-alrashed-lectures_202602', 'khaled-alrashed_94_Lectures_Mp3_up-by-muslem',
    'Khaled_Rachid_uP_bY_mUSLEm', 'Islamic_Tape-142_uP_bY_mUSLEm', 'SalafDoctrine_KhaledAlRached_Audios',
    'Khaled-Alrashed-Lectures', 'khaledRashedmp3', 'way_669',
]
# نسخ مطابقة من الموسوعة نفسها (عناوينها بترميز قديم)، تُستعمل روابطَ بديلة فقط
RASHED_MIRRORS = ['Mawsoa_Khaled-Errached_mp3', '312___________khaled-alrashed-312-dars-khotba-almawsoo3a-alsawteyya']


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
    return [(f, base + urllib.parse.quote(f['name'])) for f in files]


# عناوين اختُصرت أكثر من اللازم عند حذف اسم الشيخ منها
TITLE_FIX = {'إبن': 'ابن الوليد', 'لمهدي': 'المهدي', 'اضحك مع': 'اضحك مع الشيخ خالد الراشد', 'ابن الواليد': 'ابن الوليد'}


def seconds(v):
    if not v:
        return 0
    total = 0
    for part in str(v).split(':'):
        total = total * 60 + float(part)
    return total


AR = re.compile('[\u0621-\u064A]')


def clean_title(t):
    t = re.sub(r'\.mp3$', '', t, flags=re.I).replace('_', ' ').replace('#', '').replace('-', ' ')
    t = re.sub(r'[ღ¸.]{2,}', ' ', t)
    t = re.sub(r'^\s*\d+\s*', '', t)
    t = re.sub(r'(لل)?(الشيخ\s*)?/?\s*خالد\s*(بن\s*)?الراشد', '', t)
    t = re.sub(r'محاضر[هة]\s*(بعنوان)?|مقطع روعة|رو+عة|مؤثر(ة)? جدا|مؤثر$|\b20\d\d\b|كامل[ةه]?$', '', t)
    t = re.sub(r'(^|\s)(الشيخ|للشيخ)(\s|$)', ' ', t)
    t = re.sub(r'ـ+', '', t)
    t = re.sub(r'\s+', ' ', t).strip(' -–.,|:;')
    return t


def norm(t):
    t = re.sub('[\u064B-\u065F\u0670]', '', t)
    for a, b in (('أ', 'ا'), ('إ', 'ا'), ('آ', 'ا'), ('ى', 'ي'), ('ة', 'ه')):
        t = t.replace(a, b)
    t = re.sub('[^\u0621-\u064A]', '', t)
    return re.sub('^ال', '', t)


def rashed():
    """يجمع كل المجموعات؛ والنسخ المكررة تُحفظ روابطَ بديلة (a) يلجأ إليها المشغّل إذا تعذّر الرابط الأساسي."""
    lst, items = [], {}
    for i, cid in enumerate(RASHED_COLLECTIONS[:1] + RASHED_MIRRORS + RASHED_COLLECTIONS[1:]):
        mirror_only = cid in RASHED_MIRRORS
        for f, url in archive_files(cid):
            raw = f.get('title') or ''
            if mirror_only:
                try:
                    raw = raw.encode('latin-1').decode('cp1256')
                except (UnicodeEncodeError, UnicodeDecodeError):
                    pass
            if not AR.search(raw) or 'blogspot' in raw:
                raw = f['name']
            if not AR.search(raw):
                continue
            title = clean_title(raw) or raw
            title = TITLE_FIX.get(title, title)
            k = norm(title)
            if len(k) < 3 or 'تمرفع' in k:
                continue
            same = items.get(k)
            if not same and i > 0 and len(k) >= 5:
                same = next((v for x, v in items.items() if len(x) >= 5 and (k in x or x in k)), None)
            if same:
                # نسخة أخرى من المادة نفسها: رابط بديل إن كانت مدتها متقاربة
                d = seconds(f.get('length'))
                if (not d or not same['d'] or abs(d - same['d']) < max(120, same['d'] * .15)) and len(same.setdefault('a', [])) < 4:
                    same['a'].append(url)
                continue
            if mirror_only:
                continue
            item = {'t': title, 'u': [url], 'd': round(seconds(f.get('length')))}
            items[k] = item
            lst.append(item)
    # ربط النسخ المطابقة بالمدة (الملف نفسه بمدة متطابقة تقريبًا)
    for cid in RASHED_MIRRORS:
        for f, url in archive_files(cid):
            d = seconds(f.get('length'))
            if d < 60:
                continue
            for item in lst:
                alts = item.setdefault('a', [])
                if abs(item['d'] - d) <= 2 and url not in alts and len(alts) < 4:
                    alts.append(url)
                    break
    for item in lst:
        if not item.get('a'):
            item.pop('a', None)
        else:
            # خادم مجموعة kalrashed يخفق كثيرًا؛ نقدّم النسخ الأخرى ونجعله احتياطيًا
            urls = sorted(item['u'] + item['a'], key=lambda u: '/kalrashed/' in u)
            item['u'], item['a'] = urls[:1], urls[1:]
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
