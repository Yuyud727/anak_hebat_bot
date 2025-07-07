# 🌟 Bot Anak Hebat 🌈

**Bot Telegram yang dirancang khusus untuk membantu anak-anak menjadi lebih hebat!**

Bot ini menyediakan motivasi harian, tips kebiasaan baik, dan bacaan edukatif untuk mendukung perkembangan karakter positif anak-anak.

---

## 📋 Daftar Isi

- [✨ Fitur Utama](#-fitur-utama)
- [🎯 Target Pengguna](#-target-pengguna)
- [🔧 Instalasi](#-instalasi)
- [⚙️ Konfigurasi](#️-konfigurasi)
- [🚀 Cara Menjalankan](#-cara-menjalankan)
- [📱 Penggunaan Bot](#-penggunaan-bot)
- [👑 Panel Admin](#-panel-admin)
- [🗄️ Database](#️-database)
- [📁 Struktur Project](#-struktur-project)
- [🛠️ Development](#️-development)
- [❓ FAQ](#-faq)
- [📞 Support](#-support)

---

## ✨ Fitur Utama

### 🔥 Untuk Anak-anak:
- **💪 Motivasi Harian**: Kata-kata penyemangat dan afirmasi positif
- **📚 Kebiasaan Baik**: Tips melakukan kebiasaan sehat setiap hari
- **📖 Bacaan Edukatif**: Dongeng, cerita, dan artikel untuk anak
- **🤖 Interaksi Mudah**: Cukup ketik keyword untuk mendapat response

### 👑 Untuk Admin (Orangtua):
- **🎛️ GUI Admin Panel**: Interface yang user-friendly
- **👥 Manajemen Member**: Kelola daftar anak yang terdaftar
- **📢 Broadcast System**: Kirim pesan ke semua anak
- **🔑 Keyword Management**: Kelola response otomatis
- **📊 Statistik**: Monitor aktivitas dan engagement
- **💬 Message Monitoring**: Pantau percakapan anak

---

## 🎯 Target Pengguna

- **👨‍👩‍👧‍👦 Orangtua** yang ingin memberikan konten edukatif untuk anak
- **🏫 Guru** yang menggunakan Telegram untuk komunikasi dengan siswa
- **📚 Pustakawan** yang ingin menyediakan bacaan interaktif
- **🧑‍💻 Developer** yang ingin mengembangkan bot edukatif

---

## 🔧 Instalasi

### 📋 Prerequisites

- **☕ Java JDK 17** atau lebih tinggi
- **🏗️ Apache Maven 3.8+**
- **🎨 JavaFX SDK 24.0.1** (untuk GUI)
- **🗄️ Database**: SQLite (default) atau MySQL
- **📱 Bot Telegram** (token dari @BotFather)

### 📥 Download & Setup

1. **Clone Repository**
```bash
git clone https://github.com/your-username/bot-anak-hebat.git
cd bot-anak-hebat
```

2. **Install JavaFX** (jika belum ada)
```bash
# Download JavaFX SDK 24.0.1 dari:
# https://gluonhq.com/products/javafx/
# Extract ke C:\javafx-sdk-24.0.1\
```

3. **Build Project**
```bash
mvn clean package
```

---

## ⚙️ Konfigurasi

### 📝 Setup Bot Telegram

1. **Buat Bot Baru**
   - Chat dengan [@BotFather](https://t.me/botfather)
   - Gunakan command `/newbot`
   - Ikuti instruksi untuk membuat bot
   - Simpan **bot token** yang diberikan

2. **Dapatkan Admin Chat ID**
   - Kirim pesan ke bot Anda
   - Atau gunakan [@userinfobot](https://t.me/userinfobot) untuk mendapatkan Chat ID

### 🔧 File Konfigurasi

Buat file `src/main/resources/config.properties`:

```properties
# ====================================
# KONFIGURASI BOT ANAK HEBAT
# ====================================

# Bot Configuration
bot.token=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz
bot.username=AnakHebatBot
admin.chat.id=123456789

# Database Configuration (SQLite - Recommended)
database.type=sqlite
database.path=anak_hebat_bot.db

# MySQL Configuration (Alternative)
# database.type=mysql
# mysql.url=jdbc:mysql://localhost:3306/anak_hebat_bot?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
# mysql.username=root
# mysql.password=your_password

# Template Pesan
bot.welcome.message=🌟 Selamat datang di Bot Anak Hebat! 🌈
bot.registration.prompt=🌈 Halo anak hebat! Siapa nama kamu?
bot.phone.prompt=📞 Sekarang tulis nomor telepon orangtua kamu ya!
bot.registration.success=🎉 Selamat datang, {name}! Kamu sekarang sudah menjadi bagian dari komunitas Anak Hebat!
bot.not.registered=🔐 Kamu belum terdaftar nih. Ketik /register dulu ya untuk bergabung!
menu.main=🌟 MENU ANAK HEBAT 🌈 - Mau belajar apa hari ini?
```

---

## 🚀 Cara Menjalankan

### 🖥️ GUI Mode (Recommended)

**Windows:**
```bash
# Double-click run_gui.bat
# atau jalankan dari command prompt:
run_gui.bat
```

**Linux/Mac:**
```bash
chmod +x run_gui.sh
./run_gui.sh
```

### 💻 Console Mode

```bash
java -jar target/mytelegrambot-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 🐳 Docker (Coming Soon)

```bash
docker run -d --name bot-anak-hebat \
  -v $(pwd)/config.properties:/app/config.properties \
  bot-anak-hebat:latest
```

---

## 📱 Penggunaan Bot

### 🔤 Commands untuk Anak

| Command | Deskripsi |
|---------|-----------|
| `/start` | Mulai menggunakan bot |
| `/register` | Daftar sebagai anak hebat |
| `/menu` | Lihat menu utama |
| `/motivasi` | Menu kata-kata motivasi |
| `/kebiasaan` | Menu tips kebiasaan baik |
| `/baca_ebook` | Menu bacaan dan dongeng |
| `/broadcast` | Lihat pengumuman terbaru |

### 🔑 Keywords yang Tersedia

#### 💪 Kategori Motivasi:
- `semangat` - Kata penyemangat untuk anak
- `kamu hebat` - Afirmasi positif
- `percaya diri` - Tips membangun kepercayaan diri
- `pantang menyerah` - Motivasi untuk tidak mudah menyerah
- `berani` - Cara menjadi anak yang berani

#### 📚 Kategori Kebiasaan:
- `sikat gigi` - Cara menjaga kesehatan gigi
- `cuci tangan` - Tips cuci tangan yang benar
- `belajar` - Cara belajar yang menyenangkan
- `membaca` - Tips gemar membaca
- `olahraga` - Olahraga ringan untuk anak
- `tidur` - Pentingnya tidur yang cukup
- `makan sehat` - Tips makan makanan bergizi

#### 📖 Kategori Baca Ebook:
- `dongeng` - Dongeng tradisional yang seru
- `cerita` - Cerita petualangan dan persahabatan
- `fabel` - Cerita hewan dengan pesan moral
- `legenda` - Legenda nusantara yang menarik
- `artikel anak` - Artikel sains dan pengetahuan
- `buku` - Rekomendasi buku untuk anak

### 💬 Contoh Percakapan

```
Anak: semangat
Bot: 🌟 Wah, kamu anak yang hebat! Semangat terus ya dalam belajar dan bermain. Ingat, setiap anak punya keistimewaan sendiri! 💪✨

Anak: sikat gigi
Bot: 🦷 Sikat gigi sangat penting! Sikat gigi 2 kali sehari (pagi dan malam) dengan pasta gigi yang mengandung fluoride. Jangan lupa sikat selama 2 menit ya! 😁✨

Anak: dongeng
Bot: 📚✨ Dongeng mengajarkan nilai-nilai baik dengan cara yang menyenangkan! Ada dongeng Kancil, Si Malin Kundang, atau dongeng dari negeri lain. Mau dengar dongeng apa? 🏰🦋
```

---

## 👑 Panel Admin

### 🖥️ GUI Features

**🤖 Bot Control**
- Start/Stop bot
- Monitor log aktivitas
- Validasi konfigurasi

**👥 Manajemen Anak Hebat**
- Tambah/hapus anak yang terdaftar
- Lihat daftar member aktif
- Monitor aktivitas per anak

**💬 Monitor Percakapan**
- Lihat history percakapan
- Filter pesan masuk/keluar
- Search dalam pesan
- Export data percakapan

**📢 Broadcast System**
- Kirim pesan ke semua anak
- Pilih kategori (motivasi/kebiasaan/bacaan)
- Track delivery status
- History broadcast

**🔑 Keyword Management**
- Tambah/edit/hapus keywords
- Kelola response per kategori
- Preview response lengkap
- Validasi input

**📊 Statistik**
- Total anak hebat terdaftar
- Jumlah percakapan
- Anak paling aktif
- Statistik per jam/hari

### 👨‍💻 Admin Commands (via Telegram)

| Command | Deskripsi |
|---------|-----------|
| `/admin` | Panel admin via Telegram |
| `/members` | Lihat daftar member |
| `/stats` | Statistik bot |
| `/broadcast kategori\|judul\|isi` | Kirim broadcast |
| `/addmember chatid\|nama\|telepon` | Tambah member manual |
| `/deletemember chatid` | Hapus member |

**Contoh Broadcast:**
```
/broadcast motivasi|Semangat Pagi|Selamat pagi anak hebat! Hari ini kita akan belajar hal baru yang seru! 🌟
```

---

## 🗄️ Database

### 📊 Schema Database

Bot menggunakan 4 tabel utama:

**👥 members**
- `chat_id` (PRIMARY KEY) - ID Telegram anak
- `name` - Nama lengkap anak
- `phone_number` - Nomor telepon orangtua
- `registration_date` - Tanggal bergabung
- `is_active` - Status aktif
- `last_activity` - Aktivitas terakhir

**🔑 keywords**
- `id` (PRIMARY KEY) - ID keyword
- `keyword` - Kata kunci
- `response` - Response yang dikirim
- `category` - Kategori (motivasi/kebiasaan/baca_ebook)
- `is_active` - Status aktif
- `created_date` - Tanggal dibuat

**📢 broadcast_messages**
- `id` (PRIMARY KEY) - ID broadcast
- `title` - Judul pesan
- `content` - Isi pesan
- `category` - Kategori broadcast
- `created_date` - Tanggal dibuat
- `is_sent` - Status terkirim
- `recipient_count` - Jumlah penerima

**💬 messages**
- `id` (PRIMARY KEY) - ID pesan
- `chat_id` - ID Telegram
- `direction` - Arah pesan (incoming/outgoing)
- `message` - Isi pesan
- `timestamp` - Waktu pesan

### 🔧 Database Setup

**SQLite (Default):**
- Otomatis membuat file `anak_hebat_bot.db`
- Tidak perlu setup tambahan
- Cocok untuk penggunaan personal

**MySQL (Production):**
```sql
CREATE DATABASE anak_hebat_bot;
-- Jalankan script database_setup.sql
```

---

## 📁 Struktur Project

```
bot-anak-hebat/
├── 📁 src/main/java/com/mycompany/mytelegrambot/
│   ├── 🗂️ database/
│   │   └── DatabaseManager.java      # Database operations
│   ├── 🗂️ gui/
│   │   └── KeywordManagementPanel.java  # Swing GUI components
│   ├── 🗂️ model/
│   │   ├── BroadcastMessage.java     # Broadcast model
│   │   ├── Keyword.java              # Keyword model
│   │   ├── Member.java               # Member model
│   │   └── Message.java              # Message model
│   ├── 🗂️ service/
│   │   └── TelegramBotService.java   # Core bot logic
│   ├── KeywordManagementTab.java     # JavaFX keyword management
│   ├── MainApp.java                  # Console application
│   ├── MainAppGUI.java               # JavaFX GUI application
│   └── MyTelegramBot.java            # Bot wrapper class
├── 📁 src/main/resources/
│   └── config.properties             # Configuration file
├── 📁 target/
│   └── mytelegrambot-1.0-SNAPSHOT-jar-with-dependencies.jar
├── 📄 pom.xml                        # Maven configuration
├── 📄 run_gui.bat                    # Windows launcher
├── 📄 run_gui.sh                     # Linux/Mac launcher
├── 📄 database_setup.sql             # Database schema
└── 📄 README.md                      # This file
```

---

## 🛠️ Development

### 🧱 Tech Stack

- **☕ Java 17+** - Core language
- **🏗️ Maven** - Dependency management
- **🤖 TelegramBots API** - Telegram integration
- **🎨 JavaFX** - Modern GUI framework
- **🖼️ Swing** - Alternative GUI components
- **🗄️ SQLite/MySQL** - Database storage
- **🔧 JDBC** - Database connectivity

### 📦 Dependencies

```xml
<dependencies>
    <!-- Telegram Bot API -->
    <dependency>
        <groupId>org.telegram</groupId>
        <artifactId>telegrambots</artifactId>
        <version>6.7.0</version>
    </dependency>
    
    <!-- JavaFX Controls -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>24.0.1</version>
    </dependency>
    
    <!-- SQLite JDBC -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.42.0.0</version>
    </dependency>
    
    <!-- MySQL Connector -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
</dependencies>
```

### 🔨 Build Commands

```bash
# Clean & compile
mvn clean compile

# Run tests
mvn test

# Package JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Run specific class
mvn exec:java -Dexec.mainClass="com.mycompany.mytelegrambot.MainAppGUI"
```

### 🧪 Adding New Keywords

1. **Via GUI Admin Panel:**
   - Buka tab "Keywords"
   - Klik "Tambah Keyword"
   - Isi form dan simpan

2. **Via Database:**
```sql
INSERT INTO keywords (keyword, response, category) VALUES 
('keyword_baru', '🌟 Response untuk anak hebat!', 'motivasi');
```

3. **Via Code:**
```java
Keyword newKeyword = new Keyword("keyword_baru", "🌟 Response untuk anak hebat!", "motivasi");
dbManager.saveKeyword(newKeyword);
```

### 🎨 Customizing UI

**Mengubah Color Theme:**
```java
// Di MainAppGUI.java, ubah color constant
private static final String PRIMARY_COLOR = "#9b59b6"; // Purple theme
private static final String SUCCESS_COLOR = "#27ae60"; // Green
private static final String ERROR_COLOR = "#e74c3c";   // Red
```

**Menambah Tab Baru:**
```java
Tab newTab = new Tab("🆕 Tab Baru");
newTab.setContent(createNewTabPanel());
tabPane.getTabs().add(newTab);
```

---

## ❓ FAQ

### 🤔 Pertanyaan Umum

**Q: Apakah bot ini gratis?**
A: Ya, Bot Anak Hebat adalah open source dan gratis digunakan.

**Q: Apakah data anak-anak aman?**
A: Ya, semua data disimpan lokal di database Anda sendiri, tidak dikirim ke server lain.

**Q: Bisakah menambah kategori baru selain motivasi, kebiasaan, dan bacaan?**
A: Ya, bisa dengan memodifikasi kode dan database schema.

**Q: Apakah bisa digunakan untuk grup Telegram?**
A: Saat ini bot dirancang untuk chat personal, tapi bisa dikembangkan untuk grup.

**Q: Bagaimana cara backup data?**
A: Untuk SQLite, backup file `.db`. Untuk MySQL, gunakan `mysqldump`.

### 🔧 Troubleshooting

**Bot tidak merespon:**
1. Periksa bot token di `config.properties`
2. Pastikan bot sudah di-start via admin panel
3. Cek koneksi internet
4. Lihat log error di console

**GUI tidak muncul:**
1. Pastikan JavaFX SDK terinstall
2. Periksa path JavaFX di `run_gui.bat`
3. Coba jalankan console mode sebagai fallback
4. Update Java ke versi terbaru

**Database error:**
1. Untuk SQLite: pastikan folder writable
2. Untuk MySQL: periksa koneksi dan credentials
3. Cek apakah tabel sudah dibuat
4. Lihat log error untuk detail

**Memory issues:**
1. Increase heap size: `-Xmx1g`
2. Monitor memory usage di admin panel
3. Clear old messages secara berkala
4. Restart bot jika perlu

---

## 🤝 Contributing

Kami menyambut kontribusi dari developer! 

### 📝 Cara Berkontribusi:

1. **Fork** repository ini
2. Buat **feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit** perubahan (`git commit -m 'Add some AmazingFeature'`)
4. **Push** ke branch (`git push origin feature/AmazingFeature`)
5. Buat **Pull Request**

### 🎯 Area yang Bisa Dikembangkan:

- 🌐 Multi-language support
- 🎮 Mini games edukatif
- 📊 Analytics yang lebih detail
- 🔔 Notification system
- 📱 Mobile app companion
- 🌟 Gamifikasi dengan poin/badge
- 🎵 Audio content support
- 📸 Image content management

---

## 📄 License

Bot Anak Hebat dilisensikan under **MIT License**. Lihat file [LICENSE](LICENSE) untuk detail.

```
MIT License

Copyright (c) 2024 Bot Anak Hebat

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📞 Support

### 💬 Butuh Bantuan?

- 📧 **Email**: support@bot-anak-hebat.com
- 💬 **Telegram**: [@BotAnakHebatSupport](https://t.me/BotAnakHebatSupport)
- 🐛 **Issues**: [GitHub Issues](https://github.com/your-username/bot-anak-hebat/issues)
- 📖 **Documentation**: [Wiki](https://github.com/your-username/bot-anak-hebat/wiki)

### 🌟 Follow Us

- 📱 **Telegram Channel**: [@BotAnakHebatNews](https://t.me/BotAnakHebatNews)
- 🐦 **Twitter**: [@BotAnakHebat](https://twitter.com/BotAnakHebat)
- 📘 **Facebook**: [Bot Anak Hebat](https://facebook.com/BotAnakHebat)

---

## 🎉 Acknowledgments

- 🤖 **TelegramBots Java Library** - Framework bot Telegram
- 🎨 **JavaFX Community** - Modern UI framework
- 👨‍👩‍👧‍👦 **Orangtua dan Anak** - Inspirasi dan feedback
- 📚 **Komunitas Edukasi** - Dukungan konten edukatif
- 🌟 **Open Source Community** - Berbagi pengetahuan

---

<div align="center">

## 🌟 Terima Kasih telah menggunakan Bot Anak Hebat! 🌈

**Membantu anak-anak menjadi lebih hebat, satu pesan pada satu waktu.** 💫

---

**Made with ❤️ for Indonesian Children**

*Bot ini didedikasikan untuk semua anak Indonesia yang bercita-cita menjadi hebat!*

</div>#
