package nasidaunjeruk.model;

/** Fitur dashboard pegawai. Tiap peran menampilkan daftar fitur sendiri. */
public enum Fitur {
    PESANAN("Pesanan masuk"),
    KELOLA_MENU("Kelola menu"),
    KELOLA_MEJA("Kelola meja"),
    LAPORAN("Laporan penjualan"),
    PEGAWAI("Kelola pegawai");

    private final String label;

    Fitur(String label) { this.label = label; }

    public String getLabel() { return label; }
}
