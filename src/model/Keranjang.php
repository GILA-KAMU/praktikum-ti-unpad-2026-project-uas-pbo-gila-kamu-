<?php
declare(strict_types=1);

/** Penampung sementara item sebelum checkout. */
class Keranjang
{
    public const MAKS_PER_ITEM = 20;

    /** @var array<string, DetailPesanan> */
    private array $items = [];

    public function tambahItem(DetailPesanan $detail): void
    {
        $key = $detail->getKey();
        if (isset($this->items[$key])) {
            $baru = $this->items[$key]->getJumlah() + $detail->getJumlah();
            $this->items[$key]->setJumlah(min($baru, self::MAKS_PER_ITEM));
        } else {
            $detail->setJumlah(min($detail->getJumlah(), self::MAKS_PER_ITEM));
            $this->items[$key] = $detail;
        }
    }

    public function ubahJumlah(string $key, int $jumlah): void
    {
        if (!isset($this->items[$key])) return;
        if ($jumlah <= 0) {
            $this->hapusItem($key);
            return;
        }
        $this->items[$key]->setJumlah(min($jumlah, self::MAKS_PER_ITEM));
    }

    public function hapusItem(string $key): void { unset($this->items[$key]); }
    public function kosongkan(): void { $this->items = []; }

    public function hitungTotal(): float
    {
        $total = 0.0;
        foreach ($this->items as $d) {
            $total += $d->hitungSubtotal();
        }
        return $total;
    }

    /** @return array<string, DetailPesanan> */
    public function getItems(): array { return $this->items; }
    public function isKosong(): bool { return count($this->items) === 0; }

    public function jumlahItem(): int
    {
        return array_sum(array_map(fn(DetailPesanan $d) => $d->getJumlah(), $this->items));
    }
}
