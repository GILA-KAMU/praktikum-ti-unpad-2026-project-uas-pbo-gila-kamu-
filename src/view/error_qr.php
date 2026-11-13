<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

view_header('QR tidak valid');
?>
<section class="pesan-error">
  <h1>QR code tidak dikenali</h1>
  <p>Nomor meja atau kodenya tidak cocok. Scan ulang QR code yang ada di meja Anda.
     Jika masih gagal, panggil pelayan.</p>
</section>
<?php view_footer();
