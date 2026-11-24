package nasidaunjeruk.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import nasidaunjeruk.util.QrCode;

public class Meja {

    private final int idMeja;
    private final int nomorMeja;
    private final String tokenQr;
    private String status;

    public Meja(int idMeja, int nomorMeja, String tokenQr, String status) {
        this.idMeja = idMeja;
        this.nomorMeja = nomorMeja;
        this.tokenQr = tokenQr;
        this.status = status == null ? "kosong" : status;
    }

    /** Bandingkan kode meja dengan aman (waktu perbandingan tidak bocorkan isi token). */
    public boolean validasiToken(String token) {
        if (token == null) return false;
        return MessageDigest.isEqual(
                tokenQr.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8));
    }

    /** Kode meja acak baru (12 karakter heksadesimal). */
    public static String buatToken() {
        byte[] b = new byte[6];
        new SecureRandom().nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    /** Isi QR code meja ini, mis. "NASIDAUNJERUK;meja=5;token=a1b2c3". */
    public String isiQr() {
        return QrCode.isiMeja(nomorMeja, tokenQr);
    }

    public void setTerisi() { status = "terisi"; }
    public void setKosong() { status = "kosong"; }

    public int getIdMeja() { return idMeja; }
    public int getNomorMeja() { return nomorMeja; }
    public String getTokenQr() { return tokenQr; }
    public String getStatus() { return status; }
    public boolean isTerisi() { return "terisi".equals(status); }
}
