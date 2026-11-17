/** Capacidad compartida por hortaliza y queso. */
public interface Refrigerado extends Almacenable {
    @Override default Ambiente ambiente() { return Ambiente.FRIO; }
}
