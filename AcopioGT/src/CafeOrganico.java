import java.math.BigDecimal;
import java.time.LocalDate;

public final class CafeOrganico extends Cafe {
    private final String certificadora;
    private final LocalDate selloValidoHasta;
    public CafeOrganico(String codigo, String productor, LocalDate ingreso, BigDecimal peso,
                        Calidad calidad, BigDecimal humedad, String certificadora,
                        LocalDate selloValidoHasta) {
        super(codigo, productor, ingreso, peso, calidad, humedad);
        this.certificadora = Validacion.texto(certificadora, "Certificadora");
        this.selloValidoHasta = Validacion.requerido(selloValidoHasta, "Vigencia del sello");
        if (selloValidoHasta.isBefore(ingreso))
            throw new IllegalArgumentException("Sello vencido en la fecha de ingreso.");
    }
    public String certificadora() { return certificadora; }
    public LocalDate selloValidoHasta() { return selloValidoHasta; }
    @Override public String categoria() { return "CAFE_ORGANICO"; }
    @Override protected BigDecimal calcularPago() {
        return super.calcularPago().add(pesoRecibido().multiply(new BigDecimal("0.75")));
    }
}
