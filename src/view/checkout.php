<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';

$meja = wajib_meja();
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('keranjang.php');
}

$keranjang = keranjang_ambil();
if ($keranjang->isKosong()) {
    flash('err', 'Keranjang masih kosong.');
    redirect('menu.php');
}

$info = $_SESSION['info_pesanan'] ?? ['nama' => '', 'catatan' => ''];
$id = (new PesananDAO())->simpan($meja, $info['nama'], $info['catatan'], $keranjang);

$_SESSION['pesanan_ids'][] = $id;
unset($_SESSION['keranjang'], $_SESSION['info_pesanan']);
redirect('bayar.php?id=' . $id);
