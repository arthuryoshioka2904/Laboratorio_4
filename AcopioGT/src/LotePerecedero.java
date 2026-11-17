import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public abstract class LotePerecedero extends Lote implements Perecedero {
    private final int vidaUtil;
    protected LotePerecedero(String codigo, String productor, LocalDate ingreso,
                            BigDecimal peso, Calidad calidad, int vidaUtil) {
        super(codigo, productor, ingreso, peso, calidad);
        if (vidaUtil <= 0) throw new IllegalArgumentException("Vida útil debe ser positiva.");
        ingreso.plusDays(vidaUtil); // Comprueba desbordamiento antes de aceptar el objeto.
        this.vidaUtil = vidaUtil;
    }
    public final int vidaUtil() { return vidaUtil; }
    @Override public final LocalDate fechaVencimiento() { return ingreso().plusDays(vidaUtil); }
    @Override public final Optional<LocalDate> vencimiento() { return Optional.of(fechaVencimiento()); }
}
