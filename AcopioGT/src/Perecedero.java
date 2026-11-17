import java.time.LocalDate;
public interface Perecedero {
    LocalDate fechaVencimiento();
    default boolean estaVencido(LocalDate fecha) { return fecha.isAfter(fechaVencimiento()); }
}
