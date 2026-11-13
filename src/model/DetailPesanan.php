<?php
declare(strict_types=1);

class DetailPesanan
{
    public function __construct(
        private Menu $menu,
        private int $jumlah,
        private string $catatanItem = ''
    ) {}

    public function hitungSubtotal(): float
    {
        return $this->menu->getHarga() * $this->jumlah;
    }

    /** Kunci unik baris: menu yang sama dengan catatan/varian sama digabung. */
    public function getKey(): string
    {
        return $this->menu->getIdMenu() . '|' . mb_strtolower($this->catatanItem);
    }

    public function getMenu(): Menu { return $this->menu; }
    public function getJumlah(): int { return $this->jumlah; }
    public function setJumlah(int $jumlah): void { $this->jumlah = max(1, $jumlah); }
    public function getCatatanItem(): string { return $this->catatanItem; }
}
