import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Thread1 extends Thread {
    
    /**
     * flag que nos dice si quedan más referencias en la lista de referencias
     */
    final CyclicBarrier barrera;
    final FlagHayMasRefencias flagHayMasReferencias;
    final TablaDePaginas tablaDePaginas;
    final RAM ram;
    final ArrayList<ArrayList<Object>> pageNumbersAndIO;
    private int hits = 0;
    private int misses = 0;
    private long tiempoTotal = 0;  // Tiempo en nanosegundos


    public Thread1(FlagHayMasRefencias flagHayMasReferencias, TablaDePaginas tablaDePaginas, RAM ram, ArrayList<ArrayList<Object>> pageNumbersAndIO, CyclicBarrier barrera){
        this.flagHayMasReferencias = flagHayMasReferencias;
        this.tablaDePaginas = tablaDePaginas;
        this.ram = ram;
        this.pageNumbersAndIO = pageNumbersAndIO;
        this.barrera = barrera;
    }
    
    @Override
    public void run(){
        for(ArrayList<Object> pageNumberAndIO: pageNumbersAndIO){
            
            int page = (int) pageNumberAndIO.get(0);
            char IOOperation = (char) pageNumberAndIO.get(1);
            int indiceMarcoDePagina = tablaDePaginas.buscarMarcoPagina(page,IOOperation);
            
            // si !=-1 no ocurre fallo de página
            if(indiceMarcoDePagina != -1){
                
                hits++;
                tiempoTotal += 25;  // Tiempo de acceso en caso de hit (25 ns)

            //Si es -1 ocurrió falló de página
            } else {
                
                misses++;
                tiempoTotal += 10_000_000;  // Tiempo de acceso en caso de miss (10 ms)

                // Variable que guarda el indice de marco pagina libre una vez sea retornado por el NRU o si la RAM está vacía se retorna la siguiente entrada vacía en orden
                int indiceMarcoPaginaLibre;
                

                if (!ram.estaLlena()){

                    indiceMarcoPaginaLibre = ram.nextMarcoDePaginaLibre();
                    ram.addMarcoDePagina(indiceMarcoPaginaLibre, page);

                } else {
                    //Aplicar NRU, el método pageIndexToRemove calcula el indice a remover según NRU:
                    indiceMarcoPaginaLibre = 0;
                    System.out.print("Pagina a remover según NRU: ");
                    System.out.println(indiceMarcoPaginaLibre);


                    ram.replaceMarcoDePagina(indiceMarcoPaginaLibre, page);
                    

                }

                //si la ram está llena o no, actualizo la TP, no hay necesidad de actualizar la TA
                tablaDePaginas.addEntrada(page, indiceMarcoPaginaLibre, IOOperation);

            }


            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //cuando termine actualizamos flag
        flagHayMasReferencias.setflagHayMasReferencias(false);
        System.out.print("Hits :");
        System.out.println(hits);
        System.out.print("Misses :");
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