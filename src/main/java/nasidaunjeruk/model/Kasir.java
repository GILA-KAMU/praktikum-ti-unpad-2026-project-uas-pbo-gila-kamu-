package nasidaunjeruk.model;

import java.util.List;
import nasidaunjeruk.dao.PesananDAO;

public class Kasir extends Pengguna implements PengelolaPesanan {

    public Kasir(int idPengguna, String nama, String username, String password) {
        super(idPengguna, nama, username, password);
    }

    @Override
    public String getRole() { return "kasir"; }

    @Override
    public List<Pesanan> lihatPesananMasuk() {
        return new PesananDAO().findAktif();
    }

    @Override
    public boolean ubahStatusPesanan(Pesanan pesanan) {
        return new PesananDAO().majukanStatus(pesanan);
    }

    /** Selesaikan semua pesanan disajikan di meja itu dan kosongkan meja. */
    public boolean tutupMeja(int idMeja) {
        return new PesananDAO().tutupMeja(idMeja);
    }

    @Override
    public List<Fitur> tampilkanDashboard() {
        return List.of(Fitur.PESANAN);
    }
}
