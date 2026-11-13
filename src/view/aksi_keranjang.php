<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';

wajib_meja();
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('menu.php');
}

$keranjang = keranjang_ambil();
$aksi = $_POST['aksi'] ?? '';
$kembali = ($_POST['kembali'] ?? 'menu') === 'keranjang' ? 'keranjang.php' : 'menu.php';

switch ($aksi) {
    case 'tambah':
        $menu = (new MenuDAO())->findById((int)($_POST['id_menu'] ?? 0));
        if ($menu === null || !$menu->isTersedia()) {
            flash('err', 'Menu tidak tersedia.');
            break;
        }
        $catatan = [];
        $varian = trim((string)($_POST['varian'] ?? ''));
        if ($menu->getVarianList()) {
            if (!in_array($varian, $menu->getVarianList(), true)) {
                flash('err', 'Pilih salah satu varian dulu.');
                break;
            }
            $catatan[] = $varian;
        }
        $teks = trim((string)($_POST['catatan'] ?? ''));
        if ($teks !== '') {
            $catatan[] = mb_substr($teks, 0, 100);
        }
        $jumlah = max(1, min((int)($_POST['jumlah'] ?? 1), Keranjang::MAKS_PER_ITEM));
        $keranjang->tambahItem(new DetailPesanan($menu, $jumlah, implode(', ', $catatan)));
        flash('ok', $menu->getNamaMenu() . ' masuk ke keranjang.');
        break;

    case 'ubah':
        $keranjang->ubahJumlah((string)($_POST['key'] ?? ''), (int)($_POST['jumlah'] ?? 1));
        break;

    case 'hapus':
        $keranjang->hapusItem((string)($_POST['key'] ?? ''));
        flash('ok', 'Item dihapus dari keranjang.');
        break;

    case 'kosongkan':
        $keranjang->kosongkan();
        unset($_SESSION['info_pesanan']);
        flash('ok', 'Keranjang dikosongkan.');
        break;

    case 'simpan_info':
        $_SESSION['info_pesanan'] = [
            'nama'    => mb_substr(trim((string)($_POST['nama'] ?? '')), 0, 100),
            'catatan' => mb_substr(trim((string)($_POST['catatan_umum'] ?? '')), 0, 300),
        ];
        flash('ok', 'Catatan pesanan disimpan.');
        break;
}

keranjang_simpan($keranjang);
redirect($kembali);
