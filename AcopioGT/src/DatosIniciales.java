import java.math.BigDecimal;
import java.time.LocalDate;

public final class DatosIniciales {
    private DatosIniciales() { }
    public static AcopioGT crear() {
        LocalDate f = LocalDate.of(2026, 3, 12);
        AcopioGT s = new AcopioGT(f);
        s.registrarBodega(new Bodega("SECA-1", new BigDecimal("2000"), false));
        s.registrarBodega(new Bodega("SECA-2", new BigDecimal("1500"), false));
        s.registrarBodega(new Bodega("FRIA-1", new BigDecimal("1000"), true));
        s.cargarInicial(new Cafe("CAF-A", "Ana", f.minusDays(5), new BigDecimal("500"), Calidad.A, new BigDecimal("13.4")), "SECA-1");
        s.cargarInicial(new Cafe("CAF-B", "Beto", f.minusDays(5), new BigDecimal("100"), Calidad.B, new BigDecimal("12")), "SECA-2");
        s.cargarInicial(new CafeOrganico("ORG-A", "Celia", f.minusDays(4), new BigDecimal("500"), Calidad.A, new BigDecimal("13.4"), "CertiGT", f.plusDays(300)), "SECA-1");
        s.cargarInicial(new CafeOrganico("ORG-C", "Diego", f.minusDays(3), new BigDecimal("50"), Calidad.C, new BigDecimal("8"), "EcoGT", f.plusDays(300)), "SECA-2");
        s.cargarInicial(new Cardamomo("CAR-B", "Elsa", f.minusDays(179), new BigDecimal("80"), Calidad.B, new BigDecimal("90")), "SECA-1");
        s.cargarInicial(new Cardamomo("CAR-C", "Felipe", f.minusDays(20), new BigDecimal("30"), Calidad.C, new BigDecimal("80")), "SECA-2");
        s.cargarInicial(new Hortaliza("HOR-A", "Gaby", f.minusDays(7), new BigDecimal("200"), Calidad.A, 7), "FRIA-1");
        s.cargarInicial(new Hortaliza("HOR-C", "Hugo", f, new BigDecimal("40"), Calidad.C, 3), "FRIA-1");
        s.cargarInicial(new QuesoFresco("QUE-A", "Inés", f.minusDays(19), new BigDecimal("40"), Calidad.A, new BigDecimal("25")), "FRIA-1");
        s.cargarInicial(new QuesoFresco("QUE-B", "José", f.minusDays(2), new BigDecimal("20"), Calidad.B, new BigDecimal("10")), "FRIA-1");
        return s;
    }
}
