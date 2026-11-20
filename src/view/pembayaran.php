<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

wajib_role('kasir');
$kasir = pegawai_objek();
$batas = (int)config('batas_bayar_menit');
(new PesananDAO())->batalkanKedaluwarsa($batas);

if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['aksi'] ?? '') === 'verifikasi') {
    $p = (new PesananDAO())->findById((int)($_POST['id'] ?? 0));
    try {
        if ($p === null) throw new RuntimeException('Pesanan tidak ditemukan.');
        $kembalian = $kasir->verifikasiPembayaran($p, (float)($_POST['uang'] ?? 0));
        flash('ok', 'Pesanan #' . $p->getIdPesanan() . ' lunas. Kembalian: ' . rupiah($kembalian));
    } catch (PDOException $e) {
        flash('err', 'Terjadi kesalahan database. Coba lagi.');
    } catch (RuntimeException $e) {
        flash('err', $e->getMessage());
    }
    redirect('pembayaran.php');
}

$daftar = $kasir->lihatMenungguBayar();
view_header('Pembayaran tunai', null, true);
?>
<h1 class="judul">Menunggu pembayaran tunai</h1>

<?php if (!$daftar): ?>
  <p class="kosong">Tidak ada pesanan yang menunggu pembayaran tunai.</p>
<?php else: ?>
  <ul class="daftar-status">
  <?php foreach ($daftar as $p): ?>
    <li class="kartu-status">
      <div class="kartu-kepala">
        <h2>Meja <?= $p->getNomorMeja() ?> &middot; #<?= $p->getIdPesanan() ?></h2>
        <span class="sisa-waktu">Sisa <?= intdiv($p->sisaDetik($batas), 60) ?> menit</span>
      </div>
      <?php daftar_item($p); ?>
      <div class="kartu-total"><?= rupiah($p->getTotalHarga()) ?></div>
      <form method="post" class="form-uang">
        <input type="hidden" name="aksi" value="verifikasi">
        <input type="hidden" name="id" value="<?= $p->getIdPesanan() ?>">
        <label>Uang diterima (Rp)
          <input type="number" name="uang" min="<?= (int)$p->getTotalHarga() ?>" step="500"
                 value="<?= (int)$p->getTotalHarga() ?>" required>
        </label>
        <button class="btn btn-utama" type="submit">Konfirmasi lunas</button>
      </form>
    </li>
  <?php endforeach; ?>
  </ul>
<?php endif; ?>
<script>
// Muat ulang daftar tiap 20 detik, kecuali kasir sedang mengisi kolom
setInterval(function () {
  var sedangIsi = document.activeElement && document.activeElement.tagName === 'INPUT';
  if (!sedangIsi) location.reload();
}, 20000);
</script>
<?php view_footer();
