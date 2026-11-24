package nasidaunjeruk.model;

/** Satu baris laporan penjualan: tanggal/bulan, jumlah pesanan, dan pendapatannya. */
public record BarisLaporan(String periode, int jumlahPesanan, double total) {}
