<?php
declare(strict_types=1);

class Admin extends Pengguna
{
    public function getRole(): string { return 'admin'; }

    /** Minggu 1: hanya membaca daftar menu. CRUD lengkap di minggu 2. */
    public function kelolaMenu(): array
    {
        return (new MenuDAO())->findAll();
    }

    public function kelolaMeja(): void { /* TODO minggu 2 */ }

    public function generateQR(Meja $meja): string
    {
        // Isi QR: alamat menu beserta nomor meja dan token
        return '/?meja=' . $meja->getNomorMeja() . '&token=' . $meja->getTokenQr();
    }

    public function lihatLaporan(): void { /* TODO minggu 2 */ }

    public function tampilkanDashboard(): array
    {
        return [
            'Kelola menu dan kategori',
            'Kelola meja dan QR code',
            'Kelola pesanan',
            'Laporan penjualan',
            'Kelola pegawai',
        ];
    }
}
