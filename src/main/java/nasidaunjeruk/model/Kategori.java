package nasidaunjeruk.model;

public class Kategori {

    private final int idKategori;
    private final String namaKategori;

    public Kategori(int idKategori, String namaKategori) {
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
    }

    public int getIdKategori() { return idKategori; }
    public String getNamaKategori() { return namaKategori; }

    /** Dipakai JComboBox untuk menampilkan nama kategori. */
    @Override
    public String toString() { return namaKategori; }
}
