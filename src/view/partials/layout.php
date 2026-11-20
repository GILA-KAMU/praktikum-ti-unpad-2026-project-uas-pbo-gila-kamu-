<?php
declare(strict_types=1);

function view_header(string $judul, ?Meja $meja = null, bool $pegawai = false): void
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
<body class="<?= $pegawai ? 'pegawai' : '' ?>">
<header class="topbar">
  <div class="brand">Nasi Daun Jeruk</div>
  <?php if ($pegawai && !empty($_SESSION['pegawai'])): ?>
    <nav class="nav-pegawai" aria-label="Menu pegawai">
      <a href="dashboard.php">Dashboard</a>
      <a href="logout.php">Keluar</a>
    </nav>
  <?php endif; ?>
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

/** Daftar item sebuah pesanan: "2x Nama (catatan)". */
function daftar_item(Pesanan $p): void
{
    echo '<ul class="item-pesanan">';
    foreach ($p->getDaftarDetail() as $d) {
        echo '<li>' . $d->getJumlah() . '&times; ' . e($d->getMenu()->getNamaMenu());
        if ($d->getCatatanItem() !== '') {
            echo ' <span class="catatan-item">' . e($d->getCatatanItem()) . '</span>';
        }
        echo '</li>';
    }
    echo '</ul>';
    if ($p->getCatatan()) {
        echo '<p class="catatan-item">Catatan: ' . e($p->getCatatan()) . '</p>';
    }
}

function badge_status(string $status): string
{
    return '<span class="badge badge-' . e($status) . '">' . e(label_status($status)) . '</span>';
}
