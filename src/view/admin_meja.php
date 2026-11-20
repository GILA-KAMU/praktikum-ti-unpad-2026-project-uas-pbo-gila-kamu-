<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

wajib_role('admin');
$admin = pegawai_objek();
$dao = new MejaDAO();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $aksi = $_POST['aksi'] ?? '';
    try {
        if ($aksi === 'tambah_meja') {
            $nomor = (int)($_POST['nomor'] ?? 0);
            if ($nomor <= 0) throw new RuntimeException('Nomor meja tidak valid.');
            $dao->insert($nomor, bin2hex(random_bytes(6)));
            flash('ok', "Meja $nomor ditambahkan.");
        } elseif ($aksi === 'token_baru') {
            $dao->updateToken((int)($_POST['id_meja'] ?? 0), bin2hex(random_bytes(6)));
            flash('ok', 'Token baru dibuat. QR code lama tidak berlaku lagi, cetak ulang QR meja itu.');
        }
    } catch (PDOException $e) {
        flash('err', 'Nomor meja itu sudah dipakai.');
    } catch (RuntimeException $e) {
        flash('err', $e->getMessage());
    }
    redirect('admin_meja.php');
}

$daftarMeja = $admin->kelolaMeja();
$dasar = base_url();

view_header('Kelola meja', null, true);
?>
<h1 class="judul">Meja dan QR code</h1>

<form method="post" class="form-grid panel-isi">
  <input type="hidden" name="aksi" value="tambah_meja">
  <label>Nomor meja baru
    <input type="number" name="nomor" min="1" value="<?= $dao->nomorBerikutnya() ?>" required>
  </label>
  <button class="btn btn-utama" type="submit">Tambah meja</button>
</form>

<p class="catatan-kecil">
  Alamat dasar QR saat ini: <strong><?= e($dasar) ?></strong>.
  Agar QR bisa dipindai dari HP, isi <code>base_url</code> di <code>config.php</code> dengan IP laptop
  (mis. <code>http://192.168.1.10:8000</code>) dan jalankan server dengan <code>php -S 0.0.0.0:8000</code>.
  Gambar QR dibuat lewat internet.
</p>

<ul class="grid-meja">
<?php foreach ($daftarMeja as $m): $url = $admin->generateQR($m, $dasar); ?>
  <li class="kartu-meja">
    <h2>Meja <?= $m->getNomorMeja() ?></h2>
    <span class="badge badge-<?= $m->getStatus() === 'kosong' ? 'selesai' : 'diproses' ?>"><?= e(ucfirst($m->getStatus())) ?></span>
    <img src="<?= e(qr_gambar($url, 180)) ?>" width="180" height="180" alt="QR code meja <?= $m->getNomorMeja() ?>">
    <a class="tautan-qr" href="<?= e($url) ?>" target="_blank" rel="noopener"><?= e($url) ?></a>
    <form method="post" onsubmit="return confirm('QR code lama tidak akan berlaku lagi. Lanjutkan?');">
      <input type="hidden" name="aksi" value="token_baru">
      <input type="hidden" name="id_meja" value="<?= $m->getIdMeja() ?>">
      <button class="btn" type="submit">Buat token baru</button>
    </form>
  </li>
<?php endforeach; ?>
</ul>
<button class="btn btn-utama sembunyi-cetak" type="button" onclick="window.print()">Cetak semua QR</button>
<?php view_footer();
