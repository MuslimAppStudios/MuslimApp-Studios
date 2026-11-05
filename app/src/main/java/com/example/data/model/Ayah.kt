package com.example.data.model

data class Ayah(
    val id: Int,
    val surahName: String,
    val ayahNumber: Int,
    val arabicText: String,
    val turkishTranslation: String,
    val tafsirNote: String = "",
    val topic: String = "Hidayet"
)

val CuratedAyahs = listOf(
    Ayah(
        id = 1,
        surahName = "Bakara Sûresi",
        ayahNumber = 186,
        arabicText = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
        turkishTranslation = "Kullarım sana beni sorduklarında bilsinler ki şüphesiz ben onlara çok yakınım. Bana dua edenin duasına icabet ederim.",
        tafsirNote = "Cenab-ı Hak kuluna şah damarından daha yakındır; her samimi yalvarış ve niyaz O'nun katında karşılıksız kalmaz.",
        topic = "Dua ve Yakınlık"
    ),
    Ayah(
        id = 2,
        surahName = "İnşirah Sûresi",
        ayahNumber = 5,
        arabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
        turkishTranslation = "Şüphesiz her güçlükle beraber muhakkak bir kolaylık vardır. Evet, her güçlükle beraber bir kolaylık vardır.",
        tafsirNote = "Zorluk ne kadar ağırlaşırsa kolaylığın gelmesi o kadar yakındır. Müminin kalbine ferahlık ve sabır müjdesidir.",
        topic = "Sabır ve Ümit"
    ),
    Ayah(
        id = 3,
        surahName = "Ra'd Sûresi",
        ayahNumber = 28,
        arabicText = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
        turkishTranslation = "Onlar, iman etmiş ve kalpleri Allah'ın zikriyle huzura kavuşmuş kimselerdir. Dikkat edin; kalpler ancak Allah'ı anmakla mutmain olur.",
        tafsirNote = "Dünyevi hiçbir fani şey kalbe kalıcı huzur veremez; ruhun gıdası ve şifası ancak Yüce Yaratıcı'yı zikretmektir.",
        topic = "Huzur ve Zikir"
    ),
    Ayah(
        id = 4,
        surahName = "Zümer Sûresi",
        ayahNumber = 53,
        arabicText = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا",
        turkishTranslation = "De ki: Ey kendilerine kötülük edip aşırı giden kullarım! Allah'ın rahmetinden ümidinizi kesmeyin. Muhakkak ki Allah bütün günahları bağışlar.",
        tafsirNote = "Tevbe kapısı nefes aldıkça ardına kadar açıktır. Rahmeti gazabını geçmiş olan Rabbimiz günahkar da olsa dönen kulunu sever.",
        topic = "Tevbe ve Mağfiret"
    ),
    Ayah(
        id = 5,
        surahName = "Tâhâ Sûresi",
        ayahNumber = 14,
        arabicText = "إِنَّنِي أَنَا اللَّهُ لَا إِلَٰهَ إِلَّا أَنَا فَاعْبُدْنِي وَأَقِمِ الصَّلَاةَ لِذِكْرِي",
        turkishTranslation = "Şüphesiz ben, evet ben Allah'ım. Benden başka hiçbir ilâh yoktur. Şu halde yalnız bana kulluk et ve beni anmak için namazı dosdoğru kıl.",
        tafsirNote = "Namaz müminin miracı ve Allah'ı hatırlamanın en yüce vesilesidir.",
        topic = "Namaz"
    ),
    Ayah(
        id = 6,
        surahName = "Haşr Sûresi",
        ayahNumber = 18,
        arabicText = "يَا أَيُّهَا الَّذِينَ آمَنُوا اتَّقُوا اللَّهَ وَلْتَنظُرْ نَفْسٌ مَّا قَدَّمَتْ لِغَدٍ ۖ وَاتَّقُوا اللَّهَ",
        turkishTranslation = "Ey iman edenler! Allah'a karşı gelmekten sakının ve her nefis yarın için (ahirete) ne hazırladığına baksın.",
        tafsirNote = "Ömür sermayesi hızla tükenirken her gün kendimizi muhasebe etmek ve ahiret azığı hazırlamak esastır.",
        topic = "Muhasebe ve Takva"
    ),
    Ayah(
        id = 7,
        surahName = "Talâk Sûresi",
        ayahNumber = 3,
        arabicText = "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ ۚ إِنَّ اللَّهَ بَالِغُ أَمْرِهِ",
        turkishTranslation = "Kim Allah'a tevekkül ederse O, kendisine yeter. Şüphesiz Allah, emrini yerine getirendir.",
        tafsirNote = "Sebeplere sarıldıktan sonra neticeyi Allah'a havale eden kul, endişe ve telaştan kurtulur.",
        topic = "Tevekkül"
    ),
    Ayah(
        id = 8,
        surahName = "Âl-i İmrân Sûresi",
        ayahNumber = 134,
        arabicText = "الَّذِينَ يُنفِقُونَ فِي السَّرَّاءِ وَالضَّرَّاءِ وَالْكَاظِمِينَ الْغَيْظَ وَالْعَافِينَ عَنِ النَّاسِ ۗ وَاللَّهُ يُحِبُّ الْمُحْسِنِينَ",
        turkishTranslation = "Onlar bollukta da darlıkta da infak edenler, öfkelerini yutanlar ve insanları affedenlerdir. Allah iyilik edenleri sever.",
        tafsirNote = "Güçlü insan bileği bükülmeyen değil, öfkelendiğinde nefsine hakim olup bağışlayandır.",
        topic = "Ahlak ve İhsan"
    ),
    Ayah(
        id = 9,
        surahName = "Ankebût Sûresi",
        ayahNumber = 45,
        arabicText = "إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنكَرِ ۗ وَلَذِكْرُ اللَّهِ أَكْبَرُ",
        turkishTranslation = "Şüphesiz namaz, insanı hayasızlıktan ve kötülükten alıkoyar. Allah'ı anmak ise elbette en büyük ibadettir.",
        tafsirNote = "Huşû ile kılınan namaz, mümini günah bataklıklarından koruyan bir zırhtır.",
        topic = "Namaz ve Korunma"
    ),
    Ayah(
        id = 10,
        surahName = "Furkan Sûresi",
        ayahNumber = 74,
        arabicText = "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
        turkishTranslation = "Rabbimiz! Bize eşlerimizden ve nesillerimizden göz aydınlığı olacak kimseler bağışla ve bizi takva sahiplerine önder kıl.",
        tafsirNote = "Huzurlu bir yuva ve salih evlatlar istemek Kur'an'ın tavsiye ettiği en güzel dualardandır.",
        topic = "Aile ve Dua"
    )
)
