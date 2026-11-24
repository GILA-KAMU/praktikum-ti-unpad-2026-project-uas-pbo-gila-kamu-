package nasidaunjeruk.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import nasidaunjeruk.model.Meja;

public class MejaDAO extends BaseDAO {

    static Meja dariBaris(ResultSet r) throws SQLException {
        return new Meja(r.getInt("id_meja"), r.getInt("nomor_meja"), r.getString("token_qr"), r.getString("status"));
    }

    /** Mengembalikan Meja bila nomor dan kode cocok, selain itu null. */
    public Meja findByNomorDanToken(int nomor, String token) {
        try (PreparedStatement st = db().prepareStatement("SELECT * FROM meja WHERE nomor_meja = ?")) {
            isi(st, nomor);
            try (ResultSet r = st.executeQuery()) {
                if (!r.next()) return null;
                Meja meja = dariBaris(r);
                return meja.validasiToken(token) ? meja : null;
            }
        } catch (SQLException e) {
            throw gagal("memeriksa meja", e);
        }
    }

    public boolean updateStatus(int idMeja, String status) {
        if (!"kosong".equals(status) && !"terisi".equals(status)) return false;
        try (PreparedStatement st = db().prepareStatement("UPDATE meja SET status = ? WHERE id_meja = ?")) {
            isi(st, status, idMeja);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw gagal("mengubah status meja", e);
        }
    }

    public List<Meja> findAll() {
        List<Meja> hasil = new ArrayList<>();
        try (Statement st = db().createStatement();
             ResultSet r = st.executeQuery("SELECT * FROM meja ORDER BY nomor_meja")) {
            while (r.next()) hasil.add(dariBaris(r));
            return hasil;
        } catch (SQLException e) {
            throw gagal("memuat meja", e);
        }
    }

    public int nomorBerikutnya() {
        try (Statement st = db().createStatement();
             ResultSet r = st.executeQuery("SELECT COALESCE(MAX(nomor_meja), 0) + 1 FROM meja")) {
            return r.next() ? r.getInt(1) : 1;
        } catch (SQLException e) {
            throw gagal("membaca nomor meja", e);
        }
    }

    public int insert(int nomor, String token) {
        try (PreparedStatement st = db().prepareStatement(
                "INSERT INTO meja (nomor_meja, token_qr) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            isi(st, nomor, token);
            st.executeUpdate();
            try (ResultSet k = st.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw gagal("menambah meja", e);
        }
    }

    /** Kode baru membuat kode lama tidak berlaku lagi. */
    public boolean updateToken(int idMeja, String token) {
        try (PreparedStatement st = db().prepareStatement("UPDATE meja SET token_qr = ? WHERE id_meja = ?")) {
            isi(st, token, idMeja);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw gagal("mengganti kode meja", e);
        }
    }
}
