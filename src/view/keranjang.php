<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

$meja = wajib_meja();
$keranjang = keranjang_ambil();
$info = $_SESSION['info_pesanan'] ?? ['nama' => '', 'catatan' => ''];

view_header('Keranjang', $meja);
?>
<a class="tautan-kembali" href="menu.php">Kembali ke menu</a>
<h1 class="judul">Keranjang</h1>

<?php if ($keranjang->isKosong()): ?>
  <section class="kosong">
    <p>Keranjang masih kosong. Pilih menu dulu, lalu kembali ke sini.</p>
    <a class="btn btn-utama" href="menu.php">Lihat menu</a>
  </section>
<?php else: ?>
  <ul class="daftar-keranjang">
  <?php foreach ($keranjang->getItems() as $key => $d): ?>
    <li class="baris">
      <div class="baris-info">
        <h3><?= e($d->getMenu()->getNamaMenu()) ?></h3>
        <?php if ($d->getCatatanItem() !== ''): ?>
          <p class="catatan-item"><?= e($d->getCatatanItem()) ?></p>
        <?php endif; ?>
        <p class="harga-satuan"><?= rupiah($d->getMenu()->getHarga()) ?> per porsi</p>
      </div>
      <div class="baris-subtotal"><?= rupiah($d->hitungSubtotal()) ?></div>

      <form method="post" action="aksi_keranjang.php" class="form-jumlah">
        <input type="hidden" name="aksi" value="ubah">
        <input type="hidden" name="key" value="<?= e($key) ?>">
        <input type="hidden" name="kembali" value="keranjang">
        <label>Jumlah
          <input type="number" name="jumlah" value="<?= $d->getJumlah() ?>" min="0" max="<?= Keranjang::MAKS_PER_ITEM ?>">
        </label>
        <button class="btn" type="submit">Ubah</button>
      </form>
      <form method="post" action="aksi_keranjang.php">
        <input type="hidden" name="aksi" value="hapus">
        <input type="hidden" name="key" value="<?= e($key) ?>">
        <input type="hidden" name="kembali" value="keranjang">
        <button class="btn btn-bahaya" type="submit">Hapus</button>
      </form>
    </li>
  <?php endforeach; ?>
  </ul>

  <form method="post" action="aksi_keranjang.php" class="form-info">
    <input type="hidden" name="aksi" value="simpan_info">
    <input type="hidden" name="kembali" value="keranjang">
    <label>Nama (boleh dikosongkan)
      <input type="text" name="nama" maxlength="100" value="<?= e($info['nama']) ?>">
    </label>
    <label>Catatan untuk seluruh pesanan
      <textarea name="catatan_umum" rows="2" maxlength="300" placeholder="Contoh: pedas banget"><?= e($info['catatan']) ?></textarea>
    </label>
    <button class="btn" type="submit">Simpan catatan</button>
  </form>

  <section class="total">
    <span>Total</span>
    <strong><?= rupiah($keranjang->hitungTotal()) ?></strong>
  </section>

  <div class="aksi-bawah">
    <button class="btn btn-utama" type="button" disabled title="Tersedia di pengerjaan minggu 2">Checkout</button>
    <form method="post" action="aksi_keranjang.php">
      <input type="hidden" name="aksi" value="kosongkan">
      <input type="hidden" name="kembali" value="keranjang">
      <button class="btn btn-bahaya" type="submit">Kosongkan keranjang</button>
    </form>
  </div>
  <p class="catatan-kecil">Checkout dan pembayaran dibuat di minggu 2.</p>
<?php endif; ?>
<?php view_footer();
