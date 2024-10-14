public class FlagHayMasRefencias {
    
    /**
     * true si quedan más referencias en la lista de refencias, false de lo contrario.
     */
    private boolean flagHayMasReferencias;

    /**
     * Método para crear un flag. Si es falso significa que no quedan más referencias en la lista de referencias
     * Este flag es importante ya que determina cuándo debe parar el thread2
     * @param flagHayMasReferencias: boolean con el que se inicia el flag.
     */
    public FlagHayMasRefencias(boolean flagHayMasReferencias){
        this.flagHayMasReferencias = flagHayMasReferencias;
    }

    /**
     * Método para setear el flag. Es sincronizado porque el thread 1 y 2 de la opción 2 van a acceder a este flag.
     * @param value
     */
    public synchronized void setflagHayMasReferencias(boolean value){
        this.flagHayMasReferencias = value;
    }

    /**
     * Método que retorna el flah. Es sincronizado porque el thread 1 y 2 de la opción 2 van a acceder a este flag.
     * @return
     */
    public synchronized boolean getflagHayMasReferencias(){
        return flagHayMasReferencias;
    }

}
