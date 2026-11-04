# YC Live - Ultra Düşük Gecikmeli (<70ms) Android Ekran Yayını

**YC Live**, PUBG Mobile gibi yüksek tempolu, HDR ve 60/90 FPS rekabetçi oyunları yerel Wi-Fi ağı üzerinden bilgisayar veya tablet tarayıcısına **minimum gecikme (<70ms)** ve **sıfır kare kaybı (drop/stuttering)** ile aktaran bağımsız bir Android uygulamasıdır.

Hantal WebRTC, gecikmeli RTSP veya yüksek bant genişliği tüketen HTTP MJPEG protokolleri yerine; **WebSocket üzerinden saf H.264 NAL Unit (Annex-B)** akışı ve istemci tarafında **WebCodecs API (`VideoDecoder`)** ile sıfır tamponlama (`Buffer = 0`) mimarisini kullanır.

---

## 🚀 Öne Çıkan Özellikler

- **Donanımsal Kodlama (Hardware Encoding):** `MediaProjection` + `VirtualDisplay` + `MediaCodec` (Input Surface modu, sıfır bellek kopyalama).
- **Gömülü Web & WebSocket Sunucusu:** Android içinde hafif, yüksek performanslı **Ktor CIO** motoru (Port: 8080).
- **Sıfır Gecikmeli Oynatıcı (Zero-Latency WebCodecs Player):** Harici kütüphane gerektirmeyen, tarayıcının GPU'sunu doğrudan kullanan HTML5 oynatıcı.
- **Anlık Bağlantı ve Hızlı Kurtarma (Instant Sync Frame):** Yeni bir izleyici bağlandığında önbellekteki SPS/PPS parametreleri hemen iletilir ve donanımsal kodlayıcıdan anında IDR Anahtar Kare (Keyframe) talep edilir.
- **Android 14/15 Tam Uyumluluk:** `ForegroundService` (`mediaProjection` tipi) ve ekran kaydı arka plan koruması (`WAKE_LOCK`).
- **Modern Jetpack Compose Dark Arayüzü:** Tek tıkla başlatma/durdurma, anlık IP kopyalama, canlı izleyici sayacı, 720p/1080p çözünürlük, 60/90 FPS ve 15-35 Mbps CBR ayarları.
- **Bulutta Otomatik APK Derleme:** Yerel Android Studio ve SDK kurulumu gerekmeden, GitHub Actions üzerinden 2 dakikada indirilebilir `.apk` üretimi.

---

## 📦 Sıfırdan GitHub ile Otomatik APK Derleme ve Telefona İndirme

Bilgisayarınızda Android Studio, Java veya SDK kurulu olmasına **gerek yoktur**. GitHub Actions sizin yerinize bulutta derleyecektir:

### 1. Boş Bir GitHub Reposu Oluşturun
1. [GitHub](https://github.com/) hesabınıza giriş yapın.
2. Sağ üstteki **+** simgesine tıklayıp **New repository** seçeneğini seçin.
3. Repo adı olarak `YCLive` yazın, repoyu **Public** veya **Private** yapın.
4. "Add a README file" veya .gitignore kutularını **işaretlemeyin** (boş repo olsun) ve **Create repository** butonuna basın.

### 2. Kodları Reponuza Yükleyin
Proje klasörünüzde (`c:\Users\yigit\OneDrive\Desktop\YCLive`) bir terminal (PowerShell veya Komut Satırı) açıp şu komutları sırasıyla çalıştırın:

```bash
git init
git add .
git commit -m "feat: Initial commit YC Live ultra low latency streamer"
git branch -M main
git remote add origin https://github.com/KULLANICI_ADINIZ/YCLive.git
git push -u origin main
```
*(Not: `KULLANICI_ADINIZ` kısmına kendi GitHub kullanıcı adınızı yazın)*

### 3. Otomatik Derlemeyi Takip Edin ve APK'yı İndirin
1. GitHub reponuzun sayfasında üst menüden **Actions** sekmesine tıklayın.
2. **Build YC Live APK** iş akışının otomatik olarak başladığını göreceksiniz.
3. Yaklaşık 2-3 dakika içinde yeşil onay işareti (`✓ Build Android APK`) çıkacaktır.
4. Tamamlanan çalıştırmaya tıklayın ve sayfanın en altındaki **Artifacts** bölümüne inin.
5. **`YCLive-Debug-APK`** dosyasına tıklayarak indirin (ZIP olarak iner).
6. ZIP dosyasını açtığınızda içinden çıkan **`app-debug.apk`** dosyasını Android telefonunuza yükleyin.

---

## 🎮 Kullanım ve Yayın Rehberi

1. **Aynı Ağa Bağlanın:** Android telefonunuz ile yayını izleyeceğiniz bilgisayarın aynı Wi-Fi ağına (tercihen 5 GHz veya kablolu yönlendirici) bağlı olduğundan emin olun.
2. **Uygulamayı Açın:** Telefonunuzda **YC Live** uygulamasını başlatın.
3. **Ayarlarınızı Seçin:**
   - Rekabetçi PUBG Mobile için: **1080p (FHD)** | **60 FPS** (Cihazınız destekliyorsa **90 FPS**) | **25 Mbps**.
4. **YAYINI BAŞLAT:** Butona dokunun ve Android sisteminin sorduğu "Ekran kaydı başlatılsın mı?" onay kutusunda **Şimdi Başlat**'ı seçin.
5. **Tarayıcıda Açın:**
   - Uygulamada görünen bağlantıyı kopyalayın (Örn: `http://192.168.1.50:8080`).
   - Bilgisayarınızda **Google Chrome**, **Microsoft Edge** veya **Opera** tarayıcısını açıp adresi yapıştırın.
   - Görüntü **<70ms** gecikmeyle anında tam donanım hızlandırmalı olarak açılacaktır.
6. **OBS Studio ile Canlı Yayın (Opsiyonel):**
   - OBS içinde Kaynaklar -> **+** -> **Tarayıcı (Browser Source)** ekleyin.
   - URL kısmına `http://[TELEFON_IP]:8080` yazın. Genişlik: `1920`, Yükseklik: `1080` girin. Tam ekran yayın kalitesi elde edin.

---

## 🛠️ Teknik Mimari Detayları

```
+-------------------------------------------------------------+
|                     ANDROID TELEFON                         |
|                                                             |
|  [ Ekran Görüntüsü (PUBG Mobile) ]                           |
|                  │ (Zero-Copy GPU Surface)                  |
|                  ▼                                          |
|  [ MediaCodec Donanımsal Kodlayıcı ] (video/avc Baseline)   |
|                  │ H.264 NAL Units (Annex-B)                |
|                  ▼                                          |
|  [ Ktor CIO Embedded HTTP & WebSocket Server ] (Port: 8080) |
+──────────────────┼──────────────────────────────────────────+
                   │ Yerel Wi-Fi (WebSocket binary frames)
                   ▼
+-------------------------------------------------------------+
|                 İSTEMCİ BİLGİSAYAR / TARAYICI               |
|                                                             |
|  [ WebSocket İstemcisi ]                                    |
|                  │ SPS / PPS / Video Slices                 |
|                  ▼                                          |
|  [ WebCodecs VideoDecoder ] (Buffer = 0, optimizeForLatency)|
|                  │ Decoded VideoFrame                       |
|                  ▼                                          |
|  [ HTML5 Canvas Context 2D ] (Anında Çizim & frame.close()) |
|                  │                                          |
|                  ▼                                          |
|          < 70ms Toplam Gecikme, 60/90 FPS                   |
+-------------------------------------------------------------+
```

---

## 📂 Dosya Yapısı

```
YCLive/
├── .github/workflows/
│   └── build-apk.yml               # GitHub Actions CI/CD otomatik APK derleme
├── gradle/wrapper/
│   └── gradle-wrapper.properties   # Gradle 8.7 yapılandırması
├── app/
│   ├── build.gradle.kts            # Ktor CIO, WebSockets & Jetpack Compose bağımlılıkları
│   ├── proguard-rules.pro          # Optimizasyon ve ProGuard kuralları
│   └── src/main/
│       ├── AndroidManifest.xml     # MediaProjection & ForegroundService izinleri
│       ├── assets/
│       │   └── index.html          # WebCodecs sıfır gecikmeli HTML5 oynatıcı
│       ├── java/com/yclive/streamer/
│       │   ├── MainActivity.kt     # Jetpack Compose ana aktivite ve izin yönetimi
│       │   ├── ScreenCaptureService.kt # MediaProjection, MediaCodec ve servis döngüsü
│       │   ├── LocalStreamServer.kt # Ktor CIO HTTP/WebSocket sunucu motoru
│       │   ├── StreamConfig.kt     # Çözünürlük, FPS ve Bitrate yapılandırmaları
│       │   ├── NetworkUtils.kt     # Yerel IP tespit yardımcı sınıfı
│       │   └── ui/
│       │       ├── MainScreen.kt   # Koyu temalı modern arayüz bileşenleri
│       │       └── theme/          # Renk, tipografi ve tema tanımları
│       └── res/                    # İkonlar, renkler ve XML kaynakları
├── build.gradle.kts                # Kök proje Gradle ayarı (AGP 8.4.2, Kotlin 1.9.24)
├── settings.gradle.kts             # Modül ve repo tanımları
├── gradlew & gradlew.bat           # Gradle Wrapper çalıştırıcıları
└── README.md                       # Detaylı kılavuz
```
