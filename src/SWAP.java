public class SWAP {
    private int [] paginas;
    private int tamanio;

/**
 * Constructor de la SWAP, crea un arreglo del tamanio especificado y con sus entradas igual a 0
 * @param tamanio
 */
    public SWAP(int tamanio){
        this.tamanio = tamanio;
        this.paginas = new int[tamanio];
    }

    public void savePagina(int indicePagina, int contenidoPagina) {
        paginas[indicePagina] = contenidoPagina;
    }

    /**
     * Método para transformar todas las entradas de las páginas en la SWAP a 1
     */
    public void cargarSWAP(){
        for(int i = 0; i < tamanio; i++){
            paginas[i] = 1;
        }
    }

    /**
     * Retorna el contenido de página. Este método se debería usar cuando haya un fallo de página
     * @param indicePagina
     * @return el contenido de una página
     */
    public int getPagina(int indicePagina){
        return paginas[indicePagina];
    }
}
