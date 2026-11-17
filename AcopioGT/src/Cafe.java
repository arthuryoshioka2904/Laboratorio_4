import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Cafe extends Lote {
    private final BigDecimal humedad;
    public Cafe(String codigo, String productor, LocalDate ingreso, BigDecimal peso,
                Calidad calidad, BigDecimal humedad) {
        super(codigo, productor, ingreso, peso, calidad);
        this.humedad = Validacion.rango(humedad, "Humedad", 8, 14);
    }
    public final BigDecimal humedad() { return humedad; }
    @Override public String categoria() { return "CAFE"; }
    @Override public Ambiente ambiente() { return Ambiente.SECO; }
    @Override protected BigDecimal calcularPago() {
        BigDecimal tarifa = switch (calidad()) {
            case A -> new BigDecimal("12"); case B -> new BigDecimal("9");
            case C -> new BigDecimal("6");
        };
        BigDecimal puntos = humedad.subtract(new BigDecimal("12")).max(BigDecimal.ZERO)
            .setScale(0, RoundingMode.CEILING);
        return pesoRecibido().multiply(tarifa)
            .multiply(BigDecimal.ONE.subtract(puntos.multiply(new BigDecimal("0.03"))));
    }
}
