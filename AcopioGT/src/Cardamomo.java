import java.math.BigDecimal;
import java.time.LocalDate;

public final class Cardamomo extends LotePerecedero {
    private final BigDecimal granoVerde;
    public Cardamomo(String codigo, String productor, LocalDate ingreso, BigDecimal peso,
                     Calidad calidad, BigDecimal granoVerde) {
        super(codigo, productor, ingreso, peso, calidad, 180);
        this.granoVerde = Validacion.rango(granoVerde, "Grano verde", 0, 100);
    }
    public BigDecimal granoVerde() { return granoVerde; }
    @Override public String categoria() { return "CARDAMOMO"; }
    @Override public Ambiente ambiente() { return Ambiente.SECO; }
    @Override protected BigDecimal calcularPago() {
        BigDecimal tarifa = switch (calidad()) {
            case A -> new BigDecimal("60"); case B -> new BigDecimal("48");
            case C -> new BigDecimal("35");
        };
        return pesoRecibido().multiply(tarifa).multiply(
            granoVerde.compareTo(new BigDecimal("85")) >= 0 ? new BigDecimal("1.10") : BigDecimal.ONE);
    }
}
