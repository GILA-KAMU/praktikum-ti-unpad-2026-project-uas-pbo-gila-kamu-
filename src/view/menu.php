<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

$meja = wajib_meja();

// Setelah scan, bersihkan URL dari token supaya tidak ikut terbagikan.
if (isset($_GET['token'])) {
    redirect('menu.php');
}

$daftarMenu = (new MenuDAO())->findAll();
$perKategori = [];
foreach ($daftarMenu as $m) {
    $perKategori[$m->getNamaKategori()][] = $m;
}
$keranjang = keranjang_ambil();

view_header('Menu', $meja);
?>
<h1 class="judul">Mau makan apa?</h1>

<?php foreach ($perKategori as $kategori => $items): ?>
  <section class="kategori">
    <h2><?= e($kategori) ?></h2>
    <ul class="daftar-menu">
    <?php foreach ($items as $m): ?>
      <li class="item <?= $m->isTersedia() ? '' : 'habis' ?>">
        <div class="item-info">
          <h3><?= e($m->getNamaMenu()) ?></h3>
          <p><?= e($m->getDeskripsi()) ?></p>
        </div>
        <div class="item-harga"><?= rupiah($m->getHarga()) ?></div>

        <?php if ($m->isTersedia()): ?>
        <form method="post" action="aksi_keranjang.php" class="item-form">
          <input type="hidden" name="aksi" value="tambah">
          <input type="hidden" name="id_menu" value="<?= $m->getIdMenu() ?>">
          <input type="hidden" name="kembali" value="menu">

          <?php if ($m->getVarianList()): ?>
            <label>Pilihan
              <select name="varian" required>
                <?php foreach ($m->getVarianList() as $v): ?>
                  <option value="<?= e($v) ?>"><?= e($v) ?></option>
                <?php endforeach; ?>
              </select>
            </label>
          <?php endif; ?>

          <label>Catatan
            <input type="text" name="catatan" maxlength="100" placeholder="Contoh: tanpa sambal">
          </label>

          <label class="kolom-jumlah">Jumlah
            <input type="number" name="jumlah" value="1" min="1" max="<?= Keranjang::MAKS_PER_ITEM ?>">
          </label>

          <button type="submit" class="btn btn-utama">Tambah ke keranjang</button>
        </form>
        <?php else: ?>
          <p class="stok-habis">Sedang habis</p>
        <?php endif; ?>
      </li>
    <?php endforeach; ?>
    </ul>
  </section>
<?php endforeach; ?>

<?php if (!$keranjang->isKosong()): ?>
  <a class="bar-keranjang" href="keranjang.php">
    <span><?= $keranjang->jumlahItem() ?> item di keranjang</span>
    <strong><?= rupiah($keranjang->hitungTotal()) ?></strong>
  </a>
<?php endif; ?>
<?php view_footer();
