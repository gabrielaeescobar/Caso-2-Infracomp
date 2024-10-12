import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Main {
    private int hits;
    private int misses;
    private Imagen imagen;
    private ArrayList<ReferenciaPagina> referencias;

    // Método para leer un archivo de texto y devolver el mensaje como un array de caracteres
    public static int leerArchivoTexto(String ruta, char[] mensaje) {
        int longitud = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            int c;
            while ((c = br.read()) != -1) {
                mensaje[longitud++] = (char) c;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return longitud;
    }

    // Método para esconder un mensaje en una imagen
    public static void esconderMensajeEnImagen() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        try {
            System.out.println("Nombre del archivo con la imagen a procesar: ");
            String rutaImagen = br.readLine();

            Imagen imagen = new Imagen(rutaImagen);

            System.out.println("Nombre del archivo con el mensaje a esconder: ");
            String rutaMensaje = br.readLine();

            char[] mensaje = new char[8000]; // Suponiendo un máximo de 8000 caracteres
            int longitud = leerArchivoTexto(rutaMensaje, mensaje);

            imagen.esconder(mensaje, longitud);
            imagen.escribirImagen("src/imgs/salida.bmp");
            System.out.println("El mensaje ha sido escondido en la imagen '" + rutaImagen + "_salida'.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para recuperar un mensaje escondido en una imagen
    public static void recuperarMensajeDeImagen() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        try {
            System.out.println("Nombre del archivo con el mensaje escondido: ");
            String ruta = br.readLine();

            Imagen imagen = new Imagen(ruta);
            int longitud = imagen.leerLongitud();
            char[] mensaje = new char[longitud];

            imagen.recuperar(mensaje, longitud);

            System.out.println("Nombre del archivo para almacenar el mensaje recuperado: ");
            String salida = br.readLine();
            
            // Guardar el mensaje recuperado en un archivo de texto
            try (FileOutputStream fos = new FileOutputStream(salida)) {
                for (char c : mensaje) {
                    fos.write(c);
                }
            }

            System.out.println("El mensaje ha sido recuperado y almacenado en el archivo: " + salida);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void generarReferencias(int tamanioPagina, String archivoImagen){

    }

    public void escribirArchivoReferencias(String archivoSalida, ArrayList<ReferenciaPagina> referencias){

    }

    public void simularPaginacion(int numMarcos, String archivoReferencias){

    }

    //correr la simulación
    public static void main(String[] args) {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        try {
            System.out.println("Seleccione una opción: ");
            System.out.println("1. Esconder un mensaje en una imagen.");
            System.out.println("2. Recuperar un mensaje de una imagen.");
            int opcion = Integer.parseInt(br.readLine());

            switch (opcion) {
                case 1:
                    esconderMensajeEnImagen();
                    break;
                case 2:
                    recuperarMensajeDeImagen();
                    break;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
