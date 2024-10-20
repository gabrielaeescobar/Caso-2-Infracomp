public class ReferenciaPagina {
    private int paginaVirtual;
    private int desplazamiento;
    private int posicion;
    private String tipo; // Indica si es una referencia a la imagen o al mensaje
    private int fila; // Para la imagen, la fila del píxel
    private int columna; // Para la imagen, la columna del píxel
    private String color; // "R", "G", o "B" para la imagen

    // Constructor para las referencias de la imagen
    public ReferenciaPagina(int paginaVirtual, int desplazamiento, int fila, int columna, String color) {
        this.paginaVirtual = paginaVirtual;
        this.desplazamiento = desplazamiento;
        this.tipo = "Imagen";
        this.fila = fila;
        this.columna = columna;
        this.color = color;
    }

    // Constructor para las referencias del mensaje
    public ReferenciaPagina(int paginaVirtual, int desplazamiento, int posicion) {
        this.paginaVirtual = paginaVirtual;
        this.desplazamiento = desplazamiento;
        this.posicion = posicion;
        this.tipo = "Mensaje";
    }

    // Sobrescribir el método toString() para mostrar la referencia en el formato correcto
    @Override
    public String toString() {
        if (tipo.equals("Imagen")) {
            return tipo + "[" + fila + "][" + columna + "]." + color + "," + paginaVirtual + "," + desplazamiento + ",R";
        } else {
            return tipo + "[" + posicion + "]," + paginaVirtual + "," + desplazamiento + ",W";
        }
    }
}
