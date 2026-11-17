import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/** Validaciones puras: no modifican existencias ni contabilidad. */
public final class Validacion {
    private Validacion() { }
    public static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty())
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        return valor.trim();
    }
    public static String codigo(String valor) {
        return texto(valor, "Código").toUpperCase(Locale.ROOT);
    }
    public static BigDecimal positivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0)
            throw new IllegalArgumentException(campo + " debe ser positivo.");
        return valor;
    }
    public static BigDecimal rango(BigDecimal valor, String campo, int min, int max) {
        if (valor == null || valor.compareTo(BigDecimal.valueOf(min)) < 0
                || valor.compareTo(BigDecimal.valueOf(max)) > 0)
            throw new IllegalArgumentException(campo + " debe estar entre " + min + " y " + max + ".");
        return valor;
    }
    public static <T> T requerido(T valor, String campo) {
        return Objects.requireNonNull(valor, campo + " es obligatorio.");
    }
}
