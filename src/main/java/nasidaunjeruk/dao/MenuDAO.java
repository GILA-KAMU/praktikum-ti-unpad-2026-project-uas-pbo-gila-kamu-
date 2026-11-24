package nasidaunjeruk.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import nasidaunjeruk.model.Kategori;
import nasidaunjeruk.model.Menu;

public class MenuDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT m.*, k.nama_kategori FROM menu m JOIN kategori k ON k.id_kategori = m.id_kategori";

    /** Dipakai juga oleh PesananDAO untuk membaca menu di detail pesanan. */
    static Menu dariBaris(ResultSet r) throws SQLException {
        return new Menu(
                r.getInt("id_menu"), r.getInt("id_kategori"), r.getString("nama_kategori"),
                r.getString("nama_menu"), r.getString("deskripsi"), r.getDouble("harga"),
                r.getString("gambar"), r.getBoolean("tersedia"), r.getString("varian"));
    }

    public List<Menu> findAll() {
        List<Menu> hasil = new ArrayList<>();
        try (Statement st = db().createStatement();
             ResultSet r = st.executeQuery(SELECT + " ORDER BY k.id_kategori, m.id_menu")) {
            while (r.next()) hasil.add(dariBaris(r));
            return hasil;
        } catch (SQLException e) {
            throw gagal("memuat menu", e);
        }
    }

    public Menu findById(int id) {
        try (PreparedStatement st = db().prepareStatement(SELECT + " WHERE m.id_menu = ?")) {
            isi(st, id);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? dariBaris(r) : null;
            }
        } catch (SQLException e) {
            throw gagal("memuat menu", e);
        }
    }

    public int insert(int idKategori, String nama, String deskripsi, double harga, boolean tersedia, String varian) {
        try (PreparedStatement st = db().prepareStatement(
                "INSERT INTO menu (id_kategori, nama_menu, deskripsi, harga, tersedia, varian) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            isi(st, idKategori, nama, deskripsi, harga, tersedia, varian);
            st.executeUpdate();
            try (ResultSet k = st.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw gagal("menambah menu", e);
        }
    }

    public boolean update(int id, int idKategori, String nama, String deskripsi, double harga,
                          boolean tersedia, String varian) {
        try (PreparedStatement st = db().prepareStatement(
                "UPDATE menu SET id_kategori=?, nama_menu=?, deskripsi=?, harga=?, tersedia=?, varian=? WHERE id_menu=?")) {
            isi(st, idKategori, nama, deskripsi, harga, tersedia, varian, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw gagal("mengubah menu", e);
        }
    }

    /** Gagal (DataException pelanggaran data) bila menu sudah pernah dipesan. */
    public boolean delete(int id) {
        try (PreparedStatement st = db().prepareStatement("DELETE FROM menu WHERE id_menu = ?")) {
            isi(st, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw gagal("menghapus menu", e);
        }
    }

    public List<Kategori> findKategori() {
        List<Kategori> hasil = new ArrayList<>();
        try (Statement st = db().createStatement();
             ResultSet r = st.executeQuery("SELECT * FROM kategori ORDER BY id_kategori")) {
            while (r.next()) hasil.add(new Kategori(r.getInt("id_kategori"), r.getString("nama_kategori")));
            return hasil;
        } catch (SQLException e) {
            throw gagal("memuat kategori", e);
        }
    }

    public int insertKategori(String nama) {
        try (PreparedStatement st = db().prepareStatement(
                "INSERT INTO kategori (nama_kategori) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            isi(st, nama);
            st.executeUpdate();
            try (ResultSet k = st.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw gagal("menambah kategori", e);
        }
    }
}
