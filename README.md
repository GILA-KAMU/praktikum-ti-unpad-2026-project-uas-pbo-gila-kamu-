# Sistem Pemesanan Digital Nasi Daun Jeruk

Aplikasi pemesanan mandiri berbasis **QR code meja** untuk warung Nasi Daun Jeruk.
Pelanggan cukup memindai QR di meja, memilih menu, lalu checkout tanpa perlu memanggil pelayan.
Pegawai (Admin dan Kasir) mengelola pesanan, pembayaran, dan laporan lewat dashboard.

Dibangun dengan **PHP (OOP)** dan **MySQL** sebagai Proyek UAS Praktikum Pemrograman Berorientasi Objek,
Teknik Informatika Universitas Padjadjaran 2026.

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
- Masuk lewat QR code meja (`/?meja=<nomor>&token=<token>`) yang divalidasi ke database
- Melihat daftar menu beserta varian (misalnya sambal Merah/Hijau, telur Dadar/Ceplok)
- Menambah, mengubah, dan menghapus item di keranjang
- Checkout dan pembayaran dengan batas waktu bayar *(minggu 2)*

### Pegawai
- Login dengan peran **Admin** atau **Kasir**
- Dashboard Admin dan Kasir *(minggu 2)*
- Konfirmasi pembayaran *(minggu 2)*
- Laporan penjualan *(minggu 2)*

---

## Konsep OOP yang Dipakai

- **Class model**: representasi data seperti Menu, Meja, dan Pengguna
- **DAO (Data Access Object)**: memisahkan logika akses database dari tampilan (`MenuDAO`, `MejaDAO`, `PenggunaDAO`)
- **Enkapsulasi**: data diakses lewat method, bukan langsung dari query di halaman
- **PDO**: koneksi database dengan prepared statement untuk mencegah SQL injection

---

## Struktur Folder

```
.
├── config.php                  # Konfigurasi koneksi database
├── index.php                   # Titik masuk dari QR code, diarahkan ke halaman menu
├── database/
│   └── db_nasi_daun_jeruk.sql  # Skema + data awal
└── src/
    └── view/
        ├── menu.php            # Halaman menu pelanggan
        └── login.php           # Halaman login pegawai
```

---

## Kebutuhan

- PHP **8.1+** dengan ekstensi `pdo_mysql` dan `mbstring` aktif
- MySQL / MariaDB (bisa lewat **XAMPP** atau **Laragon**)

---

## Cara Menjalankan

1. **Clone repo**
   ```bash
   git clone <url-repo>
   cd praktikum-ti-unpad-2026-project-uas-pbo-gila-kamu-
   ```

2. **Import database**
   ```bash
   mysql -u root -p < database/db_nasi_daun_jeruk.sql
   ```
   Bisa juga lewat phpMyAdmin: buat database `db_nasi_daun_jeruk`, lalu import file `.sql`-nya.

3. **Sesuaikan konfigurasi** di `config.php`:
   ```php
   'host'   => '127.0.0.1',
   'dbname' => 'db_nasi_daun_jeruk',
   'user'   => 'root',
   'pass'   => '',
   'batas_bayar_menit' => 10,
   ```

4. **Jalankan server** dari folder proyek:
   ```bash
   php -S localhost:8000
   ```

5. **Buka di browser**
   - Pelanggan (simulasi scan QR): <http://localhost:8000/?meja=5&token=a1b2c3>
     > Token untuk meja 1–4 bisa dilihat di tabel `meja`.
   - Login pegawai: <http://localhost:8000/src/view/login.php>

---

## Akun Demo

| Peran | Username | Password  |
|-------|----------|-----------|
| Admin | `admin`  | `admin123` |
| Kasir | `kasir1` | `kasir123` |

---

## Progres

| Minggu | Target | Status |
|--------|--------|--------|
| 1 | Database, class model, DAO (Menu, Meja), validasi QR, halaman menu, keranjang, login | Selesai |
| 2 | Checkout, pembayaran, dashboard Admin/Kasir, laporan | Dalam pengerjaan |

---

## Catatan Perubahan dari Rancangan Awal

- Kolom **`menu.varian`** ditambahkan untuk menyimpan pilihan varian (Merah/Hijau, Dadar/Ceplok).
- Class **`PenggunaDAO`** ditambahkan untuk menangani login pegawai.
