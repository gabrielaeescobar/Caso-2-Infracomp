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
        Boolean continuar = true;

        try {
            while (continuar){
                System.out.println("======================================================================================");
                System.out.println("=============================Seleccione una opción====================================");
                System.out.println("1. Generacion de referencias.");
                System.out.println("2. Calcular datos buscados: número de fallas de página, porcentaje de hits, tiempos.");
                System.out.println("3. Esconder mensaje en imagen.");
                System.out.println("4. Recuperar mensaje de imagen.");
                System.out.println("5. Salir.");
                System.out.println("======================================================================================");



                int opcion = Integer.parseInt(br.readLine());
    
                if (opcion==1){
                    System.out.println("Seguimos trabajando en esto....");

                }
                else if(opcion ==2){
                    System.out.println("Seguimos trabajando en esto....");
                }
                else if (opcion == 3) {
                    esconderMensajeEnImagen();
                }
                else if (opcion == 4) {
                    recuperarMensajeDeImagen();
                }
                else if (opcion ==5 ){
                    continuar= false;
                }
                
                else {
                    System.out.println("Opción no válida.");
                }
    
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
