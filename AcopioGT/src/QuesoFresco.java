import java.math.BigDecimal;
import java.time.LocalDate;

public final class QuesoFresco extends LotePerecedero implements Refrigerado {
    private final BigDecimal grasa;
    public QuesoFresco(String codigo, String productor, LocalDate ingreso, BigDecimal peso,
                       Calidad calidad, BigDecimal grasa) {
        super(codigo, productor, ingreso, peso, calidad, 21);
        this.grasa = Validacion.rango(grasa, "Grasa", 10, 40);
        if (calidad == Calidad.C) throw new IllegalArgumentException("No se acepta queso de calidad C.");
    }
    public BigDecimal grasa() { return grasa; }
    @Override public String categoria() { return "QUESO_FRESCO"; }
    @Override public Ambiente ambiente() { return Refrigerado.super.ambiente(); }
    @Override protected BigDecimal calcularPago() {
        BigDecimal tarifa = calidad() == Calidad.A ? new BigDecimal("38") : new BigDecimal("30");
        return pesoRecibido().multiply(tarifa.subtract(new BigDecimal("1.50")));
    }
}
