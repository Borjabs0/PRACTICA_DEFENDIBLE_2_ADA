package lib;
import java.util.Scanner;

public class ConsoleMenu {
    private static Scanner scanner = new Scanner(System.in);
    private static final int CAPACIDAD_INICIAL = 5; // Corrigiendo la variable
    private final String texto; // Encabezado
    private ConsoleMenu[] opciones; // Almacena las opciones del menú
    private int numOpciones; // Indica el número de opciones en el menú

    /**
     * Constructor público
     * @param texto Asigna la variable como argumento
     */
    public ConsoleMenu(String texto) {
        this.texto = texto;
        this.opciones = new ConsoleMenu[CAPACIDAD_INICIAL]; // Inicializando el arreglo
        this.numOpciones = 0;
    }

    /**
     * Método que amplía la capacidad del array de opciones.
     */
    private void ampliarCapacidad() {
        ConsoleMenu[] copia = new ConsoleMenu[opciones.length * 2];
        for (int i = 0; i < opciones.length; i++) {
            copia[i] = opciones[i];
        }
        opciones = copia;
    }

    /**
     * Método que añade una nueva opción al menú
     * @param texto Texto de la opción a añadir
     * @return Este objeto para encadenamiento (opcional)
     */
    public ConsoleMenu addOpcion(String texto) {
        if (numOpciones == opciones.length) { // El array está lleno, ampliar capacidad
            ampliarCapacidad();
        }
        opciones[numOpciones++] = new ConsoleMenu(texto); // Añadir la opción y aumentar el contador
        return this; // Permitir encadenamiento (opcional)
    }

 
    /**
     * Muestra el menú y retorna la opción seleccionada
     * @return Opción seleccionada
     */
    public int mostrarMenu() {
        int opcion;
        do {
            try {
                System.out.println(this);	
                opcion = Integer.parseInt(scanner.nextLine().trim());
                if (opcion < 0 || opcion > numOpciones) {
                    System.err.println("Elija una opción válida");
                } else {
                    return opcion; // Retorna la opción válida
                }
            } catch (NumberFormatException e) {
                System.err.println("Por favor, ingrese un número.");
            }
        } while (true);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("**************************\n");
        sb.append("***  ").append(texto).append("  ***\n");
        sb.append("**************************\n");
        for (int i = 0; i < numOpciones; i++) {
            sb.append(i + 1).append(". ").append(opciones[i].texto).append("\n"); // Ajustar con String format
        }
        sb.append("0. Exit\n");
        sb.append("*******************\n");
        sb.append("Elija una opción: \n");
        return sb.toString();
    }
}
