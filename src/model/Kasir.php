<?php
declare(strict_types=1);

class Kasir extends Pengguna
{
    public function getRole(): string { return 'kasir'; }

    /** @return Pesanan[] pesanan lunas yang sedang berjalan */
    public function lihatPesananMasuk(): array
    {
        return (new PesananDAO())->findAktif();
    }

    /** @return Pesanan[] pesanan yang menunggu pembayaran tunai */
    public function lihatMenungguBayar(): array
    {
        return (new PesananDAO())->findMenungguPembayaran();
    }

    /**
     * Konfirmasi pembayaran tunai. Mengembalikan kembalian.
     * @throws RuntimeException bila uang kurang atau pesanan tidak bisa dibayar lagi
     */
    public function verifikasiPembayaran(Pesanan $pesanan, float $uangDiterima): float
    {
        if ($pesanan->getStatus() !== 'menunggu_pembayaran') {
            throw new RuntimeException('Pesanan ini sudah tidak menunggu pembayaran.');
        }
        $bayar = new PembayaranTunai(0, $pesanan->getTotalHarga());
        $bayar->setUangDiterima($uangDiterima);
        if (!$bayar->prosesPembayaran()) {
            throw new RuntimeException('Uang yang diterima kurang dari total pesanan.');
        }
        if (!(new PesananDAO())->konfirmasiPembayaran($pesanan->getIdPesanan(), $this->idPengguna)) {
            throw new RuntimeException('Pembayaran gagal dicatat. Pesanan mungkin sudah dibatalkan.');
        }
        return $bayar->hitungKembalian();
    }

    /** Dibayar -> Diproses -> Disajikan. */
    public function ubahStatusPesanan(Pesanan $pesanan): bool
    {
        return (new PesananDAO())->majukanStatus($pesanan);
    }

    /** Selesaikan semua pesanan disajikan di meja itu dan kosongkan meja. */
    public function tutupMeja(int $idMeja): bool
    {
        return (new PesananDAO())->tutupMeja($idMeja);
    }

    public function tampilkanDashboard(): array
    {
        return [
            'Pesanan menunggu pembayaran tunai' => 'pembayaran.php',
            'Verifikasi pembayaran'             => 'pembayaran.php',
            'Proses pesanan'                    => 'pesanan.php',
            'Tutup meja'                        => 'pesanan.php',
        ];
    }
}
