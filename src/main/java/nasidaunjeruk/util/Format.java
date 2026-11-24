package nasidaunjeruk.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Fungsi bantu format tampilan. */
public final class Format {

    private static final DateTimeFormatter WAKTU = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Format() {}

    /** 18000 menjadi "Rp 18.000". */
    public static String rupiah(double angka) {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        return "Rp " + new DecimalFormat("#,##0", s).format(Math.round(angka));
    }

    public static String waktu(LocalDateTime w) {
        return w == null ? "-" : w.format(WAKTU);
    }

    /** "Rp 20.000", "20000", atau "20.000" menjadi 20000. Kosong menjadi -1. */
    public static long angka(String teks) {
        String digit = teks == null ? "" : teks.replaceAll("[^0-9]", "");
        if (digit.isEmpty() || digit.length() > 15) return -1;
        return Long.parseLong(digit);
    }
}
