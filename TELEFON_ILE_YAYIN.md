# Telefonla Yayın Rehberi (Bilgisayarsız)

> Sadece Android telefonun varsa **kodu telefonda derleyemezsin** (Android projesi, SDK ve
> Gradle gerektirir; Termux'ta teorik olarak kurulabilir ama saatlerce uğraş ve çoğu paket
> sorunlu olduğu için önerilmez). Bunun yerine **bulutta (GitHub Actions) derle**,
> üretilen dosyayı telefona indir, **Play Console'u telefondan** doldur. Tamamı telefondan yapılır.

## Kısaca Akış
GitHub repo → kodları yükle → keystore üret (Codespaces) → GitHub Actions ile imzalı AAB üret
→ AAB'yi telefona indir → Play Console'a yükle → 12 testçi + 14 gün → yayınla.

---

## Adım 1 — GitHub Hesabı ve Repo
1. Telefon tarayıcısında [github.com](https://github.com) aç, ücretsiz hesap oluştur.
2. **+ → New repository** → ad: `aichatbot` → Public veya Private → **Create repository**.
   (Dilersen otomatik gelen README'yi oluştur.)

## Adım 2 — Proje Dosyalarını Repo'ya Yükle
1. `AIChatBot-starter-v2.zip`'i telefona indir, bir zip açıcı (ZArchiver vb.) ile çıkar.
2. Repo sayfasında **Add file → Upload files**.
3. Android "Dosyalar" seçicisi klasör seçemez; **klasörleri tek tek oluşturup** içindeki
   dosyaları seçerek yükle:
   - Kök: `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`
   - `app/` → `build.gradle.kts`, `proguard-rules.pro`
   - `app/src/main/` → `AndroidManifest.xml`
   - `app/src/main/java/...` → 4 `.kt` dosyası (aynı klasör yapısını kur: `com/example/aichatbot/...`)
   - `app/src/main/res/values/` → `strings.xml`, `themes.xml`, `colors.xml`
   - `app/src/main/res/drawable/` → `ic_launcher_foreground.xml`
   - `app/src/main/res/mipmap-anydpi-v26/` → `ic_launcher.xml`, `ic_launcher_round.xml`
   - `.github/workflows/` → `build-release.yml`
   > Yeni klasör: yükleme ekranında üstte klasör adını yazıp "create folder" yaparsın.
   > 39 dosya, tek seferlik; 20–30 dakika sürer. Aynı yolu iki kez yapman gerekmez.
4. **Commit changes** de.

## Adım 3 — Keystore Üret (İmza Anahtarı) — Codespaces ile
Keystore kaybolursa uygulamanı bir daha güncelleyemezsin. Bu yüzden **parolalarını not al**.
1. Repo sayfasında **Code → Codespaces → Create codespace on main** (ayda ~120 saat ücretsiz).
2. Açılan tarayıcı terminalinde (alt paneldeki terminal):
   ```bash
   sudo apt-get update -qq && sudo apt-get install -y -qq openjdk-17-jdk-headless
   keytool -genkeypair -v -keystore keystore-release.jks -alias release \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
   Sorulan sorulara cevap ver (ad, kuruluş vb. — kendin için, herhangi bir şey olabilir;
   **parolayı iki kez aynı girmelisin ve unutma**).
3. Keystore'u base64'e çevir (bunu GitHub secret'ına koyacağız):
   ```bash
   base64 -w0 keystore-release.jks
   ```
   Çıkan uzun metni kopyala. (Çıktı uzun — Codespaces terminalinde tıklayıp seç,
   sonra sağ tık ile kopyala.)
4. Codespaces'i kapatabilirsin (sol üstte üç çizgi → Codespaces → Stop).

## Adım 4 — GitHub Secret'ları Ekle
Repo sayfasında **Settings → Secrets and variables → Actions → New repository secret**, şunları ekle:
| Secret adı | Değer |
|---|---|
| `KEYSTORE_BASE64` | Adım 3'te kopyaladığın base64 metin |
| `KEYSTORE_PASSWORD` | Keystore parolan |
| `KEY_ALIAS` | `release` |
| `KEY_PASSWORD` | Anahtar parolan (keystore parolasıyla aynı olabilir) |

## Adım 5 — Bulutta Derle (imzalı AAB gelsin)
1. Repo sayfasında **Actions** sekmesi → solda **Build Signed AAB** → sağda **Run workflow → Run workflow**.
2. Bitince yeşil onay görünür; **signed-aab** artefaktının yanındaki **⬇** ile AAB'yi telefona indir.
   (Artefakt 90 gün saklanır.)

## Adım 6 — Play Console (Telefondan, Tarayıcıdan)
1. [play.google.com/console/signup](https://play.google.com/console/signup) → Google hesabıyla gir →
   **US$25 tek seferlik** kayıt ücretini öde (Visa/Mastercard/Amex/Discover).
2. **Kişisel hesap** seçtiysen (13 Kasım 2023 sonrası) uygulaman prodüksiyona geçmeden önce
   **en az 12 testçi + 14 gün kesintisiz kapalı test** şartı var.
   [Resmi kaynak](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)
3. **Create app** → ad, dil, tür → devamında 8 bölümü doldur:
   - Mağaza listesi (açıklama, `art/ic_launcher_512.png` ikonu, ekran görüntüleri)
   - İçerik derecelendirme anketi, hedef kitle, veri güvenliği formu (ChatGPT tarzı: mesajlar sağlayıcıya aktarılır → beyan et)
   - Gizlilik politikası URL'si (`PRIVACY_POLICY.md`'yi düzenleyip GitHub Pages / Google Sites'ta yayınla)
   - **İlk sürüm**: Adım 5'te indirdiğin imzalı `.aab` dosyasını yükle
   - Yayın kanalı olarak **Kapalı test (Closed testing)** seç
4. Testçileri ekle (Google Group ile), 14 gün beklet, **prodüksiyon erişimi başvurusu** yap,
   onay gelince **Prodüksiyon** kanalına sürümü ver.
   [Hedef API kuralı: 31 Ağu 2026'dan itibaren API 36 zorunlu — proje hazır](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en)

## Adım 7 — API Anahtarı (bkz. sohbetimizdeki açıklama)
- Test dönemi için en pratik: **OpenRouter** (ücretsiz modeller) —
  [openrouter.ai/keys](https://openrouter.ai/keys) adresinden anahtar al.
- Uygulamada: Taban URL `https://openrouter.ai/api`, model `openrouter/free` (veya `:free` uzantılı bir model).
- Ya da **Gemini**: [aistudio.google.com/app/apikeys](https://aistudio.google.com/app/apikeys) →
  ücretsiz anahtar; Taban URL `https://generativelanguage.googleapis.com/v1beta/openai`.
- Anahtarı asla kodun içine gömme; uygulama içindeki Ayarlar ekranında kullanıcı kendi girer
  (bu projede böyle). Yayın aşamasında bir proxy ile sunucuda tutman önerilir.

## Maliyet
| Kalem | Tutar |
|---|---|
| Google geliştirici hesabı | US$25 (tek seferlik) |
| GitHub Actions | Ücretsiz (public repo; aylık 2000 dk) |
| Codespaces | Ayda ~120 saat ücretsiz kotalı |
| LLM API (ücretsiz katmanlar) | $0 (OpenRouter ücretsiz modeller / Gemini ücretsiz) |
