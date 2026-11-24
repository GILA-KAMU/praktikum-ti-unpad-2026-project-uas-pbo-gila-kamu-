package nasidaunjeruk.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/** Abstract class induk Admin dan Kasir. */
public abstract class Pengguna {

    protected final int idPengguna;
    protected String nama;
    protected final String username;
    protected final String password; // hash bcrypt

    protected Pengguna(int idPengguna, String nama, String username, String password) {
        this.idPengguna = idPengguna;
        this.nama = nama;
        this.username = username;
        this.password = password;
    }

    /** Cek username dan password (dibandingkan dengan hash bcrypt). */
    public boolean login(String username, String passwordPlain) {
        if (username == null || passwordPlain == null || password == null) return false;
        boolean usernameCocok = MessageDigest.isEqual(
                this.username.getBytes(StandardCharsets.UTF_8),
                username.getBytes(StandardCharsets.UTF_8));
        // Hash dari PHP berawalan $2y$, isinya sama dengan $2a$ yang dikenali jBCrypt
        String hash = password.startsWith("$2y$") ? "$2a$" + password.substring(4) : password;
        try {
            return usernameCocok && BCrypt.checkpw(passwordPlain, hash);
        } catch (IllegalArgumentException e) {
            return false; // format hash tidak dikenali
        }
    }

    /** Hash bcrypt untuk password baru (tetap bisa dibaca versi PHP). */
    public static String hashPassword(String passwordPlain) {
        return BCrypt.hashpw(passwordPlain, BCrypt.gensalt(10));
    }

    public int getIdPengguna() { return idPengguna; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama.trim(); }
    public String getUsername() { return username; }

    public abstract String getRole();

    /** Fitur dashboard sesuai peran (polymorphism). */
    public abstract List<Fitur> tampilkanDashboard();
}
