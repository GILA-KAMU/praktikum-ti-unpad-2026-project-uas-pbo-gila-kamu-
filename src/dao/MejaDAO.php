<?php
declare(strict_types=1);

class MejaDAO
{
    private PDO $db;

    public function __construct()
    {
        $this->db = DatabaseConnection::getConnection();
    }

    /** Mengembalikan Meja bila nomor dan token cocok, selain itu null. */
    public function findByNomorDanToken(int $nomor, string $token): ?Meja
    {
        $st = $this->db->prepare('SELECT * FROM meja WHERE nomor_meja = ?');
        $st->execute([$nomor]);
        $r = $st->fetch();
        if (!$r) return null;

        $meja = new Meja((int)$r['id_meja'], (int)$r['nomor_meja'], $r['token_qr'], $r['status']);
        return $meja->validasiToken($token) ? $meja : null;
    }

    public function updateStatus(int $idMeja, string $status): bool
    {
        if (!in_array($status, ['kosong', 'terisi'], true)) return false;
        $st = $this->db->prepare('UPDATE meja SET status = ? WHERE id_meja = ?');
        return $st->execute([$status, $idMeja]);
    }

    private function hydrate(array $r): Meja
    {
        return new Meja((int)$r['id_meja'], (int)$r['nomor_meja'], $r['token_qr'], $r['status']);
    }

    /** @return Meja[] */
    public function findAll(): array
    {
        return array_map([$this, 'hydrate'], $this->db->query('SELECT * FROM meja ORDER BY nomor_meja')->fetchAll());
    }

    public function nomorBerikutnya(): int
    {
        return (int)$this->db->query('SELECT COALESCE(MAX(nomor_meja), 0) + 1 FROM meja')->fetchColumn();
    }

    public function insert(int $nomor, string $token): int
    {
        $st = $this->db->prepare('INSERT INTO meja (nomor_meja, token_qr) VALUES (?, ?)');
        $st->execute([$nomor, $token]);
        return (int)$this->db->lastInsertId();
    }

    /** Token baru membuat QR code lama tidak berlaku lagi. */
    public function updateToken(int $idMeja, string $token): bool
    {
        $st = $this->db->prepare('UPDATE meja SET token_qr = ? WHERE id_meja = ?');
        return $st->execute([$token, $idMeja]);
    }
}
