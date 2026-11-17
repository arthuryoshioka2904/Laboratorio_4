import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

/** Colección encapsulada; cada recorrido respeta el orden natural de Lote. */
public class Bodega implements Iterable<Lote> {
    private final String codigo;
    private final BigDecimal capacidad;
    private final Ambiente ambiente;
    private final NavigableSet<Lote> lotes = new TreeSet<>();
    public Bodega(String codigo, BigDecimal capacidad, boolean camaraFria) {
        this(codigo, capacidad, camaraFria ? Ambiente.FRIO : Ambiente.SECO);
    }
    public Bodega(String codigo, BigDecimal capacidad, Ambiente ambiente) {
        this.codigo = Validacion.codigo(codigo);
        this.capacidad = Validacion.positivo(capacidad, "Capacidad");
        this.ambiente = Validacion.requerido(ambiente, "Ambiente");
    }
    public final String codigo() { return codigo; }
    public final BigDecimal capacidad() { return capacidad; }
    public final Ambiente ambiente() { return ambiente; }
    public final BigDecimal ocupacion() {
        BigDecimal total = BigDecimal.ZERO;
        for (Lote lote : this) total = total.add(lote.disponibles());
        return total;
    }
    public boolean compatible(Almacenable lote) { return ambiente.equals(lote.ambiente()); }
    public final void validarIngreso(Lote lote) {
        if (lote.estado() != EstadoLote.DISPONIBLE || lotes.contains(lote))
            throw new IllegalArgumentException("Lote no disponible o ya almacenado.");
        if (!compatible(lote)) throw new IllegalArgumentException("Bodega incompatible con " + lote.ambiente());
        if (ocupacion().add(lote.pesoRecibido()).compareTo(capacidad) > 0)
            throw new IllegalArgumentException("Capacidad insuficiente en " + codigo);
    }
    final void almacenar(Lote lote) { validarIngreso(lote); lotes.add(lote); }
    @Override public final Iterator<Lote> iterator() {
        Iterator<Lote> interno = lotes.iterator();
        return new Iterator<>() {
            private Lote actual;
            @Override public boolean hasNext() { return interno.hasNext(); }
            @Override public Lote next() { actual = interno.next(); return actual; }
            @Override public void remove() {
                if (actual == null) throw new IllegalStateException("Primero llame next().");
                if (actual.estado() == EstadoLote.DISPONIBLE)
                    throw new IllegalStateException("No se retiran lotes disponibles.");
                interno.remove(); actual = null;
            }
        };
    }
    @Override public String toString() {
        return codigo + " | " + ambiente.nombre() + " | " + ocupacion().toPlainString()
            + "/" + capacidad.toPlainString() + " kg";
    }
}
