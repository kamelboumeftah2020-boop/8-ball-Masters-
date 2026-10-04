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
print('surahs:', len(data['surahs']))
