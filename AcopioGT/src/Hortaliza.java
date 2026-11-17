import java.math.BigDecimal;
import java.time.LocalDate;

public final class Hortaliza extends LotePerecedero implements Refrigerado {
    public Hortaliza(String codigo, String productor, LocalDate ingreso, BigDecimal peso,
                     Calidad calidad, int vidaUtil) {
        super(codigo, productor, ingreso, peso, calidad, vidaUtil);
        if (vidaUtil < 3 || vidaUtil > 14)
            throw new IllegalArgumentException("Vida útil debe estar entre 3 y 14 días.");
    }
    @Override public String categoria() { return "HORTALIZA"; }
    @Override public Ambiente ambiente() { return Refrigerado.super.ambiente(); }
    @Override protected BigDecimal calcularPago() {
        BigDecimal tarifa = switch (calidad()) {
            case A -> new BigDecimal("4"); case B -> new BigDecimal("3");
            case C -> new BigDecimal("2");
        };
        return pesoRecibido().multiply(new BigDecimal("0.95")).multiply(tarifa);
    }
}
