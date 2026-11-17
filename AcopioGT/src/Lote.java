import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Optional;

/** Identidad y claves de orden inmutables. El servicio controla los cambios de peso. */
public abstract class Lote implements Comparable<Lote>, Almacenable {
    private final String codigo;
    private final String productor;
    private final LocalDate ingreso;
    private final BigDecimal pesoRecibido;
    private final Calidad calidad;
    private BigDecimal disponibles;
    private EstadoLote estado = EstadoLote.DISPONIBLE;
    protected Lote(String codigo, String productor, LocalDate ingreso,
                   BigDecimal peso, Calidad calidad) {
        this.codigo = Validacion.codigo(codigo);
        this.productor = Validacion.texto(productor, "Productor");
        this.ingreso = Validacion.requerido(ingreso, "Fecha de ingreso");
        this.pesoRecibido = Validacion.positivo(peso, "Peso");
        this.calidad = Validacion.requerido(calidad, "Calidad");
        this.disponibles = peso;
    }
    public final String codigo() { return codigo; }
    public final String productor() { return productor; }
    public final LocalDate ingreso() { return ingreso; }
    public final BigDecimal pesoRecibido() { return pesoRecibido; }
    public final Calidad calidad() { return calidad; }
    @Override public final BigDecimal disponibles() { return disponibles; }
    public final EstadoLote estado() { return estado; }
    public abstract String categoria();
    @Override public abstract Ambiente ambiente();
    protected abstract BigDecimal calcularPago();
    public final BigDecimal pago() { return Dinero.monto(calcularPago()); }
    public final BigDecimal costoPorKg() {
        return pago().divide(pesoRecibido, MathContext.DECIMAL128);
    }
    /** Prorratea sin redondear el costo unitario previamente. */
    public final BigDecimal costoDe(BigDecimal kg) {
        return pago().multiply(kg).divide(pesoRecibido, MathContext.DECIMAL128);
    }
    public Optional<LocalDate> vencimiento() { return Optional.empty(); }
    public final boolean vencido(LocalDate fecha) {
        return vencimiento().map(fecha::isAfter).orElse(false);
    }
    final void consumir(BigDecimal kg) {
        Validacion.positivo(kg, "Kilogramos");
        if (estado != EstadoLote.DISPONIBLE || kg.compareTo(disponibles) > 0)
            throw new IllegalArgumentException("Peso no disponible en " + codigo);
        disponibles = disponibles.subtract(kg);
        if (disponibles.signum() == 0) estado = EstadoLote.AGOTADO;
    }
    final BigDecimal descartar(LocalDate fecha) {
        if (estado != EstadoLote.DISPONIBLE || !vencido(fecha))
            throw new IllegalArgumentException("El lote no puede descartarse.");
        BigDecimal perdida = Dinero.monto(costoDe(disponibles));
        disponibles = BigDecimal.ZERO;
        estado = EstadoLote.DESCARTADO;
        return perdida;
    }
    @Override public final int compareTo(Lote otro) {
        Optional<LocalDate> a = vencimiento(), b = otro.vencimiento();
        int cmp = Boolean.compare(b.isPresent(), a.isPresent());
        if (cmp == 0) cmp = a.orElse(ingreso).compareTo(b.orElse(otro.ingreso));
        if (cmp == 0) cmp = calidad.compareTo(otro.calidad);
        if (cmp == 0) cmp = codigo.compareTo(otro.codigo);
        return cmp;
    }
    @Override public String toString() {
        return codigo + " | " + categoria() + " | " + calidad + " | "
            + disponibles.toPlainString() + " kg | ingreso " + ingreso + " | vence "
            + vencimiento().map(LocalDate::toString).orElse("no aplica") + " | " + estado;
    }
}
