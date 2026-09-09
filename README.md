# AI Sohbet Asistanı — Android Starter Projesi

Play Store'da yayınlamak için hazırlanmış, OpenAI uyumlu bir AI sohbet uygulaması.

## Teknoloji
- Kotlin + Jetpack Compose (Material 3)
- OkHttp (API çağrıları), coroutines
- Hedef SDK: **36 (Android 16)** — Play Store'un 31 Ağustos 2026 sonrası şartı
- Minimum SDK: 26

## Özellikler
- Sohbet arayüzü (mesaj balonları, otomatik kaydırma, yükleme göstergesi)
- Ayarlar: API taban URL, API anahtarı, model adı (cihazda saklanır)
- Uygunsuz yanıtı raporla (uzun basma) — Play AI içerik politikası gereği
- Uyarlanabilir uygulama ikonu + Play mağaza listesi için 512px PNG

## Bilgisayarında Çalıştırma
1. [Android Studio](https://developer.android.com/studio) kur (güncel sürüm).
2. `File > Open` ile bu klasörü (`AIChatBot/`) aç — Gradle sürümünü otomatik indirir.
3. Telefonunda **Geliştirici Seçenekleri > USB Hata Ayıklama**'yı aç.
4. `Run ▶` ile telefona kur.

## Yapılandırma
Uygulamayı aç → sağ üstteki ⚙️ → gir:
- **API Taban URL**: `https://api.openai.com` (veya kendi backend proxy'n)
- **API Anahtarı**: OpenAI (veya sağlayıcının) anahtarı
- **Model**: `gpt-4o-mini`, `gpt-4o`, Gemini vb. (OpenAI uyumlu uç noktalar)

> OpenRouter, Azure OpenAI, Groq, Gemini (OpenAI uyumlu mod) gibi servisler de aynı
> `/v1/chat/completions` formatını destekler; yalnızca taban URL + anahtar + model değişir.

## Güvenlik (ÖNEMLİ)
- API anahtarını uygulama içine GÖMME. Build edilmiş APK'dan herkes çıkarabilir.
- Bu starter'da kullanıcı kendi anahtarını cihazında girer (SharedPreferences).
- Yayın için önerilen mimari: `Uygulama → senin backend proxy'n → LLM API`.
  Proxy sayesinde anahtar sunucunda kalır, kullanımı loglayabilir, limit koyabilirsin.
  (İstersen sana küçük bir Node.js veya Python proxy kodu da yazabilirim.)

## İçindekiler
- `YAYIN_REHBERI.md` — Play Store yayın sürecinin adım adım anlatımı
- `PRIVACY_POLICY.md` — mağaza listesi için gizlilik politikası metni (düzenle!)
- `art/ic_launcher_512.png` — Play Console'a yüklenecek uygulama ikonu
