<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';
require_once __DIR__ . '/partials/layout.php';

if (!empty($_SESSION['pegawai'])) {
    redirect('dashboard.php');
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = trim((string)($_POST['username'] ?? ''));
    $password = (string)($_POST['password'] ?? '');

    $user = (new PenggunaDAO())->findByUsername($username);
    if ($user !== null && $user->login($username, $password)) {
        session_regenerate_id(true);
        $_SESSION['pegawai'] = [
            'id'   => $user->getIdPengguna(),
            'nama' => $user->getNama(),
            'role' => $user->getRole(),
        ];
        redirect('dashboard.php');
    }
    flash('err', 'Username atau password salah.');
    redirect('login.php');
}

view_header('Masuk pegawai');
?>
<h1 class="judul">Masuk pegawai</h1>
<form method="post" class="form-login">
  <label>Username
    <input type="text" name="username" required autocomplete="username">
  </label>
  <label>Password
    <input type="password" name="password" required autocomplete="current-password">
  </label>
  <button class="btn btn-utama" type="submit">Masuk</button>
</form>
<?php view_footer();
