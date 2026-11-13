<?php
declare(strict_types=1);

class PembayaranQRIS extends Pembayaran
{
    private string $kodeTransaksi = '';

    public function setKodeTransaksi(string $kode): void { $this->kodeTransaksi = $kode; }
    public function getKodeTransaksi(): string { return $this->kodeTransaksi; }

    /** Verifikasi otomatis (simulasi): lunas jika kode transaksi terisi. */
    public function prosesPembayaran(): bool
    {
        if ($this->kodeTransaksi !== '') {
            $this->statusBayar = 'lunas';
            return true;
        }
        return false;
    }
}
