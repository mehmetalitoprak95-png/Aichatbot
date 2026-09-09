# Play Store Yayın Rehberi — Adım Adım

> Güncel tarih: 7 Eylül 2026. Play politikaları zamanla değişir; yayından önce
> [resmi Play Console yardım merkezini](https://support.google.com/googleplay/android-developer/) kontrol et.

## 1) Geliştirici Hesabı Aç (`$25`, tek seferlik)
1. [play.google.com/console/signup](https://play.google.com/console/signup) adresine git.
2. E-posta ile Google hesabına gir, ülke/şirket bilgilerini doldur.
3. **US$25 tek seferlik kayıt ücreti** öde (Visa, MasterCard, Amex, Discover kabul edilir).
   - Yıllık üyelik yok, uygulama başına ek ücret yok.
   - [Resmi kaynak: Google Play Geliştirici Hesabı Kaydı](https://support.google.com/googleplay/android-developer/answer/6112435?hl=en)
4. **Kişisel hesap** seçtiysen (13 Kasım 2023 sonrası açılan hesaplar):
   - Yeni uygulaman **prodüksiyona geçmeden önce** aşağıdaki teste tabi:
   - **En az 12 test kullanıcısının dahil olduğu, 14 gün kesintisiz süren kapalı (closed) test.**
   - Test bitince Play Console'dan prodüksiyon erişimine başvur, onayı bekle.
   - [Resmi kaynak: Yeni kişisel geliştirici hesapları için uygulama testi gereksinimleri](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)
   - İpucu: Bu 14 günü beklerken mağaza listesini ve uygulama kalitesini kusursuzlaştır.

## 2) Projeyi Yükle ve Derle
1. [Android Studio](https://developer.android.com/studio) ile `AIChatBot/` klasörünü aç.
2. **Uygulama kimliğini değiştir** (zorunlu):
   - `app/build.gradle.kts` → `applicationId = "com.example.aichatbot"` satırını
     kendi alan adın gibi benzersiz bir değere çevir:
     `com.seninmarkan.sohbet` vb. (`com.example` ile yayın yapılamaz; ilk yüklemeden sonra değişmez).
3. **İmzalama anahtarı (keystore) oluştur**:
   - Android Studio: `Build > Generate Signed App Bundle / APK > Create new...`
   - Keystore'u **çok güvenli bir yere yedekle** — kaybedersen uygulamanı bir daha güncelleyemezsin.
4. **AAB üret**: `Build > Generate Signed App Bundle` → `.aab` dosyası (ilk yükleme için zorunlu format).

## 3) Play Console'a Yükle
1. **Play Console > Tüm uygulamalar > Uygulama oluştur**.
2. Uygulama adı, varsayılan dil, uygulama türü (uygulama/oyun) ve ücretsiz/ücretli seç.
3. Menüdeki adımları sırayla doldur (**8 bölüm**: Mağaza listesi, Uygulama erişimi, İçerik derecelendirmesi, Hedef kitle, Veri güvenliği, Politikalar, Reklam, İlk sürüm).
4. **Mağaza listesi**:
   - Kısa açıklama (80 karakter), tam açıklama (4000 karakter), kategori, uygulama ikonu (512×512 PNG — projedeki `art/ic_launcher_512.png`), öne çıkan görsel (feature graphic 1024×500), ekran görüntüleri (en az 2, cihaz ekranı).
   - En az 2 dil etiketi gerekebilir; Türkçe + İngilizce eklemen yeterli olur.
5. **İçerik derecelendirmesi (IARC)**: Kısa anketi doldur; uygulama için derece belirlenir.
6. **Hedef kitle**: 13 yaş altına yönelik değil işaretle; (AI politikasında 18+ önerilir — karar senin, içerik güvenliği önlemlerine bağlı).
7. **Veri güvenliği formu (Data Safety)**: Topladığın verileri bildir. Bu starter için:
   - "Kullanıcı mesajları (sohbet içeriği) sağlayıcıya aktarılır" → **toplanır + paylaşılır**,
   - "API anahtarı cihazda saklanır" → **toplanmaz** olarak beyan edilebilir,
   - Diğer veriler: uygulama bu sürümde analitik kullanmıyor → hiçbiri.
   - Form ile kodun **tutarlı olması zorunlu**, inceleme bunu kontrol eder.
8. **AI İçerik Politikası** (AI üreten uygulamalar için):
   - Uygulama içinde **raporlama/bildirim arayüzü** bulunmalı (bu starter'da: mesaja uzun bas → "Yanıtı raporla"; yayın öncesi bunu gerçek backend'ine bağla).
   - Zararlı/uygunsuz içerik üretimini önleyen filtreler tanımlanmalı (model tarafı sağlar; yine de senin kullanım koşullarını ve yanıt filtrelerini dokümante et).
   - [Resmi kaynak: AI Üretilen İçerik politikası](https://support.google.com/googleplay/android-developer/answer/14094294?hl=en) ve [AI üreten uygulamalar için gereklilikler](https://support.google.com/googleplay/android-developer/answer/13985936?hl=en)
9. **Gizlilik politikası URL'si**: `PRIVACY_POLICY.md`'yi kendi bilgilerinle düzenle; GitHub Pages / Google Sites'ta yayınla; linki konsola gir.
10. **İlk sürüm (Release)**: İmzalı `.aab` dosyasını yükle; **Önce iç test > kapalı test** kanalını seç (yeni kişisel hesaplar için zorunlu test süreci budur).

## 4) Test Süreci (Yeni Kişisel Hesaplar İçin Zorunlu)
1. **Kapalı test** oluştur (Closed testing), 12+ test kullanıcısını e-posta ile ekle (Google Grupları ile yönetmek kolaydır).
2. Test kullanıcıları uygulamayı kurup **14 gün kesintisiz** test etsin ve "opt-in" etsin.
3. 14 gün dolunca **"Prodüksiyon erişimi başvurusu"** yap; Google incelemesini bekle (genelde birkaç gün).
4. Onay sonrası **prodüksiyon** kanalına sürüm çıkar → genel inceleme (saatler–birkaç gün).

> İpucu: 14 günlük bekleme sırasında sürümü kapalı testte tut; test kullanıcılarının geri bildirimine göre hata düzelt, ekran görüntülerini güncelle. Test kullanıcıları "12 kişi" şartını sağlamalı — google gruplarında 13-15 kişi toplamak sigorta sağlar.

## 5) Yayın Sonrası
- Veri güvenliği formu ve politikaların güncel kalması için yılda bir kontrol.
- **SDK hedefi kuralı**: 31 Ağustos 2026'den itibaren yeni uygulama ve güncellemeler en az **Android 16 (API 36)** hedeflemeli. Bu starter `targetSdk = 36` ile geldi; yıllık güncellemeleri takip et.
  - [Resmi kaynak: Hedef API düzeyi gereksinimleri](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en)
- Kullanıcı yorumlarına dön; AI uygulamalarında kötüye kullanım bildirimlerini izle.

## Maliyet Özeti (Başlangıç)
| Kalem | Tutar |
|---|---|
| Google geliştirici hesabı (tek seferlik) | US$25 |
| LLM API kullanımı (ör. gpt-4o-mini) | Kullanıma göre (çok düşük; test döneminde birkaç dolar) |
| Backend proxy (opsiyonel) | Kendi sunucun yoksa ~$0–10/ay |
| Hosting (gizlilik politikası sayfası) | Ücretsiz (GitHub Pages) |
