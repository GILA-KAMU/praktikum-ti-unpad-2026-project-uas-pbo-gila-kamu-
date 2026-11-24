package nasidaunjeruk.view.pelanggan;

import com.github.sarxos.webcam.Webcam;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import nasidaunjeruk.util.QrCode;
import nasidaunjeruk.view.Ui;

/**
 * Menyalakan webcam dan terus membaca gambar sampai QR meja ditemukan.
 * Kamera dibaca di thread terpisah supaya jendela tidak macet.
 *
 * Tampilan dibuat dengan GUI Builder NetBeans: buka tab Design untuk mengubahnya (file DialogScanQr.form).
 */
class DialogScanQr extends javax.swing.JDialog {

    private volatile boolean berhenti = false;
    private volatile boolean daftarSiap = false;
    private volatile int indeksKamera = 0;
    private volatile String hasil;

    DialogScanQr(Window induk) {
        super(induk, ModalityType.DOCUMENT_MODAL);
        initComponents();
        cmbKamera.addActionListener(e -> {
            if (daftarSiap && cmbKamera.getSelectedIndex() >= 0) indeksKamera = cmbKamera.getSelectedIndex();
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                Thread t = new Thread(DialogScanQr.this::jalankan, "scan-qr");
                t.setDaemon(true);
                t.start();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                berhenti = true; // thread kamera berhenti dan kamera dimatikan
            }
        });
        setLocationRelativeTo(induk);
    }

    /** Tampilkan dialog dan tunggu. Mengembalikan isi QR meja, atau null bila batal. */
    String tampilkan() {
        setVisible(true);
        return hasil;
    }

    // ---------- berjalan di thread kamera ----------
    private void jalankan() {
        Webcam kamera = null;
        int dipakai = -1;
        try {
            List<Webcam> semua = Webcam.getWebcams();
            if (semua.isEmpty()) {
                gagal("Kamera tidak ditemukan.");
                return;
            }
            SwingUtilities.invokeLater(() -> {
                for (Webcam w : semua) cmbKamera.addItem(w.getName());
                cmbKamera.setSelectedIndex(0);
                daftarSiap = true;
            });

            while (!berhenti) {
                int mau = indeksKamera;
                if (mau != dipakai) {
                    if (kamera != null) kamera.close();
                    kamera = semua.get(mau);
                    aturUkuran(kamera);
                    tampilkanTeks("Menyalakan kamera...");
                    kamera.open();
                    dipakai = mau;
                }
                BufferedImage gambar = kamera.getImage();
                if (gambar == null) {
                    Thread.sleep(50);
                    continue;
                }
                String isi = QrCode.baca(gambar);
                BufferedImage pratinjau = pratinjau(gambar);
                SwingUtilities.invokeLater(() -> {
                    lblLayar.setText(null);
                    lblLayar.setIcon(new ImageIcon(pratinjau));
                });
                if (isi != null) {
                    if (QrCode.bacaIsiMeja(isi) != null) {
                        hasil = isi;
                        SwingUtilities.invokeLater(this::dispose);
                        return;
                    }
                    SwingUtilities.invokeLater(() ->
                            lblStatus.setText("QR terbaca, tapi bukan QR meja Nasi Daun Jeruk."));
                }
                Thread.sleep(60);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable e) { // termasuk error library native kamera
            gagal("Kamera tidak bisa dipakai: " + e.getMessage());
        } finally {
            if (kamera != null) kamera.close();
        }
    }

    /** Pakai 640x480 bila ada; selain itu ukuran terbesar yang tersedia. */
    private static void aturUkuran(Webcam kamera) {
        if (kamera.isOpen()) return;
        Dimension pilih = null;
        for (Dimension d : kamera.getViewSizes()) {
            if (d.width == 640 && d.height == 480) {
                pilih = d;
                break;
            }
            if (pilih == null || d.width * d.height > pilih.width * pilih.height) pilih = d;
        }
        if (pilih != null) kamera.setViewSize(pilih);
    }

    /** Gambar dicerminkan (seperti kaca) supaya mudah mengarahkan QR, plus kotak bantu di tengah. */
    private static BufferedImage pratinjau(BufferedImage asli) {
        int w = asli.getWidth(), h = asli.getHeight();
        BufferedImage hasil = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = hasil.createGraphics();
        g.drawImage(asli, w, 0, -w, h, null);
        int sisi = Math.min(w, h) * 3 / 5;
        g.setColor(new Color(0xDDDDDD));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect((w - sisi) / 2, (h - sisi) / 2, sisi, sisi, 16, 16);
        g.dispose();
        return hasil;
    }

    private void tampilkanTeks(String teks) {
        SwingUtilities.invokeLater(() -> {
            lblLayar.setIcon(null);
            lblLayar.setText(teks);
        });
    }

    private void gagal(String pesan) {
        SwingUtilities.invokeLater(() -> {
            lblLayar.setIcon(null);
            lblLayar.setText("<html><center>" + Ui.escape(pesan)
                    + "<br><br>Gunakan <b>Pilih gambar QR</b> atau ketik kode meja.</center></html>");
            lblStatus.setText("Kamera tidak tersedia.");
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblJudul = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        lblLayar = new javax.swing.JLabel();
        lblKamera = new javax.swing.JLabel();
        cmbKamera = new javax.swing.JComboBox<>();
        btnBatal = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Scan QR meja");

        lblJudul.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblJudul.setText("Scan QR code di meja Anda");

        lblStatus.setForeground(new java.awt.Color(102, 102, 102));
        lblStatus.setText("Arahkan QR code di meja ke kamera.");

        lblLayar.setBackground(new java.awt.Color(0, 0, 0));
        lblLayar.setForeground(new java.awt.Color(255, 255, 255));
        lblLayar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLayar.setOpaque(true);
        lblLayar.setText("Menyalakan kamera...");

        lblKamera.setForeground(new java.awt.Color(102, 102, 102));
        lblKamera.setText("Kamera");


        btnBatal.setText("Batal");
        btnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblJudul)
                    .addComponent(lblStatus)
                    .addComponent(lblLayar, javax.swing.GroupLayout.PREFERRED_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblKamera)
                        .addGap(8, 8, 8)
                        .addComponent(cmbKamera, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 16, Short.MAX_VALUE)
                        .addComponent(btnBatal)))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblJudul)
                .addGap(4, 4, 4)
                .addComponent(lblStatus)
                .addGap(10, 10, 10)
                .addComponent(lblLayar, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKamera)
                    .addComponent(cmbKamera, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBatal))
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        dispose();
    }//GEN-LAST:event_btnBatalActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblLayar;
    private javax.swing.JLabel lblKamera;
    private javax.swing.JComboBox<String> cmbKamera;
    private javax.swing.JButton btnBatal;
    // End of variables declaration//GEN-END:variables
}
