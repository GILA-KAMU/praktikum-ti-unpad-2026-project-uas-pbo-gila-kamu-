<?php
declare(strict_types=1);

class Admin extends Pengguna
{
    public function getRole(): string { return 'admin'; }

    /** @return Menu[] */
    public function kelolaMenu(): array
    {
        return (new MenuDAO())->findAll();
    }

    /** @return Meja[] */
    public function kelolaMeja(): array
    {
        return (new MejaDAO())->findAll();
    }

    /** Isi QR code: alamat lengkap menu beserta nomor meja dan token. */
    public function generateQR(Meja $meja, string $baseUrl = ''): string
    {
        return rtrim($baseUrl, '/') . '/?meja=' . $meja->getNomorMeja() . '&token=' . $meja->getTokenQr();
    }

    /** @return Pesanan[] pesanan yang sudah lunas */
    public function kelolaPesanan(): array
    {
        return (new PesananDAO())->findAktif();
    }

    public function ubahStatusPesanan(Pesanan $pesanan): bool
    {
        return (new PesananDAO())->majukanStatus($pesanan);
    }

    /** @return array<int, array<string, mixed>> */
    public function lihatLaporan(string $periode = 'harian'): array
    {
        return (new PesananDAO())->laporanPenjualan($periode);
    }

    public function tambahKasir(string $nama, string $username, string $password): int
    {
        return (new PenggunaDAO())->insert($nama, $username, password_hash($password, PASSWORD_DEFAULT), 'kasir');
    }

    public function tampilkanDashboard(): array
    {
        return [
            'Kelola menu dan kategori' => 'admin_menu.php',
            'Kelola meja dan QR code'  => 'admin_meja.php',
            'Kelola pesanan'           => 'pesanan.php',
            'Laporan penjualan'        => 'laporan.php',
            'Kelola pegawai'           => 'admin_pegawai.php',
        ];
    }
}
