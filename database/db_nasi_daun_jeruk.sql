-- Database Sistem Pemesanan Digital Nasi Daun Jeruk
-- Import: mysql -u root -p < database/db_nasi_daun_jeruk.sql

CREATE DATABASE IF NOT EXISTS db_nasi_daun_jeruk
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE db_nasi_daun_jeruk;

DROP TABLE IF EXISTS pembayaran;
DROP TABLE IF EXISTS detail_pesanan;
DROP TABLE IF EXISTS pesanan;
DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS kategori;
DROP TABLE IF EXISTS pengguna;
DROP TABLE IF EXISTS meja;

CREATE TABLE meja (
  id_meja     INT AUTO_INCREMENT PRIMARY KEY,
  nomor_meja  INT NOT NULL UNIQUE,
  token_qr    VARCHAR(64) NOT NULL,
  status      ENUM('kosong','terisi') NOT NULL DEFAULT 'kosong'
);

CREATE TABLE pengguna (
  id_pengguna INT AUTO_INCREMENT PRIMARY KEY,
  nama        VARCHAR(100) NOT NULL,
  username    VARCHAR(50) NOT NULL UNIQUE,
  password    VARCHAR(255) NOT NULL,           -- hash (password_hash)
  role        ENUM('admin','kasir') NOT NULL
);

CREATE TABLE kategori (
  id_kategori   INT AUTO_INCREMENT PRIMARY KEY,
  nama_kategori VARCHAR(50) NOT NULL
);

CREATE TABLE menu (
  id_menu     INT AUTO_INCREMENT PRIMARY KEY,
  id_kategori INT NOT NULL,
  nama_menu   VARCHAR(100) NOT NULL,
  deskripsi   TEXT,
  harga       DECIMAL(10,2) NOT NULL,
  gambar      VARCHAR(255),
  tersedia    BOOLEAN NOT NULL DEFAULT TRUE,
  varian      VARCHAR(100) NULL,               -- TAMBAHAN: pilihan varian dipisah koma, mis. 'Merah,Hijau'
  FOREIGN KEY (id_kategori) REFERENCES kategori(id_kategori)
);

CREATE TABLE pesanan (
  id_pesanan     INT AUTO_INCREMENT PRIMARY KEY,
  id_meja        INT NOT NULL,
  nama_pelanggan VARCHAR(100),
  catatan        TEXT,
  waktu_pesan    DATETIME DEFAULT CURRENT_TIMESTAMP,
  total_harga    DECIMAL(12,2),
  status ENUM('menunggu_pembayaran','dibayar','diproses','disajikan','selesai','dibatalkan')
         NOT NULL DEFAULT 'menunggu_pembayaran',
  FOREIGN KEY (id_meja) REFERENCES meja(id_meja)
);

CREATE TABLE detail_pesanan (
  id_detail    INT AUTO_INCREMENT PRIMARY KEY,
  id_pesanan   INT NOT NULL,
  id_menu      INT NOT NULL,
  jumlah       INT NOT NULL,
  subtotal     DECIMAL(12,2),
  catatan_item VARCHAR(255),
  FOREIGN KEY (id_pesanan) REFERENCES pesanan(id_pesanan) ON DELETE CASCADE,
  FOREIGN KEY (id_menu)    REFERENCES menu(id_menu)
);

CREATE TABLE pembayaran (
  id_pembayaran INT AUTO_INCREMENT PRIMARY KEY,
  id_pesanan    INT NOT NULL,
  id_pengguna   INT NULL,                      -- kasir verifikator, NULL jika QRIS otomatis
  metode        ENUM('tunai','qris'),
  jumlah_bayar  DECIMAL(12,2),
  waktu_bayar   DATETIME,
  status_bayar  ENUM('belum','lunas') NOT NULL DEFAULT 'belum',
  FOREIGN KEY (id_pesanan)  REFERENCES pesanan(id_pesanan) ON DELETE CASCADE,
  FOREIGN KEY (id_pengguna) REFERENCES pengguna(id_pengguna)
);

-- ===== DATA AWAL =====
INSERT INTO meja (nomor_meja, token_qr) VALUES
  (1, 'a1b2c3d4e5f6'), (2, 'b2c3d4e5f6a1'), (3, 'c3d4e5f6a1b2'),
  (4, 'd4e5f6a1b2c3'), (5, 'a1b2c3');

-- Password: admin123 dan kasir123 (ganti setelah demo)
INSERT INTO pengguna (nama, username, password, role) VALUES
  ('Administrator', 'admin', '$2y$10$jeo9ZBh5xthMExG7GN5LdOotTJM8uXGmP73crEBBguNtxqRXHF2HG', 'admin'),
  ('Kasir Satu',    'kasir1','$2y$10$ilML0mpZT0QtclOlCpfEOegW7K1FI1x.qzqF2z/U4zBXCDYlxCjQq', 'kasir');

INSERT INTO kategori (nama_kategori) VALUES ('Paket Nasi'), ('Tambahan');

INSERT INTO menu (id_kategori, nama_menu, deskripsi, harga, varian) VALUES
  (1, 'Paket Nasi Daun Jeruk Ayam Goreng', 'Nasi harum daun jeruk dengan ayam goreng.', 18000, NULL),
  (1, 'Paket Nasi Daun Jeruk Ayam Bakar',  'Nasi harum daun jeruk dengan ayam bakar.',  18000, NULL),
  (2, 'Sambal Tambahan', 'Pilih sambal merah atau hijau.', 2000, 'Merah,Hijau'),
  (2, 'Telur Tambahan',  'Pilih telur dadar atau ceplok.', 4000, 'Dadar,Ceplok');
