<?php
declare(strict_types=1);

function view_header(string $judul, ?Meja $meja = null): void
{
    ?>
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><?= e($judul) ?> | Nasi Daun Jeruk</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,400;12..96,600;12..96,800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="assets/style.css">
</head>
<body>
<header class="topbar">
  <div class="brand">Nasi Daun Jeruk</div>
  <?php if ($meja): ?>
    <div class="meja-badge" aria-label="Nomor meja">
      <span class="meja-teks">Meja</span>
      <span class="meja-nomor"><?= $meja->getNomorMeja() ?></span>
    </div>
  <?php endif; ?>
</header>
<main class="wrap">
<?php
    if (!empty($_SESSION['flash'])) {
        $f = $_SESSION['flash'];
        unset($_SESSION['flash']);
        echo '<div class="flash flash-' . e($f['tipe']) . '" role="status">' . e($f['pesan']) . '</div>';
    }
}

function view_footer(): void
{
    echo "</main>\n</body>\n</html>";
}
