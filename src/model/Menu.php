<?php
declare(strict_types=1);

class Menu
{
    public function __construct(
        private int $idMenu,
        private int $idKategori,
        private string $namaKategori,
        private string $namaMenu,
        private ?string $deskripsi,
        private float $harga,
        private ?string $gambar,
        private bool $tersedia,
        private ?string $varian = null
    ) {}

    public function isTersedia(): bool { return $this->tersedia; }
    public function setTersedia(bool $tersedia): void { $this->tersedia = $tersedia; }
    public function getHarga(): float { return $this->harga; }

    public function getIdMenu(): int { return $this->idMenu; }
    public function getIdKategori(): int { return $this->idKategori; }
    public function getNamaKategori(): string { return $this->namaKategori; }
    public function getNamaMenu(): string { return $this->namaMenu; }
    public function getDeskripsi(): ?string { return $this->deskripsi; }
    public function getGambar(): ?string { return $this->gambar; }

    /** Contoh: 'Merah,Hijau' menjadi ['Merah','Hijau']. */
    public function getVarianList(): array
    {
        if ($this->varian === null || trim($this->varian) === '') {
            return [];
        }
        return array_map('trim', explode(',', $this->varian));
    }
}
