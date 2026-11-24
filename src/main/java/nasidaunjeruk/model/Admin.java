package nasidaunjeruk.model;

import java.awt.image.BufferedImage;
import java.util.List;
import nasidaunjeruk.dao.MejaDAO;
import nasidaunjeruk.dao.MenuDAO;
import nasidaunjeruk.dao.PenggunaDAO;
import nasidaunjeruk.dao.PesananDAO;
import nasidaunjeruk.util.QrCode;

public class Admin extends Pengguna implements PengelolaPesanan {

    public Admin(int idPengguna, String nama, String username, String password) {
        super(idPengguna, nama, username, password);
    }

    @Override
    public String getRole() { return "admin"; }

    public List<Menu> kelolaMenu() {
        return new MenuDAO().findAll();
    }

    public List<Meja> kelolaMeja() {
        return new MejaDAO().findAll();
    }

    /** Tambah meja dengan kode acak. Mengembalikan id meja baru. */
    public int tambahMeja(int nomor) {
        if (nomor <= 0) throw new IllegalArgumentException("Nomor meja tidak valid.");
        return new MejaDAO().insert(nomor, Meja.buatToken());
    }

    /** Kartu QR meja siap cetak (berisi nomor meja dan token). */
    public BufferedImage generateQR(Meja meja) {
        return QrCode.kartuMeja(meja.getNomorMeja(), meja.getTokenQr());
    }

    /** Kode baru membuat kode lama (QR lama) tidak berlaku lagi. */
    public void buatKodeBaru(Meja meja) {
        new MejaDAO().updateToken(meja.getIdMeja(), Meja.buatToken());
    }

    @Override
    public List<Pesanan> lihatPesananMasuk() {
        return new PesananDAO().findAktif();
    }

    @Override
    public boolean ubahStatusPesanan(Pesanan pesanan) {
        return new PesananDAO().majukanStatus(pesanan);
    }

    /** periode: "harian" atau "bulanan". */
    public List<BarisLaporan> lihatLaporan(String periode) {
        return new PesananDAO().laporanPenjualan(periode);
    }

    public int tambahKasir(String nama, String username, String password) {
        if (nama == null || nama.isBlank() || username == null || username.isBlank()) {
            throw new IllegalArgumentException("Nama dan username wajib diisi.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter.");
        }
        return new PenggunaDAO().insert(nama.trim(), username.trim(), hashPassword(password), "kasir");
    }

    @Override
    public List<Fitur> tampilkanDashboard() {
        return List.of(Fitur.PESANAN, Fitur.KELOLA_MENU, Fitur.KELOLA_MEJA, Fitur.LAPORAN, Fitur.PEGAWAI);
    }
}
