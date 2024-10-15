import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Thread2 extends Thread {
    /**
     * flag que nos dice si quedan más referencias en la lista de referencias
     */
    final FlagHayMasRefencias flagHayMasReferencias;
    /**
     * La tabla de páginas del proceso
     */
    TablaDePaginas tablaDePaginas;

    final CyclicBarrier barrera;
    
    /**
     * Constructor 
     * @param flagHayMasReferencias: flag que me dice si hay más referencias, este thread se va a ajecutar hasta que este flag sea falso
     * @param tablaDePaginas: la tabla de páginas del proceso
     */
    public Thread2(FlagHayMasRefencias flagHayMasReferencias, TablaDePaginas tablaDePaginas, CyclicBarrier barrera){
        this.flagHayMasReferencias = flagHayMasReferencias;
        this.tablaDePaginas = tablaDePaginas;
        this.barrera = barrera;
    }


    @Override
    /**
     * Este método hace clear del bit R cada 2 milisegundos hasta que no haya más referencias en la lista de referencias.
     */
    public void run(){

        while(flagHayMasReferencias.getflagHayMasReferencias()){
            
            //actualizamos todos los bit R a 0.
            tablaDePaginas.clearRbit();

            // Esperamos dos milisegundos y seguimos
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            barrera.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    




}
