import java.util.Scanner;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        new Consola(new Scanner(System.in), DatosIniciales.crear()).ejecutar();
    }
}
