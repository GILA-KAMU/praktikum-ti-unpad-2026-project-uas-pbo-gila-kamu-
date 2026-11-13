<?php
declare(strict_types=1);

class Meja
{
    public function __construct(
        private int $idMeja,
        private int $nomorMeja,
        private string $tokenQr,
        private string $status = 'kosong'
    ) {}

    public function validasiToken(string $token): bool
    {
        return hash_equals($this->tokenQr, $token);
    }

    public function setTerisi(): void { $this->status = 'terisi'; }
    public function setKosong(): void { $this->status = 'kosong'; }

    public function getIdMeja(): int { return $this->idMeja; }
    public function getNomorMeja(): int { return $this->nomorMeja; }
    public function getTokenQr(): string { return $this->tokenQr; }
    public function getStatus(): string { return $this->status; }
}
