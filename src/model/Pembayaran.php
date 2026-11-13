<?php
declare(strict_types=1);

abstract class Pembayaran
{
    public function __construct(
        protected int $idPembayaran,
        protected float $jumlahBayar,
        protected string $statusBayar = 'belum'
    ) {}

    /** Tiap metode bayar punya cara verifikasi sendiri (polymorphism). */
    abstract public function prosesPembayaran(): bool;

    public function getStatusBayar(): string { return $this->statusBayar; }
    public function getJumlahBayar(): float { return $this->jumlahBayar; }
}
