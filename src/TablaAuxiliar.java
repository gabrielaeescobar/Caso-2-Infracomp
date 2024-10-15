public class TablaAuxiliar {

    private int[] tablaAuxiliar;


    public TablaAuxiliar(int tamanio){

        this.tablaAuxiliar = new int[tamanio];
        
        for (int i = 0; i < tamanio; i++){
            // la tabla auxiliar se va a guardar en orden, es decir, que la página 0 debería estar en la página 0 de la SWAP.
            tablaAuxiliar[i] = i;
        }
    }

    public int buscarPagina(int indicePagina){
        return tablaAuxiliar[indicePagina];
    }
}
