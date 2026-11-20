<?php
declare(strict_types=1);

/**
 * Bootstrap aplikasi: autoload class, session, dan fungsi bantu.
 * Dipanggil di awal setiap halaman di src/view/.
 */
spl_autoload_register(function (string $class): void {
    foreach (['model', 'dao', 'database'] as $folder) {
        $file = __DIR__ . "/$folder/$class.php";
        if (is_file($file)) {
            require_once $file;
            return;
        }
    }
});

date_default_timezone_set((string)(config('zona_waktu') ?: 'Asia/Jakarta'));

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// ---------- Fungsi bantu umum ----------
/** Baca nilai dari config.php. */
function config(string $kunci): mixed
{
    static $c = null;
    $c ??= require __DIR__ . '/../config.php';
    return $c[$kunci] ?? null;
}

function e(?string $teks): string
{
    return htmlspecialchars((string)$teks, ENT_QUOTES, 'UTF-8');
}

function rupiah(float $angka): string
{
    return 'Rp ' . number_format($angka, 0, ',', '.');
}

function redirect(string $url): never
{
    header('Location: ' . $url);
    exit;
}

function flash(string $tipe, string $pesan): void
{
    $_SESSION['flash'] = ['tipe' => $tipe, 'pesan' => $pesan];
}

// ---------- Meja (hasil scan QR) ----------
/**
 * Validasi meja dari URL (?meja=&token=) atau dari session.
 * Token dicek ulang ke database di setiap halaman.
 */
function meja_aktif(): ?Meja
{
    if (isset($_GET['meja'], $_GET['token'])) {
        $meja = (new MejaDAO())->findByNomorDanToken((int)$_GET['meja'], (string)$_GET['token']);
        if ($meja === null) {
            unset($_SESSION['meja'], $_SESSION['keranjang'], $_SESSION['pesanan_ids']);
            return null;
        }
        // Ganti meja = keranjang lama dibuang
        if (($_SESSION['meja']['nomor'] ?? null) !== $meja->getNomorMeja()) {
            unset($_SESSION['keranjang'], $_SESSION['info_pesanan'], $_SESSION['pesanan_ids']);
        }
        $_SESSION['meja'] = ['nomor' => $meja->getNomorMeja(), 'token' => $meja->getTokenQr()];
        return $meja;
    }

    if (isset($_SESSION['meja'])) {
        return (new MejaDAO())->findByNomorDanToken(
            (int)$_SESSION['meja']['nomor'],
            (string)$_SESSION['meja']['token']
        );
    }
    return null;
}

/** Wajib punya meja valid, jika tidak tampilkan halaman error QR. */
function wajib_meja(): Meja
{
    $meja = meja_aktif();
    if ($meja === null) {
        http_response_code(403);
        require __DIR__ . '/view/error_qr.php';
        exit;
    }
    return $meja;
}

// ---------- Keranjang di session ----------
/** Bangun ulang Keranjang dari session. Harga selalu diambil dari database. */
function keranjang_ambil(): Keranjang
{
    $k = new Keranjang();
    $dao = new MenuDAO();
    foreach ($_SESSION['keranjang'] ?? [] as $row) {
        $menu = $dao->findById((int)$row['id_menu']);
        if ($menu !== null && $menu->isTersedia()) {
            $k->tambahItem(new DetailPesanan($menu, (int)$row['jumlah'], (string)$row['catatan']));
        }
    }
    return $k;
}

function keranjang_simpan(Keranjang $k): void
{
    $_SESSION['keranjang'] = [];
    foreach ($k->getItems() as $d) {
        $_SESSION['keranjang'][] = [
            'id_menu' => $d->getMenu()->getIdMenu(),
            'jumlah'  => $d->getJumlah(),
            'catatan' => $d->getCatatanItem(),
        ];
    }
}

// ---------- Login pegawai ----------
function wajib_login(): array
{
    if (empty($_SESSION['pegawai'])) {
        redirect('login.php');
    }
    return $_SESSION['pegawai'];
}

/** Wajib login dengan salah satu peran, jika tidak kembali ke dashboard. */
function wajib_role(string ...$roles): array
{
    $pegawai = wajib_login();
    if (!in_array($pegawai['role'], $roles, true)) {
        flash('err', 'Anda tidak punya akses ke halaman itu.');
        redirect('dashboard.php');
    }
    return $pegawai;
}

/** Objek Admin atau Kasir sesuai pegawai yang login (polymorphism). */
function pegawai_objek(): Pengguna
{
    $p = wajib_login();
    $kelas = $p['role'] === 'admin' ? Admin::class : Kasir::class;
    return new $kelas((int)$p['id'], $p['nama'], '', '');
}

// ---------- Pesanan pelanggan ----------
/** Pesanan hanya boleh dibuka oleh sesi yang membuatnya. */
function pesanan_sesi(int $id): ?Pesanan
{
    if (!in_array($id, $_SESSION['pesanan_ids'] ?? [], true)) {
        return null;
    }
    return (new PesananDAO())->findById($id);
}

function label_status(string $status): string
{
    return [
        'menunggu_pembayaran' => 'Menunggu pembayaran',
        'dibayar'   => 'Dibayar',
        'diproses'  => 'Diproses',
        'disajikan' => 'Disajikan',
        'selesai'   => 'Selesai',
        'dibatalkan' => 'Dibatalkan',
    ][$status] ?? $status;
}

// ---------- QR code ----------
/** Alamat dasar aplikasi untuk isi QR (config 'base_url' menimpa deteksi otomatis). */
function base_url(): string
{
    $c = config('base_url');
    if (is_string($c) && $c !== '') {
        return rtrim($c, '/');
    }
    $skema = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $path = preg_replace('#/src/view/[^/]+$#', '', $_SERVER['SCRIPT_NAME'] ?? '');
    return $skema . '://' . ($_SERVER['HTTP_HOST'] ?? 'localhost') . $path;
}

/** Gambar QR dibuat oleh layanan api.qrserver.com (butuh internet). */
function qr_gambar(string $isi, int $ukuran = 220): string
{
    return 'https://api.qrserver.com/v1/create-qr-code/?size=' . $ukuran . 'x' . $ukuran
         . '&data=' . rawurlencode($isi);
}
