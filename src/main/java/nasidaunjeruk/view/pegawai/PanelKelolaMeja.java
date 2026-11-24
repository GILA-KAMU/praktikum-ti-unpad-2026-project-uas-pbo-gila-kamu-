package nasidaunjeruk.view.pegawai;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import nasidaunjeruk.dao.MejaDAO;
import nasidaunjeruk.database.DataException;
import nasidaunjeruk.model.Admin;
import nasidaunjeruk.model.Meja;
import nasidaunjeruk.view.Segarkan;
import nasidaunjeruk.view.Ui;

/**
 * Daftar meja, kartu QR tiap meja (bisa disimpan untuk dicetak), dan pembuatan kode baru.
 *
 * Tampilan dibuat dengan GUI Builder NetBeans: buka tab Design untuk mengubahnya (file PanelKelolaMeja.form).
 */
class PanelKelolaMeja extends javax.swing.JPanel implements Segarkan {

    private static File folderTerakhir;

    private final Admin admin;
    private final DefaultTableModel model = Ui.modelTabel("Nomor meja", "Kode meja", "Status");
    private List<Meja> daftar = new ArrayList<>();

    PanelKelolaMeja(Admin admin) {
        this.admin = admin;
        initComponents();
        Ui.aturTabel(tblMeja, model);
        spnNomorBaru.setModel(new SpinnerNumberModel(1, 1, 999, 1));
        tblMeja.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) perbaruiPratinjau();
        });
        perbaruiPratinjau();
    }

    @Override
    public void segarkan() {
        Meja lama = terpilih();
        try {
            daftar = admin.kelolaMeja();
            spnNomorBaru.setValue(new MejaDAO().nomorBerikutnya());
        } catch (RuntimeException e) {
            Ui.tangani(this, e);
            return;
        }
        model.setRowCount(0);
        int pilih = daftar.isEmpty() ? -1 : 0;
        for (int i = 0; i < daftar.size(); i++) {
            Meja m = daftar.get(i);
            model.addRow(new Object[]{"Meja " + m.getNomorMeja(), m.getTokenQr(), m.isTerisi() ? "Terisi" : "Kosong"});
            if (lama != null && m.getIdMeja() == lama.getIdMeja()) pilih = i;
        }
        if (pilih >= 0) tblMeja.setRowSelectionInterval(pilih, pilih);
        perbaruiPratinjau();
    }

    private Meja terpilih() {
        int i = tblMeja.getSelectedRow();
        return (i >= 0 && i < daftar.size()) ? daftar.get(i) : null;
    }

    private void perbaruiPratinjau() {
        Meja m = terpilih();
        btnSimpanQr.setEnabled(m != null);
        btnKodeBaru.setEnabled(m != null);
        if (m == null) {
            lblPratinjau.setIcon(null);
            lblPratinjau.setText("Pilih meja untuk melihat QR-nya.");
            return;
        }
        Image kartu = admin.generateQR(m).getScaledInstance(300, 410, Image.SCALE_SMOOTH);
        lblPratinjau.setText(null);
        lblPratinjau.setIcon(new ImageIcon(kartu));
    }

    private void tambah() {
        int nomor = (Integer) spnNomorBaru.getValue();
        try {
            admin.tambahMeja(nomor);
            Ui.info(this, "Meja " + nomor + " ditambahkan. Jangan lupa simpan dan cetak kartu QR-nya.");
        } catch (DataException e) {
            Ui.error(this, e.isPelanggaranData() ? "Nomor meja itu sudah dipakai." : e.getMessage());
        } catch (RuntimeException e) {
            Ui.tangani(this, e);
        }
        segarkan();
    }

    private void kodeBaru() {
        Meja m = terpilih();
        if (m == null) return;
        if (!Ui.konfirmasi(this, "QR dan kode lama meja " + m.getNomorMeja() + " tidak akan berlaku lagi. Lanjutkan?")) {
            return;
        }
        try {
            admin.buatKodeBaru(m);
            Ui.info(this, "Kode baru dibuat. Simpan dan cetak ulang kartu QR meja " + m.getNomorMeja() + ".");
        } catch (RuntimeException e) {
            Ui.tangani(this, e);
        }
        segarkan();
    }

    private void simpanQr() {
        Meja m = terpilih();
        if (m == null) return;
        JFileChooser pilih = new JFileChooser(folderTerakhir);
        pilih.setDialogTitle("Simpan kartu QR meja " + m.getNomorMeja());
        pilih.setSelectedFile(new File(folderTerakhir, namaFile(m)));
        if (pilih.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = pilih.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) file = new File(file.getParentFile(), file.getName() + ".png");
        if (file.exists() && !Ui.konfirmasi(this, file.getName() + " sudah ada. Timpa?")) return;
        folderTerakhir = file.getParentFile();
        try {
            ImageIO.write(admin.generateQR(m), "png", file);
            Ui.info(this, "Kartu QR tersimpan:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            Ui.error(this, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private void simpanSemua() {
        if (daftar.isEmpty()) return;
        JFileChooser pilih = new JFileChooser(folderTerakhir);
        pilih.setDialogTitle("Pilih folder untuk menyimpan semua kartu QR");
        pilih.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (pilih.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File folder = pilih.getSelectedFile();
        folderTerakhir = folder;
        try {
            for (Meja m : daftar) {
                ImageIO.write(admin.generateQR(m), "png", new File(folder, namaFile(m)));
            }
            Ui.info(this, daftar.size() + " kartu QR tersimpan di:\n" + folder.getAbsolutePath());
        } catch (IOException e) {
            Ui.error(this, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private static String namaFile(Meja m) {
        return "qr-meja-" + m.getNomorMeja() + ".png";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblNomorBaru = new javax.swing.JLabel();
        spnNomorBaru = new javax.swing.JSpinner();
        btnTambahMeja = new javax.swing.JButton();
        btnSimpanSemua = new javax.swing.JButton();
        scrTabel = new javax.swing.JScrollPane();
        tblMeja = new javax.swing.JTable();
        lblPratinjau = new javax.swing.JLabel();
        btnSimpanQr = new javax.swing.JButton();
        btnKodeBaru = new javax.swing.JButton();


        lblNomorBaru.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNomorBaru.setText("Nomor meja baru");


        btnTambahMeja.setBackground(new java.awt.Color(100, 100, 100));
        btnTambahMeja.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambahMeja.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahMeja.setText("Tambah meja");
        btnTambahMeja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahMejaActionPerformed(evt);
            }
        });

        btnSimpanSemua.setText("Simpan semua kartu QR...");
        btnSimpanSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanSemuaActionPerformed(evt);
            }
        });


        scrTabel.setViewportView(tblMeja);

        lblPratinjau.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPratinjau.setText("Pilih meja untuk melihat QR-nya.");

        btnSimpanQr.setBackground(new java.awt.Color(100, 100, 100));
        btnSimpanQr.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpanQr.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpanQr.setText("Simpan kartu QR...");
        btnSimpanQr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanQrActionPerformed(evt);
            }
        });

        btnKodeBaru.setText("Buat kode baru");
        btnKodeBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKodeBaruActionPerformed(evt);
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
                        .addComponent(lblNomorBaru)
                        .addGap(8, 8, 8)
                        .addComponent(spnNomorBaru, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnTambahMeja)
                        .addGap(8, 8, 8)
                        .addComponent(btnSimpanSemua))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPratinjau, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSimpanQr)
                                .addGap(8, 8, 8)
                                .addComponent(btnKodeBaru)))))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNomorBaru)
                    .addComponent(spnNomorBaru, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTambahMeja)
                    .addComponent(btnSimpanSemua))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPratinjau, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSimpanQr)
                            .addComponent(btnKodeBaru))))
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahMejaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahMejaActionPerformed
        tambah();
    }//GEN-LAST:event_btnTambahMejaActionPerformed

    private void btnSimpanSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanSemuaActionPerformed
        simpanSemua();
    }//GEN-LAST:event_btnSimpanSemuaActionPerformed

    private void btnSimpanQrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanQrActionPerformed
        simpanQr();
    }//GEN-LAST:event_btnSimpanQrActionPerformed

    private void btnKodeBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKodeBaruActionPerformed
        kodeBaru();
    }//GEN-LAST:event_btnKodeBaruActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblNomorBaru;
    private javax.swing.JSpinner spnNomorBaru;
    private javax.swing.JButton btnTambahMeja;
    private javax.swing.JButton btnSimpanSemua;
    private javax.swing.JScrollPane scrTabel;
    private javax.swing.JTable tblMeja;
    private javax.swing.JLabel lblPratinjau;
    private javax.swing.JButton btnSimpanQr;
    private javax.swing.JButton btnKodeBaru;
    // End of variables declaration//GEN-END:variables
}
