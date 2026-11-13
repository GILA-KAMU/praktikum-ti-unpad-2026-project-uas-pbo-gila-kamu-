<?php
declare(strict_types=1);

class PembayaranTunai extends Pembayaran
{
    private float $uangDiterima = 0.0;

    public function setUangDiterima(float $uang): void { $this->uangDiterima = $uang; }

    public function hitungKembalian(): float
    {
        return max(0.0, $this->uangDiterima - $this->jumlahBayar);
    }

    /** Dikonfirmasi kasir: lunas bila uang diterima cukup. */
    public function prosesPembayaran(): bool
    {
        if ($this->uangDiterima >= $this->jumlahBayar) {
            $this->statusBayar = 'lunas';
            return true;
        }
        return false;
    }
}
