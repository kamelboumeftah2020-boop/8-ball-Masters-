#!/usr/bin/env python3
"""يبني فهرس مصحف ورش (مجمع الملك فهد، الإصدار العاشر) من بيانات KFGQPC.

صفحات ورش نفسها تُبنى في التطبيق من المصدر (js/warshData.js)، وتُقرأ من الإنترنت
أو بعد تحميلها؛ وهذا الفهرس الصغير وحده مضمّن ليعمل فهرس السور والأجزاء دون اتصال.
(منطق بناء الكتل هنا مطابق لما في js/warshData.js، ويفيد في فحص البيانات.)

المصدر: github.com/thetruetruth/quran-data-kfgqpc (warsh/data/warshData_v10.json)
لكل آية: رقم الصفحة، وسطر البداية والنهاية. ينتج:
  (للفحص فقط، مع --pages) data/warsh/{1..604}.json  →  {"j": الجزء, "b": [كتل]}
    ["h", سورة]                     سطر اسم السورة
    ["b"]                           سطر البسملة
    ["t", عدد الأسطر, [[سورة, آية, نص, ختم], ...]]   كتلة آيات تملأ عددًا من الأسطر
      ختم = 1 إن كان النص ينتهي بعلامة الآية (0 لجزء أول من آية تكمل في الصفحة التالية)
  data/warsh-index.json  →  {"p": [[سورة, آية, جزء] لأول آية في كل صفحة],
                              "s": [صفحة بداية كل سورة], "j": [صفحة بداية كل جزء],
                              "q": [[الحزب, الربع ٠–٣, يبدأ في الصفحة ١/٠] لكل صفحة]}
"""
import json, os, re, sys, urllib.request

SRC = 'https://cdn.jsdelivr.net/gh/thetruetruth/quran-data-kfgqpc@main/warsh/data/warshData_v10.json'
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
OUT = os.path.join(ROOT, 'data', 'warsh')


def load():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    if args:
        return json.load(open(args[0], encoding='utf-8-sig'))
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


def quarters(data):
    """الأحزاب وأرباعها في رواية ورش، من علامات الأثمان (۞) في نص مجمع الملك فهد.

    في كل جزء حزبان، وفي كل حزب ثمانية أثمان، والربع ثمنان. والعلامة لا تُكتب غالبًا إذا بدأ الثمن
    مع أول سورة (وقليلًا ما تسقط في غيره)، فنكمل الناقص بحيث تتقارب أطوال الأثمان، مقدّمين بدايات السور.
    """
    offs, pos = [], 0
    for x in data:
        offs.append(pos)
        pos += len(x['aya_text']) + 1
    first_page = lambda x: int(str(x['page']).split('-')[0])
    bounds = []  # (الموضع، الصفحة) لبداية كل ثمن
    for j in range(1, 31):
        idx = [i for i, x in enumerate(data) if x['jozz'] == j]
        start, end = offs[idx[0]], offs[idx[-1]] + len(data[idx[-1]]['aya_text'])
        fixed = [(start, first_page(data[idx[0]]))]
        cand = []
        for i in idx:
            t = data[i]['aya_text']
            if '۞' in t and not (i == idx[0] and t.index('۞') <= 2):
                fixed.append((offs[i] + t.index('۞'), first_page(data[i])))
            elif i != idx[0]:
                cand.append((offs[i], first_page(data[i]), data[i]['aya_no'] == 1))
        need = 16 - len(fixed)
        assert need >= 0, (j, len(fixed))
        chosen = pick(sorted(fixed), cand, need, end) if need else []
        b = sorted(fixed + chosen)
        assert len(b) == 16, (j, len(b))
        for e, (o, pg) in enumerate(b):
            bounds.append((o, pg, 2 * j - 1 + e // 8, (e % 8) // 2, e % 2 == 0))
    # لكل صفحة: الربع الذي يبدأ فيها إن وُجد، وإلا الربع الجاري عند أولها
    out = []
    for p in range(1, 605):
        first = min(offs[i] for i, x in enumerate(data) if first_page(x) == p)
        here = [b for b in bounds if b[1] == p and b[4]]
        if here:
            out.append([here[0][2], here[0][3], 1])
        else:
            cur = [b for b in bounds if b[0] <= first and b[4]][-1]
            out.append([cur[2], cur[3], 0])
    return out


def pick(fixed, cand, k, end):
    """يختار k من بدايات الآيات حدودًا للأثمان الناقصة، بأقل تباين في أطوالها، مع تفضيل بدايات السور."""
    span = (end - fixed[0][0]) / 16
    penalty = {True: 0, False: (span * 0.6) ** 2}
    pts = sorted([(o, pg, True, 0) for o, pg in fixed] + [(o, pg, False, penalty[s1]) for o, pg, s1 in cand])
    n = len(pts)
    INF = float('inf')
    # best[i][c]: أقل كلفة إذا كان pts[i] حدًّا مختارًا واستُعمل c من المرشحين حتى i
    best = [[INF] * (k + 1) for _ in range(n)]
    back = [[None] * (k + 1) for _ in range(n)]
    best[0][0] = 0
    for i in range(1, n):
        for c in range(k + 1):
            cc = c - (0 if pts[i][2] else 1)
            if cc < 0:
                continue
            for h in range(i - 1, -1, -1):
                if best[h][cc] < INF:
                    v = best[h][cc] + (pts[i][0] - pts[h][0]) ** 2 + pts[i][3]
                    if v < best[i][c]:
                        best[i][c], back[i][c] = v, (h, cc)
                if pts[h][2]:
                    break  # لا يُتخطّى حدّ ثابت
    # آخر حدّ: الثابت الأخير أو مرشح بعده
    last_fixed = max(i for i in range(n) if pts[i][2])
    end_cost = [(best[i][k] + (end - pts[i][0]) ** 2, i) for i in range(last_fixed, n) if best[i][k] < INF]
    _, i = min(end_cost)
    chosen, c = [], k
    while i is not None and back[i][c] is not None:
        if not pts[i][2]:
            chosen.append(pts[i][:2])
        i, c = back[i][c]
    return chosen


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

    if '--pages' in sys.argv:
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
        if '--pages' in sys.argv:
            json.dump({'j': items[0]['j'], 'b': blocks}, open(os.path.join(OUT, f'{p}.json'), 'w', encoding='utf-8'),
                      ensure_ascii=False, separators=(',', ':'))
        index.append([items[0]['s'], items[0]['a'], items[0]['j']])
        for it in items:
            sura_page.setdefault(it['s'], p)
            juz_page.setdefault(it['j'], p)
    index = {'p': index, 's': [sura_page[i] for i in range(1, 115)], 'j': [juz_page[i] for i in range(1, 31)], 'q': quarters(data)}
    json.dump(index, open(os.path.join(ROOT, 'data', 'warsh-index.json'), 'w', encoding='utf-8'), separators=(',', ':'))
    print('pages:', len(index['p']), 'ayat:', len(data))


if __name__ == '__main__':
    main()
