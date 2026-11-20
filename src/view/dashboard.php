<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

$pegawai = wajib_login();
$user = pegawai_objek(); // Admin atau Kasir (polymorphism)

view_header('Dashboard ' . $pegawai['role'], null, true);
?>
<h1 class="judul">Halo, <?= e($user->getNama()) ?></h1>
<p>Masuk sebagai <?= e($user->getRole()) ?>.</p>
<ul class="fitur">
  <?php foreach ($user->tampilkanDashboard() as $label => $url): ?>
    <li><a href="<?= e($url) ?>"><?= e($label) ?></a></li>
  <?php endforeach; ?>
</ul>
<?php view_footer();
