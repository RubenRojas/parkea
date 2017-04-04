package cl.telios.parkea.Classes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cl.telios.parkea.R;

/**
 * Created by Alvaro on 04-04-2017.
 */

public class AdapterListadoOcupados extends RecyclerView.Adapter<AdapterListadoOcupados.OcupadosViewHolder> {

    List<Registro> ocupados;
    public static class OcupadosViewHolder extends RecyclerView.ViewHolder {

        TextView ficha, hora_ingreso, patente, tiempo_parcial;

        OcupadosViewHolder(View itemView) {
            super(itemView);

            ficha = (TextView)itemView.findViewById(R.id.ficha);
            hora_ingreso = (TextView)itemView.findViewById(R.id.hora_ingreso);
            patente = (TextView)itemView.findViewById(R.id.patente);
            tiempo_parcial = (TextView) itemView.findViewById(R.id.tiempo_parcial);

        }
    }
    public AdapterListadoOcupados(List<Registro> ocupados){
        this.ocupados = ocupados;
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    @Override
    public OcupadosViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_listado_ocupados, viewGroup, false);

        OcupadosViewHolder pvh = new OcupadosViewHolder(v);
        return pvh;
    }
    @Override
    public void onBindViewHolder(OcupadosViewHolder ocupadoViewHolder, int i) {
        Registro tmp = ocupados.get(i);
        ocupadoViewHolder.ficha.setText(tmp.getEtiqueta());
        ocupadoViewHolder.hora_ingreso.setText(tmp.getHora_inicio());
        ocupadoViewHolder.patente.setText(tmp.getPatente());
        ocupadoViewHolder.tiempo_parcial.setText(tmp.getTiempo_parcial());

    }
    @Override
    public int getItemCount() {
        try{
            return ocupados.size();
        }
        catch(Exception e){
            //LogViewer.d("Develop", e.getMessage().toString());
            return 0;
        }
    }
}

