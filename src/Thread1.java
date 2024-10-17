import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Thread1 extends Thread {
    
    /**
     * flag que nos dice si quedan más referencias en la lista de referencias
     */
    final CyclicBarrier barrera;
    final FlagHayMasRefencias flagHayMasReferencias;
    final TablaDePaginas tablaDePaginas;
    final TablaAuxiliar tablaAuxiliar;
    final SWAP swap;
    final RAM ram;
    final ArrayList<ArrayList<Object>> pageNumbersAndIO;
    final int numeroDeReferencias;
    private int hits = 0;
    private int misses = 0;
    private long tiempoTotal = 0;  // Tiempo en nanosegundos


    public Thread1(FlagHayMasRefencias flagHayMasReferencias, TablaDePaginas tablaDePaginas, TablaAuxiliar tablaAuxiliar, SWAP swap, RAM ram, ArrayList<ArrayList<Object>> pageNumbersAndIO, int numeroDeReferencias, CyclicBarrier barrera){
        this.flagHayMasReferencias = flagHayMasReferencias;
        this.tablaDePaginas = tablaDePaginas;
        this.tablaAuxiliar = tablaAuxiliar;
        this.swap = swap;
        this.ram = ram;
        this.pageNumbersAndIO = pageNumbersAndIO;
        //TODO revisar si lo necesito
        this.numeroDeReferencias = numeroDeReferencias;
        this.barrera = barrera;
    }
    
    @Override
    //TODO falta copiar una página a SWAP en caso de que el bit M diga que esté modificado, y la vaya a eliminar según NRU
    public void run(){
        for(ArrayList<Object> pageNumberAndIO: pageNumbersAndIO){
            
            int pageNumber = (int) pageNumberAndIO.get(0);
            char IOOperation = (char) pageNumberAndIO.get(1);
            int indiceMarcoDePagina = tablaDePaginas.buscarMarcoPagina(pageNumber,IOOperation);
            
            // si !=-1 no ocurre fallo de página
            if(indiceMarcoDePagina != -1){
                
                hits++;
                // System.out.print("Contenido página: ");
                // System.out.println(ram.getMarcoDePagina(indiceMarcoDePagina));
                tiempoTotal += 25;  // Tiempo de acceso en caso de hit (25 ns)

            //Si es -1 ocurrió falló de página
            } else {
                
                misses++;
                tiempoTotal += 10_000_000;  // Tiempo de acceso en caso de miss (10 ms)
                // voy a swap y traigo la pagina
                int indiceSWAP = tablaAuxiliar.buscarPagina(pageNumber);
                int paginaSWAP = swap.getPagina(indiceSWAP);

                // Variable que guarda el indice de marco pagina libre una vez sea retornado por el NRU o si la RAM está vacía se retorna la siguiente entrada vacía en orden
                int indiceMarcoPaginaLibre;
                

                if (!ram.estaLlena()){

                    indiceMarcoPaginaLibre = ram.nextMarcoDePaginaLibre();
                    ram.addMarcoDePagina(indiceMarcoPaginaLibre, paginaSWAP);
                    //retorno la página
                    // System.out.print("Contenido página: ");
                    // System.out.println(paginaSWAP);

                } else {
                    //Aplicar NRU, el método pageIndexToRemove calcula el indice a remover según NRU:
                    indiceMarcoPaginaLibre = tablaDePaginas.pageIndexToRemove();
                    // System.out.print("Indice a remover segun NRU: ");
                    // System.out.println(indiceMarcoPaginaLibre);
                    //TODO Pegar a SWAP si se modificó la pagina que voy a eliminar:
                    if (IOOperation == 'W') {
                        // Guardar la página en SWAP antes de eliminarla si fue modificada
                        swap.savePagina(indiceMarcoPaginaLibre, ram.getMarcoDePagina(indiceMarcoPaginaLibre));
                    }

                    ram.getAndRemoverMarcoDePagina(indiceMarcoPaginaLibre);
                    ram.addMarcoDePagina(indiceMarcoPaginaLibre, paginaSWAP);

                }

                //si la ram está llena o no, actualizo la TP, no hay necesidad de actualizar la TA
                tablaDePaginas.asignarMarcoAPagina(pageNumber, indiceMarcoPaginaLibre, IOOperation);

            }


            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Cuando termine, actualizamos flag
        flagHayMasReferencias.setflagHayMasReferencias(false);
        System.out.print("Hits : ");
        System.out.println(hits);
        System.out.print("Misses : ");
        System.out.println(misses);
        System.out.print("Tiempo Total (ns) : ");
        System.out.println(tiempoTotal);
        System.out.print("Tiempo Total (segundos) : ");
        System.out.println(tiempoTotal / 1_000_000_000.0);
        
        try {
            barrera.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public int getHits(){
        return hits;
    }

    public int getMisses(){
        return misses;
    }

    public long getTiempoTotal(){
        return tiempoTotal;
    }
}
