package nasidaunjeruk.model;

import java.util.List;

/** Interface untuk pegawai yang boleh memproses pesanan (Admin dan Kasir). */
public interface PengelolaPesanan {

    /** Pesanan yang masih berjalan (Masuk, Diproses, Disajikan). */
    List<Pesanan> lihatPesananMasuk();

    /** Masuk -> Diproses -> Disajikan. False bila status sudah diubah pegawai lain. */
    boolean ubahStatusPesanan(Pesanan pesanan);
}
