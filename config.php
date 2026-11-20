<?php
// Konfigurasi aplikasi. Sesuaikan dengan komputer masing-masing.
return [
    'host'   => '127.0.0.1',
    'dbname' => 'db_nasi_daun_jeruk',
    'user'   => 'root',
    'pass'   => '',

    // Batas waktu bayar (menit). Lewat dari ini pesanan otomatis dibatalkan.
    'batas_bayar_menit' => 10,

    // Zona waktu PHP dan MySQL harus sama supaya hitung mundur akurat.
    'zona_waktu'   => 'Asia/Jakarta',
    'offset_mysql' => '+07:00',

    // Alamat dasar aplikasi untuk isi QR code. Kosongkan agar dideteksi otomatis.
    // Agar bisa discan dari HP, isi dengan IP laptop, mis. 'http://192.168.1.10:8000'
    'base_url' => '',
];
