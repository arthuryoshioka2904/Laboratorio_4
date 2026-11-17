import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Dinero {
    private Dinero() { }
    public static BigDecimal monto(BigDecimal valor) { return valor.setScale(2, RoundingMode.HALF_UP); }
    public static String quetzales(BigDecimal valor) { return "Q" + monto(valor).toPlainString(); }
}
