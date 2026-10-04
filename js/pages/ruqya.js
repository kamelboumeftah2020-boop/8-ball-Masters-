// الرقية الشرعية: آيات الرقية (من نص المصحف المضمّن)، وأدعيتها الثابتة في السنة، وتلاوتها مسموعة
import { $, arNum, esc, fetchJSON, copyText, icons, durLabel } from '../core.js';
import { surahName, playLecture, toggle, isCurrentLecture, audio, events as pEvents } from '../player.js';
import { getLectures } from './mawaiz.js';

// [السورة، من، إلى، عنوان، ملاحظة]
const AYAT = [
  [1, 1, 7, 'سورة الفاتحة', 'رقى بها أبو سعيد رضي الله عنه لديغًا فبرأ، فقال النبي ﷺ: «وما يدريك أنها رقية» — متفق عليه'],
  [2, 255, 255, 'آية الكرسي', 'من قرأها لم يزل عليه من الله حافظ ولا يقربه شيطان — رواه البخاري'],
  [2, 285, 286, 'خاتمة سورة البقرة', '«من قرأ بالآيتين من آخر سورة البقرة في ليلة كفتاه» — متفق عليه'],
  [112, 1, 4, 'سورة الإخلاص', 'كان النبي ﷺ إذا اشتكى ينفث على نفسه بالمعوذات — متفق عليه'],
  [113, 1, 5, 'سورة الفلق', '«ما تعوّذ متعوّذ بمثلهما» — رواه أبو داود والنسائي، وصححه الألباني'],
  [114, 1, 6, 'سورة الناس', ''],
  [2, 1, 5, 'أول سورة البقرة', 'ومما ذكر أهل العلم قراءته في الرقية'],
  [2, 102, 102, 'من سورة البقرة (في السحر)', 'مما ذكر أهل العلم قراءته على المسحور، كالشيخ ابن باز رحمه الله'],
  [7, 117, 122, 'من سورة الأعراف', 'مما ذكر أهل العلم قراءته على المسحور'],
  [10, 79, 82, 'من سورة يونس', 'مما ذكر أهل العلم قراءته على المسحور'],
  [20, 65, 69, 'من سورة طه', 'مما ذكر أهل العلم قراءته على المسحور'],
];

const DUA = [
  { title: 'رقية النبي ﷺ للمريض', t: 'اللَّهُمَّ رَبَّ النَّاسِ، أَذْهِبِ الْبَأْسَ، اشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا', src: 'متفق عليه' },
  { title: 'رقية جبريل عليه السلام للنبي ﷺ', t: 'بِاسْمِ اللَّهِ أَرْقِيكَ، مِنْ كُلِّ شَيْءٍ يُؤْذِيكَ، مِنْ شَرِّ كُلِّ نَفْسٍ أَوْ عَيْنِ حَاسِدٍ، اللَّهُ يَشْفِيكَ، بِاسْمِ اللَّهِ أَرْقِيكَ', src: 'رواه مسلم' },
  { title: 'ومن رقية جبريل أيضًا', t: 'بِاسْمِ اللَّهِ يُبْرِيكَ، وَمِنْ كُلِّ دَاءٍ يَشْفِيكَ، وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ، وَشَرِّ كُلِّ ذِي عَيْنٍ', src: 'رواه مسلم' },
  { title: 'من يجد ألمًا في جسده', t: 'يضع يده على موضع الألم ويقول: بِسْمِ اللَّهِ (ثلاثًا)، ثم يقول سبع مرات: أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ', src: 'رواه مسلم' },
  { title: 'الدعاء للمريض', t: 'أَسْأَلُ اللَّهَ الْعَظِيمَ، رَبَّ الْعَرْشِ الْعَظِيمِ، أَنْ يَشْفِيَكَ (سبع مرات)', src: 'رواه أبو داود والترمذي، وصححه الألباني' },
  { title: 'تعويذ الأطفال', t: 'أُعِيذُكَ بِكَلِمَاتِ اللَّهِ التَّامَّةِ، مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ', src: 'رواه البخاري — وكان النبي ﷺ يعوّذ بها الحسن والحسين' },
  { title: 'رقية القرحة والجرح', t: 'يضع ريقه على إصبعه ثم يضعها بالتراب ويقول: بِسْمِ اللَّهِ، تُرْبَةُ أَرْضِنَا، بِرِيقَةِ بَعْضِنَا، يُشْفَى سَقِيمُنَا، بِإِذْنِ رَبِّنَا', src: 'متفق عليه' },
  { title: 'إذا رأى ما يعجبه (من العين)', t: 'يدعو بالبركة: اللَّهُمَّ بَارِكْ فِيهِ', src: '«إذا رأى أحدكم من أخيه ما يعجبه فليدعُ له بالبركة» — رواه أحمد وابن ماجه، وصححه الألباني' },
  { title: 'النفث في الكفين', t: 'يجمع كفّيه ثم ينفث فيهما ويقرأ الإخلاص والفلق والناس، ثم يمسح بهما ما استطاع من جسده، ثلاث مرات', src: 'رواه البخاري' },
];

export async function renderRuqya(view, args, ctx) {
  ctx.back('history');
  view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';
  const surahs = new Map();
  await Promise.all([...new Set(AYAT.map(a => a[0]))].map(async s => surahs.set(s, await fetchJSON(`data/quran/${s}.json`))));
  const lec = await getLectures().catch(() => null);
  if (!ctx.alive()) return;
  const sp = lec?.speakers.find(s => s.id === 'ruqya');
  const list = sp ? lec.items.ruqya : [];
  const ayat = AYAT.map(([s, from, to, title, note]) => {
    const ay = surahs.get(s).slice(from - 1, to);
    const text = ay.map(a => `${a[2]} ﴿${arNum(a[1])}﴾`).join(' ');
    return { title, note, text, ref: `[سورة ${surahName(s)}: ${from}${to > from ? '–' + to : ''}]` };
  });
  view.innerHTML = `
    <div class="note-box">${icons.info} الرقية الشرعية تكون بكلام الله وأسمائه وصفاته والأدعية الثابتة، باللسان العربي المفهوم، مع اعتقاد أنها لا تؤثر بذاتها بل بتقدير الله تعالى. ويرقي المسلم نفسه وأهله؛ فهو أرجى للإجابة.</div>
    ${list.length ? `<div class="section-head"><h2>الرقية مسموعة</h2><a href="#/mawaiz/s/ruqya">التحميل</a></div>
    <div class="list-card" id="rAudio">${list.map((it, i) => `<button class="row lecture" data-i="${i}"><span class="play-ic">${icons.play}</span><span class="meta"><strong>${esc(it.t.replace('الرقية الشرعية بصوت ', ''))}</strong><small>${it.d ? durLabel(it.d) : ''}</small></span></button>`).join('')}</div>` : ''}
    <div class="section-head"><h2>آيات الرقية</h2></div>
    <div class="list" id="rAyat">${ayat.map((a, i) => `
      <article class="card dhikr">
        <h3>${esc(a.title)}</h3>
        <p class="dhikr-text quran">${esc(a.text)}</p>
        ${a.note ? `<p class="fadl">${esc(a.note)}</p>` : ''}
        <div class="dhikr-foot"><small class="src">${esc(a.ref)}</small><button class="icon-btn sm" data-copy="a${i}" aria-label="نسخ">${icons.copy}</button></div>
      </article>`).join('')}</div>
    <div class="section-head"><h2>أدعية الرقية من السنة</h2></div>
    <div class="list" id="rDua">${DUA.map((d, i) => `
      <article class="card dhikr">
        <h3>${esc(d.title)}</h3>
        <p class="dhikr-text">${esc(d.t)}</p>
        <div class="dhikr-foot"><small class="src">${esc(d.src)}</small><button class="icon-btn sm" data-copy="d${i}" aria-label="نسخ">${icons.copy}</button></div>
      </article>`).join('')}</div>
    <div class="note-box warn">${icons.info} احذر من السحرة والمشعوذين، ومن التمائم والحروز والطلاسم وما لا يُفهم معناه، فقد قال النبي ﷺ: «إن الرقى والتمائم والتِّوَلة شرك» — رواه أبو داود، وصححه الألباني؛ والمقصود الرقى الشركية.</div>`;
  view.addEventListener('click', e => {
    const c = e.target.closest('[data-copy]');
    if (c) {
      const k = c.dataset.copy, i = +k.slice(1);
      copyText(k[0] === 'a' ? `﴿${ayat[i].text}﴾ ${ayat[i].ref}` : `${DUA[i].t}\n— ${DUA[i].src}`);
      return;
    }
    const r = e.target.closest('#rAudio .lecture');
    if (r && sp) { const i = +r.dataset.i; if (isCurrentLecture('ruqya', i)) toggle(); else playLecture(sp, list, i); }
  });
  const mark = () => document.querySelectorAll('#rAudio .lecture').forEach(el => {
    const cur = isCurrentLecture('ruqya', +el.dataset.i);
    el.classList.toggle('current', cur);
    el.querySelector('.play-ic').innerHTML = cur && !audio.paused ? icons.pause : icons.play;
  });
  pEvents.addEventListener('change', mark); pEvents.addEventListener('state', mark);
  ctx.cleanup(() => { pEvents.removeEventListener('change', mark); pEvents.removeEventListener('state', mark); });
  mark();
}
