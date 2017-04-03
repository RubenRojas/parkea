package cl.telios.parkea.Classes;

/**
 * Created by Alvaro on 31-03-2017.
 */

public class Registro {
    private String hora_inicio;
    private String hora_termino;
    private String tiempo_total;
    private String valor;
    private String id_parking_espacio;

    public Registro() {}

    public String getHora_inicio() {
        return hora_inicio;
    }

    public void setHora_inicio(String hora_inicio) {
        this.hora_inicio = hora_inicio;
    }

    public String getHora_termino() {
        return hora_termino;
    }

    public void setHora_termino(String hora_termino) {
        this.hora_termino = hora_termino;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getId_parking_espacio() {
        return id_parking_espacio;
    }

    public void setId_parking_espacio(String id_parking_espacio) {
        this.id_parking_espacio = id_parking_espacio;
    }

    public String getTiempo_total() {
        return tiempo_total;
    }

    public void setTiempo_total(String tiempo_total) {
        this.tiempo_total = tiempo_total;
    }
}
