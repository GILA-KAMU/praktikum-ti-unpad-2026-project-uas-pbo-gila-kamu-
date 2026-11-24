package nasidaunjeruk.view.pelanggan;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import nasidaunjeruk.dao.MejaDAO;
import nasidaunjeruk.dao.MenuDAO;
import nasidaunjeruk.dao.PesananDAO;
import nasidaunjeruk.model.DetailPesanan;
import nasidaunjeruk.model.Keranjang;
import nasidaunjeruk.model.Meja;
import nasidaunjeruk.model.Menu;
import nasidaunjeruk.util.Format;
import nasidaunjeruk.view.Segarkan;
import nasidaunjeruk.view.Ui;

/**
 * Jendela pelanggan untuk satu meja: menu, keranjang, dan status pesanan.
 *
 * Tampilan dibuat dengan GUI Builder NetBeans: buka tab Design untuk mengubahnya (file FramePelanggan.form).
 * Isi tiap tab ada di PanelMenu, PanelKeranjang, dan PanelStatus.
 */
public class FramePelanggan extends javax.swing.JFrame {

    private final Meja meja;
    private final Keranjang keranjang = new Keranjang();
    /** Pesanan yang dibuat dari jendela ini (seperti session di versi web). */
    private final List<Integer> pesananIds = new ArrayList<>();

    private final PanelMenu panelMenu;
    private final PanelKeranjang panelKeranjang;
    private final PanelStatus panelStatus;
    private final Timer hapusPesan = new Timer(3000, e -> this.lblPesan.setText(" "));

    public FramePelanggan(Meja meja) {
        this.meja = meja;
        initComponents();
        setTitle("Meja " + meja.getNomorMeja() + " · Nasi Daun Jeruk");
        lblMeja.setText("Meja " + meja.getNomorMeja());
        hapusPesan.setRepeats(false);

        panelMenu = new PanelMenu(this);
        panelKeranjang = new PanelKeranjang(this);
        panelStatus = new PanelStatus(this);
        tabUtama.addTab("Menu", panelMenu);
        tabUtama.addTab("Keranjang", panelKeranjang);
        tabUtama.addTab("Status pesanan", panelStatus);
        tabUtama.addChangeListener(e -> {
            if (tabUtama.getSelectedComponent() instanceof Segarkan s) s.segarkan();
        });

        keranjangBerubah();
        setSize(920, 680);
        setLocationByPlatform(true);
    }

    Meja getMeja() { return meja; }
    Keranjang getKeranjang() { return keranjang; }
    List<Integer> getPesananIds() { return pesananIds; }

    /** Pesan singkat di bagian atas yang hilang sendiri setelah 3 detik. */
    void kabari(String pesan) {
        lblPesan.setText(pesan);
        hapusPesan.restart();
    }

    /** Perbarui judul tab dan ringkasan setiap kali isi keranjang berubah. */
    void keranjangBerubah() {
        int n = keranjang.jumlahItem();
        tabUtama.setTitleAt(1, n == 0 ? "Keranjang" : "Keranjang (" + n + ")");
        lblRingkas.setText(n == 0 ? "Keranjang kosong"
                : n + " item · " + Format.rupiah(keranjang.hitungTotal()));
        panelKeranjang.segarkan();
    }

    void bukaTabMenu() { tabUtama.setSelectedComponent(panelMenu); }

    /** Simpan keranjang sebagai pesanan baru (langsung diteruskan ke dapur), lalu buka status pesanan. */
    void checkout(String nama, String catatan) {
        if (keranjang.isKosong()) {
            Ui.error(this, "Keranjang masih kosong.");
            return;
        }
        try {
            // Kode meja bisa diganti admin; pesanan dari kode lama ditolak
            if (new MejaDAO().findByNomorDanToken(meja.getNomorMeja(), meja.getTokenQr()) == null) {
                Ui.error(this, "Kode meja ini sudah tidak berlaku. Silakan masuk ulang dengan kode yang baru.");
                dispose();
                return;
            }
            List<String> dibuang = segarkanIsiKeranjang();
            if (!dibuang.isEmpty()) {
                keranjangBerubah();
                Ui.info(this, "Menu berikut sedang habis dan dikeluarkan dari keranjang:\n- " + String.join("\n- ", dibuang));
                if (keranjang.isKosong()) return;
            }
            String total = Format.rupiah(keranjang.hitungTotal());
            if (!Ui.konfirmasi(this, "Kirim pesanan dengan total " + total + " ke dapur?")) {
                return;
            }
            int id = new PesananDAO().simpan(meja, nama, catatan, keranjang);
            pesananIds.add(id);
            keranjang.kosongkan();
            panelKeranjang.bersihkanInfo();
            keranjangBerubah();
            Ui.info(this, "Pesanan #" + id + " diterima dan diteruskan ke dapur.\nTerima kasih!");
            panelStatus.segarkan();
            tabUtama.setSelectedComponent(panelStatus);
        } catch (RuntimeException e) {
            Ui.tangani(this, e);
        }
    }

    /**
     * Ambil ulang harga dan ketersediaan menu dari database sebelum checkout.
     * Mengembalikan nama menu yang sudah tidak tersedia.
     */
    private List<String> segarkanIsiKeranjang() {
        MenuDAO dao = new MenuDAO();
        List<DetailPesanan> lama = keranjang.getItems();
        List<String> dibuang = new ArrayList<>();
        keranjang.kosongkan();
        for (DetailPesanan d : lama) {
            Menu terbaru = dao.findById(d.getMenu().getIdMenu());
            if (terbaru == null || !terbaru.isTersedia()) {
                dibuang.add(d.getMenu().getNamaMenu());
            } else {
                keranjang.tambahItem(new DetailPesanan(terbaru, d.getJumlah(), d.getCatatanItem()));
            }
        }
        return dibuang;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlKepala = new javax.swing.JPanel();
        lblMeja = new javax.swing.JLabel();
        lblPesan = new javax.swing.JLabel();
        lblRingkas = new javax.swing.JLabel();
        tabUtama = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Meja · Nasi Daun Jeruk");

        pnlKepala.setBackground(new java.awt.Color(228, 228, 228));

        lblMeja.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblMeja.setText("Meja 0");

        lblPesan.setForeground(new java.awt.Color(102, 102, 102));
        lblPesan.setText(" ");

        lblRingkas.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblRingkas.setText("Keranjang kosong");

        javax.swing.GroupLayout pnlKepalaLayout = new javax.swing.GroupLayout(pnlKepala);
        pnlKepala.setLayout(pnlKepalaLayout);
        pnlKepalaLayout.setHorizontalGroup(
            pnlKepalaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKepalaLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlKepalaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMeja)
                    .addComponent(lblPesan))
                .addGap(0, 16, Short.MAX_VALUE)
                .addComponent(lblRingkas)
                .addGap(20, 20, 20))
        );
        pnlKepalaLayout.setVerticalGroup(
            pnlKepalaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlKepalaLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnlKepalaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlKepalaLayout.createSequentialGroup()
                        .addComponent(lblMeja)
                        .addGap(2, 2, 2)
                        .addComponent(lblPesan))
                    .addGroup(pnlKepalaLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(lblRingkas)))
                .addGap(10, 10, 10))
        );


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlKepala, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabUtama, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlKepala, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(tabUtama, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlKepala;
    private javax.swing.JLabel lblMeja;
    private javax.swing.JLabel lblPesan;
    private javax.swing.JLabel lblRingkas;
    private javax.swing.JTabbedPane tabUtama;
    // End of variables declaration//GEN-END:variables
}
