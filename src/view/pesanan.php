<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

$pegawai = wajib_role('admin', 'kasir');
$user = pegawai_objek();
$dao = new PesananDAO();
$dao->batalkanKedaluwarsa((int)config('batas_bayar_menit'));

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $aksi = $_POST['aksi'] ?? '';
    if ($aksi === 'majukan') {
        $p = $dao->findById((int)($_POST['id'] ?? 0));
        if ($p !== null && $user->ubahStatusPesanan($p)) {
            flash('ok', 'Status pesanan #' . $p->getIdPesanan() . ' diubah.');
        } else {
            flash('err', 'Status tidak bisa diubah. Mungkin sudah diubah pegawai lain.');
        }
    } elseif ($aksi === 'tutup_meja' && $user instanceof Kasir) {
        if ($user->tutupMeja((int)($_POST['id_meja'] ?? 0))) {
            flash('ok', 'Meja ditutup dan kembali kosong.');
        } else {
            flash('err', 'Meja belum bisa ditutup: masih ada pesanan yang belum dibayar atau belum disajikan.');
        }
    }
    redirect('pesanan.php');
}

$perMeja = [];
foreach ($dao->findAktif() as $p) {
    $perMeja[$p->getNomorMeja()][] = $p;
}
ksort($perMeja);

$aksiLabel = ['dibayar' => 'Mulai proses', 'diproses' => 'Tandai disajikan'];

header('Refresh: 15');
view_header('Pesanan masuk', null, true);
?>
<h1 class="judul">Pesanan masuk</h1>

<?php if (!$perMeja): ?>
  <p class="kosong">Belum ada pesanan lunas yang berjalan.</p>
<?php endif; ?>

<?php foreach ($perMeja as $nomor => $daftar): ?>
  <section class="blok-meja">
    <h2>Meja <?= $nomor ?></h2>
    <ul class="daftar-status">
    <?php foreach ($daftar as $p): ?>
      <li class="kartu-status">
        <div class="kartu-kepala">
          <h3>Pesanan #<?= $p->getIdPesanan() ?></h3>
          <?= badge_status($p->getStatus()) ?>
        </div>
        <?php daftar_item($p); ?>
        <?php if ($p->getNamaPelanggan()): ?>
          <p class="catatan-kecil">Atas nama <?= e($p->getNamaPelanggan()) ?></p>
        <?php endif; ?>
        <div class="kartu-total"><?= rupiah($p->getTotalHarga()) ?></div>
        <?php if ($p->statusBerikutnya() !== null): ?>
          <form method="post">
            <input type="hidden" name="aksi" value="majukan">
            <input type="hidden" name="id" value="<?= $p->getIdPesanan() ?>">
            <button class="btn btn-utama" type="submit"><?= e($aksiLabel[$p->getStatus()]) ?></button>
          </form>
        <?php endif; ?>
      </li>
    <?php endforeach; ?>
    </ul>
    <?php if ($user instanceof Kasir): ?>
      <form method="post" class="tutup-meja">
        <input type="hidden" name="aksi" value="tutup_meja">
        <input type="hidden" name="id_meja" value="<?= $daftar[0]->getMeja()->getIdMeja() ?>">
        <button class="btn" type="submit">Tutup meja <?= $nomor ?></button>
      </form>
    <?php endif; ?>
  </section>
<?php endforeach; ?>
<?php view_footer();
