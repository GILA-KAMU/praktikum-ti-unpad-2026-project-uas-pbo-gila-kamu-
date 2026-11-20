<?php
declare(strict_types=1);

class PesananDAO
{
    private PDO $db;

    private const SELECT = 'SELECT p.*, mj.nomor_meja, mj.token_qr, mj.status AS status_meja
                            FROM pesanan p JOIN meja mj ON mj.id_meja = p.id_meja';

    /** Status pesanan yang sudah lunas dan dihitung sebagai penjualan. */
    private const STATUS_LUNAS = "('dibayar','diproses','disajikan','selesai')";

    public function __construct()
    {
        $this->db = DatabaseConnection::getConnection();
    }

    // ---------- membaca ----------
    private function hydrate(array $r): Pesanan
    {
        $meja = new Meja((int)$r['id_meja'], (int)$r['nomor_meja'], $r['token_qr'], $r['status_meja']);

        $st = $this->db->prepare(
            'SELECT d.jumlah, d.catatan_item, m.*, k.nama_kategori
             FROM detail_pesanan d
             JOIN menu m ON m.id_menu = d.id_menu
             JOIN kategori k ON k.id_kategori = m.id_kategori
             WHERE d.id_pesanan = ? ORDER BY d.id_detail'
        );
        $st->execute([$r['id_pesanan']]);
        $detail = [];
        foreach ($st->fetchAll() as $d) {
            $menu = new Menu(
                (int)$d['id_menu'], (int)$d['id_kategori'], $d['nama_kategori'], $d['nama_menu'],
                $d['deskripsi'], (float)$d['harga'], $d['gambar'], (bool)$d['tersedia'], $d['varian']
            );
            $detail[] = new DetailPesanan($menu, (int)$d['jumlah'], (string)$d['catatan_item']);
        }

        $st = $this->db->prepare('SELECT * FROM pembayaran WHERE id_pesanan = ? ORDER BY id_pembayaran DESC LIMIT 1');
        $st->execute([$r['id_pesanan']]);
        $bayar = null;
        if ($p = $st->fetch()) {
            $kelas = $p['metode'] === 'qris' ? PembayaranQRIS::class : PembayaranTunai::class;
            $bayar = new $kelas((int)$p['id_pembayaran'], (float)$p['jumlah_bayar'], $p['status_bayar']);
        }

        return new Pesanan(
            (int)$r['id_pesanan'], $meja, $r['nama_pelanggan'], $r['catatan'],
            new DateTimeImmutable($r['waktu_pesan']), $detail, $r['status'], $bayar, (float)$r['total_harga']
        );
    }

    /** @return Pesanan[] */
    private function ambil(string $where, array $params = [], string $order = 'p.waktu_pesan'): array
    {
        $st = $this->db->prepare(self::SELECT . " $where ORDER BY $order");
        $st->execute($params);
        return array_map([$this, 'hydrate'], $st->fetchAll());
    }

    public function findById(int $id): ?Pesanan
    {
        return $this->ambil('WHERE p.id_pesanan = ?', [$id])[0] ?? null;
    }

    /** @return Pesanan[] terbaru dulu */
    public function findByIds(array $ids): array
    {
        $ids = array_values(array_map('intval', $ids));
        if (!$ids) return [];
        $in = implode(',', array_fill(0, count($ids), '?'));
        return $this->ambil("WHERE p.id_pesanan IN ($in)", $ids, 'p.id_pesanan DESC');
    }

    /** @return Pesanan[] pesanan lunas yang masih berjalan (tampil di dashboard) */
    public function findAktif(): array
    {
        return $this->ambil("WHERE p.status IN ('dibayar','diproses','disajikan')");
    }

    /** @return Pesanan[] pesanan yang menunggu uang tunai dikonfirmasi kasir */
    public function findMenungguPembayaran(): array
    {
        return $this->ambil(
            "WHERE p.status = 'menunggu_pembayaran' AND EXISTS (
                SELECT 1 FROM pembayaran b
                WHERE b.id_pesanan = p.id_pesanan AND b.metode = 'tunai' AND b.status_bayar = 'belum')"
        );
    }

    /** @return Pesanan[] */
    public function findByMeja(int $idMeja): array
    {
        return $this->ambil('WHERE p.id_meja = ?', [$idMeja], 'p.id_pesanan DESC');
    }

    // ---------- menulis ----------
    /** Simpan pesanan baru (status Menunggu Pembayaran) beserta detailnya. */
    public function simpan(Meja $meja, ?string $nama, ?string $catatan, Keranjang $k): int
    {
        $this->db->beginTransaction();
        try {
            $st = $this->db->prepare(
                'INSERT INTO pesanan (id_meja, nama_pelanggan, catatan, total_harga) VALUES (?,?,?,?)'
            );
            $st->execute([$meja->getIdMeja(), $nama ?: null, $catatan ?: null, $k->hitungTotal()]);
            $id = (int)$this->db->lastInsertId();

            $ins = $this->db->prepare(
                'INSERT INTO detail_pesanan (id_pesanan, id_menu, jumlah, subtotal, catatan_item) VALUES (?,?,?,?,?)'
            );
            foreach ($k->getItems() as $d) {
                $ins->execute([
                    $id, $d->getMenu()->getIdMenu(), $d->getJumlah(),
                    $d->hitungSubtotal(), $d->getCatatanItem() ?: null,
                ]);
            }
            $this->db->commit();
            return $id;
        } catch (Throwable $e) {
            $this->db->rollBack();
            throw $e;
        }
    }

    /** Ubah status; $dariStatus mencegah dua pegawai menimpa satu sama lain. */
    public function updateStatus(int $idPesanan, string $status, ?string $dariStatus = null): bool
    {
        if (!in_array($status, Pesanan::STATUS, true)) return false;
        $sql = 'UPDATE pesanan SET status = ? WHERE id_pesanan = ?';
        $params = [$status, $idPesanan];
        if ($dariStatus !== null) {
            $sql .= ' AND status = ?';
            $params[] = $dariStatus;
        }
        $st = $this->db->prepare($sql);
        $st->execute($params);
        return $st->rowCount() > 0;
    }

    public function majukanStatus(Pesanan $pesanan): bool
    {
        $next = $pesanan->statusBerikutnya();
        return $next !== null
            && $this->updateStatus($pesanan->getIdPesanan(), $next, $pesanan->getStatus());
    }

    /** Batalkan semua pesanan yang belum dibayar melewati batas waktu. */
    public function batalkanKedaluwarsa(int $menit): int
    {
        $menit = max(1, $menit);
        return (int)$this->db->exec(
            "UPDATE pesanan SET status = 'dibatalkan'
             WHERE status = 'menunggu_pembayaran' AND waktu_pesan < (NOW() - INTERVAL $menit MINUTE)"
        );
    }

    // ---------- pembayaran ----------
    /** Catat metode bayar pilihan pelanggan (status belum). Mengganti pilihan sebelumnya. */
    public function pilihMetode(int $idPesanan, string $metode, float $jumlah): int
    {
        if (!in_array($metode, ['tunai', 'qris'], true)) {
            throw new InvalidArgumentException('Metode bayar tidak dikenal.');
        }
        $this->db->beginTransaction();
        try {
            $this->db->prepare("DELETE FROM pembayaran WHERE id_pesanan = ? AND status_bayar = 'belum'")
                     ->execute([$idPesanan]);
            $st = $this->db->prepare(
                "INSERT INTO pembayaran (id_pesanan, metode, jumlah_bayar, status_bayar) VALUES (?,?,?,'belum')"
            );
            $st->execute([$idPesanan, $metode, $jumlah]);
            $id = (int)$this->db->lastInsertId();
            $this->db->commit();
            return $id;
        } catch (Throwable $e) {
            $this->db->rollBack();
            throw $e;
        }
    }

    /**
     * Tandai pembayaran lunas: pesanan menjadi Dibayar dan meja Terisi.
     * $idPengguna = kasir yang memverifikasi (null untuk QRIS otomatis).
     * Gagal (false) bila pesanan sudah tidak menunggu pembayaran, mis. sudah dibatalkan.
     */
    public function konfirmasiPembayaran(int $idPesanan, ?int $idPengguna): bool
    {
        $this->db->beginTransaction();
        try {
            $st = $this->db->prepare(
                "UPDATE pesanan SET status = 'dibayar' WHERE id_pesanan = ? AND status = 'menunggu_pembayaran'"
            );
            $st->execute([$idPesanan]);
            if ($st->rowCount() === 0) {
                $this->db->rollBack();
                return false;
            }
            $st = $this->db->prepare(
                "UPDATE pembayaran SET status_bayar = 'lunas', waktu_bayar = NOW(), id_pengguna = ?
                 WHERE id_pesanan = ? AND status_bayar = 'belum'"
            );
            $st->execute([$idPengguna, $idPesanan]);
            if ($st->rowCount() === 0) {
                $this->db->rollBack();
                return false;
            }
            $this->db->prepare(
                "UPDATE meja SET status = 'terisi'
                 WHERE id_meja = (SELECT id_meja FROM pesanan WHERE id_pesanan = ?)"
            )->execute([$idPesanan]);
            $this->db->commit();
            return true;
        } catch (Throwable $e) {
            $this->db->rollBack();
            throw $e;
        }
    }

    /**
     * Tutup meja: pesanan Disajikan menjadi Selesai dan meja kembali Kosong.
     * Ditolak bila masih ada pesanan yang belum dibayar atau belum disajikan.
     */
    public function tutupMeja(int $idMeja): bool
    {
        $st = $this->db->prepare(
            "SELECT COUNT(*) FROM pesanan
             WHERE id_meja = ? AND status IN ('menunggu_pembayaran','dibayar','diproses')"
        );
        $st->execute([$idMeja]);
        if ((int)$st->fetchColumn() > 0) return false;

        $this->db->beginTransaction();
        try {
            $this->db->prepare("UPDATE pesanan SET status = 'selesai' WHERE id_meja = ? AND status = 'disajikan'")
                     ->execute([$idMeja]);
            $this->db->prepare("UPDATE meja SET status = 'kosong' WHERE id_meja = ?")
                     ->execute([$idMeja]);
            $this->db->commit();
            return true;
        } catch (Throwable $e) {
            $this->db->rollBack();
            throw $e;
        }
    }

    // ---------- laporan ----------
    /** @return array<int, array{periode:string, jumlah_pesanan:int|string, total:float|string}> */
    public function laporanPenjualan(string $periode = 'harian'): array
    {
        $format = $periode === 'bulanan' ? '%Y-%m' : '%Y-%m-%d';
        $limit  = $periode === 'bulanan' ? 12 : 31;
        $sql = "SELECT DATE_FORMAT(waktu_pesan, '$format') AS periode,
                       COUNT(*) AS jumlah_pesanan, SUM(total_harga) AS total
                FROM pesanan WHERE status IN " . self::STATUS_LUNAS . "
                GROUP BY periode ORDER BY periode DESC LIMIT $limit";
        return $this->db->query($sql)->fetchAll();
    }

    /** @return array<int, array{nama_menu:string, terjual:int|string}> */
    public function menuTerlaris(int $limit = 5): array
    {
        $limit = max(1, $limit);
        $sql = "SELECT m.nama_menu, SUM(d.jumlah) AS terjual
                FROM detail_pesanan d
                JOIN pesanan p ON p.id_pesanan = d.id_pesanan
                JOIN menu m ON m.id_menu = d.id_menu
                WHERE p.status IN " . self::STATUS_LUNAS . "
                GROUP BY m.id_menu, m.nama_menu ORDER BY terjual DESC LIMIT $limit";
        return $this->db->query($sql)->fetchAll();
    }
}
