<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

$meja = wajib_meja();
$batas = (int)config('batas_bayar_menit');
(new PesananDAO())->batalkanKedaluwarsa($batas);
$daftar = (new PesananDAO())->findByIds($_SESSION['pesanan_ids'] ?? []);

header('Refresh: 15'); // perbarui status otomatis
view_header('Status pesanan', $meja);
?>
<a class="tautan-kembali" href="menu.php">Kembali ke menu</a>
<h1 class="judul">Status pesanan</h1>

<?php if (!$daftar): ?>
  <section class="kosong">
    <p>Belum ada pesanan dari meja ini.</p>
    <a class="btn btn-utama" href="menu.php">Lihat menu</a>
  </section>
<?php else: ?>
  <ul class="daftar-status">
  <?php foreach ($daftar as $p): ?>
    <li class="kartu-status">
      <div class="kartu-kepala">
        <h2>Pesanan #<?= $p->getIdPesanan() ?></h2>
        <?= badge_status($p->getStatus()) ?>
      </div>
      <?php daftar_item($p); ?>
      <div class="kartu-total"><?= rupiah($p->getTotalHarga()) ?></div>

      <?php if ($p->getStatus() === 'menunggu_pembayaran'): ?>
        <p class="catatan-kecil">Pesanan belum diteruskan ke dapur sampai pembayaran lunas.</p>
        <a class="btn btn-utama" href="bayar.php?id=<?= $p->getIdPesanan() ?>">Lanjut bayar</a>
      <?php elseif ($p->getStatus() === 'dibatalkan'): ?>
        <p class="catatan-kecil">Dibatalkan karena belum dibayar dalam <?= $batas ?> menit.</p>
      <?php elseif ($p->getStatus() === 'dibayar'): ?>
        <p class="catatan-kecil">Pembayaran diterima. Menunggu dapur memproses.</p>
      <?php elseif ($p->getStatus() === 'diproses'): ?>
        <p class="catatan-kecil">Sedang disiapkan di dapur.</p>
      <?php elseif ($p->getStatus() === 'disajikan'): ?>
        <p class="catatan-kecil">Pesanan sudah diantar ke meja Anda. Selamat makan!</p>
      <?php endif; ?>
    </li>
  <?php endforeach; ?>
  </ul>
  <a class="btn" href="menu.php">Tambah pesanan</a>
  <p class="catatan-kecil">Pesanan tambahan dibayar terpisah. Halaman ini diperbarui otomatis.</p>
<?php endif; ?>
<?php view_footer();
