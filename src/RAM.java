public class RAM {
    
    private int[] marcosDePagina;
    private int marcosOcupados = 0;
    private final int tamanio;

    public RAM(int tamanio){
        this.tamanio = tamanio;
        this.marcosDePagina = new int[tamanio];
    }


    /**
     * Retorna el contenido de un marco de página.
     * @param indiceMarcoDePagina
     * @return el contenido de una página
     */
    public int getMarcoDePagina(int indiceMarcoDePagina){
        return marcosDePagina[indiceMarcoDePagina];
    }


    /**
     * Agrega una página al marco indicado
     * 
     * @param indiceMarcoDePagina
     * @param pagina
     * @pre la entrada en el indiceMarcoDePagina debería estar en 0, es decir, es la primera vez que se pone una página en el marco indicado o 
     * se llamó el método getAndRemoverMarcoDePagina anteriormente
     */

    public void addMarcoDePagina(int indiceMarcoDePagina, int pagina){
        // Pongo el contenido de una página en el marco indicado
        marcosDePagina[indiceMarcoDePagina] = pagina;
        marcosOcupados++;
    }


    /**
     * Libera el marco en el indiceMarcoDePagina indicado, y retorna el contenido.
     * @param indiceMarcoDePagina
     * @return El marco de página quitado.
     */
    public int getAndRemoverMarcoDePagina(int indiceMarcoDePagina){
        
        // Obtengo el marco de página que busco
        int marcoDePagina =  marcosDePagina[indiceMarcoDePagina];
        
        // libero el marcoDePagina poniendolo como 0
        marcosDePagina[indiceMarcoDePagina] = 0;
        marcosOcupados--;

        return marcoDePagina;

    }

    
    /**
     * Me dice si la RAM está llena.
     * Una vez este método retorne false no se debería volver a llamar, ya que sabemos que la RAM va a seguir llena.
     * 
     * @return true si sí está llena, false de lo contrario
     */
    public boolean estaLlena(){
        if (marcosOcupados==tamanio){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Devuelve la siguiente posición libre en la RAM en orden. Si está llena va a devolver el máximo tamaño de la RAM.
     * Este método se debe llamar si no se ha llenado la RAM, de lo contrario debe utilizar el algoritmo NRU.
     * 
     * @return el indice del marco de página libre si no está llena la RAM.
     * @pre la memoria debe tener al menos una posición libre.
     */
    public int nextMarcoDePaginaLibre(){
        //Guardamos en orden las páginas
        return marcosOcupados;
    }
    

}
