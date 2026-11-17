/** Extensible sin modificar los consumidores de Almacenable. */
public record Ambiente(String nombre) {
    public static final Ambiente SECO = new Ambiente("SECO");
    public static final Ambiente FRIO = new Ambiente("FRIO");
    public Ambiente { nombre = Validacion.codigo(nombre); }
}
