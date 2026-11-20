<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

wajib_role('admin');
$admin = pegawai_objek();
$dao = new MenuDAO();

/** 'Merah, Hijau' menjadi 'Merah,Hijau'; kosong menjadi null. */
function rapikan_varian(string $teks): ?string
{
    $bagian = array_filter(array_map('trim', explode(',', $teks)), fn($v) => $v !== '');
    return $bagian ? implode(',', $bagian) : null;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $aksi = $_POST['aksi'] ?? '';
    try {
        if ($aksi === 'tambah_kategori') {
            $nama = trim((string)($_POST['nama_kategori'] ?? ''));
            if ($nama === '') throw new RuntimeException('Nama kategori wajib diisi.');
            $dao->insertKategori($nama);
            flash('ok', 'Kategori ditambahkan.');
        } elseif ($aksi === 'tambah_menu' || $aksi === 'ubah_menu') {
            $nama = trim((string)($_POST['nama_menu'] ?? ''));
            $harga = (float)($_POST['harga'] ?? 0);
            if ($nama === '' || $harga <= 0) throw new RuntimeException('Nama dan harga wajib diisi dengan benar.');
            $idKategori = (int)($_POST['id_kategori'] ?? 0);
            $deskripsi = trim((string)($_POST['deskripsi'] ?? '')) ?: null;
            $varian = rapikan_varian((string)($_POST['varian'] ?? ''));
            if ($aksi === 'tambah_menu') {
                $dao->insert($idKategori, $nama, $deskripsi, $harga, $varian);
                flash('ok', 'Menu ditambahkan.');
            } else {
                $dao->update((int)$_POST['id_menu'], $idKategori, $nama, $deskripsi, $harga, isset($_POST['tersedia']), $varian);
                flash('ok', 'Menu diperbarui.');
            }
        } elseif ($aksi === 'hapus_menu') {
            $dao->delete((int)($_POST['id_menu'] ?? 0));
            flash('ok', 'Menu dihapus.');
        }
    } catch (PDOException $e) {
        flash('err', $aksi === 'hapus_menu'
            ? 'Menu sudah pernah dipesan, jadi tidak bisa dihapus. Hapus centang "Tersedia" saja.'
            : 'Data tidak tersimpan. Periksa kategori dan isian Anda.');
    } catch (RuntimeException $e) {
        flash('err', $e->getMessage());
    }
    redirect('admin_menu.php');
}

$daftarMenu = $admin->kelolaMenu();
$kategori = $dao->findKategori();

function pilih_kategori(array $kategori, int $dipilih = 0): void
{
    echo '<select name="id_kategori" required>';
    foreach ($kategori as $k) {
        $sel = (int)$k['id_kategori'] === $dipilih ? ' selected' : '';
        echo '<option value="' . (int)$k['id_kategori'] . '"' . $sel . '>' . e($k['nama_kategori']) . '</option>';
    }
    echo '</select>';
}

view_header('Kelola menu', null, true);
?>
<h1 class="judul">Kelola menu</h1>

<details class="panel" open>
  <summary>Tambah menu baru</summary>
  <form method="post" class="form-grid">
    <input type="hidden" name="aksi" value="tambah_menu">
    <label>Nama menu <input type="text" name="nama_menu" maxlength="100" required></label>
    <label>Kategori <?php pilih_kategori($kategori); ?></label>
    <label>Harga (Rp) <input type="number" name="harga" min="1" step="500" required></label>
    <label>Varian (pisahkan koma) <input type="text" name="varian" maxlength="100" placeholder="Contoh: Merah,Hijau"></label>
    <label class="lebar">Deskripsi <input type="text" name="deskripsi"></label>
    <button class="btn btn-utama" type="submit">Tambah menu</button>
  </form>
</details>

<details class="panel">
  <summary>Tambah kategori</summary>
  <form method="post" class="form-grid">
    <input type="hidden" name="aksi" value="tambah_kategori">
    <label>Nama kategori <input type="text" name="nama_kategori" maxlength="50" required></label>
    <button class="btn" type="submit">Tambah kategori</button>
  </form>
</details>

<h2 class="sub">Daftar menu</h2>
<?php foreach ($daftarMenu as $m): ?>
  <details class="panel">
    <summary>
      <?= e($m->getNamaMenu()) ?> &middot; <?= rupiah($m->getHarga()) ?>
      <?= $m->isTersedia() ? '' : '<span class="badge badge-dibatalkan">Tidak tersedia</span>' ?>
    </summary>
    <form method="post" class="form-grid">
      <input type="hidden" name="aksi" value="ubah_menu">
      <input type="hidden" name="id_menu" value="<?= $m->getIdMenu() ?>">
      <label>Nama menu <input type="text" name="nama_menu" maxlength="100" value="<?= e($m->getNamaMenu()) ?>" required></label>
      <label>Kategori <?php pilih_kategori($kategori, $m->getIdKategori()); ?></label>
      <label>Harga (Rp) <input type="number" name="harga" min="1" step="500" value="<?= (int)$m->getHarga() ?>" required></label>
      <label>Varian <input type="text" name="varian" value="<?= e(implode(',', $m->getVarianList())) ?>"></label>
      <label class="lebar">Deskripsi <input type="text" name="deskripsi" value="<?= e($m->getDeskripsi()) ?>"></label>
      <label class="centang"><input type="checkbox" name="tersedia" <?= $m->isTersedia() ? 'checked' : '' ?>> Tersedia</label>
      <button class="btn btn-utama" type="submit">Simpan perubahan</button>
    </form>
    <form method="post" onsubmit="return confirm('Hapus menu ini?');">
      <input type="hidden" name="aksi" value="hapus_menu">
      <input type="hidden" name="id_menu" value="<?= $m->getIdMenu() ?>">
      <button class="btn btn-bahaya" type="submit">Hapus menu</button>
    </form>
  </details>
<?php endforeach; ?>
<?php view_footer();
