package nasidaunjeruk.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nasidaunjeruk.model.BarisLaporan;
import nasidaunjeruk.model.DetailPesanan;
import nasidaunjeruk.model.Keranjang;
import nasidaunjeruk.model.Meja;
import nasidaunjeruk.model.MenuTerlaris;
import nasidaunjeruk.model.Pesanan;
import nasidaunjeruk.model.StatusPesanan;

public class PesananDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT p.*, mj.nomor_meja, mj.token_qr, mj.status AS status_meja "
            + "FROM pesanan p JOIN meja mj ON mj.id_meja = p.id_meja";

    // ---------- membaca ----------
    private List<Pesanan> ambil(String where, String order, Object... params) {
        List<Pesanan> hasil = new ArrayList<>();
        try (PreparedStatement st = db().prepareStatement(SELECT + " " + where + " ORDER BY " + order)) {
            isi(st, params);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    Meja meja = new Meja(r.getInt("id_meja"), r.getInt("nomor_meja"),
                            r.getString("token_qr"), r.getString("status_meja"));
                    hasil.add(new Pesanan(
                            r.getInt("id_pesanan"), meja, r.getString("nama_pelanggan"), r.getString("catatan"),
                            r.getObject("waktu_pesan", LocalDateTime.class),
                            StatusPesanan.dariKode(r.getString("status")), r.getDouble("total_harga")));
                }
            }
            for (Pesanan p : hasil) {
                p.setDaftarDetail(ambilDetail(p.getIdPesanan()));
            }
            return hasil;
        } catch (SQLException e) {
            throw gagal("memuat pesanan", e);
        }
    }

    private List<DetailPesanan> ambilDetail(int idPesanan) throws SQLException {
        List<DetailPesanan> detail = new ArrayList<>();
        try (PreparedStatement st = db().prepareStatement(
                "SELECT d.jumlah, d.catatan_item, m.*, k.nama_kategori FROM detail_pesanan d "
                + "JOIN menu m ON m.id_menu = d.id_menu "
                + "JOIN kategori k ON k.id_kategori = m.id_kategori "
                + "WHERE d.id_pesanan = ? ORDER BY d.id_detail")) {
            isi(st, idPesanan);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    detail.add(new DetailPesanan(MenuDAO.dariBaris(r), r.getInt("jumlah"), r.getString("catatan_item")));
                }
            }
        }
        return detail;
    }

    public Pesanan findById(int id) {
        List<Pesanan> hasil = ambil("WHERE p.id_pesanan = ?", "p.id_pesanan", id);
        return hasil.isEmpty() ? null : hasil.get(0);
    }

    /** Pesanan dengan id tertentu, terbaru dulu. */
    public List<Pesanan> findByIds(List<Integer> ids) {
        if (ids.isEmpty()) return new ArrayList<>();
        String tanda = String.join(",", Collections.nCopies(ids.size(), "?"));
        return ambil("WHERE p.id_pesanan IN (" + tanda + ")", "p.id_pesanan DESC", ids.toArray());
    }

    /** Pesanan yang masih berjalan (tampil di dashboard pegawai). */
    public List<Pesanan> findAktif() {
        return ambil("WHERE p.status IN ('masuk','diproses','disajikan')", "mj.nomor_meja, p.waktu_pesan");
    }

    public List<Pesanan> findByMeja(int idMeja) {
        return ambil("WHERE p.id_meja = ?", "p.id_pesanan DESC", idMeja);
    }

    // ---------- menulis ----------
    /**
     * Simpan pesanan baru (status Masuk) beserta detailnya dan tandai meja terisi.
     * Semua dalam satu transaksi. Mengembalikan id pesanan.
     */
    public int simpan(Meja meja, String nama, String catatan, Keranjang k) {
        return transaksi("menyimpan pesanan", c -> {
            int id;
            try (PreparedStatement st = c.prepareStatement(
                    "INSERT INTO pesanan (id_meja, nama_pelanggan, catatan, total_harga, status) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                isi(st, meja.getIdMeja(), kosongJadiNull(nama), kosongJadiNull(catatan), k.hitungTotal(),
                        StatusPesanan.MASUK.getKode());
                st.executeUpdate();
                try (ResultSet kunci = st.getGeneratedKeys()) {
                    kunci.next();
                    id = kunci.getInt(1);
                }
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO detail_pesanan (id_pesanan, id_menu, jumlah, subtotal, catatan_item) VALUES (?,?,?,?,?)")) {
                for (DetailPesanan d : k.getItems()) {
                    isi(ins, id, d.getMenu().getIdMenu(), d.getJumlah(), d.hitungSubtotal(),
                            kosongJadiNull(d.getCatatanItem()));
                    ins.executeUpdate();
                }
            }
            try (PreparedStatement st = c.prepareStatement("UPDATE meja SET status = 'terisi' WHERE id_meja = ?")) {
                isi(st, meja.getIdMeja());
                st.executeUpdate();
            }
            return id;
        });
    }

    private static String kosongJadiNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /** Ubah status; dariStatus mencegah dua pegawai menimpa satu sama lain. */
    public boolean updateStatus(int idPesanan, StatusPesanan status, StatusPesanan dariStatus) {
        String sql = "UPDATE pesanan SET status = ? WHERE id_pesanan = ?" + (dariStatus != null ? " AND status = ?" : "");
        try (PreparedStatement st = db().prepareStatement(sql)) {
            if (dariStatus != null) {
                isi(st, status.getKode(), idPesanan, dariStatus.getKode());
            } else {
                isi(st, status.getKode(), idPesanan);
            }
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw gagal("mengubah status pesanan", e);
        }
    }

    public boolean majukanStatus(Pesanan pesanan) {
        StatusPesanan next = pesanan.statusBerikutnya();
        return next != null && updateStatus(pesanan.getIdPesanan(), next, pesanan.getStatus());
    }

    /**
     * Tutup meja: pesanan Disajikan menjadi Selesai dan meja kembali Kosong.
     * Ditolak (false) bila masih ada pesanan yang belum disajikan.
     */
    public boolean tutupMeja(int idMeja) {
        return transaksi("menutup meja", c -> {
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT COUNT(*) FROM pesanan WHERE id_meja = ? AND status IN ('masuk','diproses')")) {
                isi(st, idMeja);
                try (ResultSet r = st.executeQuery()) {
                    if (r.next() && r.getInt(1) > 0) return false;
                }
            }
            try (PreparedStatement st = c.prepareStatement(
                    "UPDATE pesanan SET status = 'selesai' WHERE id_meja = ? AND status = 'disajikan'")) {
                isi(st, idMeja);
                st.executeUpdate();
            }
            try (PreparedStatement st = c.prepareStatement("UPDATE meja SET status = 'kosong' WHERE id_meja = ?")) {
                isi(st, idMeja);
                st.executeUpdate();
            }
            return true;
        });
    }

    // ---------- laporan ----------
    /** periode "harian" (31 hari terakhir) atau "bulanan" (12 bulan terakhir). */
    public List<BarisLaporan> laporanPenjualan(String periode) {
        boolean bulanan = "bulanan".equals(periode);
        String format = bulanan ? "%Y-%m" : "%Y-%m-%d";
        int limit = bulanan ? 12 : 31;
        String sql = "SELECT DATE_FORMAT(waktu_pesan, '" + format + "') AS periode, "
                + "COUNT(*) AS jumlah_pesanan, SUM(total_harga) AS total FROM pesanan "
                + "GROUP BY periode ORDER BY periode DESC LIMIT " + limit;
        List<BarisLaporan> hasil = new ArrayList<>();
        try (Statement st = db().createStatement(); ResultSet r = st.executeQuery(sql)) {
            while (r.next()) {
                hasil.add(new BarisLaporan(r.getString("periode"), r.getInt("jumlah_pesanan"), r.getDouble("total")));
            }
            return hasil;
        } catch (SQLException e) {
            throw gagal("membuat laporan", e);
        }
    }

    public List<MenuTerlaris> menuTerlaris(int limit) {
        String sql = "SELECT m.nama_menu, SUM(d.jumlah) AS terjual FROM detail_pesanan d "
                + "JOIN menu m ON m.id_menu = d.id_menu "
                + "GROUP BY m.id_menu, m.nama_menu ORDER BY terjual DESC LIMIT " + Math.max(1, limit);
        List<MenuTerlaris> hasil = new ArrayList<>();
        try (Statement st = db().createStatement(); ResultSet r = st.executeQuery(sql)) {
            while (r.next()) hasil.add(new MenuTerlaris(r.getString("nama_menu"), r.getInt("terjual")));
            return hasil;
        } catch (SQLException e) {
            throw gagal("memuat menu terlaris", e);
        }
    }
}
