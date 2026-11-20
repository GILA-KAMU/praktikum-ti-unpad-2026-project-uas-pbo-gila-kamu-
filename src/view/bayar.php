<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

$meja = wajib_meja();
$batas = (int)config('batas_bayar_menit');
$dao = new PesananDAO();
$dao->batalkanKedaluwarsa($batas);

$id = (int)($_GET['id'] ?? $_POST['id'] ?? 0);
$pesanan = pesanan_sesi($id);
if ($pesanan === null) {
    flash('err', 'Pesanan tidak ditemukan.');
    redirect('menu.php');
}
$kembali = 'bayar.php?id=' . $id;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if ($pesanan->getStatus() !== 'menunggu_pembayaran') {
        flash('err', 'Pesanan ini sudah tidak bisa dibayar.');
        redirect('status.php');
    }
    $aksi = $_POST['aksi'] ?? '';

    if ($aksi === 'pilih') {
        $metode = (string)($_POST['metode'] ?? '');
        if (in_array($metode, ['tunai', 'qris'], true)) {
            $dao->pilihMetode($id, $metode, $pesanan->getTotalHarga());
        }
        redirect($kembali);
    }

    if ($aksi === 'qris_bayar') {
        $bayar = $pesanan->getPembayaran();
        if ($bayar instanceof PembayaranQRIS) {
            // Simulasi: pada sistem asli kode ini datang dari penyedia QRIS
            $bayar->setKodeTransaksi('SIM-' . strtoupper(bin2hex(random_bytes(4))));
            if ($bayar->prosesPembayaran() && $dao->konfirmasiPembayaran($id, null)) {
                flash('ok', 'Pembayaran berhasil. Pesanan Anda diteruskan ke dapur.');
                redirect('status.php');
            }
        }
        flash('err', 'Pembayaran gagal. Coba lagi.');
        redirect($kembali);
    }
    redirect($kembali);
}

if ($pesanan->getStatus() !== 'menunggu_pembayaran' && $pesanan->getStatus() !== 'dibatalkan') {
    redirect('status.php');
}

$bayar = $pesanan->getPembayaran();
$metode = $bayar?->getMetode();

view_header('Pembayaran', $meja);
?>
<h1 class="judul">Pembayaran</h1>

<?php if ($pesanan->getStatus() === 'dibatalkan'): ?>
  <section class="pesan-error">
    <h1>Waktu bayar habis</h1>
    <p>Pesanan #<?= $id ?> dibatalkan karena belum dibayar dalam <?= $batas ?> menit.
       Pilih menu lagi untuk membuat pesanan baru.</p>
    <a class="btn btn-utama" href="menu.php">Kembali ke menu</a>
  </section>
<?php else: ?>
  <section class="ringkasan">
    <h2>Pesanan #<?= $id ?></h2>
    <?php daftar_item($pesanan); ?>
    <div class="total"><span>Total</span><strong><?= rupiah($pesanan->getTotalHarga()) ?></strong></div>
    <p class="hitung-mundur">Sisa waktu bayar
      <strong id="sisa" data-detik="<?= $pesanan->sisaDetik($batas) ?>"></strong></p>
  </section>

  <?php if ($metode === null): ?>
    <h2 class="sub">Pilih cara bayar</h2>
    <div class="pilihan-bayar">
      <form method="post">
        <input type="hidden" name="id" value="<?= $id ?>">
        <input type="hidden" name="aksi" value="pilih">
        <input type="hidden" name="metode" value="qris">
        <button class="btn btn-utama" type="submit">Bayar dengan QRIS</button>
        <p class="catatan-kecil">Scan dan bayar, pesanan langsung diteruskan ke dapur.</p>
      </form>
      <form method="post">
        <input type="hidden" name="id" value="<?= $id ?>">
        <input type="hidden" name="aksi" value="pilih">
        <input type="hidden" name="metode" value="tunai">
        <button class="btn" type="submit">Bayar tunai ke kasir</button>
        <p class="catatan-kecil">Pesanan diteruskan setelah kasir menerima uang Anda.</p>
      </form>
    </div>

  <?php elseif ($metode === 'qris'): ?>
    <h2 class="sub">Bayar dengan QRIS</h2>
    <div class="qris">
      <img src="<?= e(qr_gambar('QRIS-SIMULASI|pesanan=' . $id . '|total=' . (int)$pesanan->getTotalHarga(), 200)) ?>"
           width="200" height="200" alt="QR pembayaran QRIS">
      <p>Scan QR ini dengan aplikasi pembayaran Anda.</p>
      <form method="post">
        <input type="hidden" name="id" value="<?= $id ?>">
        <input type="hidden" name="aksi" value="qris_bayar">
        <button class="btn btn-utama" type="submit">Simulasikan pembayaran berhasil</button>
      </form>
      <p class="catatan-kecil">Tombol ini hanya untuk demo. Pada sistem asli, status lunas datang otomatis dari penyedia QRIS.</p>
    </div>
    <form method="post" class="ganti-metode">
      <input type="hidden" name="id" value="<?= $id ?>">
      <input type="hidden" name="aksi" value="pilih">
      <input type="hidden" name="metode" value="tunai">
      <button class="btn" type="submit">Ganti ke tunai</button>
    </form>

  <?php else: ?>
    <h2 class="sub">Bayar tunai ke kasir</h2>
    <div class="tunai">
      <p>Datang ke kasir dan sebutkan <strong>Meja <?= $meja->getNomorMeja() ?></strong>
         dengan total <strong><?= rupiah($pesanan->getTotalHarga()) ?></strong>.</p>
      <p>Halaman ini otomatis berpindah setelah kasir mengonfirmasi.</p>
    </div>
    <form method="post" class="ganti-metode">
      <input type="hidden" name="id" value="<?= $id ?>">
      <input type="hidden" name="aksi" value="pilih">
      <input type="hidden" name="metode" value="qris">
      <button class="btn" type="submit">Ganti ke QRIS</button>
    </form>
  <?php endif; ?>

  <script>
  (function () {
    var el = document.getElementById('sisa');
    if (!el) return;
    var s = parseInt(el.dataset.detik, 10);
    function tik() {
      if (s <= 0) { el.textContent = '0:00'; setTimeout(function () { location.reload(); }, 1500); return; }
      var m = Math.floor(s / 60), d = s % 60;
      el.textContent = m + ':' + (d < 10 ? '0' : '') + d;
      s--; setTimeout(tik, 1000);
    }
    tik();
    // Cek berkala: pindah ke halaman status setelah kasir mengonfirmasi tunai
    setInterval(function () { location.reload(); }, 8000);
  })();
  </script>
<?php endif; ?>
<?php view_footer();
