import java.util.ArrayList;

public class TablaDePaginas {
    
    /**
     * La tabla de páginas que contiene las entradas. Cada entrada de la tabla de página es un arreglo de 4 entradas así: [pageNumber,pageFrameNumber,Rbit,Mbit]
     * pageFrameNumber: me dice en qué marco está la página con pageNumber que estoy buscando.
     */
    private ArrayList<ArrayList<Integer>> tablaDePaginas;

    /**
     * Crea una tabla con tamanio entradas.
     * @param tamanio
     */
    public TablaDePaginas(){
        this.tablaDePaginas = new ArrayList<ArrayList<Integer>>(); 
    }

    /**
     * Método que busca en qué marco de página de la RAM está una determinada página, y si encuentra la página en RAM, actualiza los bits R y M:
     * Si es una operación de lectura, solo actualiza el bit R, si es una de escritura, actualiza ambos bits.
     * Además es syncrhonized porque modifica y lee la tbala de páginas
     * 
     * @param indicePagina índice de la página que estoy buscando
     * @param OperacionIO 'R' si es read, 'W' si es write.
     * @return -1 si no está la página en RAM. de lo contrario el índice del marco de página en RAM
     * @pos Actualiza los bits R y M, si la página está en RAM.
     */
    public synchronized int buscarMarcoPagina(int pagina, char OperacionIO){

        boolean paginaEnRAM = false;
        // en entradaBuscada se guarda la entrada que se está buscando
        // Cada entrada de la tabla de página es un arreglo de 4 entradas así: [pageNumber,pageFrameNumber,Rbit,Mbit]
        ArrayList<Integer> entradaBuscada = new ArrayList<Integer>(4);

        // Se gaurdará el índice de la entrada que contiene la página que busco
        int i = 0;
        while (!paginaEnRAM && i < tablaDePaginas.size()) {
            
            ArrayList<Integer> entradaI = tablaDePaginas.get(i);
            
            //obtengo pageNumber de la entrada i y reviso si corresponde a la que estoy buscando
            if (entradaI.get(0).equals(pagina)) {
                paginaEnRAM = true;
                entradaBuscada = entradaI;
            }
            
            i++; // increment the counter at the end of each iteration
        }

        //Si la pagina está en la RAM actualizamos los bits dependiendo de la operación
        if (paginaEnRAM){
            i--; //se incrementó una vez más
            //Si la operación fue de lectura, solo actualiza el bit R (posición 2).
            if(OperacionIO =='R'){

                entradaBuscada.set(2,1);
            
            //Si la operación fue de escritura actualizo el bit R y M.
            } else if (OperacionIO =='W'){
                entradaBuscada.set(2,1);
                entradaBuscada.set(3,1);
            }

            tablaDePaginas.set(i,entradaBuscada);

            //retorno el indice del marco de página
            return entradaBuscada.get(1);
        
        //Si la página no está retornamos -1
        } else {
            return -1;
        }
    }

    /**
     * Metodo que asigna un marco a una página en una TP.
     * @param pagina
     * @param indiceMarcoDePagina
     * @param OperacionIO: 'R' si es read, 'W' si es write.
     * @pos Asigna el indiceMarcoDePagina al indicePagina en la TP.
     */
    public synchronized void addEntrada(int pagina, int indiceMarcoDePagina, char OperacionIO){
        //[pageNumber,pageFrameNumber,Rbit,Mbit]
        ArrayList<Integer> nuevaEntrada = new ArrayList<Integer>(4);

        nuevaEntrada.add(pagina);
        nuevaEntrada.add(indiceMarcoDePagina);
        
        //Si la operación fue de lectura, el bit R = 1, bit M = 0.
        if(OperacionIO =='R'){
            nuevaEntrada.add(1);
            nuevaEntrada.add(0);
        
        //Si la operación fue de escritura actualizo el bit R y M.
        } else if (OperacionIO =='W'){
            nuevaEntrada.add(1);
            nuevaEntrada.add(1);
        }

        tablaDePaginas.add(nuevaEntrada);

    }


    /**
     * Método que limpia los bits R a 0. Es invocado por el thread 2.
     * @pos actualiza todos los bits R a 0
     */
    public synchronized void clearRbit(){
        
        for(int i = 0; i< tablaDePaginas.size(); i++){
            ArrayList<Integer> entrada = tablaDePaginas.get(i);
            entrada.set(2,0);
            tablaDePaginas.set(i,entrada);
        }
    }

    /**
     * calcula el índice del marco de página a remover según el algoritmo NRU
     * @return el índice del marco de página a remover
     * @pos quita la entrada en la tabla de páginas asociada al marco de página calculado por NRU
     */
    public synchronized int frameToRemoveNRU() {
        // Create lists to store pages according to their class (0, 1, 2, 3)
        ArrayList<ArrayList<Integer>> class0 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> class1 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> class2 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> class3 = new ArrayList<>();
        
        // Classify pages based on R and M bits
        for (ArrayList<Integer> entrada : tablaDePaginas) {
            int Rbit = entrada.get(2);
            int Mbit = entrada.get(3);
            
            if (Rbit == 0 && Mbit == 0) {
                class0.add(entrada);
            } else if (Rbit == 0 && Mbit == 1) {
                class1.add(entrada);
            } else if (Rbit == 1 && Mbit == 0) {
                class2.add(entrada);
            } else if (Rbit == 1 && Mbit == 1) {
                class3.add(entrada);
            }
        }
        
        
        // Try to find the page to remove from the lowest non-empty class
        ArrayList<Integer> pageToRemove = null;
        
        if (!class0.isEmpty()) {
            pageToRemove = findLowestPageNumber(class0);
        } else if (!class1.isEmpty()) {
            pageToRemove = findLowestPageNumber(class1);
        } else if (!class2.isEmpty()) {
            pageToRemove = findLowestPageNumber(class2);
        } else if (!class3.isEmpty()) {
            pageToRemove = findLowestPageNumber(class3);
        }
    
        // If no pages are found, return -1 (though in practice, this shouldn't happen)
        if (pageToRemove == null) {
            return -1;
        }
        
        // Get the frame number to remove (second element of the page)
        int frameToRemove = pageToRemove.get(1);
        
        // Remove the page from the page table
        tablaDePaginas.remove(pageToRemove);
        
        // Return the frame number of the page to remove
        return frameToRemove;
    }

    // Helper function to find the entry with the lowest page number
    private ArrayList<Integer> findLowestPageNumber(ArrayList<ArrayList<Integer>> classList) {
        ArrayList<Integer> lowestPage = null;
        for (ArrayList<Integer> entry : classList) {
            if (lowestPage == null || entry.get(0) < lowestPage.get(0)) {
                lowestPage = entry;
            }
        }
        return lowestPage;
    }
    
}
