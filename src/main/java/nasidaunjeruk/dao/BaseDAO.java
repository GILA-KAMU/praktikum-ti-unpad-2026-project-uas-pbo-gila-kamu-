package nasidaunjeruk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nasidaunjeruk.database.DataException;
import nasidaunjeruk.database.DatabaseConnection;

/** Induk semua DAO: koneksi, pengisian parameter, dan transaksi. */
abstract class BaseDAO {

    @FunctionalInterface
    protected interface KerjaSql<T> {
        T jalankan(Connection c) throws SQLException;
    }

    protected Connection db() {
        return DatabaseConnection.getConnection();
    }

    /** Isi tanda ? pada query secara berurutan (prepared statement, aman dari SQL injection). */
    protected static void isi(PreparedStatement st, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            st.setObject(i + 1, params[i]);
        }
    }

    protected static DataException gagal(String aksi, SQLException e) {
        return new DataException("Gagal " + aksi + ": " + e.getMessage(), e);
    }

    /** Jalankan beberapa query sebagai satu transaksi: semua berhasil atau semua dibatalkan. */
    protected <T> T transaksi(String aksi, KerjaSql<T> kerja) {
        Connection c = db();
        try {
            c.setAutoCommit(false);
            T hasil = kerja.jalankan(c);
            c.commit();
            return hasil;
        } catch (SQLException e) {
            rollback(c);
            throw gagal(aksi, e);
        } catch (RuntimeException e) {
            rollback(c);
            throw e;
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {
                // koneksi akan dibuat ulang bila rusak
            }
        }
    }

    private static void rollback(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignored) {
            // tidak ada yang bisa dilakukan lagi
        }
    }
}
