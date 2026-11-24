package nasidaunjeruk.model;

import java.util.Locale;

public class DetailPesanan {

    private final Menu menu;
    private int jumlah;
    private final String catatanItem;

    public DetailPesanan(Menu menu, int jumlah, String catatanItem) {
        this.menu = menu;
        this.jumlah = Math.max(1, jumlah);
        this.catatanItem = catatanItem == null ? "" : catatanItem;
    }

    public double hitungSubtotal() {
        return menu.getHarga() * jumlah;
    }

    /** Kunci unik baris: menu yang sama dengan catatan/varian sama digabung. */
    public String getKey() {
        return menu.getIdMenu() + "|" + catatanItem.toLowerCase(Locale.ROOT);
    }

    public Menu getMenu() { return menu; }
    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { this.jumlah = Math.max(1, jumlah); }
    public String getCatatanItem() { return catatanItem; }

    /** Contoh: "2x Telur Tambahan (Dadar)". */
    @Override
    public String toString() {
        return jumlah + "x " + menu.getNamaMenu() + (catatanItem.isEmpty() ? "" : " (" + catatanItem + ")");
    }
}
