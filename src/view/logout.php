<?php
declare(strict_types=1);
require_once __DIR__ . '/../Main.php';

unset($_SESSION['pegawai']);
session_regenerate_id(true);
redirect('login.php');
