package nasidaunjeruk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Penampung sementara item sebelum checkout. */
public class Keranjang {

    public static final int MAKS_PER_ITEM = 20;

    private final Map<String, DetailPesanan> items = new LinkedHashMap<>();

    public void tambahItem(DetailPesanan detail) {
        DetailPesanan lama = items.get(detail.getKey());
        if (lama != null) {
            lama.setJumlah(Math.min(lama.getJumlah() + detail.getJumlah(), MAKS_PER_ITEM));
        } else {
            detail.setJumlah(Math.min(detail.getJumlah(), MAKS_PER_ITEM));
            items.put(detail.getKey(), detail);
        }
    }

    /** Jumlah 0 atau kurang berarti item dihapus. */
    public void ubahJumlah(String key, int jumlah) {
        DetailPesanan d = items.get(key);
        if (d == null) return;
        if (jumlah <= 0) {
            hapusItem(key);
            return;
        }
        d.setJumlah(Math.min(jumlah, MAKS_PER_ITEM));
    }

    public void hapusItem(String key) { items.remove(key); }
    public void kosongkan() { items.clear(); }

    public double hitungTotal() {
        double total = 0;
        for (DetailPesanan d : items.values()) {
            total += d.hitungSubtotal();
        }
        return total;
    }

    /** Salinan daftar item (urut sesuai waktu ditambahkan). */
    public List<DetailPesanan> getItems() { return new ArrayList<>(items.values()); }
    public boolean isKosong() { return items.isEmpty(); }

    public int jumlahItem() {
        int n = 0;
        for (DetailPesanan d : items.values()) {
            n += d.getJumlah();
        }
        return n;
    }
}
