<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

wajib_role('admin');
$admin = pegawai_objek();
$periode = ($_GET['periode'] ?? 'harian') === 'bulanan' ? 'bulanan' : 'harian';
$baris = $admin->lihatLaporan($periode);
$terlaris = (new PesananDAO())->menuTerlaris(5);

$totalPesanan = array_sum(array_map(fn($b) => (int)$b['jumlah_pesanan'], $baris));
$totalUang = array_sum(array_map(fn($b) => (float)$b['total'], $baris));

view_header('Laporan penjualan', null, true);
?>
<h1 class="judul">Laporan penjualan</h1>

<div class="tab-periode">
  <a class="btn <?= $periode === 'harian' ? 'btn-utama' : '' ?>" href="?periode=harian">Harian</a>
  <a class="btn <?= $periode === 'bulanan' ? 'btn-utama' : '' ?>" href="?periode=bulanan">Bulanan</a>
</div>
<p class="catatan-kecil">Hanya pesanan yang sudah lunas. Pesanan yang menunggu bayar atau dibatalkan tidak dihitung.</p>

<?php if (!$baris): ?>
  <p class="kosong">Belum ada penjualan.</p>
<?php else: ?>
  <div class="tabel-geser">
  <table class="tabel">
    <thead><tr><th><?= $periode === 'harian' ? 'Tanggal' : 'Bulan' ?></th><th>Jumlah pesanan</th><th>Pendapatan</th></tr></thead>
    <tbody>
    <?php foreach ($baris as $b): ?>
      <tr><td><?= e($b['periode']) ?></td><td><?= (int)$b['jumlah_pesanan'] ?></td><td><?= rupiah((float)$b['total']) ?></td></tr>
    <?php endforeach; ?>
    </tbody>
    <tfoot><tr><th>Total</th><th><?= $totalPesanan ?></th><th><?= rupiah($totalUang) ?></th></tr></tfoot>
  </table>
  </div>

  <h2 class="sub">Menu terlaris</h2>
  <ol class="terlaris">
    <?php foreach ($terlaris as $t): ?>
      <li><?= e($t['nama_menu']) ?> <strong><?= (int)$t['terjual'] ?> porsi</strong></li>
    <?php endforeach; ?>
  </ol>
<?php endif; ?>
<?php view_footer();
