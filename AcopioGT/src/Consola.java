import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/** Las únicas selecciones de categorías están en el registro de formularios. */
public final class Consola {
    private record Base(String codigo, String productor, BigDecimal peso, Calidad calidad) { }
    private final Scanner entrada;
    private final AcopioGT sistema;
    private final Map<String, Function<Base, Lote>> formularios = new LinkedHashMap<>();
    public Consola(Scanner entrada, AcopioGT sistema) {
        this.entrada = entrada;
        this.sistema = sistema;
        formularios.put("CAFE", b -> new Cafe(b.codigo, b.productor, sistema.fecha(), b.peso, b.calidad, decimal("Humedad (%)")));
        formularios.put("CAFE_ORGANICO", b -> new CafeOrganico(b.codigo, b.productor, sistema.fecha(), b.peso, b.calidad,
            decimal("Humedad (%)"), leer("Entidad certificadora"), fecha("Sello válido hasta (AAAA-MM-DD)")));
        formularios.put("CARDAMOMO", b -> new Cardamomo(b.codigo, b.productor, sistema.fecha(), b.peso, b.calidad, decimal("Grano verde (%)")));
        formularios.put("HORTALIZA", b -> new Hortaliza(b.codigo, b.productor, sistema.fecha(), b.peso, b.calidad, entero("Vida útil (3 a 14 días)")));
        formularios.put("QUESO_FRESCO", b -> new QuesoFresco(b.codigo, b.productor, sistema.fecha(), b.peso, b.calidad, decimal("Grasa (%)")));
    }
    private String leer(String mensaje) {
        System.out.print(mensaje + ": ");
        if (!entrada.hasNextLine()) throw new NoSuchElementException("Fin de entrada");
        return entrada.nextLine().trim();
    }
    private BigDecimal decimal(String mensaje) { return new BigDecimal(leer(mensaje)); }
    private int entero(String mensaje) { return Integer.parseInt(leer(mensaje)); }
    private LocalDate fecha(String mensaje) { return LocalDate.parse(leer(mensaje)); }
    private String categoria() {
        List<String> nombres = new ArrayList<>(formularios.keySet());
        for (int i = 0; i < nombres.size(); i++) System.out.println((i + 1) + ". " + nombres.get(i));
        int indice = entero("Categoría") - 1;
        if (indice < 0 || indice >= nombres.size()) throw new IllegalArgumentException("Categoría inválida.");
        return nombres.get(indice);
    }
    private Lote crearLote() {
        String categoria = categoria();
        Base b = new Base(leer("Código del lote"), leer("Productor"), decimal("Peso recibido (kg)"),
            Calidad.valueOf(leer("Calidad (A/B/C)").toUpperCase(Locale.ROOT)));
        return formularios.get(categoria).apply(b);
    }
    private void preliquidarORecibir(boolean recibir) {
        Lote lote = crearLote();
        AcopioGT.Preliquidacion p = sistema.preliquidar(lote);
        System.out.println("Pago: " + Dinero.quetzales(p.pago()) + " | Vence: "
            + p.vencimiento().map(LocalDate::toString).orElse("no aplica"));
        if (recibir) {
            String bodega = leer("Código de bodega");
            if (leer("¿Aceptar recepción y pago? (S/N)").equalsIgnoreCase("S")) {
                sistema.recibir(lote, bodega);
                System.out.println("Lote recibido y pago registrado.");
            } else System.out.println("Recepción cancelada.");
        }
    }
    private void registrarBodega() {
        String codigo = leer("Código de bodega");
        BigDecimal capacidad = decimal("Capacidad máxima (kg)");
        String respuesta = leer("¿Cámara fría? (S/N)").toUpperCase(Locale.ROOT);
        if (!respuesta.equals("S") && !respuesta.equals("N")) throw new IllegalArgumentException("Escriba S o N.");
        sistema.registrarBodega(new Bodega(codigo, capacidad, respuesta.equals("S")));
        System.out.println("Bodega registrada.");
    }
    private void pedido() {
        String comprador = leer("Comprador");
        String categoria = categoria();
        AcopioGT.Despacho d = sistema.despachar(comprador, categoria, decimal("Kilogramos solicitados"));
        for (AcopioGT.Linea linea : d.lineas())
            System.out.println(linea.lote() + " en " + linea.bodega() + ": " + linea.kg().toPlainString() + " kg");
        System.out.println("Total cobrado: " + Dinero.quetzales(d.total()));
    }
    private void existencias() {
        for (String categoria : formularios.keySet()) {
            System.out.println("Categoría " + categoria);
            BigDecimal total = BigDecimal.ZERO;
            for (Lote lote : sistema.existencias()) if (lote.categoria().equals(categoria)) {
                System.out.println("  " + lote + " | " + sistema.ubicacion(lote.codigo()));
                total = total.add(lote.disponibles());
            }
            System.out.println("Total: " + total.toPlainString() + " kg");
        }
    }
    private void bodegas() {
        for (Bodega bodega : sistema.bodegas()) {
            System.out.println(bodega);
            for (Lote lote : bodega) System.out.println("  " + lote);
        }
    }
    private void contabilidad() {
        sistema.totales().forEach((categoria, t) -> System.out.println(categoria + " | pagado "
            + Dinero.quetzales(t.pagado()) + " | cobrado " + Dinero.quetzales(t.cobrado())
            + " | pérdidas " + Dinero.quetzales(t.perdido())));
    }
    public void ejecutar() {
        while (true) {
            System.out.println("\nAcopioGT | Fecha de operación: " + sistema.fecha());
            System.out.println("1. Pre-liquidar\n2. Recibir lote\n3. Registrar bodega\n4. Pedido y despacho"
                + "\n5. Avanzar fecha\n6. Existencias por categoría\n7. Contenido de bodegas"
                + "\n8. Vencen en los próximos 3 días\n9. Reporte contable\n10. Historial de lotes\n0. Salir");
            try {
                switch (entero("Opción")) {
                    case 0 -> { System.out.println("Hasta pronto."); return; }
                    case 1 -> preliquidarORecibir(false);
                    case 2 -> preliquidarORecibir(true);
                    case 3 -> registrarBodega();
                    case 4 -> pedido();
                    case 5 -> System.out.println("Pérdida registrada: " + Dinero.quetzales(
                        sistema.avanzarFecha(fecha("Nueva fecha (AAAA-MM-DD)"))));
                    case 6 -> existencias();
                    case 7 -> bodegas();
                    case 8 -> {
                        List<Lote> lotes = sistema.proximosAVencer();
                        if (lotes.isEmpty()) System.out.println("No hay lotes próximos a vencer.");
                        lotes.forEach(System.out::println);
                    }
                    case 9 -> contabilidad();
                    case 10 -> sistema.historial().forEach(l -> System.out.println(l + " | bodega original " + sistema.ubicacion(l.codigo())));
                    default -> System.out.println("Opción inválida.");
                }
            } catch (NoSuchElementException e) {
                System.out.println("Entrada finalizada."); return;
            } catch (RuntimeException e) {
                System.out.println("Operación rechazada: " + (e.getMessage() == null ? "datos inválidos" : e.getMessage()));
            }
        }
    }
}
