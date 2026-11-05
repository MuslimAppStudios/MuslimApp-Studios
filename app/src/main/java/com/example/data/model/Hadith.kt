package com.example.data.model

data class Hadith(
    val id: Int,
    val text: String,
    val source: String,
    val narrator: String,
    val reflection: String = "",
    val topic: String = "Ahlak"
)

val CuratedHadiths = listOf(
    Hadith(
        id = 1,
        text = "Ameller niyetlere göredir. Herkes için ancak niyet ettiği şey vardır.",
        source = "Buhârî (Bed'ü'l-Vahy, 1), Müslim (İmâre, 155)",
        narrator = "Hz. Ömer b. Hattâb (r.a.)",
        reflection = "Bir amelin Allah katında değer kazanmasının yegâne şartı, sırf O'nun rızası gözetilerek ihlasla yapılmasıdır.",
        topic = "İhlas ve Niyet"
    ),
    Hadith(
        id = 2,
        text = "Sizin en hayırlınız, Kur'an'ı öğrenen ve onu öğretendir.",
        source = "Buhârî (Fezâilü'l-Kur'ân, 21), Tirmizî (Sevâbül-Kur'ân, 14)",
        narrator = "Hz. Osman b. Affân (r.a.)",
        reflection = "Kur'an ile bağ kurmak, onu okuyup anlamak ve başkalarına ulaştırmak en şerefli meşguliyettir.",
        topic = "Kur'an"
    ),
    Hadith(
        id = 3,
        text = "Müslüman, elinden ve dilinden diğer Müslümanların emniyette olduğu (zarar görmediği) kimsedir.",
        source = "Buhârî (Îmân, 4), Müslim (Îmân, 64)",
        narrator = "Hz. Abdullah b. Amr (r.a.)",
        reflection = "Gerçek dindarlık sadece ibadetlerde değil, insan ilişkilerinde güvenilir, adil ve nezaketli olmaktadır.",
        topic = "Güzel Ahlak"
    ),
    Hadith(
        id = 4,
        text = "Kıyamet gününde kulun ilk hesaba çekileceği ameli namazdır. Eğer namazı düzgün çıkarsa kurtulmuş ve kazanmıştır.",
        source = "Tirmizî (Salât, 188), Ebû Dâvûd (Salât, 145)",
        narrator = "Hz. Ebû Hüreyre (r.a.)",
        reflection = "Namaz dinin direğidir; diğer tüm salih amellerin kabulü namazın sıhhatine bağlı kılınmıştır.",
        topic = "Namaz"
    ),
    Hadith(
        id = 5,
        text = "Dua, ibadetin ta kendisidir (özüdür).",
        source = "Ebû Dâvûd (Vitr, 23), Tirmizî (Daavât, 1)",
        narrator = "Hz. Nu'mân b. Beşîr (r.a.)",
        reflection = "Kul dua ederek acziyetini itiraf eder, Cenab-ı Hakk'ın sınırsız kudretine sığınır; bu kulluğun zirvesidir.",
        topic = "Dua"
    ),
    Hadith(
        id = 6,
        text = "Birbirinize buğzetmeyiniz, birbirinize haset etmeyiniz, birbirinize sırt çevirmeyiniz. Ey Allah'ın kulları, kardeş olunuz!",
        source = "Buhârî (Edeb, 57), Müslim (Birr, 23)",
        narrator = "Hz. Enes b. Mâlik (r.a.)",
        reflection = "Kin, haset ve küslük kalbi karartır; affedicilik ve muhabbet toplumu kaynaştırır.",
        topic = "Kardeşlik"
    ),
    Hadith(
        id = 7,
        text = "İki nimet vardır ki insanların çoğu onların kıymetini bilmekte aldanmıştır: Sağlık ve boş vakit.",
        source = "Buhârî (Rikâk, 1), Tirmizî (Zühd, 1)",
        narrator = "Hz. İbn Abbâs (r.a.)",
        reflection = "Gençlik, sıhhat ve zaman geri gelmeyecek hazinelerdir; ebedi hayatı kazanmak için iyi değerlendirilmelidir.",
        topic = "Zaman ve Şükür"
    ),
    Hadith(
        id = 8,
        text = "Kolaylaştırınız, zorlaştırmayınız; müjdeleyiniz, nefret ettirmeyiniz.",
        source = "Buhârî (İlim, 11), Müslim (Cihâd, 6)",
        narrator = "Hz. Enes b. Mâlik (r.a.)",
        reflection = "İslam fıtrat dinidir. İnsanlara dini sevdirerek ve merhamet diliyle yaklaşmak Nebevî usuldür.",
        topic = "Tebliğ ve Kolaylık"
    ),
    Hadith(
        id = 9,
        text = "Kişi sevdiğiyle beraberdir.",
        source = "Buhârî (Edeb, 96), Müslim (Birr, 165)",
        narrator = "Hz. Abdullah b. Mes'ûd (r.a.)",
        reflection = "Kimin yolundan gidersek, gönlümüzü kime bağlarsak ahirette de onunla haşrolunuruz.",
        topic = "Sevgi ve Muhabbet"
    ),
    Hadith(
        id = 10,
        text = "Mümin bir delikten iki defa ısırılmaz (aynı hataya iki kez düşmez).",
        source = "Buhârî (Edeb, 83), Müslim (Zühd, 63)",
        narrator = "Hz. Ebû Hüreyre (r.a.)",
        reflection = "Müslüman ferasetli, basiretli ve uyanık olmalıdır.",
        topic = "Basiret ve Feraset"
    )
)
