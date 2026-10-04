#!/usr/bin/env python3
"""يحفظ نص المصحف (الرسم العثماني) في data/quran/ ليعمل المصحف دون اتصال.

التشغيل: python3 tools/build_quran.py
"""
import json
import urllib.request

with urllib.request.urlopen('https://api.alquran.cloud/v1/quran/quran-uthmani', timeout=180) as r:
    data = json.load(r)['data']
for s in data['surahs']:
    ayahs = [[a['number'], a['numberInSurah'], a['text'].replace('\ufeff', ''), a['page'], a['juz'], 1 if a['sajda'] else 0] for a in s['ayahs']]
    with open(f"data/quran/{s['number']}.json", 'w', encoding='utf-8') as f:
        json.dump(ayahs, f, ensure_ascii=False, separators=(',', ':'))
# فهرس الصفحات (٦٠٤ صفحة كمصحف المدينة): لكل صفحة مقاطعها [السورة، أول آية، آخر آية]، والجزء والحزب،
# وربع الحزب [رقم الربع ١–٢٤٠، ويبدأ في هذه الصفحة ١/٠]: الربع الذي يبدأ في الصفحة إن وُجد، وإلا الربع الجاري
pages = {}
prev_q = 0
for s in data['surahs']:
    for a in s['ayahs']:
        q = a['hizbQuarter']
        pg = pages.setdefault(a['page'], {'parts': [], 'juz': a['juz'], 'hizb': (q - 1) // 4 + 1, 'q': [q, 0]})
        if q != prev_q and pg['q'][1] == 0:
            pg['q'] = [q, 1]
        prev_q = q
        if pg['parts'] and pg['parts'][-1][0] == s['number']:
            pg['parts'][-1][2] = a['numberInSurah']
        else:
            pg['parts'].append([s['number'], a['numberInSurah'], a['numberInSurah']])
index = [[pages[p]['parts'], pages[p]['juz'], pages[p]['hizb'], pages[p]['q']] for p in sorted(pages)]
with open('data/pages.json', 'w', encoding='utf-8') as f:
    json.dump(index, f, separators=(',', ':'))
print('surahs:', len(data['surahs']), 'pages:', len(index))
