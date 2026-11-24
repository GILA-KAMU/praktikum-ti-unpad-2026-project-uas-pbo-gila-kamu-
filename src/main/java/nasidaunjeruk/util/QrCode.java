package nasidaunjeruk.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Membuat dan membaca QR code (library ZXing). */
public final class QrCode {

    /** Isi QR meja yang sudah dipecah: nomor meja dan token. */
    public record KodeMeja(int nomor, String token) {}

    /** Cocok dengan "meja=5;token=abc" (versi desktop) maupun "?meja=5&token=abc" (QR versi web lama). */
    private static final Pattern POLA_MEJA = Pattern.compile("meja=(\\d{1,4})\\W+token=([0-9A-Za-z]{1,64})");

    private QrCode() {}

    // ---------- isi QR meja ----------
    public static String isiMeja(int nomor, String token) {
        return "NASIDAUNJERUK;meja=" + nomor + ";token=" + token;
    }

    /** Null bila teks bukan QR meja. */
    public static KodeMeja bacaIsiMeja(String isi) {
        if (isi == null) return null;
        Matcher m = POLA_MEJA.matcher(isi);
        return m.find() ? new KodeMeja(Integer.parseInt(m.group(1)), m.group(2)) : null;
    }

    // ---------- membuat & membaca ----------
    public static BufferedImage buat(String isi, int ukuran) {
        try {
            Map<EncodeHintType, Object> hint = Map.of(
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            return MatrixToImageWriter.toBufferedImage(
                    new QRCodeWriter().encode(isi, BarcodeFormat.QR_CODE, ukuran, ukuran, hint));
        } catch (WriterException e) {
            throw new IllegalArgumentException("QR code gagal dibuat: " + e.getMessage(), e);
        }
    }

    /** Teks di dalam QR pada gambar, atau null bila tidak ada QR yang terbaca. */
    public static String baca(BufferedImage gambar) {
        if (gambar == null) return null;
        LuminanceSource sumber = new BufferedImageLuminanceSource(gambar);
        try {
            return new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(sumber)),
                    Map.of(DecodeHintType.TRY_HARDER, Boolean.TRUE)).getText();
        } catch (ReaderException e) {
            return null;
        }
    }

    // ---------- kartu meja untuk dicetak ----------
    /** Kartu siap cetak: nama warung, QR meja, nomor meja, dan kode manual. */
    public static BufferedImage kartuMeja(int nomor, String token) {
        int lebar = 600, tinggi = 820, ukuranQr = 460;
        BufferedImage kartu = new BufferedImage(lebar, tinggi, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = kartu.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, lebar, tinggi);
        g.setColor(new Color(0x646464));
        g.fillRect(0, 0, lebar, 110);

        g.setColor(Color.WHITE);
        tengah(g, "Nasi Daun Jeruk", new Font(Font.SANS_SERIF, Font.BOLD, 40), lebar, 70);

        g.drawImage(buat(isiMeja(nomor, token), ukuranQr), (lebar - ukuranQr) / 2, 140, null);

        g.setColor(Color.BLACK);
        tengah(g, "MEJA " + nomor, new Font(Font.SANS_SERIF, Font.BOLD, 56), lebar, 680);
        g.setColor(new Color(0x444444));
        tengah(g, "Scan QR ini untuk memesan", new Font(Font.SANS_SERIF, Font.PLAIN, 24), lebar, 730);
        g.dispose();
        return kartu;
    }

    private static void tengah(Graphics2D g, String teks, Font font, int lebar, int y) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(teks, (lebar - fm.stringWidth(teks)) / 2, y);
    }
}
