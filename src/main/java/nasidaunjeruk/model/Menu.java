package nasidaunjeruk.model;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private final int idMenu;
    private final int idKategori;
    private final String namaKategori;
    private final String namaMenu;
    private final String deskripsi;
    private final double harga;
    private final String gambar;
    private boolean tersedia;
    private final String varian;

    public Menu(int idMenu, int idKategori, String namaKategori, String namaMenu, String deskripsi,
                double harga, String gambar, boolean tersedia, String varian) {
        this.idMenu = idMenu;
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
        this.namaMenu = namaMenu;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.gambar = gambar;
        this.tersedia = tersedia;
        this.varian = varian;
    }

    public boolean isTersedia() { return tersedia; }
    public void setTersedia(boolean tersedia) { this.tersedia = tersedia; }
    public double getHarga() { return harga; }

    public int getIdMenu() { return idMenu; }
    public int getIdKategori() { return idKategori; }
    public String getNamaKategori() { return namaKategori; }
    public String getNamaMenu() { return namaMenu; }
    public String getDeskripsi() { return deskripsi; }
    public String getGambar() { return gambar; }

    /** Contoh: "Merah,Hijau" menjadi [Merah, Hijau]. */
    public List<String> getVarianList() {
        List<String> hasil = new ArrayList<>();
        if (varian == null) return hasil;
        for (String v : varian.split(",")) {
            if (!v.isBlank()) hasil.add(v.trim());
        }
        return hasil;
    }

    /** " Merah, ,Hijau " menjadi "Merah,Hijau"; kosong menjadi null. */
    public static String rapikanVarian(String teks) {
        if (teks == null) return null;
        List<String> bagian = new ArrayList<>();
        for (String v : teks.split(",")) {
            if (!v.isBlank()) bagian.add(v.trim());
        }
        return bagian.isEmpty() ? null : String.join(",", bagian);
    }
}
