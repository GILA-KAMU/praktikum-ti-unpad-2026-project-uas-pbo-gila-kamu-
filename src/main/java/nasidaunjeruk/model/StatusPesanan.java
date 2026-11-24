package nasidaunjeruk.model;

/** Status pesanan. Kode sama dengan nilai ENUM di tabel pesanan. */
public enum StatusPesanan {
    MASUK("masuk", "Masuk"),
    DIPROSES("diproses", "Diproses"),
    DISAJIKAN("disajikan", "Disajikan"),
    SELESAI("selesai", "Selesai");

    private final String kode;
    private final String label;

    StatusPesanan(String kode, String label) {
        this.kode = kode;
        this.label = label;
    }

    /** Alur status oleh pegawai: Masuk -> Diproses -> Disajikan. Null bila tidak bisa dimajukan. */
    public StatusPesanan berikutnya() {
        return switch (this) {
            case MASUK -> DIPROSES;
            case DIPROSES -> DISAJIKAN;
            default -> null;
        };
    }

    public static StatusPesanan dariKode(String kode) {
        for (StatusPesanan s : values()) {
            if (s.kode.equals(kode)) return s;
        }
        throw new IllegalArgumentException("Status tidak dikenal: " + kode);
    }

    public String getKode() { return kode; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
