<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

$pegawai = wajib_login();
// Buat objek sesuai role dari session (polymorphism)
$kelas = $pegawai['role'] === 'admin' ? Admin::class : Kasir::class;
$user = new $kelas((int)$pegawai['id'], $pegawai['nama'], '', '');

view_header('Dashboard ' . $pegawai['role']);
?>
<h1 class="judul">Halo, <?= e($user->getNama()) ?></h1>
<p>Masuk sebagai <?= e($user->getRole()) ?>. Fitur berikut dikerjakan di minggu 2:</p>
<ul class="fitur">
  <?php foreach ($user->tampilkanDashboard() as $fitur): ?>
    <li><?= e($fitur) ?></li>
  <?php endforeach; ?>
</ul>
<a class="btn" href="logout.php">Keluar</a>
<?php view_footer();
