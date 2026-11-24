package nasidaunjeruk.view.pegawai;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import nasidaunjeruk.dao.MenuDAO;
import nasidaunjeruk.database.DataException;
import nasidaunjeruk.model.Admin;
import nasidaunjeruk.model.Kategori;
import nasidaunjeruk.model.Menu;
import nasidaunjeruk.util.Format;
import nasidaunjeruk.view.Segarkan;
import nasidaunjeruk.view.Ui;

/**
 * Tambah, ubah, nonaktifkan, dan hapus menu, serta tambah kategori.
 *
 * Tampilan dibuat dengan GUI Builder NetBeans: buka tab Design untuk mengubahnya (file PanelKelolaMenu.form).
 */
class PanelKelolaMenu extends javax.swing.JPanel implements Segarkan {

    private final Admin admin;
    private final MenuDAO dao = new MenuDAO();
    private final DefaultTableModel model = Ui.modelTabel("ID", "Nama menu", "Kategori", "Harga", "Varian", "Tersedia");
    private List<Menu> daftar = new ArrayList<>();
    private Integer idDiubah = null; // null = menu baru

    PanelKelolaMenu(Admin admin) {
        this.admin = admin;
        initComponents();
        Ui.aturTabel(tblMenu, model);
        Ui.lebarKolom(tblMenu, 40, 280, 120, 100, 140, 80);
        tblMenu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiForm();
        });
        txtVarian.putClientProperty("JTextField.placeholderText", "Pisahkan dengan koma, mis. Merah,Hijau");
        menuBaru();
    }

    @Override
    public void segarkan() {
        Integer pilih = idDiubah;
        try {
            daftar = admin.kelolaMenu();
            Object k = cmbKategori.getSelectedItem();
            cmbKategori.removeAllItems();
            for (Kategori kat : dao.findKategori()) cmbKategori.addItem(kat);
            if (k instanceof Kategori lama) pilihKategori(lama.getIdKategori());
        } catch (RuntimeException e) {
            Ui.tangani(this, e);
            return;
        }
        model.setRowCount(0);
        for (Menu m : daftar) {
            model.addRow(new Object[]{m.getIdMenu(), m.getNamaMenu(), m.getNamaKategori(), Format.rupiah(m.getHarga()),
                    String.join(", ", m.getVarianList()), m.isTersedia() ? "Ya" : "Tidak"});
        }
        tblMenu.clearSelection();
        for (int i = 0; pilih != null && i < daftar.size(); i++) {
            if (daftar.get(i).getIdMenu() == pilih) tblMenu.setRowSelectionInterval(i, i);
        }
        if (tblMenu.getSelectedRow() < 0) menuBaru();
    }

    private void pilihKategori(int id) {
        for (int i = 0; i < cmbKategori.getItemCount(); i++) {
            if (cmbKategori.getItemAt(i).getIdKategori() == id) cmbKategori.setSelectedIndex(i);
        }
    }

    private void isiForm() {
        int i = tblMenu.getSelectedRow();
        if (i < 0 || i >= daftar.size()) return;
        Menu m = daftar.get(i);
        idDiubah = m.getIdMenu();
        txtNama.setText(m.getNamaMenu());
        pilihKategori(m.getIdKategori());
        txtHarga.setText(String.valueOf((long) m.getHarga()));
        txtVarian.setText(String.join(",", m.getVarianList()));
        txtDeskripsi.setText(m.getDeskripsi() == null ? "" : m.getDeskripsi());
        chkTersedia.setSelected(m.isTersedia());
        btnSimpan.setText("Simpan perubahan");
        btnHapus.setEnabled(true);
    }

    private void menuBaru() {
        idDiubah = null;
        tblMenu.clearSelection();
        txtNama.setText("");
        txtHarga.setText("");
        txtVarian.setText("");
        txtDeskripsi.setText("");
        chkTersedia.setSelected(true);
        btnSimpan.setText("Tambah menu");
        btnHapus.setEnabled(false);
    }

    private void simpan() {
        String n = txtNama.getText().trim();
        long h = Format.angka(txtHarga.getText());
        if (n.isEmpty() || h <= 0) {
            Ui.error(this, "Nama dan harga wajib diisi dengan benar.");
            return;
        }
        if (!(cmbKategori.getSelectedItem() instanceof Kategori k)) {
            Ui.error(this, "Pilih kategori dulu.");
            return;
        }
        String d = txtDeskripsi.getText().trim();
        String v = Menu.rapikanVarian(txtVarian.getText());
        try {
            if (idDiubah == null) {
                idDiubah = dao.insert(k.getIdKategori(), n, d.isEmpty() ? null : d, h, chkTersedia.isSelected(), v);
                Ui.info(this, "Menu ditambahkan.");
            } else {
                dao.update(idDiubah, k.getIdKategori(), n, d.isEmpty() ? null : d, h, chkTersedia.isSelected(), v);
                Ui.info(this, "Menu diperbarui.");
            }
        } catch (DataException e) {
            Ui.error(this, "Data tidak tersimpan. Periksa kategori dan isian Anda.\n\n" + e.getMessage());
        }
        segarkan();
    }

    private void hapus() {
        if (idDiubah == null || !Ui.konfirmasi(this, "Hapus menu \"" + txtNama.getText() + "\"?")) return;
        try {
            dao.delete(idDiubah);
            idDiubah = null;
            Ui.info(this, "Menu dihapus.");
        } catch (DataException e) {
            if (e.isPelanggaranData()) {
                Ui.error(this, "Menu sudah pernah dipesan, jadi tidak bisa dihapus.\nHapus centang \"Tersedia\" saja.");
            } else {
                Ui.tangani(this, e);
            }
        }
        segarkan();
    }

    private void tambahKategori() {
        String n = JOptionPane.showInputDialog(this, "Nama kategori baru:", "Tambah kategori", JOptionPane.PLAIN_MESSAGE);
        if (n == null) return;
        n = n.trim();
        if (n.isEmpty() || n.length() > 50) {
            Ui.error(this, "Nama kategori wajib diisi (maksimal 50 karakter).");
            return;
        }
        try {
            int id = dao.insertKategori(n);
            segarkan();
            pilihKategori(id);
        } catch (RuntimeException e) {
            Ui.tangani(this, e);
        }
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
        tblMenu = new javax.swing.JTable();
        lblNama = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        lblKategori = new javax.swing.JLabel();
        cmbKategori = new javax.swing.JComboBox<>();
        btnTambahKategori = new javax.swing.JButton();
        lblHarga = new javax.swing.JLabel();
        txtHarga = new javax.swing.JTextField();
        lblVarian = new javax.swing.JLabel();
        txtVarian = new javax.swing.JTextField();
        lblDeskripsi = new javax.swing.JLabel();
        txtDeskripsi = new javax.swing.JTextField();
        chkTersedia = new javax.swing.JCheckBox();
        btnSimpan = new javax.swing.JButton();
        btnBaru = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();



        scrTabel.setViewportView(tblMenu);

        lblNama.setText("Nama menu");


        lblKategori.setText("Kategori");


        btnTambahKategori.setText("Tambah kategori...");
        btnTambahKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahKategoriActionPerformed(evt);
            }
        });

        lblHarga.setText("Harga (Rp)");


        lblVarian.setText("Varian");


        lblDeskripsi.setText("Deskripsi");


        chkTersedia.setSelected(true);
        chkTersedia.setText("Tersedia");

        btnSimpan.setBackground(new java.awt.Color(100, 100, 100));
        btnSimpan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSimpan.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpan.setText("Tambah menu");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        btnBaru.setText("Menu baru");
        btnBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBaruActionPerformed(evt);
            }
        });

        btnHapus.setForeground(new java.awt.Color(179, 38, 30));
        btnHapus.setText("Hapus menu");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNama)
                            .addComponent(lblKategori)
                            .addComponent(lblHarga)
                            .addComponent(lblVarian)
                            .addComponent(lblDeskripsi))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNama, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(btnTambahKategori))
                            .addComponent(txtHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtVarian, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .addComponent(txtDeskripsi, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .addComponent(chkTersedia)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSimpan)
                        .addGap(8, 8, 8)
                        .addComponent(btnBaru)
                        .addGap(8, 8, 8)
                        .addComponent(btnHapus)))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(scrTabel, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNama)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKategori)
                    .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTambahKategori))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHarga)
                    .addComponent(txtHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVarian)
                    .addComponent(txtVarian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDeskripsi)
                    .addComponent(txtDeskripsi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(chkTersedia)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan)
                    .addComponent(btnBaru)
                    .addComponent(btnHapus))
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahKategoriActionPerformed
        tambahKategori();
    }//GEN-LAST:event_btnTambahKategoriActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        simpan();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBaruActionPerformed
        menuBaru();
    }//GEN-LAST:event_btnBaruActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        hapus();
    }//GEN-LAST:event_btnHapusActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrTabel;
    private javax.swing.JTable tblMenu;
    private javax.swing.JLabel lblNama;
    private javax.swing.JTextField txtNama;
    private javax.swing.JLabel lblKategori;
    private javax.swing.JComboBox<nasidaunjeruk.model.Kategori> cmbKategori;
    private javax.swing.JButton btnTambahKategori;
    private javax.swing.JLabel lblHarga;
    private javax.swing.JTextField txtHarga;
    private javax.swing.JLabel lblVarian;
    private javax.swing.JTextField txtVarian;
    private javax.swing.JLabel lblDeskripsi;
    private javax.swing.JTextField txtDeskripsi;
    private javax.swing.JCheckBox chkTersedia;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnBaru;
    private javax.swing.JButton btnHapus;
    // End of variables declaration//GEN-END:variables
}
