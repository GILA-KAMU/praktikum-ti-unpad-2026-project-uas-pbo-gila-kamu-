package nasidaunjeruk;

import javax.swing.SwingUtilities;
import nasidaunjeruk.database.DatabaseConnection;
import nasidaunjeruk.view.HalamanAwal;
import nasidaunjeruk.view.Ui;

/** Titik masuk aplikasi desktop Nasi Daun Jeruk. */
public class Main {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
        SwingUtilities.invokeLater(() -> {
            Ui.pasangTema();
            HalamanAwal awal = new HalamanAwal();
            awal.setVisible(true);
            awal.cekKoneksi();
        });
    }
}
