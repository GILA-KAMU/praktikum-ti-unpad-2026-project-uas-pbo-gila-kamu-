<?php
// Titik masuk dari QR code: /?meja=5&token=a1b2c3
$query = $_SERVER['QUERY_STRING'] ?? '';
header('Location: src/view/menu.php' . ($query !== '' ? '?' . $query : ''));
exit;
