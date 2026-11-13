<?php
declare(strict_types=1);

/** Tambahan di luar PDF: dipakai untuk login pegawai. */
class PenggunaDAO
{
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
