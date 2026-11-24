package nasidaunjeruk.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pesanan {

    private final int idPesanan;
    private final Meja meja;
    private final String namaPelanggan;
    private final String catatan;
    private final LocalDateTime waktuPesan;
    private List<DetailPesanan> daftarDetail = new ArrayList<>();
    private StatusPesanan status;
    private final double totalHarga; // total yang tersimpan saat checkout

    public Pesanan(int idPesanan, Meja meja, String namaPelanggan, String catatan,
                   LocalDateTime waktuPesan, StatusPesanan status, double totalHarga) {
        this.idPesanan = idPesanan;
        this.meja = meja;
        this.namaPelanggan = namaPelanggan;
        this.catatan = catatan;
        this.waktuPesan = waktuPesan;
        this.status = status;
        this.totalHarga = totalHarga;
    }

    public double hitungTotal() {
        double total = 0;
        for (DetailPesanan d : daftarDetail) {
            total += d.hitungSubtotal();
        }
        return total;
    }

    /** Total saat checkout (tidak berubah walau harga menu diubah kemudian). */
    public double getTotalHarga() {
        return totalHarga > 0 ? totalHarga : hitungTotal();
    }

    public void ubahStatus(StatusPesanan statusBaru) {
        if (statusBaru == null) throw new IllegalArgumentException("Status tidak boleh kosong.");
        this.status = statusBaru;
    }

    public StatusPesanan statusBerikutnya() {
        return status.berikutnya();
    }

    /** Contoh: "2x Paket Ayam Goreng, 1x Sambal Tambahan (Merah)". */
    public String ringkasanItem() {
        List<String> bagian = new ArrayList<>();
        for (DetailPesanan d : daftarDetail) {
            bagian.add(d.toString());
        }
        return String.join(", ", bagian);
    }

    public int getNomorMeja() { return meja.getNomorMeja(); }
    public Meja getMeja() { return meja; }
    public int getIdPesanan() { return idPesanan; }
    public StatusPesanan getStatus() { return status; }
    public List<DetailPesanan> getDaftarDetail() { return daftarDetail; }
    public void setDaftarDetail(List<DetailPesanan> daftarDetail) { this.daftarDetail = daftarDetail; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public String getCatatan() { return catatan; }
    public LocalDateTime getWaktuPesan() { return waktuPesan; }
}
