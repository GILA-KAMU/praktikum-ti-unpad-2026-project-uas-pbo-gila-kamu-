<?php
declare(strict_types=1);

class Kasir extends Pengguna
{
    public function getRole(): string { return 'kasir'; }

    public function lihatPesananMasuk(): void { /* TODO minggu 2 (PesananDAO) */ }
    public function verifikasiPembayaran(): void { /* TODO minggu 2 */ }
    public function ubahStatusPesanan(): void { /* TODO minggu 2 */ }
    public function tutupMeja(): void { /* TODO minggu 2 */ }

    public function tampilkanDashboard(): array
    {
        return [
            'Pesanan menunggu pembayaran tunai',
            'Verifikasi pembayaran',
            'Proses pesanan',
            'Tutup meja',
        ];
    }
}
