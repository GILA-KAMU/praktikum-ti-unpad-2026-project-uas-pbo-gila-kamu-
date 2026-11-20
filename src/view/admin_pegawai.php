<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

wajib_role('admin');
$admin = pegawai_objek();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $nama = trim((string)($_POST['nama'] ?? ''));
    $username = trim((string)($_POST['username'] ?? ''));
    $password = (string)($_POST['password'] ?? '');
    try {
        if ($nama === '' || $username === '') throw new RuntimeException('Nama dan username wajib diisi.');
        if (strlen($password) < 6) throw new RuntimeException('Password minimal 6 karakter.');
        $admin->tambahKasir($nama, $username, $password);
        flash('ok', "Akun kasir $username dibuat.");
    } catch (PDOException $e) {
        flash('err', 'Username itu sudah dipakai.');
    } catch (RuntimeException $e) {
        flash('err', $e->getMessage());
    }
    redirect('admin_pegawai.php');
}

$pegawai = (new PenggunaDAO())->findAll();
view_header('Kelola pegawai', null, true);
?>
<h1 class="judul">Kelola pegawai</h1>

<form method="post" class="form-grid panel-isi">
  <label>Nama <input type="text" name="nama" maxlength="100" required></label>
  <label>Username <input type="text" name="username" maxlength="50" required autocomplete="off"></label>
  <label>Password <input type="password" name="password" minlength="6" required autocomplete="new-password"></label>
  <button class="btn btn-utama" type="submit">Tambah kasir</button>
</form>

<div class="tabel-geser">
<table class="tabel">
  <thead><tr><th>Nama</th><th>Username</th><th>Peran</th></tr></thead>
  <tbody>
  <?php foreach ($pegawai as $p): ?>
    <tr><td><?= e($p->getNama()) ?></td><td><?= e($p->getUsername()) ?></td><td><?= e(ucfirst($p->getRole())) ?></td></tr>
  <?php endforeach; ?>
  </tbody>
</table>
</div>
<?php view_footer();
