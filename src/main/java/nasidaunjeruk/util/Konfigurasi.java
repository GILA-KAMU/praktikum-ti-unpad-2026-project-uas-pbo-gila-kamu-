package nasidaunjeruk.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Properties;

/** Membaca config.properties dari folder proyek. Nilai bawaan dipakai bila file atau kunci tidak ada. */
public final class Konfigurasi {

    private static final Properties P = muat();

    private Konfigurasi() {}

    private static Properties muat() {
        Properties p = new Properties();
        Path file = Path.of("config.properties");
        if (Files.isRegularFile(file)) {
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                p.load(r);
            } catch (IOException e) {
                System.err.println("Gagal membaca config.properties: " + e.getMessage());
            }
        }
        return p;
    }

    private static String get(String kunci, String bawaan) {
        String v = P.getProperty(kunci);
        return (v == null || v.isBlank()) ? bawaan : v.trim();
    }

    private static int getInt(String kunci, int bawaan) {
        try {
            return Integer.parseInt(get(kunci, String.valueOf(bawaan)));
        } catch (NumberFormatException e) {
            return bawaan;
        }
    }

    public static String dbHost() { return get("db.host", "127.0.0.1"); }
    public static int dbPort() { return getInt("db.port", 3306); }
    public static String dbNama() { return get("db.nama", "db_nasi_daun_jeruk"); }
    public static String dbUser() { return get("db.user", "root"); }
    public static String dbPass() { return get("db.pass", ""); }

    public static ZoneId zonaWaktu() {
        try {
            return ZoneId.of(get("zona_waktu", "Asia/Jakarta"));
        } catch (DateTimeException e) {
            return ZoneId.of("Asia/Jakarta");
        }
    }

    /** Offset zona waktu untuk MySQL, mis. "+07:00". */
    public static String offsetZona() {
        String id = zonaWaktu().getRules().getOffset(Instant.now()).getId();
        return "Z".equals(id) ? "+00:00" : id;
    }
}
