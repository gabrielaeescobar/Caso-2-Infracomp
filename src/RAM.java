public class RAM {
    
    private int[] marcosDePagina;
    private int marcosOcupados = 0;
    private final int tamanio;

    public RAM(int tamanio){
        this.tamanio = tamanio;
        this.marcosDePagina = new int[tamanio];
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
     * Reemplaza el contendio de marco de página con índice "indiceMarcoDePagina" por "pagina"
     * @param indiceMarcoDePagina
     * @param pagina
     */
    public void replaceMarcoDePagina(int indiceMarcoDePagina, int pagina){        
        marcosDePagina[indiceMarcoDePagina] = pagina;
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
