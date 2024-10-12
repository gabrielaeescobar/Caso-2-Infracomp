import java.util.ArrayList;

public class Pagina {

    private int idPagina;
    private ArrayList<Marco> marcos;

    public Pagina (int idPagina){
        this.marcos = new ArrayList<>(); 
        this.idPagina = idPagina;
    }

    public ArrayList<Marco> getMarcos(){
        return marcos;

    }
}
