public class TablaDePaginas {
    
    /**
     * La tabla de páginas que contiene las entradas. Cada entrada de la tabla de página es un arreglo de 4 entradas así: [pageFrameNumber,abscentPresentBit,Rbit,Mbit]
     * presentAbscentBit: If this bit is 1, the entry is valid and can be used. If it is 0, the virtual page to which the entry belongs
     * is not currently in memory.
     * pageFrameNumber: me dice en qué marco está la página que estoy buscando.
     */
    private int[][] tablaDePaginas;
    private int tamanio;

    /**
     * Crea una tabla con tamanio entradas.
     * @param tamanio
     */
    public TablaDePaginas(int tamanio){
        //Cada entrada de la tabla de página es un arreglo de 4 entradas así: [pageFrameNumber,presentAbscentBit,Rbit,Mbit]. Al iniciar todas los valores son 0.
        //Por lo que presentAbscentBit de todas las entradas va a ser 0 al iniciar.
        this.tamanio = tamanio;
        this.tablaDePaginas = new int[tamanio][4]; 
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
    public synchronized int buscarMarcoPagina(int indicePagina, char OperacionIO){
        //Si el presentAbscentBit es igual a 0, entonces la página que busco no está en RAM
        if (tablaDePaginas[indicePagina][1]==0){
            return -1;
        
        } else {
            //Si la operación fue de lectura, solo actualiza el bit R.
            if(OperacionIO =='R'){
                tablaDePaginas[indicePagina][2] = 1;
            
            //Si la operación fue de escritura actualizo el bit R y M.
            } else if (OperacionIO =='W'){
                tablaDePaginas[indicePagina][2] = 1;
                tablaDePaginas[indicePagina][3] = 1;
            }
            return tablaDePaginas[indicePagina][0];
        }
    }

    /**
     * Metodo que asigna un marco a una página en una TP.
     * @param indicePagina
     * @param indiceMarcoDePagina
     * @param OperacionIO: 'R' si es read, 'W' si es write.
     * @pos Asigna el indiceMarcoDePagina al indicePagina en la TP.
     */
    public synchronized void asignarMarcoAPagina(int indicePagina, int indiceMarcoDePagina, char OperacionIO){
        tablaDePaginas[indicePagina][0] = indiceMarcoDePagina;
        //actualizamos presentAbscentBit, con lo cual la entrada es válida.
        tablaDePaginas[indicePagina][1] = 1;
        
        //Si la operación fue de lectura, solo actualiza el bit R.
        if(OperacionIO =='R'){
            tablaDePaginas[indicePagina][2] = 1;
        
        //Si la operación fue de escritura actualizo el bit R y M.
        } else if (OperacionIO =='W'){
            tablaDePaginas[indicePagina][2] = 1;
            tablaDePaginas[indicePagina][3] = 1;
        }

    }

    /**
     * Método que limpia los bits R a 0. Es invocado por el thread 2.
     * @pos actualiza todos los bits R a 0
     */
    public synchronized void clearRbit(){
        //TODO Intentar optimizar para que no tenga que recorrer toda la tabla, porque al principio no hay necesidad
        for(int i = 0; i< tamanio; i++){
            tablaDePaginas[i][2] = 0;
        }
    }


    /**
     * This method returns the index of the page to remove based on the NRU algorithm.
     * It finds the page in the lowest nonempty class (class 0: [R=0, M=0], class 1: [R=0, M=1],
     * class 2: [R=1, M=0], class 3: [R=1, M=1]) and returns the lowest index page from that class.
     * 
     * @return index of the page to remove.
     */
    public synchronized int pageIndexToRemove() {
        int pageToRemove = -1;
        
        // Check class 0 (R=0, M=0)
        for (int i = 0; i < tamanio; i++) {
            if (tablaDePaginas[i][1] == 1 && tablaDePaginas[i][2] == 0 && tablaDePaginas[i][3] == 0) {
                return i;  // Return first match from class 0
            }
        }

        // Check class 1 (R=0, M=1)
        for (int i = 0; i < tamanio; i++) {
            if (tablaDePaginas[i][1] == 1 && tablaDePaginas[i][2] == 0 && tablaDePaginas[i][3] == 1) {
                return i;  // Return first match from class 1
            }
        }

        // Check class 2 (R=1, M=0)
        for (int i = 0; i < tamanio; i++) {
            if (tablaDePaginas[i][1] == 1 && tablaDePaginas[i][2] == 1 && tablaDePaginas[i][3] == 0) {
                return i;  // Return first match from class 2
            }
        }

        // Check class 3 (R=1, M=1)
        for (int i = 0; i < tamanio; i++) {
            if (tablaDePaginas[i][1] == 1 && tablaDePaginas[i][2] == 1 && tablaDePaginas[i][3] == 1) {
                return i;  // Return first match from class 3
            }
        }

        return pageToRemove;  // Return -1 if no page to remove is found (this shouldn't happen if the table is full)
    }


}
