<?php
declare(strict_types=1);

/** Abstract class induk Admin dan Kasir. */
abstract class Pengguna
{
    public function __construct(
        protected int $idPengguna,
        protected string $nama,
        protected string $username,
        protected string $password // hash
    ) {}

    /** Cek username dan password (dibandingkan dengan hash). */
    public function login(string $username, string $passwordPlain): bool
    {
        return hash_equals($this->username, $username)
            && password_verify($passwordPlain, $this->password);
    }

    public function getIdPengguna(): int { return $this->idPengguna; }
    public function getNama(): string { return $this->nama; }
    public function setNama(string $nama): void { $this->nama = trim($nama); }
    public function getUsername(): string { return $this->username; }

    abstract public function getRole(): string;

    /** Fitur dashboard yang tampil sesuai peran (polymorphism). */
    abstract public function tampilkanDashboard(): array;
}
