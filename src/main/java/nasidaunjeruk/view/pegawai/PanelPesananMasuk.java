package nasidaunjeruk.view.pegawai;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import nasidaunjeruk.model.DetailPesanan;
import nasidaunjeruk.model.Kasir;
import nasidaunjeruk.model.PengelolaPesanan;
import nasidaunjeruk.model.Pesanan;
import nasidaunjeruk.model.StatusPesanan;
import nasidaunjeruk.util.Format;
import nasidaunjeruk.view.Segarkan;
import nasidaunjeruk.view.Ui;

/**
 * Pesanan yang sedang berjalan: Masuk -> Diproses -> Disajikan. Kasir juga bisa menutup meja.
 *
 * Tampilan dibuat dengan GUI Builder NetBeans: buka tab Design untuk mengubahnya (file PanelPesananMasuk.form).
 */
class PanelPesananMasuk extends javax.swing.JPanel implements Segarkan {

    private final PengelolaPesanan pegawai;
    private final DefaultTableModel model = Ui.modelTabel("Meja", "No.", "Waktu", "Pelanggan", "Total", "Status");
    private List<Pesanan> daftar = new ArrayList<>();

    PanelPesananMasuk(PengelolaPesanan pegawai) {
        this.pegawai = pegawai;
        initComponents();
        Ui.aturTabel(tblPesanan, model);
        Ui.lebarKolom(tblPesanan, 60, 60, 140, 200, 110, 120);
        tblPesanan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) perbaruiDetail();
        });
        btnTutupMeja.setVisible(pegawai instanceof Kasir);
        Ui.otomatis(this, 10_000, this::segarkan);
        perbaruiDetail();
    }

    @Override
    public void segarkan() {
        Pesanan lama = terpilih();
        try {
            daftar = pegawai.lihatPesananMasuk();
        } catch (RuntimeException e) {
            txtDetail.setText("Daftar pesanan gagal dimuat:\n" + e.getMessage());
            return;
        }
        model.setRowCount(0);
        int pilih = -1;
        for (int i = 0; i < daftar.size(); i++) {
            Pesanan p = daftar.get(i);
            model.addRow(new Object[]{p.getNomorMeja(), "#" + p.getIdPesanan(), Format.waktu(p.getWaktuPesan()),
                    p.getNamaPelanggan() == null ? "-" : p.getNamaPelanggan(),
                    Format.rupiah(p.getTotalHarga()), p.getStatus().getLabel()});
            if (lama != null && p.getIdPesanan() == lama.getIdPesanan()) pilih = i;
        }
        if (pilih >= 0) tblPesanan.setRowSelectionInterval(pilih, pilih);
        perbaruiDetail();
    }

    private Pesanan terpilih() {
        int i = tblPesanan.getSelectedRow();
        return (i >= 0 && i < daftar.size()) ? daftar.get(i) : null;
    }

    private void perbaruiDetail() {
        Pesanan p = terpilih();
        btnTutupMeja.setEnabled(p != null);
        if (p == null) {
            btnMajukan.setEnabled(false);
            btnMajukan.setText("Mulai proses");
            btnTutupMeja.setText("Tutup meja");
            txtDetail.setText(daftar.isEmpty() ? "Belum ada pesanan yang berjalan." : "Pilih pesanan untuk melihat detailnya.");
            return;
        }
        StatusPesanan next = p.statusBerikutnya();
        btnMajukan.setEnabled(next != null);
        btnMajukan.setText(next == StatusPesanan.DISAJIKAN ? "Tandai disajikan" : "Mulai proses");
        btnTutupMeja.setText("Tutup meja " + p.getNomorMeja());

        StringBuilder sb = new StringBuilder();
        sb.append("Meja ").append(p.getNomorMeja()).append(" · Pesanan #").append(p.getIdPesanan()).append('\n');
        sb.append("Status: ").append(p.getStatus().getLabel()).append("\n\n");
        for (DetailPesanan d : p.getDaftarDetail()) {
            sb.append("• ").append(d).append('\n');
        }
        if (p.getNamaPelanggan() != null) sb.append("\nAtas nama: ").append(p.getNamaPelanggan());
        if (p.getCatatan() != null) sb.append("\nCatatan: ").append(p.getCatatan());
        sb.append("\n\nTotal: ").append(Format.rupiah(p.getTotalHarga()));
        txtDetail.setText(sb.toString());
        txtDetail.setCaretPosition(0);
    }

    private void majukan() {
        Pesanan p = terpilih();
        if (p == null) return;
        try {
            if (!pegawai.ubahStatusPesanan(p)) {
                Ui.error(this, "Status tidak bisa diubah. Mungkin sudah diubah pegawai lain.");
            }
        } catch (RuntimeException e) {
            Ui.tangani(this, e);
        }
        segarkan();
    }

    private void tutupMeja() {
        Pesanan p = terpilih();
        if (p == null || !(pegawai instanceof Kasir kasir)) return;
        if (!Ui.konfirmasi(this, "Tutup meja " + p.getNomorMeja() + "? Pesanan yang sudah disajikan akan ditandai selesai.")) {
            return;
        }
        try {
            if (kasir.tutupMeja(p.getMeja().getIdMeja())) {
                Ui.info(this, "Meja " + p.getNomorMeja() + " ditutup dan kembali kosong.");
            } else {
                Ui.error(this, "Meja belum bisa ditutup: masih ada pesanan yang belum disajikan.");
            }
        } catch (RuntimeException e) {
            Ui.tangani(this, e);
        }
        segarkan();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrTabel = new javax.swing.JScrollPane();
        tblPesanan = new javax.swing.JTable();
        scrDetail = new javax.swing.JScrollPane();
        txtDetail = new javax.swing.JTextArea();
        btnMajukan = new javax.swing.JButton();
        btnTutupMeja = new javax.swing.JButton();



        scrTabel.setViewportView(tblPesanan);


        txtDetail.setEditable(false);
        txtDetail.setColumns(20);
        txtDetail.setLineWrap(true);
        txtDetail.setRows(5);
        txtDetail.setWrapStyleWord(true);
        scrDetail.setViewportView(txtDetail);

        btnMajukan.setBackground(new java.awt.Color(100, 100, 100));
        btnMajukan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMajukan.setForeground(new java.awt.Color(255, 255, 255));
        btnMajukan.setText("Mulai proses");
        btnMajukan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMajukanActionPerformed(evt);
            }
        });

        btnTutupMeja.setText("Tutup meja");
        btnTutupMeja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTutupMejaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                        .addGap(12, 12, 12)
                        .addComponent(scrDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnMajukan)
                        .addGap(8, 8, 8)
                        .addComponent(btnTutupMeja)))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                    .addComponent(scrDetail, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMajukan)
                    .addComponent(btnTutupMeja))
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnMajukanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMajukanActionPerformed
        majukan();
    }//GEN-LAST:event_btnMajukanActionPerformed

    private void btnTutupMejaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTutupMejaActionPerformed
        tutupMeja();
    }//GEN-LAST:event_btnTutupMejaActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrTabel;
    private javax.swing.JTable tblPesanan;
    private javax.swing.JScrollPane scrDetail;
    private javax.swing.JTextArea txtDetail;
    private javax.swing.JButton btnMajukan;
    private javax.swing.JButton btnTutupMeja;
    // End of variables declaration//GEN-END:variables
}
