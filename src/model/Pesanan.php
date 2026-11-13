<?php
declare(strict_types=1);

class Pesanan
{
    public const STATUS = ['menunggu_pembayaran', 'dibayar', 'diproses', 'disajikan', 'selesai', 'dibatalkan'];

    /** @param DetailPesanan[] $daftarDetail */
    public function __construct(
        private int $idPesanan,
        private Meja $meja,
        private ?string $namaPelanggan,
        private ?string $catatan,
        private DateTimeImmutable $waktuPesan,
        private array $daftarDetail = [],
        private string $status = 'menunggu_pembayaran',
        private ?Pembayaran $pembayaran = null
    ) {}

    public function hitungTotal(): float
    {
        return array_sum(array_map(fn(DetailPesanan $d) => $d->hitungSubtotal(), $this->daftarDetail));
    }

    public function ubahStatus(string $statusBaru): void
    {
        if (!in_array($statusBaru, self::STATUS, true)) {
            throw new InvalidArgumentException("Status tidak dikenal: $statusBaru");
        }
        $this->status = $statusBaru;
    }

    /** Pesanan hanya boleh diteruskan ke dapur jika pembayaran lunas. */
    public function isLunas(): bool
    {
        return $this->pembayaran !== null && $this->pembayaran->getStatusBayar() === 'lunas';
    }

    /** Batalkan otomatis bila belum dibayar melewati batas waktu (menit). */
    public function batalkanJikaKedaluwarsa(int $batasMenit = 10): bool
    {
        if ($this->status !== 'menunggu_pembayaran' || $this->isLunas()) {
            return false;
        }
        $batas = $this->waktuPesan->modify("+{$batasMenit} minutes");
        if (new DateTimeImmutable() > $batas) {
            $this->status = 'dibatalkan';
            return true;
        }
        return false;
    }

    public function getNomorMeja(): int { return $this->meja->getNomorMeja(); }
    public function getIdPesanan(): int { return $this->idPesanan; }
    public function getStatus(): string { return $this->status; }
    public function getDaftarDetail(): array { return $this->daftarDetail; }
    public function getNamaPelanggan(): ?string { return $this->namaPelanggan; }
    public function getCatatan(): ?string { return $this->catatan; }
    public function setPembayaran(Pembayaran $p): void { $this->pembayaran = $p; }
}
