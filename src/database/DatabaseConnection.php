<?php
declare(strict_types=1);

/** Utility Singleton untuk koneksi MySQL (PDO). */
final class DatabaseConnection
{
    private static ?PDO $conn = null;

    private function __construct() {}
    private function __clone() {}

    public static function getConnection(): PDO
    {
        if (self::$conn === null) {
            $c = require __DIR__ . '/../../config.php';
            $dsn = "mysql:host={$c['host']};dbname={$c['dbname']};charset=utf8mb4";
            self::$conn = new PDO($dsn, $c['user'], $c['pass'], [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ]);
        }
        return self::$conn;
    }

    public static function closeConnection(): void
    {
        self::$conn = null;
    }
}
