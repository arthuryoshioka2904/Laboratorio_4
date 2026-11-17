import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;

/** Pruebas ejecutables, independientes de JUnit. Compara resultados reales. */
public final class Pruebas {
    private static final LocalDate F = LocalDate.of(2026, 3, 12);
    private static int pasaron, fallaron;
    private Pruebas() { }
    private static BigDecimal n(String s) { return new BigDecimal(s); }
    private static String monto(BigDecimal x) { return Dinero.monto(x).toPlainString(); }
    private static Cafe cafe(String id, String kg, Calidad q, String h) {
        return new Cafe(id, "Productor", F, n(kg), q, n(h));
    }
    private static Hortaliza hortaliza(String id, String kg, LocalDate f, int dias) {
        return new Hortaliza(id, "Productor", f, n(kg), Calidad.A, dias);
    }
    private static AcopioGT vacio() {
        AcopioGT s = new AcopioGT(F);
        s.registrarBodega(new Bodega("S1", n("2000"), false));
        s.registrarBodega(new Bodega("S2", n("2000"), false));
        s.registrarBodega(new Bodega("F1", n("1000"), true));
        return s;
    }
    private static String codigos(Collection<Lote> lotes) {
        return String.join(",", lotes.stream().map(Lote::codigo).toList());
    }
    private static String foto(AcopioGT s) {
        StringBuilder b = new StringBuilder(s.fecha().toString());
        b.append(s.totales()).append(s.despachos());
        for (Bodega bg : s.bodegas()) {
            b.append(bg);
            for (Lote l : bg) b.append(l);
        }
        for (Lote l : s.historial()) b.append(l).append(s.ubicacion(l.codigo()));
        return b.toString();
    }
    private static String rechazo(AcopioGT s, Runnable operacion) {
        String antes = foto(s);
        boolean rechazo = false;
        try { operacion.run(); }
        catch (IllegalArgumentException | java.time.DateTimeException e) { rechazo = true; }
        if (!rechazo) return "NO se rechazó";
        return antes.equals(foto(s)) ? "rechazado; estado sin cambios" : "rechazado; ESTADO ALTERADO";
    }
    private static void caso(String caso, String entrada, String esperado, Supplier<String> accion) {
        String obtenido;
        try { obtenido = accion.get(); }
        catch (Throwable e) { obtenido = e.getClass().getSimpleName() + ": " + e.getMessage(); }
        boolean ok = esperado.equals(obtenido);
        if (ok) pasaron++; else fallaron++;
        System.out.println((pasaron + fallaron) + "\t" + caso + "\t" + entrada + "\t"
            + esperado + "\t" + obtenido.replace('\n', ' ') + "\t" + (ok ? "OK" : "FALLO"));
    }
    public static void main(String[] args) {
        System.out.println("ID\tCaso\tEntrada\tEsperado\tObtenido\tEstado");
        caso("Café A, sin descuento", "500 kg; A; 12%", "6000.00", () -> monto(cafe("C", "500", Calidad.A, "12").pago()));
        caso("Café B", "100 kg; B; 8%", "900.00", () -> monto(cafe("C", "100", Calidad.B, "8").pago()));
        caso("Café C", "100 kg; C; 12%", "600.00", () -> monto(cafe("C", "100", Calidad.C, "12").pago()));
        caso("Humedad por fracción", "500 kg; A; 13.4%", "5640.00", () -> monto(cafe("C", "500", Calidad.A, "13.4").pago()));
        caso("Fracción mínima", "100 kg; A; 12.01%", "1164.00", () -> monto(cafe("C", "100", Calidad.A, "12.01").pago()));
        caso("Un punto exacto", "100 kg; A; 13%", "1164.00", () -> monto(cafe("C", "100", Calidad.A, "13").pago()));
        caso("Humedad máxima", "100 kg; A; 14%", "1128.00", () -> monto(cafe("C", "100", Calidad.A, "14").pago()));
        caso("Bonificación orgánica", "500 kg; A; 13.4%; sello vigente", "6015.00", () -> monto(new CafeOrganico("O", "P", F, n("500"), Calidad.A, n("13.4"), "Certi", F).pago()));
        caso("Orgánico no vence por el sello", "sello válido el ingreso; consulta +400 días", "false", () -> Boolean.toString(new CafeOrganico("O", "P", F, n("1"), Calidad.C, n("12"), "Certi", F).vencido(F.plusDays(400))));
        caso("Cardamomo bono B", "80 kg; B; 90% verde", "4224.00", () -> monto(new Cardamomo("C", "P", F, n("80"), Calidad.B, n("90")).pago()));
        caso("Bono umbral exacto", "10 kg; A; 85% verde", "660.00", () -> monto(new Cardamomo("C", "P", F, n("10"), Calidad.A, n("85")).pago()));
        caso("Sin bono bajo umbral", "10 kg; A; 84.99%", "600.00", () -> monto(new Cardamomo("C", "P", F, n("10"), Calidad.A, n("84.99")).pago()));
        caso("Cardamomo C y cero verde", "10 kg; C; 0%", "350.00", () -> monto(new Cardamomo("C", "P", F, n("10"), Calidad.C, n("0")).pago()));
        caso("Merma hortaliza A", "200 kg; A; 7 días", "760.00", () -> monto(hortaliza("H", "200", F, 7).pago()));
        caso("Hortaliza B", "100 kg; B; 3 días", "285.00", () -> monto(new Hortaliza("H", "P", F, n("100"), Calidad.B, 3).pago()));
        caso("Hortaliza C", "100 kg; C; 14 días", "190.00", () -> monto(new Hortaliza("H", "P", F, n("100"), Calidad.C, 14).pago()));
        caso("Empaque queso A", "40 kg; A; grasa 10%", "1460.00", () -> monto(new QuesoFresco("Q", "P", F, n("40"), Calidad.A, n("10")).pago()));
        caso("Empaque queso B", "40 kg; B; grasa 40%", "1140.00", () -> monto(new QuesoFresco("Q", "P", F, n("40"), Calidad.B, n("40")).pago()));
        caso("Vencimiento hortaliza", "ingreso 2026-03-05; 7 días", "2026-03-12", () -> hortaliza("H", "1", F.minusDays(7), 7).fechaVencimiento().toString());
        caso("Vencimiento cardamomo", "ingreso 2026-03-12; 180 días", "2026-09-08", () -> new Cardamomo("C", "P", F, n("1"), Calidad.A, n("100")).fechaVencimiento().toString());
        caso("Vencimiento queso", "ingreso 2026-03-12; 21 días", "2026-04-02", () -> new QuesoFresco("Q", "P", F, n("1"), Calidad.A, n("20")).fechaVencimiento().toString());
        caso("Todavía válido al vencer", "hortaliza vence 2026-03-12; consulta ese día", "false", () -> Boolean.toString(hortaliza("H", "1", F.minusDays(7), 7).vencido(F)));
        caso("Vencido al día siguiente", "vence 2026-03-12; consulta 2026-03-13", "true", () -> Boolean.toString(hortaliza("H", "1", F.minusDays(7), 7).estaVencido(F.plusDays(1))));
        caso("Café no perecedero", "consulta +10000 días", "false", () -> Boolean.toString(cafe("C", "1", Calidad.A, "12").vencido(F.plusDays(10000))));
        caso("Pre-liquidación pura", "500 kg café A; 13.4%", "5640.00; sin cambios", () -> {
            AcopioGT s = vacio(); String antes = foto(s);
            String pago = monto(s.preliquidar(cafe("C", "500", Calidad.A, "13.4")).pago());
            return pago + (antes.equals(foto(s)) ? "; sin cambios" : "; ALTERADO");
        });
        caso("Recepción paga monto completo", "500 kg café A; 13.4%; S1", "5640.00; 500", () -> {
            AcopioGT s = vacio(); s.recibir(cafe("C", "500", Calidad.A, "13.4"), "S1");
            return monto(s.totales().get("CAFE").pagado()) + "; " + s.existencias().get(0).disponibles();
        });
        caso("Carga inicial sin movimientos", "10 lotes; 3 bodegas", "10; 3; 0.00", () -> {
            AcopioGT s = DatosIniciales.crear(); BigDecimal suma = BigDecimal.ZERO;
            for (AcopioGT.Totales t : s.totales().values()) suma = suma.add(t.pagado()).add(t.cobrado()).add(t.perdido());
            return s.historial().size() + "; " + s.bodegas().size() + "; " + monto(suma);
        });
        caso("Costo inicial conserva reglas", "CAF-A inicial", "11.28", () -> monto(DatosIniciales.crear().historial().stream().filter(l -> l.codigo().equals("CAF-A")).findFirst().orElseThrow().costoPorKg()));
        caso("Despacho parcial y margen", "100 kg de café 500 kg pagado Q5640", "1522.80; 400; DISPONIBLE", () -> {
            AcopioGT s = vacio(); Cafe c = cafe("C", "500", Calidad.A, "13.4"); s.recibir(c, "S1");
            return monto(s.despachar("Comprador", "CAFE", n("100")).total()) + "; " + c.disponibles() + "; " + c.estado();
        });
        caso("Agotado libera capacidad", "despacho de todo un lote de 10 kg", "AGOTADO; 0; true", () -> {
            AcopioGT s = vacio(); Cafe c = cafe("C", "10", Calidad.A, "12"); s.recibir(c, "S1"); s.despachar("X", "CAFE", n("10"));
            return c.estado() + "; " + s.bodegas().stream().filter(b -> b.codigo().equals("S1")).findFirst().orElseThrow().ocupacion() + "; " + s.existencias().isEmpty();
        });
        caso("Descarte pérdida restante", "hortaliza 200 kg Q760; despachar 150; avanzar 8 días", "190.00; DESCARTADO; 0; 190.00", () -> {
            AcopioGT s = vacio(); Hortaliza h = hortaliza("H", "200", F, 7); s.recibir(h, "F1"); s.despachar("X", "HORTALIZA", n("150"));
            return monto(s.avanzarFecha(F.plusDays(8))) + "; " + h.estado() + "; " + h.disponibles() + "; " + monto(s.totales().get("HORTALIZA").perdido());
        });
        caso("Descarte durante recorrido", "dos lotes vencen a 3 días; avanzar 4", "7.60; 0", () -> {
            AcopioGT s = vacio(); s.recibir(hortaliza("H1", "1", F, 3), "F1"); s.recibir(hortaliza("H2", "1", F, 3), "F1");
            return monto(s.avanzarFecha(F.plusDays(4))) + "; " + s.existencias().size();
        });
        caso("No duplicar pérdida", "descartar y avanzar otra vez", "0.00", () -> {
            AcopioGT s = vacio(); s.recibir(hortaliza("H", "1", F, 3), "F1"); s.avanzarFecha(F.plusDays(4)); return monto(s.avanzarFecha(F.plusDays(5)));
        });
        caso("Orden global y desempates", "perecederos primero; mismo ingreso: A antes B; código A antes Z", "H,CA,CZ,CB,N", () -> {
            AcopioGT s = vacio(); s.recibir(cafe("CB", "1", Calidad.B, "12"), "S1"); s.recibir(cafe("CZ", "1", Calidad.A, "12"), "S2");
            s.recibir(cafe("CA", "1", Calidad.A, "12"), "S1"); s.recibir(hortaliza("H", "1", F, 3), "F1");
            s.cargarInicial(new Cafe("N", "P", F.plusDays(0), n("1"), Calidad.C, n("12")), "S2");
            return codigos(s.existencias());
        });
        caso("Desempate perecederos", "misma fecha vencimiento; A antes B y código menor", "HA,HZ,HB", () -> {
            AcopioGT s = vacio(); s.recibir(new Hortaliza("HB", "P", F, n("1"), Calidad.B, 3), "F1");
            s.recibir(hortaliza("HZ", "1", F, 3), "F1"); s.recibir(hortaliza("HA", "1", F, 3), "F1"); return codigos(s.existencias());
        });
        caso("Bodega recorre orden natural", "CAFE + CARDAMOMO; carga inicial", "CAR-B,CAF-A,ORG-A", () -> {
            List<Lote> lista = new ArrayList<>(); DatosIniciales.crear().bodegas().stream().filter(b -> b.codigo().equals("SECA-1")).findFirst().orElseThrow().forEach(lista::add);
            return codigos(lista);
        });
        caso("Despacho combina bodegas", "CA 10 kg S1; CB 20 kg S2; pedido 25", "CA:10:S1,CB:15:S2; 344.25; 5", () -> {
            AcopioGT s = vacio(); s.recibir(cafe("CB", "20", Calidad.B, "12"), "S2"); s.recibir(cafe("CA", "10", Calidad.A, "12"), "S1");
            AcopioGT.Despacho d = s.despachar("X", "CAFE", n("25"));
            return String.join(",", d.lineas().stream().map(l -> l.lote() + ":" + l.kg() + ":" + l.bodega()).toList()) + "; " + monto(d.total()) + "; " + s.existencias().get(0).disponibles();
        });
        caso("No perecederos por antigüedad", "C1: fecha -1 calidad C; C2 hoy calidad A", "C1,C2", () -> {
            AcopioGT s = vacio(); s.cargarInicial(new Cafe("C1", "P", F.minusDays(1), n("1"), Calidad.C, n("12")), "S1"); s.recibir(cafe("C2", "1", Calidad.A, "12"), "S2"); return codigos(s.existencias());
        });
        caso("FEFO antes que ingreso", "H1 ingreso -4 vida14; H2 hoy vida3", "H2,H1", () -> {
            AcopioGT s = vacio(); s.cargarInicial(hortaliza("H1", "1", F.minusDays(4), 14), "F1"); s.recibir(hortaliza("H2", "1", F, 3), "F1"); return codigos(s.existencias());
        });
        caso("Reporte próximos 3 días inclusivo", "datos iniciales; incluye hoy y día +3", "HOR-A,CAR-B,QUE-A,HOR-C", () -> codigos(DatosIniciales.crear().proximosAVencer()));
        caso("Capacidad exacta aceptada", "bodega 10 kg; recibir 10 kg", "10", () -> {
            AcopioGT s = new AcopioGT(F); s.registrarBodega(new Bodega("S", n("10"), false)); s.recibir(cafe("C", "10", Calidad.A, "12"), "S"); return s.bodegas().get(0).ocupacion().toPlainString();
        });
        caso("Capacidad se libera al consumir", "bodega 10; recibir10; despachar5; recibir5", "10", () -> {
            AcopioGT s = new AcopioGT(F); s.registrarBodega(new Bodega("S", n("10"), false)); s.recibir(cafe("C", "10", Calidad.A, "12"), "S"); s.despachar("X", "CAFE", n("5")); s.recibir(cafe("D", "5", Calidad.B, "12"), "S"); return s.bodegas().get(0).ocupacion().toPlainString();
        });
        AcopioGT s = vacio(); s.recibir(cafe("C", "10", Calidad.A, "12"), "S1");
        String sinCambios = "rechazado; estado sin cambios";
        caso("Pedido insuficiente atómico", "solicitar 11; hay 10", sinCambios, () -> rechazo(s, () -> s.despachar("X", "CAFE", n("11"))));
        caso("Categoría sin stock", "solicitar 1 kg orgánico", sinCambios, () -> rechazo(s, () -> s.despachar("X", "CAFE_ORGANICO", n("1"))));
        caso("Pedido cero", "0 kg", sinCambios, () -> rechazo(s, () -> s.despachar("X", "CAFE", n("0"))));
        caso("Pedido negativo", "-1 kg", sinCambios, () -> rechazo(s, () -> s.despachar("X", "CAFE", n("-1"))));
        caso("Comprador vacío", "comprador vacío", sinCambios, () -> rechazo(s, () -> s.despachar(" ", "CAFE", n("1"))));
        caso("Peso cero", "café 0 kg", sinCambios, () -> rechazo(s, () -> s.recibir(cafe("X", "0", Calidad.A, "12"), "S1")));
        caso("Humedad baja", "7.99%", sinCambios, () -> rechazo(s, () -> s.preliquidar(cafe("X", "1", Calidad.A, "7.99"))));
        caso("Humedad alta", "14.01%", sinCambios, () -> rechazo(s, () -> s.preliquidar(cafe("X", "1", Calidad.A, "14.01"))));
        caso("Sello vencido", "vigencia fecha de ingreso -1", sinCambios, () -> rechazo(s, () -> s.recibir(new CafeOrganico("O", "P", F, n("1"), Calidad.A, n("12"), "Certi", F.minusDays(1)), "S1")));
        caso("Queso C", "calidad C; grasa20%", sinCambios, () -> rechazo(s, () -> s.recibir(new QuesoFresco("Q", "P", F, n("1"), Calidad.C, n("20")), "F1")));
        caso("Grasa fuera de rango", "9.9%", sinCambios, () -> rechazo(s, () -> s.recibir(new QuesoFresco("Q", "P", F, n("1"), Calidad.A, n("9.9")), "F1")));
        caso("Vida útil inválida", "15 días", sinCambios, () -> rechazo(s, () -> s.recibir(hortaliza("H", "1", F, 15), "F1")));
        caso("Grano verde inválido", "100.01%", sinCambios, () -> rechazo(s, () -> s.recibir(new Cardamomo("D", "P", F, n("1"), Calidad.A, n("100.01")), "S1")));
        caso("Lote duplicado normalizado", "código c; ya existe C", sinCambios, () -> rechazo(s, () -> s.recibir(cafe(" c ", "1", Calidad.A, "12"), "S1")));
        caso("Código vacío", "espacios", sinCambios, () -> rechazo(s, () -> s.recibir(cafe(" ", "1", Calidad.A, "12"), "S1")));
        caso("Bodega duplicada", "s1; ya existe S1", sinCambios, () -> rechazo(s, () -> s.registrarBodega(new Bodega("s1", n("1"), false))));
        caso("Capacidad negativa", "-1 kg", sinCambios, () -> rechazo(s, () -> s.registrarBodega(new Bodega("Z", n("-1"), false))));
        caso("Capacidad excedida", "S1 ocupada10/2000; nuevo2000", sinCambios, () -> rechazo(s, () -> s.recibir(cafe("D", "2000", Calidad.A, "12"), "S1")));
        caso("Hortaliza en seca", "1 kg HORTALIZA en S1", sinCambios, () -> rechazo(s, () -> s.recibir(hortaliza("H", "1", F, 3), "S1")));
        caso("Café en fría", "1 kg CAFE en F1", sinCambios, () -> rechazo(s, () -> s.recibir(cafe("D", "1", Calidad.A, "12"), "F1")));
        caso("Bodega inexistente", "bodega NO", sinCambios, () -> rechazo(s, () -> s.recibir(cafe("D", "1", Calidad.A, "12"), "NO")));
        caso("Ingreso fuera de fecha", "ingreso ayer en recepción normal", sinCambios, () -> rechazo(s, () -> s.recibir(new Cafe("D", "P", F.minusDays(1), n("1"), Calidad.A, n("12")), "S1")));
        caso("Fecha hacia atrás", "2026-03-11", sinCambios, () -> rechazo(s, () -> s.avanzarFecha(F.minusDays(1))));
        caso("Fecha sin avance", "misma fecha", sinCambios, () -> rechazo(s, () -> s.avanzarFecha(F)));
        caso("Futuro en carga inicial", "ingreso mañana", sinCambios, () -> rechazo(s, () -> s.cargarInicial(new Cafe("D", "P", F.plusDays(1), n("1"), Calidad.A, n("12")), "S1")));
        caso("Vencido en carga inicial", "hortaliza ingresó hace4; vida3", sinCambios, () -> rechazo(s, () -> s.cargarInicial(hortaliza("H", "1", F.minusDays(4), 3), "F1")));
        caso("Código no se reutiliza agotado", "agotar C e intentar recibir C", sinCambios, () -> {
            AcopioGT x = vacio(); x.recibir(cafe("C", "1", Calidad.A, "12"), "S1"); x.despachar("X", "CAFE", n("1"));
            return rechazo(x, () -> x.recibir(cafe("C", "1", Calidad.A, "12"), "S2"));
        });
        caso("Decimales exactos", "recibir0.3; despachar0.1 y0.2", "AGOTADO; 0.00", () -> {
            AcopioGT x = vacio(); Cafe c = cafe("C", "0.3", Calidad.A, "12"); x.recibir(c, "S1"); x.despachar("X", "CAFE", n("0.1")); x.despachar("X", "CAFE", n("0.2"));
            return c.estado() + "; " + monto(c.disponibles());
        });
        caso("Reportes sin efectos", "consultar todos los reportes", "sin cambios", () -> {
            AcopioGT x = DatosIniciales.crear(); String antes = foto(x); x.existencias(); x.proximosAVencer(); x.historial(); x.totales(); x.bodegas();
            return antes.equals(foto(x)) ? "sin cambios" : "ALTERADO";
        });
        caso("Entrada incorrecta conserva menú", "abc; 5; fecha inválida; 9; 0", "true; true; true", () -> {
            PrintStream original = System.out;
            ByteArrayOutputStream datos = new ByteArrayOutputStream();
            try {
                System.setOut(new PrintStream(datos, true, StandardCharsets.UTF_8));
                new Consola(new Scanner("abc\n5\nfecha-invalida\n9\n0\n"), DatosIniciales.crear()).ejecutar();
            } finally { System.setOut(original); }
            String salida = datos.toString(StandardCharsets.UTF_8);
            return salida.contains("Operación rechazada") + "; " + salida.contains("pagado Q0.00") + "; " + salida.contains("Hasta pronto");
        });
        caso("Pedido insuficiente en varias bodegas", "10 kg S1 + 20 kg S2; pedir31", sinCambios, () -> {
            AcopioGT x = vacio(); x.recibir(cafe("A", "10", Calidad.A, "12"), "S1"); x.recibir(cafe("B", "20", Calidad.B, "12"), "S2");
            return rechazo(x, () -> x.despachar("X", "CAFE", n("31")));
        });
        caso("Agotado no genera pérdida", "hortaliza1; despachar1; avanzar4 días", "0.00; AGOTADO", () -> {
            AcopioGT x = vacio(); Hortaliza h = hortaliza("H", "1", F, 3); x.recibir(h, "F1"); x.despachar("X", "HORTALIZA", n("1"));
            return monto(x.avanzarFecha(F.plusDays(4))) + "; " + h.estado();
        });
        caso("Descarte conserva bodega original", "H en F1; descartar", "F1; 0", () -> {
            AcopioGT x = vacio(); x.recibir(hortaliza("H", "1", F, 3), "F1"); x.avanzarFecha(F.plusDays(4));
            return x.ubicacion("H") + "; " + x.existencias().size();
        });
        caso("Merma afecta pago y no stock", "hortaliza 200 kg", "200; 3.80", () -> {
            Hortaliza h = hortaliza("H", "200", F, 7); return h.disponibles() + "; " + monto(h.costoPorKg());
        });
        caso("Café orgánico es categoría separada", "solo hay orgánico; pedir café", sinCambios, () -> {
            AcopioGT x = vacio(); x.recibir(new CafeOrganico("O", "P", F, n("1"), Calidad.A, n("12"), "Certi", F), "S1");
            return rechazo(x, () -> x.despachar("X", "CAFE", n("1")));
        });
        caso("Descarte cardamomo", "10 kg C a Q350; avanzar181 días", "350.00; DESCARTADO", () -> {
            AcopioGT x = vacio(); Cardamomo c = new Cardamomo("D", "P", F, n("10"), Calidad.C, n("0")); x.recibir(c, "S1");
            return monto(x.avanzarFecha(F.plusDays(181))) + "; " + c.estado();
        });
        caso("Descarte queso", "40 kg A a Q1460; avanzar22 días", "1460.00; DESCARTADO", () -> {
            AcopioGT x = vacio(); QuesoFresco q = new QuesoFresco("Q", "P", F, n("40"), Calidad.A, n("20")); x.recibir(q, "F1");
            return monto(x.avanzarFecha(F.plusDays(22))) + "; " + q.estado();
        });
        caso("Totales por categoría", "café10; hortaliza10; despachar café1; vencer H", "120.00; 16.20; 0.00 / 38.00; 0.00; 38.00", () -> {
            AcopioGT x = vacio(); x.recibir(cafe("C", "10", Calidad.A, "12"), "S1"); x.recibir(hortaliza("H", "10", F, 3), "F1");
            x.despachar("X", "CAFE", n("1")); x.avanzarFecha(F.plusDays(4));
            AcopioGT.Totales c = x.totales().get("CAFE"), h = x.totales().get("HORTALIZA");
            return monto(c.pagado()) + "; " + monto(c.cobrado()) + "; " + monto(c.perdido()) + " / " + monto(h.pagado()) + "; " + monto(h.cobrado()) + "; " + monto(h.perdido());
        });
        System.out.println("RESUMEN\t" + pasaron + " aprobadas\t" + fallaron + " fallidas");
        if (fallaron > 0) throw new AssertionError("Fallaron " + fallaron + " pruebas.");
    }
}
