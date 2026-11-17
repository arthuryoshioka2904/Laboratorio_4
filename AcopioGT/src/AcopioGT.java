import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Servicio de aplicación: valida y planifica antes de confirmar cambios. */
public final class AcopioGT {
    public record Preliquidacion(String codigo, String categoria, BigDecimal pago,
                                 Optional<LocalDate> vencimiento) { }
    public record Linea(String lote, String bodega, BigDecimal kg, BigDecimal costo) { }
    public record Despacho(String comprador, String categoria, LocalDate fecha,
                           List<Linea> lineas, BigDecimal total) {
        public Despacho { lineas = List.copyOf(lineas); }
    }
    public record Totales(BigDecimal pagado, BigDecimal cobrado, BigDecimal perdido) { }
    private record Toma(Lote lote, Bodega bodega, BigDecimal kg) { }
    private LocalDate fecha;
    private final Map<String, Bodega> bodegas = new TreeMap<>();
    private final Map<String, Lote> historial = new TreeMap<>();
    private final Map<String, String> ubicaciones = new TreeMap<>();
    private final Map<String, Totales> contabilidad = new TreeMap<>();
    private final List<Despacho> despachos = new ArrayList<>();
    public AcopioGT(LocalDate fecha) { this.fecha = Validacion.requerido(fecha, "Fecha"); }
    public LocalDate fecha() { return fecha; }
    public List<Bodega> bodegas() { return List.copyOf(bodegas.values()); }
    public List<Lote> historial() { return historial.values().stream().sorted().toList(); }
    public List<Despacho> despachos() { return List.copyOf(despachos); }
    public String ubicacion(String codigo) { return ubicaciones.get(Validacion.codigo(codigo)); }
    public Map<String, Totales> totales() { return Collections.unmodifiableMap(new TreeMap<>(contabilidad)); }
    public void registrarBodega(Bodega bodega) {
        Validacion.requerido(bodega, "Bodega");
        if (bodegas.containsKey(bodega.codigo())) throw new IllegalArgumentException("Código de bodega repetido.");
        if (bodega.iterator().hasNext()) throw new IllegalArgumentException("Registre una bodega vacía.");
        bodegas.put(bodega.codigo(), bodega);
    }
    private Bodega buscarBodega(String codigo) {
        Bodega bodega = bodegas.get(Validacion.codigo(codigo));
        if (bodega == null) throw new IllegalArgumentException("La bodega no existe.");
        return bodega;
    }
    private void validarNuevo(Lote lote, boolean inicial) {
        Validacion.requerido(lote, "Lote");
        if (historial.containsKey(lote.codigo())) throw new IllegalArgumentException("Código de lote repetido.");
        if (lote.estado() != EstadoLote.DISPONIBLE || lote.disponibles().compareTo(lote.pesoRecibido()) != 0)
            throw new IllegalArgumentException("El lote nuevo debe conservar todo su peso.");
        if (inicial ? lote.ingreso().isAfter(fecha) : !lote.ingreso().equals(fecha))
            throw new IllegalArgumentException("Fecha de ingreso inválida para esta operación.");
        if (lote.vencido(fecha)) throw new IllegalArgumentException("No se recibe producto vencido.");
    }
    public Preliquidacion preliquidar(Lote lote) {
        validarNuevo(lote, false);
        return new Preliquidacion(lote.codigo(), lote.categoria(), lote.pago(), lote.vencimiento());
    }
    public void recibir(Lote lote, String codigoBodega) { ingresar(lote, codigoBodega, false); }
    /** Solo para la carga inicial: conserva costo pero no registra pagos. */
    void cargarInicial(Lote lote, String codigoBodega) { ingresar(lote, codigoBodega, true); }
    private void ingresar(Lote lote, String codigoBodega, boolean inicial) {
        validarNuevo(lote, inicial);
        Bodega bodega = buscarBodega(codigoBodega);
        bodega.validarIngreso(lote);
        BigDecimal pago = lote.pago();
        bodega.almacenar(lote);
        historial.put(lote.codigo(), lote);
        ubicaciones.put(lote.codigo(), bodega.codigo()); // Nunca se reasigna.
        sumar(lote.categoria(), inicial ? BigDecimal.ZERO : pago, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    private void sumar(String categoria, BigDecimal pago, BigDecimal cobro, BigDecimal perdida) {
        Totales t = contabilidad.getOrDefault(categoria,
            new Totales(Dinero.monto(BigDecimal.ZERO), Dinero.monto(BigDecimal.ZERO), Dinero.monto(BigDecimal.ZERO)));
        contabilidad.put(categoria, new Totales(t.pagado.add(pago), t.cobrado.add(cobro), t.perdido.add(perdida)));
    }
    public List<Lote> existencias() {
        List<Lote> resultado = new ArrayList<>();
        for (Bodega bodega : bodegas.values()) for (Lote lote : bodega) resultado.add(lote);
        Collections.sort(resultado); // Mismo Comparable que usa cada bodega.
        return List.copyOf(resultado);
    }
    public List<Lote> proximosAVencer() {
        LocalDate limite = fecha.plusDays(3);
        return existencias().stream().filter(l -> l.vencimiento()
            .map(v -> !v.isBefore(fecha) && !v.isAfter(limite)).orElse(false)).toList();
    }
    public Despacho despachar(String comprador, String categoria, BigDecimal kg) {
        comprador = Validacion.texto(comprador, "Comprador");
        categoria = Validacion.codigo(categoria);
        Validacion.positivo(kg, "Kilogramos solicitados");
        BigDecimal faltan = kg;
        List<Toma> plan = new ArrayList<>();
        for (Lote lote : existencias()) {
            if (!lote.categoria().equals(categoria) || lote.vencido(fecha)) continue;
            BigDecimal tomar = faltan.min(lote.disponibles());
            plan.add(new Toma(lote, buscarBodega(ubicaciones.get(lote.codigo())), tomar));
            faltan = faltan.subtract(tomar);
            if (faltan.signum() == 0) break;
        }
        if (faltan.signum() != 0) throw new IllegalArgumentException("Existencias insuficientes: pedido rechazado.");
        List<Linea> lineas = new ArrayList<>();
        BigDecimal costo = BigDecimal.ZERO;
        for (Toma toma : plan) {
            BigDecimal parcial = toma.lote.costoDe(toma.kg);
            costo = costo.add(parcial);
            lineas.add(new Linea(toma.lote.codigo(), toma.bodega.codigo(), toma.kg, parcial));
        }
        BigDecimal total = Dinero.monto(costo.multiply(new BigDecimal("1.35")));
        Despacho resultado = new Despacho(comprador, categoria, fecha, lineas, total);
        for (Toma toma : plan) toma.lote.consumir(toma.kg);
        for (Bodega bodega : bodegas.values()) {
            Iterator<Lote> it = bodega.iterator();
            while (it.hasNext()) if (it.next().estado() == EstadoLote.AGOTADO) it.remove();
        }
        sumar(categoria, BigDecimal.ZERO, total, BigDecimal.ZERO);
        despachos.add(resultado);
        return resultado;
    }
    public BigDecimal avanzarFecha(LocalDate nueva) {
        Validacion.requerido(nueva, "Nueva fecha");
        if (!nueva.isAfter(fecha)) throw new IllegalArgumentException("La nueva fecha debe ser posterior a " + fecha);
        BigDecimal perdida = BigDecimal.ZERO;
        for (Bodega bodega : bodegas.values()) {
            Iterator<Lote> it = bodega.iterator();
            while (it.hasNext()) {
                Lote lote = it.next();
                if (lote.vencido(nueva)) {
                    BigDecimal parcial = lote.descartar(nueva);
                    it.remove();
                    sumar(lote.categoria(), BigDecimal.ZERO, BigDecimal.ZERO, parcial);
                    perdida = perdida.add(parcial);
                }
            }
        }
        fecha = nueva;
        return Dinero.monto(perdida);
    }
}
