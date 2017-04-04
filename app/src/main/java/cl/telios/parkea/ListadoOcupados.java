package cl.telios.parkea;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cl.telios.parkea.Classes.AdapterListadoOcupados;
import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Classes.Registro;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;

public class ListadoOcupados extends AppCompatActivity {
    private Button volver;
    private TextView no_resultado;
    private List<Registro> ocupados = new ArrayList<>();
    private RecyclerView rv;
    private LinearLayoutManager llm;
    private AdapterListadoOcupados adapter;
    SQLiteDatabase bd;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_ocupados);
        getSupportActionBar().hide();

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        adapter = new AdapterListadoOcupados(ocupados);
        rv.setAdapter(adapter);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,
                "PARKEA", null, Integer.parseInt(getString(R.string.database_version)));
        bd = admin.getWritableDatabase();
        Operador op = Operador.getOperador(bd);

        no_resultado = (TextView) findViewById(R.id.no_resultado);

        volver = (Button) findViewById(R.id.volver);
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListadoOcupados.this, Main.class);
                startActivity(intent);
                ListadoOcupados.this.finish();
            }
        });

        new getOcupados().execute(op.getId_parking());
    }

    private class getOcupados extends AsyncTask<String, Void, Void> {
        private List<Registro> registros;
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ListadoOcupados.this);
            pDialog.setMessage("Obteniendo registros ... ");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... params) {
            WebRequest webreq = new WebRequest();
            // Making a request to url and getting response
            String URL = "http://pruebas.parkea.cl/parkea/android/getListadoOcupados.php?id_parking="+params[0];
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);
            try {
                registros = ParseJSON(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try{
                pDialog.dismiss();
                ocupados = registros;
                if(ocupados != null){

                    rv =(RecyclerView)findViewById(R.id.rv);
                    adapter = new AdapterListadoOcupados(ocupados);
                    llm = new LinearLayoutManager(ListadoOcupados.this);
                    rv.setLayoutManager(llm);
                    rv.setHasFixedSize(true);
                    rv.setAdapter(adapter);

                    no_resultado.setVisibility(View.GONE);
                }
                else{
                    no_resultado.setText("No se encontraron registros.");
                    no_resultado.setVisibility(View.VISIBLE);
                }
            }
            catch (Exception e){
                android.util.Log.d("Develop", "excepcion->"+e.getMessage());
                new AlertDialog.Builder(ListadoOcupados.this)
                        .setTitle("Problema de Conexion")
                        .setMessage("Hubo un problema al intentar conectarse a Internet.")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                ListadoOcupados.this.finish();
                            }
                        }).create().show();
            }


        }
    }
    private List<Registro> ParseJSON(String json) throws JSONException {
        List<Registro> listado = new ArrayList<>();
        boolean pass = false;
        if (json != null) {
            JSONObject jsonObj = new JSONObject(json);
            String result = jsonObj.getString("result"); //success o no-data
            JSONArray r = jsonObj.getJSONArray("registros");
            if(result.equals("success")){
                for (int i = 0; i < r.length(); i++) {
                    JSONObject emp = r.getJSONObject(i);
                    Registro tmp = new Registro();
                    tmp.setEtiqueta(emp.getString("ficha"));
                    tmp.setHora_inicio(emp.getString("hora_inicio"));
                    tmp.setPatente(emp.getString("patente"));
                    tmp.setTiempo_parcial(emp.getString("tiempo_parcial"));
                    listado.add(tmp);
                }
                pass = true;
            }

            if(pass){
                return listado;
            }
            else{
                return null;
            }

        }
        else {
            return null;
        }
    }
}
