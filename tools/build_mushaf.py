#!/usr/bin/env python3
"""يبني بيانات أسطر مصحف المدينة (٦٠٤ صفحة × ١٥ سطرًا) في data/mushaf/.

لكل صفحة ملف فيه أسطرها الخمسة عشر بالترتيب:
- ["h", رقم السورة]: سطر اسم السورة (الإطار)
- ["b"]: سطر البسملة
- [[رمز, "سورة:آية"], ...]: كلمات السطر برموز خط الصفحة (QCF V2) ومعها رقم الآية
خطوط الصفحات من مجمع الملك فهد عبر quran.com، وتُحمَّل في التطبيق عند الحاجة.

التشغيل: python3 tools/build_mushaf.py
"""
import json
import os
import urllib.request
from concurrent.futures import ThreadPoolExecutor

API = 'https://api.quran.com/api/v4/verses/by_page/{}?words=true&word_fields=code_v2,line_number,page_number&per_page=50&mushaf=1'
OUT = 'data/mushaf'


def get(url):
    for attempt in range(4):
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'nur-app-builder'})
            with urllib.request.urlopen(req, timeout=60) as r:
                return json.load(r)
        except Exception:
            if attempt == 3:
                raise


def fetch(page):
    # الآية قد تمتد عبر صفحتين، فنعتمد صفحة كل كلمة لا صفحة الآية
    words = []
    for v in get(API.format(page))['verses']:
        for w in v['words']:
            words.append((w['page_number'], w['line_number'], w['position'], w['code_v2'], v['verse_key'], w['char_type_name']))
    return words


def main():
    os.makedirs(OUT, exist_ok=True)
    with ThreadPoolExecutor(6) as ex:
        results = list(ex.map(fetch, range(1, 605)))
    seen, pages = set(), {p: [] for p in range(1, 605)}
    for words in results:
        for pg, ln, pos, code, key, kind in words:
            if (key, pos) in seen:
                continue
            seen.add((key, pos))
            pages[pg].append([ln, code, key, pos, kind])
    def order(w):
        s, a = map(int, w[2].split(':'))
        return (s, a, w[3])
    for pg in pages:
        pages[pg].sort(key=order)
    # علامة نهاية الآية لا تبدأ سطرًا في المصحف المطبوع: إن جاءت أول السطر في البيانات
    # فمكانها آخر السطر السابق بعد آخر كلمة من آيتها
    moved = 0
    for pg in pages:
        ws = pages[pg]
        for i in range(1, len(ws)):
            if ws[i][4] == 'end' and ws[i][0] != ws[i - 1][0] and ws[i - 1][2] == ws[i][2]:
                ws[i][0] = ws[i - 1][0]
                moved += 1
    print('end markers moved:', moved)

    def first_key(p):
        return pages[p][0][2] if p <= 604 and pages[p] else None

    for p in range(1, 605):
        lines = {}
        for ln, code, key, _, _ in pages[p]:
            lines.setdefault(ln, []).append([code, key])
        total = 15 if p > 2 else max(lines)
        out = [None] * (total + 1)
        for ln, ws in lines.items():
            out[ln] = ws
        # الأسطر الفارغة: سطر اسم السورة ثم سطر البسملة قبل أول آية منها
        # (وقد يقعان في آخر الصفحة فتبدأ السورة في الصفحة التالية)
        ln = 1
        while ln <= total:
            if out[ln] is not None:
                ln += 1
                continue
            run_end = ln
            while run_end + 1 <= total and out[run_end + 1] is None:
                run_end += 1
            key = out[run_end + 1][0][1] if run_end < total else first_key(p + 1)
            s_num = int(key.split(':')[0]) if key else 0
            for k in range(ln, run_end + 1):
                out[k] = ['h', s_num]
            if s_num not in (1, 9) and run_end > ln:
                out[run_end] = ['b']
            elif s_num not in (1, 9) and run_end == ln and p > 2:
                # سطر فارغ واحد: هو البسملة إن سبقه اسم السورة في آخر الصفحة السابقة
                out[ln] = ['b'] if ln == 1 else ['h', s_num]
            ln = run_end + 1
        start = 1
        with open(f'{OUT}/{p}.json', 'w', encoding='utf-8') as f:
            json.dump(out[max(start, 1):], f, ensure_ascii=False, separators=(',', ':'))
    # فهرس الصفحات من البيانات نفسها: [[السورة، أول آية، آخر آية]...]، والجزء، والحزب
    juz = {}
    for sn in range(1, 115):
        with open(f'data/quran/{sn}.json', encoding='utf-8') as f:
            for row in json.load(f):
                juz[f'{sn}:{row[1]}'] = row[4]
    with open('data/pages.json', encoding='utf-8') as f:
        old = json.load(f)
    index = []
    for p in range(1, 605):
        parts = []
        for ln, code, key, _, _ in pages[p]:
            sn, a = map(int, key.split(':'))
            if parts and parts[-1][0] == sn:
                parts[-1][2] = max(parts[-1][2], a)
            else:
                parts.append([sn, a, a])
        index.append([parts, juz[pages[p][0][2]], old[p - 1][2]])
    with open('data/pages.json', 'w', encoding='utf-8') as f:
        json.dump(index, f, separators=(',', ':'))
    print('pages:', len(pages))


if __name__ == '__main__':
    main()
