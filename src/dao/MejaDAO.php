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
}
