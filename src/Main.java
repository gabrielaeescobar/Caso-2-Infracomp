
//importamos librerías
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Main {
    private int hits;
    private int misses;
    private Imagen imagen;
    private static ArrayList<ReferenciaPagina> referencias = new ArrayList<>();

    // leer un archivo de texto y devolver el mensaje como un array de caracteres
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

    // esconder un mensaje en una imagen
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

    // recuperar un mensaje escondido en una imagen
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

    // generar las referencias
    public static void generarReferencias(int tamanioPagina, String archivoImagen, String archivoResultado) {
        Imagen imagen = new Imagen(archivoImagen);

        // datos de la img
        int ancho = imagen.getAncho();
        int alto = imagen.getAlto();
        int totalBytesImagen = ancho * alto * 3;
        int longitudMensaje = imagen.leerLongitud();
        int NR = (longitudMensaje * 17) + 16; // 16B de longitud de msj + (longitud del mensaje * 8B lectura*
                                              // 8Bescritura +1 del cambio)
        int numPaginasImagen = (int) Math.ceil((double) totalBytesImagen / tamanioPagina);
        int numPaginasMensaje = (int) Math.ceil((double) longitudMensaje / tamanioPagina);
        int NP = (int) Math.ceil((double) ((ancho * alto * 3) + longitudMensaje) / tamanioPagina);
        // System.out.println("logitud msj: " + longitudMensaje);
        // System.out.println("Páginas necesarias para la imagen: " + numPaginasImagen);
        // System.out.println("Páginas necesarias para el mensaje: " +
        // numPaginasMensaje);
        // System.out.println("Páginas totales necesarias (NP): " + NP);

        int contadorReferencia = 0;
        int desplazamiento = 0;

        // PASO1: leer los primeros 16 bytes para la longitud del mssj
        for (int i = 0; i < 16; i++) {
            int paginaVirtual = i / tamanioPagina;
            desplazamiento = i % tamanioPagina;

            // calc de la fila y columna
            int fila = i / (ancho * 3);
            int columna = (i % (ancho * 3)) / 3;

            // def del color
            String color = "";
            if (i % 3 == 0)
                color = "R";
            else if (i % 3 == 1)
                color = "G";
            else
                color = "B";

            referencias.add(new ReferenciaPagina(paginaVirtual, desplazamiento, fila, columna, color));
            contadorReferencia++;
        }

        // PASO2: alternar la w del msj y la r de la img
        int bitIndex = 0;
        int desplazamientoMsj = 0;
        int byteMsg = 0;

        while (contadorReferencia < NR) {
            // escribir en el mensaje (W)
            int paginaVirtualMsg = numPaginasImagen + (desplazamientoMsj / tamanioPagina);
            referencias.add(new ReferenciaPagina(paginaVirtualMsg, desplazamientoMsj));
            contadorReferencia++;

            // leer de la imagen (R)
            int posicionByte = 16 + bitIndex; // desde eel byte 16
            desplazamiento = posicionByte % tamanioPagina;
            int paginaVirtual = posicionByte / tamanioPagina;
            int fila = posicionByte / (ancho * 3);
            int columna = (posicionByte % (ancho * 3)) / 3;
            String color = "";
            if (posicionByte % 3 == 0)
                color = "R";
            else if (posicionByte % 3 == 1)
                color = "G";
            else
                color = "B";

            referencias.add(new ReferenciaPagina(paginaVirtual, desplazamiento, fila, columna, color));
            bitIndex++;
            contadorReferencia++;

            // verificacion de la escritura adicional después de 8 bits
            if (bitIndex % 8 == 0 && contadorReferencia < NR) {
                referencias.add(new ReferenciaPagina(paginaVirtualMsg, desplazamientoMsj));
                desplazamientoMsj++;
                contadorReferencia++;
            }

            // incrementar el byte del mensaje después de completar 8 bits
            if (bitIndex % 8 == 0) {
                byteMsg++;
            }
        }

        // verificiacion de si hicimos exactamente NR referencias
        if (contadorReferencia != NR) {
            System.out.println("Error: No se han generado exactamente NR referencias.");
        }

        escribirArchivoReferencias(archivoResultado, referencias, tamanioPagina, alto, ancho, NR, NP);
    }

    // escirbir las referencias generadas en un archivo
    public static void escribirArchivoReferencias(String archivoSalida, ArrayList<ReferenciaPagina> referencias, int tp,
            int nf, int nc, int nr, int np) {
        try (FileWriter writer = new FileWriter(archivoSalida)) {
            writer.write("TP=" + tp + "\n");
            writer.write("NF=" + nf + "\n");
            writer.write("NC=" + nc + "\n");
            writer.write("NR=" + nr + "\n");
            writer.write("NP=" + np + "\n");
            for (ReferenciaPagina ref : referencias) {
                writer.write(ref.toString() + "\n");
            }
            System.out.println("Archivo de referencias generado exitosamente en: " + archivoSalida);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void simularPaginacion(int numMarcos, String archivoReferencias) {

    }

    /**
     * This method parses the file and returns an Object array:
     * - First element: List of pairs of pagenumber and IO operation ex: [0, 'W']
     * - Second element: NR value
     * - Third element: NP value
     * 
     * @param filePath The location of the .txt file
     * @return Object array containing pageNumbers, NR, and NP
     * @throws IOException if there's an issue reading the file
     */
    public static Object[] generarListaDeReferenciasYTamanios(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        ArrayList<ArrayList<Object>> pageNumbersAndIO = new ArrayList<ArrayList<Object>>();
        int NR = -1;
        int NP = -1;

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Extract NR and NP from the beginning of the file
            if (line.startsWith("NR=")) {
                NR = Integer.parseInt(line.substring(3));
            } else if (line.startsWith("NP=")) {
                NP = Integer.parseInt(line.substring(3));
            }

            // Process lines that match the 1st or 2nd possible formats
            if (line.matches("Imagen\\[\\d+\\]\\[\\d+\\]\\.[RGB],\\d+,\\d+,[RW]")) {
                // Example: Imagen[0][0].R,0,0,R
                String[] parts = line.split(",");
                int pageNumber = Integer.parseInt(parts[1]);
                String IOOperationString = parts[3];
                // obtenemos W o R
                char IOOperationChar = IOOperationString.charAt(0);
                ArrayList<Object> pageNumberIOOperationPair = new ArrayList<Object>();
                pageNumberIOOperationPair.add(pageNumber);
                pageNumberIOOperationPair.add(IOOperationChar);
                pageNumbersAndIO.add(pageNumberIOOperationPair);

            } else if (line.matches("Mensaje\\[\\d+\\],\\d+,\\d+,[RW]")) {
                // Example: Mensaje[0],2045,0,W
                String[] parts = line.split(",");
                int pageNumber = Integer.parseInt(parts[1]);
                String IOOperationString = parts[3];
                // obtenemos W o R
                char IOOperationChar = IOOperationString.charAt(0);
                ArrayList<Object> pageNumberIOOperationPair = new ArrayList<Object>();
                pageNumberIOOperationPair.add(pageNumber);
                pageNumberIOOperationPair.add(IOOperationChar);
                pageNumbersAndIO.add(pageNumberIOOperationPair);
            }
        }

        reader.close();

        // Return an Object array containing the pageNumbers, NR, and NP
        return new Object[] { pageNumbersAndIO, NR, NP };
    }

    // correr la simulación
    public static void main(String[] args) {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        Boolean continuar = true;

        try {
            while (continuar) {
                System.out.println(
                        "======================================================================================");
                System.out.println(
                        "=============================Seleccione una opción====================================");
                System.out.println("1. Generacion de referencias.");
                System.out.println(
                        "2. Calcular datos buscados: número de fallas de página, porcentaje de hits, tiempos.");
                System.out.println("3. Esconder mensaje en imagen.");
                System.out.println("4. Recuperar mensaje de imagen.");
                System.out.println("5. Salir.");
                System.out.println(
                        "======================================================================================");

                int opcion = Integer.parseInt(br.readLine());

                if (opcion == 1) {
                    System.out.println("Ingrese el tamaño de página (en bytes): ");
                    int tamanioPagina = Integer.parseInt(br.readLine());
                    
                    // Cambio 1: Se cambia para ingresar solo el nombre del archivo, la ruta se gestiona internamente
                    System.out.println("Nombre del archivo con la imagen que contiene el mensaje escondido (sin ruta): ");
                    String archivoImagen = br.readLine();
                    String rutaArchivoImagen = "src/imgs/" + archivoImagen;  // Asignación de ruta

                    // Mejorar la ruta de salida para el archivo de referencias
                    System.out.println("Nombre del archivo de salida para las referencias (sin .txt): ");
                    String archivoRespta = br.readLine();
                    String rutaArchivoRespuesta = "src/referencias/" + archivoRespta + ".txt";  // Asignación de ruta

                    generarReferencias(tamanioPagina, rutaArchivoImagen, rutaArchivoRespuesta);

                    System.out.println("El documento fue creado correctamente en la carpeta referencias.");

                    System.out.println("El documento fue creado correctamente.");

                } else if (opcion == 2) {
                    System.out.println("Ingrese el número de marcos de página: ");
                    int numeroMarcosPagina = Integer.parseInt(br.readLine());

                    // Cambio 2: Mejoramos la forma en la que se manejan las rutas
                    System.out.println("Ingrese el nombre del archivo de referencias (sin el .txt): ");
                    String nombreDelArchivoDeReferencias = br.readLine();

                    String rutaAlArchivo = "src/referencias/" + nombreDelArchivoDeReferencias + ".txt";  // Ruta del archivo de referencias

                    // Arreglo con la lista de referencias, Numero de referencias, Numero de Paginas
                    Object[] arregloConDatos = generarListaDeReferenciasYTamanios(rutaAlArchivo);

                    // Extraer los datos del arreglo
                    ArrayList<ArrayList<Object>> pageNumbersAndIO = (ArrayList<ArrayList<Object>>) arregloConDatos[0]; // First element: list of page numbers and io operations
                    int numeroDeReferencias = (int) arregloConDatos[1]; // Second element: NR value
                    int numeroDePaginas = (int) arregloConDatos[2]; // Third element: NP value

                    // Iniciar RAM Y SWAP
                    RAM ram = new RAM(numeroMarcosPagina);
                    SWAP swap = new SWAP(numeroDePaginas);
                    // cargamos la SWAP con todos las paginas
                    swap.cargarSWAP();

                    // Iniciamos tablas
                    TablaDePaginas tablaDePaginas = new TablaDePaginas(numeroDePaginas);
                    TablaAuxiliar tablaAuxiliar = new TablaAuxiliar(numeroDePaginas);

                    // flag para indicar al thread 2, cuándo debe parar. Cuando el thread 1 termine
                    // de leer las referencias, marca este flag como false
                    FlagHayMasRefencias flagHayMasReferencias = new FlagHayMasRefencias(true);

                    // Utilizo barrera para que se muestre el menú cuando ya todos los threads
                    // terminen
                    CyclicBarrier barrera = new CyclicBarrier(3);

                    // Thread que se encarga de resolver las referencias
                    Thread1 thread1 = new Thread1(flagHayMasReferencias, tablaDePaginas, tablaAuxiliar, swap, ram,
                            pageNumbersAndIO, numeroDeReferencias, barrera);
                    // Thread que actualiza el bit R de cada entrada
                    Thread2 thread2 = new Thread2(flagHayMasReferencias, tablaDePaginas, barrera);

                    thread1.start();
                    thread2.start();

                    barrera.await();

                } else if (opcion == 3) {
                    esconderMensajeEnImagen();
                } else if (opcion == 4) {
                    recuperarMensajeDeImagen();
                } else if (opcion == 5) {
                    continuar = false;
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
