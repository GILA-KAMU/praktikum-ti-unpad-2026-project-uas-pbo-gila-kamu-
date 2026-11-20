<?php
declare(strict_types=1);

/** Tambahan di luar PDF: dipakai untuk login pegawai. */
class PenggunaDAO
{
    private function hydrate(array $r): Pengguna
    {
        $kelas = $r['role'] === 'admin' ? Admin::class : Kasir::class;
        return new $kelas((int)$r['id_pengguna'], $r['nama'], $r['username'], $r['password']);
    }

    /** @return Pengguna[] */
    public function findAll(): array
    {
        $rows = DatabaseConnection::getConnection()->query('SELECT * FROM pengguna ORDER BY role, nama')->fetchAll();
        return array_map([$this, 'hydrate'], $rows);
    }

    public function insert(string $nama, string $username, string $passwordHash, string $role = 'kasir'): int
    {
        $db = DatabaseConnection::getConnection();
        $st = $db->prepare('INSERT INTO pengguna (nama, username, password, role) VALUES (?,?,?,?)');
        $st->execute([$nama, $username, $passwordHash, $role]);
        return (int)$db->lastInsertId();
    }

    public function findByUsername(string $username): ?Pengguna
    {
        $st = DatabaseConnection::getConnection()->prepare('SELECT * FROM pengguna WHERE username = ?');
        $st->execute([$username]);
        $r = $st->fetch();
        if (!$r) return null;

        $kelas = $r['role'] === 'admin' ? Admin::class : Kasir::class;
        return new $kelas((int)$r['id_pengguna'], $r['nama'], $r['username'], $r['password']);
    }
}
