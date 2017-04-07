package cl.telios.parkea.Classes;

/**
 * Created by Alvaro on 07-04-2017.
 */

public class TipoAuto {

    private String id;
    private String nombre;
    private String porcentaje;

    public TipoAuto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(String porcentaje) {
        this.porcentaje = porcentaje;
    }
}
