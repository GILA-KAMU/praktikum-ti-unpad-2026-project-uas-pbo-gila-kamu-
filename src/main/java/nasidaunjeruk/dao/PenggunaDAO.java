package nasidaunjeruk.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import nasidaunjeruk.model.Admin;
import nasidaunjeruk.model.Kasir;
import nasidaunjeruk.model.Pengguna;

/** Dipakai untuk login dan kelola akun pegawai. */
public class PenggunaDAO extends BaseDAO {

    /** Objek Admin atau Kasir sesuai kolom role (polymorphism). */
    private static Pengguna dariBaris(ResultSet r) throws SQLException {
        int id = r.getInt("id_pengguna");
        String nama = r.getString("nama");
        String username = r.getString("username");
        String password = r.getString("password");
        return "admin".equals(r.getString("role"))
                ? new Admin(id, nama, username, password)
                : new Kasir(id, nama, username, password);
    }

    public List<Pengguna> findAll() {
        List<Pengguna> hasil = new ArrayList<>();
        try (Statement st = db().createStatement();
             ResultSet r = st.executeQuery("SELECT * FROM pengguna ORDER BY role, nama")) {
            while (r.next()) hasil.add(dariBaris(r));
            return hasil;
        } catch (SQLException e) {
            throw gagal("memuat pegawai", e);
        }
    }

    public int insert(String nama, String username, String passwordHash, String role) {
        try (PreparedStatement st = db().prepareStatement(
                "INSERT INTO pengguna (nama, username, password, role) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            isi(st, nama, username, passwordHash, role);
            st.executeUpdate();
            try (ResultSet k = st.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw gagal("menambah pegawai", e);
        }
    }

    public Pengguna findByUsername(String username) {
        try (PreparedStatement st = db().prepareStatement("SELECT * FROM pengguna WHERE username = ?")) {
            isi(st, username);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? dariBaris(r) : null;
            }
        } catch (SQLException e) {
            throw gagal("memeriksa akun", e);
        }
    }
}
