# Checklist pengujian akhir

Centang setelah dicoba. Dari jendela awal, buka satu jendela pelanggan dan satu jendela pegawai
secara bersamaan (jendela awal tetap terbuka).

## Pelanggan
- [ ] Scan kartu QR meja lewat webcam membuka jendela menu meja itu.
- [ ] "Pilih gambar QR" dengan file kartu QR membuka jendela menu meja itu.
- [ ] QR lain (bukan QR meja) ditolak dengan pesan jelas.
- [ ] QR meja yang kodenya sudah diganti admin ditolak ("sudah tidak berlaku").
- [ ] Jendela menu menampilkan nomor meja yang benar di header.
- [ ] Varian sambal/telur dipilih dan tampil di keranjang.
- [ ] Item yang sama (varian sama) bergabung, total benar.
- [ ] Ubah jumlah di tabel keranjang; jumlah 0 menghapus item.
- [ ] "Pesan sekarang" membuat pesanan berstatus Masuk dan membuka tab Status pesanan.
- [ ] "Tambah pesanan" dari meja yang sama membuat pesanan baru.

## Kasir
- [ ] Kasir hanya melihat tab Pesanan masuk.
- [ ] Pesanan baru dari pelanggan langsung muncul di Pesanan masuk lengkap dengan nomor meja.
- [ ] Masuk, Diproses, Disajikan berjalan berurutan, dan status di jendela pelanggan ikut berubah.
- [ ] Tutup meja gagal jika masih ada pesanan belum disajikan, berhasil jika semua sudah.

## Admin
- [ ] Tambah, ubah, nonaktifkan menu; hapus menu yang pernah dipesan ditolak dengan pesan jelas.
- [ ] Tambah meja menghasilkan kartu QR; "Simpan kartu QR" dan "Simpan semua kartu QR" menghasilkan file PNG.
- [ ] Kode baru membuat QR lama ditolak saat di-scan.
- [ ] Laporan harian dan bulanan menghitung semua pesanan yang masuk.
- [ ] Tambah akun kasir, lalu login dengan akun itu.
