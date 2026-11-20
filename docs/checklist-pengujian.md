# Checklist pengujian akhir

Centang setelah dicoba. Gunakan dua browser (atau satu biasa, satu jendela privat):
satu untuk pelanggan, satu untuk pegawai.

## Pelanggan
- [ ] Token salah menampilkan "QR code tidak dikenali".
- [ ] Scan QR benar menampilkan menu dengan nomor meja di header.
- [ ] Varian sambal/telur wajib dipilih dan tampil di keranjang.
- [ ] Item yang sama (varian sama) bergabung, total benar.
- [ ] Checkout membuka halaman bayar dengan hitung mundur 10:00.
- [ ] QRIS: tombol simulasi membuat pesanan berstatus Dibayar.
- [ ] Tunai: halaman menunggu, lalu pindah otomatis setelah kasir konfirmasi.
- [ ] Pesanan tidak dibayar 10 menit menjadi Dibatalkan dan tidak bisa dibayar.
- [ ] "Tambah pesanan" dari meja yang sama membuat pesanan dan pembayaran baru.

## Kasir
- [ ] Pesanan tunai muncul di "Menunggu pembayaran tunai" lengkap dengan nomor meja.
- [ ] Uang kurang ditolak, uang cukup menampilkan kembalian.
- [ ] Pesanan menunggu bayar tidak muncul di "Pesanan masuk".
- [ ] Dibayar, Diproses, Disajikan berjalan berurutan.
- [ ] Tutup meja gagal jika masih ada pesanan belum disajikan, berhasil jika semua sudah.

## Admin
- [ ] Tambah, ubah, nonaktifkan menu; hapus menu yang pernah dipesan ditolak dengan pesan jelas.
- [ ] Tambah meja menghasilkan QR code; token baru membuat QR lama tidak berlaku.
- [ ] Laporan harian dan bulanan hanya menghitung pesanan lunas.
- [ ] Tambah akun kasir, lalu login dengan akun itu.
- [ ] Kasir tidak bisa membuka halaman Admin.
