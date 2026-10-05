#!/usr/bin/env python3
"""يبني كتب الحديث المضمّنة في التطبيق (تعمل دون إنترنت).

المصدر: github.com/AhmedBaset/hadith-json (النص العربي فقط).
ينتج للكتب الصغيرة data/hadith/{id}.json  →  {"t": العنوان, "a": المؤلف, "c": [[رقم الكتاب/الباب, الاسم]], "h": [[الرقم, الباب, النص]]}
وللصحيحين (كبيرين) ملفًا لكل كتاب منهما ليُفتح سريعًا:
  data/hadith/{id}/index.json  →  {"t", "a", "c": [[رقم الكتاب, الاسم, عدد الأحاديث]]}
  data/hadith/{id}/{رقم الكتاب}.json  →  [[الرقم, النص], ...]
"""
import json, os, re, urllib.request

BASE = 'https://cdn.jsdelivr.net/gh/AhmedBaset/hadith-json@main/db/by_book/'
BOOKS = [('nawawi', 'forties/nawawi40.json'), ('riyad', 'other_books/riyad_assalihin.json')]
SPLIT = [('bukhari', 'the_9_books/bukhari.json'), ('muslim', 'the_9_books/muslim.json')]
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

    for bid, path in SPLIT:
        with urllib.request.urlopen(BASE + path) as r:
            d = json.load(r)
        meta = d['metadata']['arabic']
        # المقدمة (رقمها 0 في المصدر) أولًا، ويبقى ترقيم الأحاديث كما في المصدر
        chapters = sorted(d['chapters'], key=lambda c: c['id'])
        os.makedirs(os.path.join(OUT, bid), exist_ok=True)
        index = []
        for c in chapters:
            hs = [[h['idInBook'], clean(h['arabic'])] for h in d['hadiths'] if h['chapterId'] == c['id'] and h.get('arabic', '').strip()]
            if not hs:
                continue
            with open(os.path.join(OUT, bid, f"{c['id']}.json"), 'w', encoding='utf-8') as f:
                json.dump(hs, f, ensure_ascii=False, separators=(',', ':'))
            index.append([c['id'], clean(c['arabic']), len(hs)])
        with open(os.path.join(OUT, bid, 'index.json'), 'w', encoding='utf-8') as f:
            json.dump({'t': meta['title'], 'a': meta['author'], 'c': index}, f, ensure_ascii=False, separators=(',', ':'))
        print(bid, sum(c[2] for c in index), 'hadith,', len(index), 'books')


if __name__ == '__main__':
    main()
