# Sistem Pemesanan Digital Nasi Daun Jeruk

Aplikasi desktop pemesanan mandiri untuk **UMKM warung Nasi Daun Jeruk**.
Pelanggan memindai **QR code di meja** (lewat webcam), memilih menu, lalu memesan tanpa perlu memanggil pelayan.
Pegawai (Admin dan Kasir) mengelola pesanan, menu, meja, dan laporan lewat dashboard.

Dibangun dengan **Java (Swing)** dan **MySQL**, dijalankan lewat **NetBeans**, sebagai Proyek UAS Praktikum
Pemrograman Berorientasi Objek, Teknik Informatika Universitas Padjadjaran 2026.

---

## Kelompok GILA KAMU!!

| Nama   | NPM    |
|--------|--------|
| Hasby  | 250030 |
| Fatih  | 250036 |
| Farhan | 250084 |

---

## Fitur

### Pelanggan
- Masuk dengan **scan QR meja lewat webcam** atau memilih **file gambar QR**; nomor meja dan token di dalam
  QR divalidasi ke tabel `meja`
- Melihat menu per kategori beserta varian (sambal Merah/Hijau, telur Dadar/Ceplok)
- Menambah, mengubah jumlah, dan menghapus item di keranjang
- **Pesan sekarang**: pesanan langsung diteruskan ke dapur
- Melihat status pesanan yang diperbarui otomatis

### Kasir
- Memproses pesanan: Masuk → Diproses → Disajikan
- Menutup meja setelah semua pesanan disajikan (pesanan menjadi Selesai, meja kembali kosong)

### Admin
- Semua fitur kasir untuk memproses pesanan
- Kelola menu dan kategori (tambah, ubah, nonaktifkan, hapus)
- Kelola meja dan **kartu QR meja** (simpan sebagai PNG untuk dicetak; kode baru membuat QR lama tidak berlaku)
- Laporan penjualan harian/bulanan dan menu terlaris
- Tambah akun kasir

---

## Database

6 tabel di `database/db_nasi_daun_jeruk.sql`:

| Tabel | Isi |
|-------|-----|
| `meja` | Nomor meja, token QR, status kosong/terisi |
| `pengguna` | Akun admin dan kasir (password di-hash bcrypt) |
| `kategori` | Kategori menu |
| `menu` | Menu, harga, varian, ketersediaan |
| `pesanan` | Pesanan per meja beserta status (masuk, diproses, disajikan, selesai) |
| `detail_pesanan` | Item di setiap pesanan |

---

## Konsep OOP yang Dipakai

- **Class & enkapsulasi**: `Menu`, `Meja`, `Pesanan`, `Keranjang`, dan lainnya menyimpan data di field `private`
  yang diakses lewat method
- **Inheritance & abstract class**: `Admin` dan `Kasir` turunan class abstrak `Pengguna`
- **Polymorphism**: `tampilkanDashboard()` menghasilkan tab berbeda untuk Admin dan Kasir; `PenggunaDAO`
  membuat objek `Admin` atau `Kasir` sesuai peran di database
- **Interface**: `PengelolaPesanan` diimplementasikan Admin dan Kasir
- **Enum**: `StatusPesanan` (beserta alur status berikutnya) dan `Fitur`
- **DAO (Data Access Object)**: query database terpisah dari tampilan (`MenuDAO`, `MejaDAO`, `PesananDAO`, `PenggunaDAO`)
- **Singleton**: `DatabaseConnection` untuk satu koneksi JDBC
- **PreparedStatement & transaksi**: mencegah SQL injection; pesanan dan detailnya disimpan dalam satu transaksi

---

## Struktur Folder

```
.                                   # Proyek NetBeans (Maven)
├── pom.xml
├── config.properties               # Koneksi database
├── database/
│   └── db_nasi_daun_jeruk.sql      # Skema + data awal
└── src/main/java/nasidaunjeruk/
    ├── Main.java                   # Titik masuk aplikasi
    ├── model/                      # Class model (Pengguna, Admin, Kasir, Menu, Pesanan, ...)
    ├── dao/                        # Akses database
    ├── database/                   # Koneksi (Singleton)
    ├── util/                       # Konfigurasi, format rupiah/waktu, QR code
    └── view/                       # Tampilan Swing
        ├── pelanggan/              # Scan QR, menu, keranjang, status
        └── pegawai/                # Dashboard admin & kasir
```

Semua halaman di `view/` dibuat dengan **GUI Builder NetBeans** (setiap `.java` punya pasangan `.form`)
dan bisa diubah lewat tab **Design** (drag-and-drop). Isi data (baris tabel, daftar menu) tetap diisi kode
saat aplikasi jalan; tampilan satu kartu menu diatur di `KartuMenu.form`. Selalu commit file `.java`
dan `.form`-nya bersamaan, dan jangan mengedit bagian `initComponents()` secara manual.

---

## Kebutuhan

- **JDK 17** atau lebih baru
- **Apache NetBeans** (sudah termasuk Maven)
- **MySQL / MariaDB** (lewat **Laragon** atau **XAMPP**)
- Webcam (opsional, untuk scan QR meja)
- Internet saat pertama kali build (Maven mengunduh driver MySQL, jBCrypt, FlatLaf, ZXing untuk QR, dan webcam-capture)

---

## Cara Menjalankan

1. **Nyalakan MySQL** di Laragon (Start All) atau XAMPP.

2. **Import database** (juga dipakai untuk memperbarui database versi lama; data pesanan lama akan terhapus)
   ```bash
   mysql -u root -p < database/db_nasi_daun_jeruk.sql
   ```
   Bisa juga lewat HeidiSQL (Laragon) atau phpMyAdmin.

3. **Sesuaikan koneksi** di `config.properties` bila user/password MySQL berbeda:
   ```properties
   db.user=root
   db.pass=
   ```

4. **Buka di NetBeans**: *File → Open Project…* → pilih folder repo ini → tekan **Run** (F6).

   Atau tanpa NetBeans, dari folder repo ini:
   ```bash
   mvn package
   java -jar target/NasiDaunJeruk-1.0-jar-with-dependencies.jar
   ```

Jendela awal tetap terbuka, jadi jendela **pelanggan** dan **kasir** bisa dibuka bersamaan untuk demo:
pesanan dari pelanggan langsung muncul di kasir, dan perubahan status dari kasir langsung terlihat di pelanggan.

---

## Akun Demo

| Peran | Username | Password  |
|-------|----------|-----------|
| Admin | `admin`  | `admin123` |
| Kasir | `kasir1` | `kasir123` |

Kartu QR tiap meja bisa disimpan sebagai PNG dari **Admin → Kelola meja → Simpan semua kartu QR**,
lalu dipakai untuk masuk sebagai pelanggan (scan lewat webcam atau **Pilih gambar QR**).

---

## Progres

| Minggu | Target | Status |
|--------|--------|--------|
| 1 | Database, class model, DAO, validasi meja, menu, keranjang, login | Selesai |
| 2 | Checkout, dashboard Admin/Kasir, laporan | Selesai |
| 3 | Pindah dari web PHP ke aplikasi desktop Java (NetBeans + Swing) | Selesai |

---

## Catatan Perubahan dari Rancangan Awal

- Kolom **`menu.varian`** ditambahkan untuk menyimpan pilihan varian (Merah/Hijau, Dadar/Ceplok).
- Class **`PenggunaDAO`** ditambahkan untuk menangani login pegawai.
- Proyek dipindah dari web PHP ke aplikasi desktop Java. Versi PHP sudah dihapus dari repo
  (masih bisa dilihat di riwayat git, commit `0321ab7`).
- QR meja berisi `NASIDAUNJERUK;meja=<nomor>;token=<token>` dan dipindai lewat webcam laptop
  (pelanggan memesan di aplikasi desktop, bukan dari HP). Token disimpan di kolom `meja.token_qr`.
- Fitur pembayaran (QRIS/tunai) beserta tabel `pembayaran` dihapus untuk menyederhanakan proyek;
  pesanan langsung diteruskan ke dapur saat dipesan.
