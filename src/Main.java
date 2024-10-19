
//importamos librerías
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main {

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
    public static void esconderMensajeEnImagen(String rutaImagen, String rutaMensaje, String rutaSalida) {
        try {
            Imagen imagen = new Imagen(rutaImagen);

            char[] mensaje = new char[8000]; // Suponiendo un máximo de 8000 caracteres
            int longitud = leerArchivoTexto(rutaMensaje, mensaje);

            imagen.esconder(mensaje, longitud);
            imagen.escribirImagen(rutaSalida);
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
            System.out.println("Nombre del archivo con el mensaje escondido sin extension: ");
            String ruta = br.readLine();
            ruta = "src/imgs/" + ruta + ".bmp";

            Imagen imagen = new Imagen(ruta);
            int longitud = imagen.leerLongitud();
            char[] mensaje = new char[longitud];

            imagen.recuperar(mensaje, longitud);

            System.out.println("Nombre del archivo para almacenar el mensaje recuperado: ");
            String salida = br.readLine();
            salida = "src/referencias/" + salida + ".txt";

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
        referencias = new ArrayList<>(); // limpia la lista de referencias, por si antes se corrio una imagen, para que
                                         // no se acomulen
        Imagen imagen = new Imagen(archivoImagen);

        // datos de la img
        int ancho = imagen.getAncho();
        int alto = imagen.getAlto();
        int totalBytesImagen = ancho * alto * 3;
        int longitudMensaje = imagen.leerLongitud();
        int NR = (longitudMensaje * 17) + 16; // 16B de longitud de msj + (longitud del mensaje * 8B lectura*
                                              // 8Bescritura +1 del cambio)
        int numPaginasImagen = (int) Math.ceil(totalBytesImagen / tamanioPagina);
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

    // escribir las referencias generadas en un archivo
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

    /**
     * This method parses the file and returns an Object array:
     * - First element: List of pairs of pagenumber and IO operation ex: [0, 'W']
     * - Second element: NP value
     * 
     * @param filePath The location of the .txt file
     * @return Object array containing pageNumbers, NR, and NP
     * @throws IOException if there's an issue reading the file
     */
    public static Object[] generarListaDeReferenciasYTamanios(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        ArrayList<ArrayList<Object>> pageNumbersAndIO = new ArrayList<ArrayList<Object>>();
        int NP = -1;

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Extract NR and NP from the beginning of the file
            if (line.startsWith("NP=")) {
                NP = Integer.parseInt(line.substring(3));

                // Process lines that match the 1st or 2nd possible formats
            } else if (line.matches("Imagen\\[\\d+\\]\\[\\d+\\]\\.[RGB],\\d+,\\d+,[RW]")) {
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
        return new Object[] { pageNumbersAndIO, NP };
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
                System.out.println("5. Ejecutar pruebas automáticas.");
                System.out.println("0. Salir.");
                System.out.println(
                        "======================================================================================");

                int opcion = Integer.parseInt(br.readLine());

                if (opcion == 1) {
                    System.out.println("Ingrese el tamaño de página (en bytes): ");
                    int tamanioPagina = Integer.parseInt(br.readLine());

                    System.out.println(
                            "Nombre del archivo con la imagen que contiene el mensaje escondido (sin ruta ni extension, por ejemplo: caso2-parrots_mod): ");
                    String archivoImagen = br.readLine();
                    String rutaArchivoImagen = "src/imgs/" + archivoImagen + ".bmp"; // Asignación de ruta

                    System.out.println(
                            "Nombre del archivo de salida para las referencias (sin .txt, por ejemplo: ref_parrots): ");
                    String archivoRespta = br.readLine();
                    String rutaArchivoRespuesta = "src/referencias/" + archivoRespta + ".txt"; // Asignación de ruta

                    generarReferencias(tamanioPagina, rutaArchivoImagen, rutaArchivoRespuesta);

                    System.out.println("El documento fue creado correctamente en la carpeta referencias.");

                } else if (opcion == 2) {
                    System.out.println("Ingrese el número de marcos de página: ");
                    int numeroMarcosPagina = Integer.parseInt(br.readLine());

                    System.out.println("Ingrese el nombre del archivo de referencias (sin el .txt): ");
                    String nombreDelArchivoDeReferencias = br.readLine();
                    String rutaAlArchivo = "src/referencias/" + nombreDelArchivoDeReferencias + ".txt"; // Ruta del
                                                                                                        // archivo de
                                                                                                        // referencias

                    // Arreglo con la lista de referencias, Numero de referencias, Numero de Paginas
                    Object[] arregloConDatos = generarListaDeReferenciasYTamanios(rutaAlArchivo);

                    // Extraer los datos del arreglo
                    ArrayList<ArrayList<Object>> pageNumbersAndIO = (ArrayList<ArrayList<Object>>) arregloConDatos[0]; // First
                                                                                                                       // element:
                                                                                                                       // list
                                                                                                                       // of
                                                                                                                       // page
                                                                                                                       // numbers
                                                                                                                       // and
                                                                                                                       // io
                                                                                                                       // operations

                    // Iniciar RAM
                    RAM ram = new RAM(numeroMarcosPagina);

                    // Iniciamos tablas
                    TablaDePaginas tablaDePaginas = new TablaDePaginas();

                    // flag para indicar al thread 2, cuándo debe parar. Cuando el thread 1 termine
                    // de leer las referencias, marca este flag como false
                    FlagHayMasRefencias flagHayMasReferencias = new FlagHayMasRefencias(true);

                    // Utilizo barrera para que se muestre el menú cuando ya todos los threads
                    // terminen
                    CyclicBarrier barrera = new CyclicBarrier(3);

                    // Thread que se encarga de resolver las referencias
                    Thread1 thread1 = new Thread1(flagHayMasReferencias, tablaDePaginas, ram,
                            pageNumbersAndIO, barrera);
                    // Thread que actualiza el bit R de cada entrada
                    Thread2 thread2 = new Thread2(flagHayMasReferencias, tablaDePaginas, barrera);

                    thread1.start();
                    thread2.start();

                    barrera.await();

                } else if (opcion == 3) {
                    System.out.println("Nombre del archivo con la imagen a procesar: ");
                    String rutaImagen = br.readLine();
                    rutaImagen = "src/imgs/" + rutaImagen + ".bmp";

                    System.out.println("Nombre del archivo con el mensaje a esconder: ");
                    String rutaMensaje = br.readLine();
                    rutaMensaje = "src/msjs/" + rutaMensaje + ".txt";

                    System.out.println("Nombre de la imagen a crear con el mensaje escondido: ");
                    String rutaSalida = br.readLine();
                    rutaSalida = "src/imgs/" + rutaSalida + ".bmp";
                    esconderMensajeEnImagen(rutaImagen, rutaMensaje, rutaSalida);
                } else if (opcion == 4) {
                    recuperarMensajeDeImagen();
                } else if (opcion == 5) {

                    System.out.println("Ingrese el tamaño página a usar ej (256, 512, 1024): ");
                    int tamanioPagina = Integer.parseInt(br.readLine());

                    // Configuración de los tamaños de imagen, mensajes y marcos de página
                    int[] tamaniosMensaje = { 100, 1000, 2000, 4000, 8000 };
                    int[] tamaniosImagen = { 256, 426 }; // Ejemplo de tamaños de imagen en píxeles
                    int[] marcosDePagina = { 4, 8 };

                    // Ruta del archivo de resultados
                    String rutaArchivoResultados = "resultados_pruebas" + tamanioPagina + ".csv";

                    // Almacenar los resultados en una lista o matriz
                    List<String> resultados = new ArrayList<>();

                    try (FileWriter writer = new FileWriter(rutaArchivoResultados)) {
                        // Escribir la cabecera del archivo CSV
                        writer.write(
                                "Pagesize (bytes),Imagen (px),Mensaje (chars),# Marcos,# Hits,# Misses,% de Hits,% de Misses,Tiempo real (ns),Tiempo en RAM (ns),Tiempo en Swap (ns)\n");

                        // Iterar sobre los escenarios
                        for (int tamanioImagen : tamaniosImagen) {
                            for (int tamanioMensaje : tamaniosMensaje) {
                                for (int marcos : marcosDePagina) {

                                    System.out.println("Ejecutando escenario:pagesize= " + tamanioPagina + "Imagen="
                                            + tamanioImagen + "px, Mensaje="
                                            + tamanioMensaje + " chars, Marcos=" + marcos);

                                    // Busca la imagen sin modificar
                                    String rutaImagen = "src/imgs/imagen_" + tamanioImagen + ".bmp";
                                    String rutaImagen_mod = "src/imgs/imagen_" + tamanioImagen + "_mod.bmp";
                                    // busca el mensaje correspondiente al tamaño
                                    String rutaMensaje = "src/msjs/mensaje_" + tamanioMensaje + ".txt";

                                    // Esconder mensaje en la imagen
                                    esconderMensajeEnImagen(rutaImagen, rutaMensaje, rutaImagen_mod);

                                    // Generar referencias para este escenario
                                    String archivoReferencias = "src/referencias/referencia_" + tamanioImagen + "_"
                                            + tamanioMensaje + "_" + marcos + ".txt";
                                    generarReferencias(tamanioPagina, rutaImagen_mod, archivoReferencias);

                                    // Leer referencias generadas
                                    Object[] arregloConDatos = generarListaDeReferenciasYTamanios(archivoReferencias);
                                    // Extraer los datos del arreglo
                                    ArrayList<ArrayList<Object>> pageNumbersAndIO = (ArrayList<ArrayList<Object>>) arregloConDatos[0]; // First
                                                                                                                                       // element:
                                                                                                                                       // list
                                                                                                                                       // of
                                                                                                                                       // page
                                                                                                                                       // numbers
                                                                                                                                       // and
                                                                                                                                       // io
                                                                                                                                       // operations

                                    RAM ram = new RAM(marcos);

                                    TablaDePaginas tablaDePaginas = new TablaDePaginas();

                                    FlagHayMasRefencias flagHayMasReferencias = new FlagHayMasRefencias(true);

                                    CyclicBarrier barrera = new CyclicBarrier(3);

                                    Thread1 thread1 = new Thread1(flagHayMasReferencias, tablaDePaginas, ram,
                                            pageNumbersAndIO, barrera);
                                    // Thread que actualiza el bit R de cada entrada
                                    Thread2 thread2 = new Thread2(flagHayMasReferencias, tablaDePaginas, barrera);

                                    thread1.start();
                                    thread2.start();

                                    try {
                                        barrera.await(); // Esperamos que los hilos terminen

                                        // Guardar resultados
                                        String resultado = tamanioPagina + ","
                                                + tamanioImagen + "," + tamanioMensaje
                                                + "," + marcos  + "," + thread1.getHits()
                                                + "," + thread1.getMisses()
                                                + "," + thread1.getPorcentajeHits()
                                                + "," + thread1.getPorcentajeMisses()
                                                + "," + thread1.getTiempoTotal()
                                                + "," + thread1.getTiempoTodoEnRam()
                                                + "," + thread1.getTiempoTodoEnSWAP() + "\n";
                                        resultados.add(resultado);
                                        writer.write(resultado);
                                        writer.flush(); // Asegurar que se escriban los datos en el archivo

                                    } catch (InterruptedException | BrokenBarrierException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        // Exportar resultados a un archivo CSV
                        exportarResultadosAArchivo(resultados, "src/resultados/resultados_pruebas.csv");
                    }
                } else if (opcion == 0) {
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

    public static void exportarResultadosAArchivo(List<String> resultados, String rutaArchivo) {
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            writer.write("Escenario, Hits, Misses, Tiempo (ns)\n"); // Cabecera

            for (String resultado : resultados) {
                writer.write(resultado + "\n");
            }

            System.out.println("Resultados exportados correctamente a: " + rutaArchivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
