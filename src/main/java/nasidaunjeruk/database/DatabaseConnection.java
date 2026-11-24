package nasidaunjeruk.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import nasidaunjeruk.util.Konfigurasi;

/** Utility Singleton untuk koneksi MySQL (JDBC). */
public final class DatabaseConnection {

    private static Connection conn;

    private DatabaseConnection() {}

    public static synchronized Connection getConnection() {
        try {
            if (conn == null || !conn.isValid(2)) {
                String url = "jdbc:mysql://" + Konfigurasi.dbHost() + ":" + Konfigurasi.dbPort() + "/" + Konfigurasi.dbNama()
                        + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
                        + "&allowPublicKeyRetrieval=true&connectTimeout=5000";
                conn = DriverManager.getConnection(url, Konfigurasi.dbUser(), Konfigurasi.dbPass());

                // Samakan zona waktu MySQL dengan aplikasi (dipakai untuk waktu pesan dan laporan)
                String offset = Konfigurasi.offsetZona();
                if (offset.matches("[+-]\\d{2}:\\d{2}")) {
                    try (Statement st = conn.createStatement()) {
                        st.execute("SET time_zone = '" + offset + "'");
                    }
                }
            }
            return conn;
        } catch (SQLException e) {
            conn = null;
            throw new DataException("Tidak bisa terhubung ke database \"" + Konfigurasi.dbNama() + "\".\n"
                    + "Pastikan MySQL (Laragon/XAMPP) sudah jalan dan database sudah di-import.\n\n"
                    + "Detail: " + e.getMessage(), e);
        }
    }

    public static synchronized void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                // tidak apa-apa, aplikasi sedang ditutup
            }
            conn = null;
        }
    }
}
