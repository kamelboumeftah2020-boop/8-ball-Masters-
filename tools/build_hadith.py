#!/usr/bin/env python3
"""يبني كتب الحديث المضمّنة في التطبيق (تعمل دون إنترنت).

المصدر: github.com/AhmedBaset/hadith-json (النص العربي فقط).
ينتج data/hadith/{id}.json  →  {"t": العنوان, "a": المؤلف, "c": [[رقم الكتاب/الباب, الاسم]], "h": [[الرقم, الباب, النص]]}
"""
import json, os, re, urllib.request

BASE = 'https://cdn.jsdelivr.net/gh/AhmedBaset/hadith-json@main/db/by_book/'
BOOKS = [('nawawi', 'forties/nawawi40.json'), ('riyad', 'other_books/riyad_assalihin.json')]
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'data', 'hadith')


def clean(t):
    t = t.replace('‏', '').replace('‎', '').replace('\r', '')
    t = t.replace('“', '«').replace('”', '»')
    t = re.sub(r'ـ{2,}', '', t)
    t = re.sub(r'[ \t]+', ' ', t)
    t = re.sub(r' *\n *', '\n', t)
    t = re.sub(r'\n{2,}', '\n', t)
    t = re.sub(r'\s+([،.:؛])', r'\1', t)
    return t.strip()


def main():
    os.makedirs(OUT, exist_ok=True)
    for bid, path in BOOKS:
        with urllib.request.urlopen(BASE + path) as r:
            d = json.load(r)
        meta = d['metadata']['arabic']
        # ترتيب الكتاب الأصلي: «المقدمات» (رقمها 0 في المصدر) أولًا ثم بقية الكتب، مع ترقيم متسلسل
        chapters = sorted([[c['id'], clean(c['arabic'])] for c in d['chapters']], key=lambda c: c[0])
        order = {c[0]: i for i, c in enumerate(chapters)}
        src = sorted((h for h in d['hadiths'] if h.get('arabic', '').strip()), key=lambda h: (order[h['chapterId']], h['idInBook']))
        items = [[i + 1, h['chapterId'], clean(h['arabic'])] for i, h in enumerate(src)]
        out = {'t': meta['title'], 'a': meta['author'], 'c': chapters, 'h': items}
        with open(os.path.join(OUT, bid + '.json'), 'w', encoding='utf-8') as f:
            json.dump(out, f, ensure_ascii=False, separators=(',', ':'))
        print(bid, len(items), 'hadith,', len(chapters), 'chapters')


if __name__ == '__main__':
    main()
