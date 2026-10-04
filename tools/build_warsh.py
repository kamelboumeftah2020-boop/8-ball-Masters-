#!/usr/bin/env python3
"""يبني صفحات مصحف ورش (مجمع الملك فهد، الإصدار العاشر) من بيانات KFGQPC.

المصدر: github.com/thetruetruth/quran-data-kfgqpc (warsh/data/warshData_v10.json)
لكل آية: رقم الصفحة، وسطر البداية والنهاية. ينتج:
  data/warsh/{1..604}.json  →  {"j": الجزء, "b": [كتل]}
    ["h", سورة]                     سطر اسم السورة
    ["b"]                           سطر البسملة
    ["t", عدد الأسطر, [[سورة, آية, نص, ختم], ...]]   كتلة آيات تملأ عددًا من الأسطر
      ختم = 1 إن كان النص ينتهي بعلامة الآية (0 لجزء أول من آية تكمل في الصفحة التالية)
  data/warsh/index.json  →  {"p": [[سورة, آية, جزء] لأول آية في كل صفحة],
                              "s": [صفحة بداية كل سورة], "j": [صفحة بداية كل جزء]}
"""
import json, os, re, sys, urllib.request

SRC = 'https://cdn.jsdelivr.net/gh/thetruetruth/quran-data-kfgqpc@main/warsh/data/warshData_v10.json'
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
OUT = os.path.join(ROOT, 'data', 'warsh')


def load():
    if len(sys.argv) > 1:
        return json.load(open(sys.argv[1], encoding='utf-8-sig'))
    with urllib.request.urlopen(SRC) as r:
        return json.loads(r.read().decode('utf-8-sig'))


def clean(t):
    t = t.replace('‏', '').strip()
    return re.sub(r'[ \t]+', ' ', t)


def split_words(text, first_lines, second_lines):
    """يقسم آية ممتدة بين صفحتين بنسبة عدد أسطرها في كل صفحة."""
    body, num = text.rsplit('\xa0', 1)
    words = body.split(' ')
    total = sum(len(w) for w in words)
    target = total * first_lines / (first_lines + second_lines)
    acc, k = 0, 0
    while k < len(words) - 1 and acc + len(words[k]) / 2 < target:
        acc += len(words[k]) + 1
        k += 1
    return ' '.join(words[:k]), ' '.join(words[k:]) + '\xa0' + num


def main():
    data = load()
    pages = {p: [] for p in range(1, 605)}
    for x in data:
        ps = [int(v) for v in x['page'].split('-')]
        text = clean(x['aya_text'])
        s, a = x['sura_no'], x['aya_no']
        if len(ps) == 1:
            pages[ps[0]].append(dict(s=s, a=a, t=text, ls=x['line_start'], le=x['line_end'], end=1, j=x['jozz']))
        else:
            n1, n2 = 15 - x['line_start'] + 1, x['line_end']
            t1, t2 = split_words(text, n1, n2)
            pages[ps[0]].append(dict(s=s, a=a, t=t1, ls=x['line_start'], le=15, end=0, j=x['jozz']))
            pages[ps[1]].insert(0, dict(s=s, a=a, t=t2, ls=1, le=x['line_end'], end=1, j=x['jozz']))

    os.makedirs(OUT, exist_ok=True)
    index, sura_page, juz_page = [], {}, {}
    for p in range(1, 605):
        items = pages[p]
        assert items, p
        blocks, cur, block = [], 0, None
        for it in items:
            if it['a'] == 1 and it['end'] == 1 and it['ls'] > cur:
                gap = it['ls'] - cur - 1
                block = None
                if gap >= 1:
                    blocks.append(['h', it['s']])
                if gap >= 2 and it['s'] != 9:
                    blocks.append(['b'])
            start = max(it['ls'], cur + 1) if block is None else it['ls']
            if block is None:
                block = ['t', 0, []]
                blocks.append(block)
                block_start = start
            block[2].append([it['s'], it['a'], it['t'], it['end']])
            cur = max(cur, it['le'])
            block[1] = cur - block_start + 1
        json.dump({'j': items[0]['j'], 'b': blocks}, open(os.path.join(OUT, f'{p}.json'), 'w', encoding='utf-8'),
                  ensure_ascii=False, separators=(',', ':'))
        index.append([items[0]['s'], items[0]['a'], items[0]['j']])
        for it in items:
            sura_page.setdefault(it['s'], p)
            juz_page.setdefault(it['j'], p)
    index = {'p': index, 's': [sura_page[i] for i in range(1, 115)], 'j': [juz_page[i] for i in range(1, 31)]}
    json.dump(index, open(os.path.join(OUT, 'index.json'), 'w', encoding='utf-8'), separators=(',', ':'))
    print('pages:', len(index['p']), 'ayat:', len(data))


if __name__ == '__main__':
    main()
