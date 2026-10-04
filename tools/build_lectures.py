#!/usr/bin/env python3
"""يبني ملف data/lectures.json للمواعظ الصوتية.

المصادر:
- IslamHouse (دار الإسلام): محاضرات وخطب كبار العلماء وأئمة الحرمين.
- أرشيف الإنترنت: مواعظ الشيخ خالد الراشد، ومواعظ مشايخ أهل السنة المعروفين بالوعظ.

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

# مواعظ مشايخ آخرين من أرشيف الإنترنت: (المعرّف، الاسم، المجموعات، ما يُحذف من العناوين)
# المقصود المواعظ والمحاضرات الوعظية فقط، فتُستبعد الخطب والدروس العلمية والشروح والسلاسل.
PREACHERS = [
    ('elahmad', 'عبد المحسن الأحمد', ['Mawsoa-mp3_Elahmad'], r'(د\.?\s*)?عبد\s*ال?محسن\s*(بن\s*\S+\s*)?الا?أ?حمد'),
    ('awadi', 'نبيل العوضي', ['Nabil-3awadi_Mawsoa-mp3'], r'نبيل\s*(بن\s*علي\s*)?العوضي'),
    ('arifi', 'محمد العريفي', ['Dr-Mohammad-Arifi_Mawsoaa_uP_bY_mUSLEm', 'lecture-alarefe-2018'], r'(د\.?\s*)?محمد\s*(بن\s*عبد\s*الرحمن\s*)?العريفي'),
    ('dowaish', 'إبراهيم الدويش', ['Islamic_Tape-140_uP_bY_mUSLEm'], r'إبراهيم\s*(بن\s*عبد\s*الله\s*)?الدويش'),
    ('breik', 'سعد البريك', ['Dr_Saad_BRIC_255_Lectures_Mp3_up-by-muslem'], r'سعد\s*البريك'),
    ('yaqoub', 'محمد حسين يعقوب', ['mohamed-hussein-yaqob-825-mp3'], r'محمد\s*حسين\s*يعقوب'),
    ('hassan', 'محمد حسان', ['Med_Hassann_uP_bY_mUSLEm', 'mohamed-hassan-mp3', 'Mp3-___-----MAWSO3AH-428-BY-MOHAMMAD-HASSAN'], r'محمد\s*حسان'),
    ('huwaini', 'أبو إسحاق الحويني', ['Al_Houwaini_Mawsoaa_577-lectures_uP_bY_mUSLEm'], r'أبو?ي?\s*إ?ا?سحاق\s*الحويني'),
    ('qarni', 'عائض القرني', ['Ayed_Al-Qarni_458_Lectures_Mp3_up-by-muslem'], r'عائض\s*(بن\s*عبد\s*الله\s*)?القرني'),
]
# ما ليس موعظة: الخطب، والدروس والشروح، والسلاسل والحلقات، والفتاوى، والشعر، والردود والمسائل الخلافية
NOT_MAWIZA = re.compile(
    r'خطب|خطبة|درس|دروس|شرح|تفسير|سلسلة|حلق[ةه]|برنامج|ألفية|الفية|متن|كتاب|فتاو|فتوى|أسئلة|اسئلة|سؤال|'
    r'قصائد|قصيدة|شعر|أمسية|امسية|لقاء|مقابلة|حوار|مناظرة|رد على|الرد|الشيعة|الرافضة|الصوفية|الإخوان|الاخوان|السياسة|'
    r'الانتخابات|الثورة|ثورة|مظاهرات|الجهاد|جهاد|العمل الجماعي|أحكام|احكام|فقه|مصطلح|الحديث الضعيف|تخريج|علوم|أصول|اصول|'
    r'سيرة الشيخ|السيرة الذاتية|عام الوفود|غزوة|المهدي|أشراط|اشراط|نهاية العالم|ألحان|الحان|حفل|تلاوة|تلاوات|'
    r'صحيح|البخاري|رحلتي|القدرية|الإرجاء|الارجاء|الليبرالية|العولمة|العلمانية|قصة الرسالة|الفن|الميلاد|المولد|حملة|تفجيرات|'
    r'ندوة|دورة|ولاة الأمر|الخلاف|الحزبية|الأحزاب|الديمقراطية')
PART = re.compile(r'[\(\[]\s*(\d+)\s*[\)\]]|(?:الجزء|ج)\s*(\d+)|\s(\d+)\s*$')


def decode_title(t):
    # بعض المجموعات عناوينها بترميز ويندوز العربي القديم
    if not AR.search(t):
        try:
            t2 = t.encode('latin-1').decode('cp1256')
            if AR.search(t2):
                return t2
        except (UnicodeEncodeError, UnicodeDecodeError):
            pass
    return t


def preacher(name_re, collections):
    """المجموعة الأولى أساسية، وما بعدها يضيف ما ليس فيها، ونسخ المادة نفسها تُحفظ روابطَ بديلة."""
    items, seen = [], {}
    for cid in collections:
        for f, url in archive_files(cid):
            raw = decode_title(f.get('title') or '')
            if not AR.search(raw):
                raw = decode_title(f['name'])
            t = re.sub(r'\.mp3$', '', raw, flags=re.I).replace('_', ' ')
            t = re.sub(name_re, '', t)
            t = re.sub(r'^\s*\d+\s*[-–.]*\s*', '', t)
            t = re.sub(r'(^|\s)(الشيخ|للشيخ|الدكتور|د\.)\s*:?(\s|$)', ' ', t)
            t = re.sub(r'\s+', ' ', t).strip(' -–.,|:;')
            d = round(seconds(f.get('length')))
            if not AR.search(t) or NOT_MAWIZA.search(t) or d < 150:
                continue
            m = PART.search(t)
            part = int(next(g for g in m.groups() if g)) if m else 1
            if part > 2:  # المحاضرات الطويلة المقسمة: نكتفي بما كان في جزأين على الأكثر
                continue
            k = norm(t)
            if len(k) < 3:
                continue
            same = seen.get(k)
            if same:
                if abs(same['d'] - d) < max(60, same['d'] * .05) and len(same.setdefault('a', [])) < 3 and url not in same['u']:
                    same['a'].append(url)
                continue
            seen[k] = {'t': t, 'u': [url], 'd': d}
            items.append(seen[k])
    return sorted(items, key=lambda x: x['t'])


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
    for sid, name, cols, name_re in PREACHERS:
        items[sid] = preacher(name_re, cols)
        speakers.append({'id': sid, 'name': name, 'group': 'mawaiz', 'src': 'archive.org'})
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
