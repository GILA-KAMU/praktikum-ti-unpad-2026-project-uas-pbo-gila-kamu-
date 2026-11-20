<?php
declare(strict_types=1);

class Pesanan
{
    public const STATUS = ['menunggu_pembayaran', 'dibayar', 'diproses', 'disajikan', 'selesai', 'dibatalkan'];

    /** Alur status oleh pegawai: Dibayar -> Diproses -> Disajikan. */
    public const STATUS_BERIKUTNYA = ['dibayar' => 'diproses', 'diproses' => 'disajikan'];

    /** @param DetailPesanan[] $daftarDetail */
    public function __construct(
        private int $idPesanan,
        private Meja $meja,
        private ?string $namaPelanggan,
        private ?string $catatan,
        private DateTimeImmutable $waktuPesan,
        private array $daftarDetail = [],
        private string $status = 'menunggu_pembayaran',
        private ?Pembayaran $pembayaran = null,
        private ?float $totalHarga = null   // total yang tersimpan saat checkout
    ) {}

    public function hitungTotal(): float
    {
        return array_sum(array_map(fn(DetailPesanan $d) => $d->hitungSubtotal(), $this->daftarDetail));
    }

    /** Total saat checkout (tidak berubah walau harga menu diubah kemudian). */
    public function getTotalHarga(): float
    {
        return $this->totalHarga ?? $this->hitungTotal();
    }

    public function ubahStatus(string $statusBaru): void
    {
        if (!in_array($statusBaru, self::STATUS, true)) {
            throw new InvalidArgumentException("Status tidak dikenal: $statusBaru");
        }
        $this->status = $statusBaru;
    }

    public function statusBerikutnya(): ?string
    {
        return self::STATUS_BERIKUTNYA[$this->status] ?? null;
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
        if ($this->sisaDetik($batasMenit) === 0) {
            $this->status = 'dibatalkan';
            return true;
        }
        return false;
    }

    /** Sisa detik sebelum pesanan kedaluwarsa (0 jika sudah lewat). */
    public function sisaDetik(int $batasMenit = 10): int
    {
        $batas = $this->waktuPesan->modify("+{$batasMenit} minutes")->getTimestamp();
        return max(0, $batas - time());
    }

    public function getNomorMeja(): int { return $this->meja->getNomorMeja(); }
    public function getMeja(): Meja { return $this->meja; }
    public function getIdPesanan(): int { return $this->idPesanan; }
    public function getStatus(): string { return $this->status; }
    public function getDaftarDetail(): array { return $this->daftarDetail; }
    public function getNamaPelanggan(): ?string { return $this->namaPelanggan; }
    public function getCatatan(): ?string { return $this->catatan; }
    public function getWaktuPesan(): DateTimeImmutable { return $this->waktuPesan; }
    public function getPembayaran(): ?Pembayaran { return $this->pembayaran; }
    public function setPembayaran(Pembayaran $p): void { $this->pembayaran = $p; }
}
