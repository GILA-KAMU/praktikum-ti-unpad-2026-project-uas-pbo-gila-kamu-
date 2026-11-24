package nasidaunjeruk.view;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.HierarchyEvent;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import nasidaunjeruk.database.DataException;

/** Fungsi bantu tampilan yang dipakai di semua layar: tema, label, tabel, dan pesan. */
public final class Ui {

    public static final Color LATAR = new Color(0xE4E4E4); // abu-abu muda: latar halaman awal
    public static final Color ABU = new Color(0x666666);   // teks redup, mis. menu yang sedang habis
    public static final Color GARIS = new Color(0xDDDDDD); // garis pemisah antar kartu menu

    private Ui() {}

    public static void pasangTema() {
        // Tanpa library native FlatLaf: menghindari peringatan "restricted method" di JDK baru
        System.setProperty("flatlaf.useNativeLibrary", "false");
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#646464"));
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("TabbedPane.tabHeight", 36);
    }

    // ---------- label ----------
    public static JLabel subjudul(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 15f));
        return l;
    }

    public static JLabel tebal(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    /** Amankan teks sebelum dimasukkan ke label HTML. */
    public static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    public static <T extends JComponent> T padding(T c, int atas, int kiri, int bawah, int kanan) {
        c.setBorder(BorderFactory.createEmptyBorder(atas, kiri, bawah, kanan));
        return c;
    }

    // ---------- tabel ----------
    /** Model tabel yang tidak bisa diedit langsung. */
    public static DefaultTableModel modelTabel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    /** Pasang model ke tabel dari GUI Builder dan samakan gayanya: baris tinggi, pilih satu baris. */
    public static void aturTabel(JTable t, TableModel model) {
        t.setModel(model);
        t.setRowHeight(28);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
    }

    public static void lebarKolom(JTable t, int... lebar) {
        for (int i = 0; i < lebar.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    // ---------- pesan ----------
    public static void info(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Gagal", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean konfirmasi(Component induk, String pesan) {
        return JOptionPane.showConfirmDialog(induk, pesan, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Tampilkan pesan error yang ramah dari exception. */
    public static void tangani(Component induk, RuntimeException e) {
        if (!(e instanceof DataException)
                && !(e instanceof IllegalArgumentException)
                && !(e instanceof IllegalStateException)) {
            e.printStackTrace();
        }
        error(induk, e.getMessage() == null ? e.toString() : e.getMessage());
    }

    /**
     * Jalankan aksi berkala selama komponen tampil di layar (mis. muat ulang daftar pesanan).
     * Timer berhenti sendiri ketika jendela komponen ditutup.
     */
    public static Timer otomatis(JComponent pemilik, int milidetik, Runnable aksi) {
        Timer t = new Timer(milidetik, e -> {
            if (!pemilik.isShowing()) return;
            try {
                aksi.run();
            } catch (RuntimeException ex) {
                System.err.println("Muat ulang otomatis gagal: " + ex.getMessage());
            }
        });
        pemilik.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (pemilik.isDisplayable()) t.start(); else t.stop();
            }
        });
        if (pemilik.isDisplayable()) t.start();
        return t;
    }
}
