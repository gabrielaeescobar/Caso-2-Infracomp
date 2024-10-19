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
    private double tiempoTodoEnRam;
    private double tiempoTodoEnSWAP;
    private double porcentajeHits;
    private double porcentajeMisses;
    



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
        System.out.print("Hits: ");
        System.out.println(hits);
        System.out.print("Misses: ");
        System.out.println(misses);

        //Porcentajes:

        int totalReferencias = hits+misses;
        
        porcentajeHits = ( (double) hits/totalReferencias)*100;
        porcentajeMisses = 100-porcentajeHits;
        System.out.print("Porcentaje hits: ");
        System.out.println(String.format("%.2f", porcentajeHits) + " %");
        System.out.print("Porcentaje misses: ");
        System.out.println(String.format("%.2f", porcentajeMisses) + " %");

        // Tiempo efectivo
        double tiempoMs = tiempoTotal/1_000_000.0;
        double tiempoS = tiempoMs/1_000.0;
        System.out.print("Tiempo Total (ns): ");
        System.out.println(tiempoTotal);
        System.out.print("Tiempo Total (ms): ");
        System.out.println(tiempoMs);
        System.out.print("Tiempo Total (segundos): ");
        System.out.println(tiempoS);

        //Tiempo si todas las referencias estuviesen en RAM y en SWAP
        tiempoTodoEnRam = (double)(totalReferencias*25)/1_000_000; // en ms
        tiempoTodoEnSWAP = totalReferencias*10; // en ms
        System.out.print("Tiempo si todas las referencias estuvieran en RAM: ");
        System.out.println(String.format("%.2f", tiempoTodoEnRam) + " ms");
        System.out.print("Tiempo si todas las referencias estuvieran en SWAP: ");
        System.out.println(tiempoTodoEnSWAP + " ms");

        
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

    public double getPorcentajeHits(){
        return porcentajeHits;
    }

    public double getPorcentajeMisses(){
        return porcentajeMisses;
    }

    public double getTiempoTodoEnRam(){
        return tiempoTodoEnRam;
    }

    public double getTiempoTodoEnSWAP(){
        return tiempoTodoEnSWAP;
    }
}