#!/usr/bin/env python3
"""يبني التفاسير المضمّنة في التطبيق (تعمل دون إنترنت).

- التفسير الميسّر (مجمع الملك فهد): api.alquran.cloud (ar.muyassar)
- تفسير السعدي «تيسير الكريم الرحمن»: api.quran.com (المعرّف 91)
ينتج data/tafsir/{muyassar,saadi}/{1..114}.json: مصفوفة نصوص بعدد آيات السورة؛
والنص الفارغ يعني أن تفسير الآية مع ما قبلها.
"""
import html, json, os, re, time, urllib.request
from concurrent.futures import ThreadPoolExecutor

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
OUT = os.path.join(ROOT, 'data', 'tafsir')


def get(url):
    for i in range(4):
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'nur-app-builder'})
            with urllib.request.urlopen(req, timeout=120) as r:
                return json.load(r)
        except Exception:
            if i == 3:
                raise
            time.sleep(2 * (i + 1))


def strip_html(t):
    # الآيات المقتبسة تُكتب بين قوسين قرآنيين، وتُحذف بقية الوسوم
    t = re.sub(r'<span class="arabic[^"]*">\s*\{?\s*(.*?)\s*\}?\s*</span>', lambda m: '﴿' + m.group(1).strip(' {}') + '﴾', t, flags=re.S)
    t = re.sub(r'<br\s*/?>|</p>', '\n', t)
    t = re.sub(r'<[^>]+>', '', t)
    t = html.unescape(t).replace('‏', '')
    t = re.sub(r'[ \t]+', ' ', t)
    t = re.sub(r' *\n[ \n]*', '\n', t)
    return t.strip()


def main():
    counts = [len(json.load(open(os.path.join(ROOT, 'data', 'quran', f'{s}.json')))) for s in range(1, 115)]
    os.makedirs(os.path.join(OUT, 'muyassar'), exist_ok=True)
    os.makedirs(os.path.join(OUT, 'saadi'), exist_ok=True)

    mu = get('https://api.alquran.cloud/v1/quran/ar.muyassar')['data']['surahs']
    for s in mu:
        texts = [a['text'].strip() for a in s['ayahs']]
        json.dump(texts, open(os.path.join(OUT, 'muyassar', f"{s['number']}.json"), 'w', encoding='utf-8'), ensure_ascii=False, separators=(',', ':'))

    def saadi(n):
        d = get(f'https://api.quran.com/api/v4/tafsirs/91/by_chapter/{n}?per_page=300')
        texts = [''] * counts[n - 1]
        for x in d['tafsirs']:
            a = int(x['verse_key'].split(':')[1])
            texts[a - 1] = strip_html(x['text'])
        json.dump(texts, open(os.path.join(OUT, 'saadi', f'{n}.json'), 'w', encoding='utf-8'), ensure_ascii=False, separators=(',', ':'))
        return n, sum(1 for t in texts if not t)

    with ThreadPoolExecutor(6) as ex:
        empty = sum(e for _, e in ex.map(saadi, range(1, 115)))
    print('muyassar:', len(mu), 'surahs; saadi grouped ayat:', empty)


if __name__ == '__main__':
    main()
