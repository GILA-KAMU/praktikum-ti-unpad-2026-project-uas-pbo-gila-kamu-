package nasidaunjeruk.database;

import java.sql.SQLIntegrityConstraintViolationException;

/** Kesalahan database yang dibungkus supaya tampilan cukup menangkap satu jenis exception. */
public class DataException extends RuntimeException {

    public DataException(String pesan, Throwable sebab) {
        super(pesan, sebab);
    }

    /** True bila gagal karena aturan tabel, mis. data kembar (UNIQUE) atau masih dipakai tabel lain (FOREIGN KEY). */
    public boolean isPelanggaranData() {
        return getCause() instanceof SQLIntegrityConstraintViolationException;
    }
}
