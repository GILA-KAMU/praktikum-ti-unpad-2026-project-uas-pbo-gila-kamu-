<?php
declare(strict_types=1);

class MenuDAO
{
    private PDO $db;

    public function __construct()
    {
        $this->db = DatabaseConnection::getConnection();
    }

    private const SELECT = 'SELECT m.*, k.nama_kategori
                            FROM menu m JOIN kategori k ON k.id_kategori = m.id_kategori';

    private function hydrate(array $r): Menu
    {
        return new Menu(
            (int)$r['id_menu'], (int)$r['id_kategori'], $r['nama_kategori'],
            $r['nama_menu'], $r['deskripsi'], (float)$r['harga'],
            $r['gambar'], (bool)$r['tersedia'], $r['varian']
        );
    }

    /** @return Menu[] */
    public function findAll(): array
    {
        $rows = $this->db->query(self::SELECT . ' ORDER BY k.id_kategori, m.id_menu')->fetchAll();
        return array_map([$this, 'hydrate'], $rows);
    }

    public function findById(int $id): ?Menu
    {
        $st = $this->db->prepare(self::SELECT . ' WHERE m.id_menu = ?');
        $st->execute([$id]);
        $r = $st->fetch();
        return $r ? $this->hydrate($r) : null;
    }

    public function insert(int $idKategori, string $nama, ?string $deskripsi, float $harga, ?string $varian = null): int
    {
        $st = $this->db->prepare(
            'INSERT INTO menu (id_kategori, nama_menu, deskripsi, harga, varian) VALUES (?,?,?,?,?)'
        );
        $st->execute([$idKategori, $nama, $deskripsi, $harga, $varian]);
        return (int)$this->db->lastInsertId();
    }

    public function update(int $id, int $idKategori, string $nama, ?string $deskripsi, float $harga, bool $tersedia, ?string $varian = null): bool
    {
        $st = $this->db->prepare(
            'UPDATE menu SET id_kategori=?, nama_menu=?, deskripsi=?, harga=?, tersedia=?, varian=? WHERE id_menu=?'
        );
        return $st->execute([$idKategori, $nama, $deskripsi, $harga, (int)$tersedia, $varian, $id]);
    }

    public function delete(int $id): bool
    {
        $st = $this->db->prepare('DELETE FROM menu WHERE id_menu = ?');
        return $st->execute([$id]);
    }
}
