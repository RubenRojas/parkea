package cl.telios.parkea;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Salida extends AppCompatActivity {
    Button salida, cancelar;
    TextView ficha, hora_ingreso, hora_salida, tiempo_total, valor, fecha;
    MaterialSpinner descuento;
    ImageButton camara;
    String msg;
    Operador op;
    SQLiteDatabase bd;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salida);
        getSupportActionBar().hide();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,
                "PARKEA", null, Integer.parseInt(getString(R.string.database_version)));
        bd = admin.getWritableDatabase();
        op = Operador.getOperador(bd);

        ficha = (TextView) findViewById(R.id.ficha);
        ficha.setText(getIntent().getStringExtra("ficha"));

        hora_ingreso = (TextView) findViewById(R.id.hora_ingreso);
        hora_ingreso.setText(getIntent().getStringExtra("hora_inicio"));

        hora_salida = (TextView) findViewById(R.id.hora_salida);
        hora_salida.setText(getIntent().getStringExtra("hora_termino"));

        fecha = (TextView) findViewById(R.id.fecha);
        fecha.setText(getIntent().getStringExtra("fecha"));

        tiempo_total = (TextView) findViewById(R.id.tiempo_total);
        tiempo_total.setText(getIntent().getStringExtra("tiempo_total"));

        valor = (TextView) findViewById(R.id.valor);
        valor.setText(getIntent().getStringExtra("valor"));

        camara = (ImageButton) findViewById(R.id.camara);

        descuento = (MaterialSpinner) findViewById(R.id.descuento);
        descuento.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                if(position != 0) {
                    new calcularDescuento().execute(op.getId_emp(), getIntent().getStringExtra("tiempo_total"), getIntent().getStringExtra("valor"), item);
                }
                else{
                    tiempo_total.setText(getIntent().getStringExtra("tiempo_total"));
                    valor.setText(getIntent().getStringExtra("valor"));
                    camara.setVisibility(View.GONE);
                }
                //Toast.makeText(Salida.this, "item->"+item+"  position->"+position+"id-> "+id, Toast.LENGTH_SHORT).show();
            }
        });

        salida = (Button)findViewById(R.id.salida);
        salida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SalidaVehiculo().execute(getIntent().getStringExtra("codigo"),op.getId_parking(), getIntent().getStringExtra("hora_termino"), (String)valor.getText(), op.getId_turno(), (String)descuento.getText(), op.getId_emp());
            }
        });

        cancelar = (Button) findViewById(R.id.cancelar);
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Salida.this);
                alertDialogBuilder.setMessage("¿Desea cancelar la salida?");
                alertDialogBuilder.setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(Salida.this, Main.class);
                                startActivity(intent);
                                Salida.this.finish();
                            }
                        });

                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CargarDescuentos().execute(op.getId_emp());
    }

    private class calcularDescuento extends AsyncTask<String, Void, Void> {
        ArrayList<String> campos;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Salida.this);
            pDialog.setMessage("Calculando descuento...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... params) {
            WebRequest webreq = new WebRequest();
            // Making a request to url and getting response
            String tmp = params[3].replace(" ", "_");
            String URL = "http://pruebas.parkea.cl/parkea/android/calcularDescuento.php?id_empresa="+params[0]+"&tiempo_total="+params[1]+"&valor="+params[2]+"&nombre="+tmp+"";
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);
            try {
                campos = actualizarDatosJSON(jsonStr);
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
                if(campos == null){
                    //android.util.Log.d("Develop", "datos incorectos");
                    new AlertDialog.Builder(Salida.this)
                            .setTitle("Error")
                            .setMessage("Ha ocurrido un error.")

                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //Login.this.finish();
                                }
                            }).create().show();
                }else{
                    tiempo_total.setText(campos.get(0));
                    valor.setText(campos.get(1));
                    if(campos.get(2).equals("si")){
                        camara.setVisibility(View.VISIBLE);
                    }
                    else{
                        camara.setVisibility(View.GONE);
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                //android.util.Log.d("Develop", "exception");
                //android.util.Log.d("Develop", e.getMessage());
                new AlertDialog.Builder(Salida.this)
                        .setTitle("Problema de Conexion")
                        .setMessage("Hubo un problema al intentar conectarse a Internet.")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Salida.this.finish();
                            }
                        }).create().show();
            }


        }
    }

    private ArrayList<String> actualizarDatosJSON(String json) throws JSONException {
        ArrayList<String> listado = new ArrayList<String>();
        if (json != null) {
            JSONObject jsonObj = new JSONObject(json);
            String result = jsonObj.getString("result"); //success o no-data
            android.util.Log.d("Develop", "result->>"+result);
            if(result.equals("success")){
                listado.add(jsonObj.getString("tiempo_nuevo"));
                listado.add(jsonObj.getString("valor_nuevo"));
                listado.add(jsonObj.getString("foto"));
            }
            return listado;
        }
        else{
            //android.util.Log.d("Develop", "json null");
            return null;
        }
    }

    private class CargarDescuentos extends AsyncTask<String, Void, Void> {
        ArrayList<String> listadoDescuentos;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Salida.this);
            pDialog.setMessage("Cargando...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... params) {
            WebRequest webreq = new WebRequest();
            // Making a request to url and getting response
            String URL = "http://pruebas.parkea.cl/parkea/android/getDescuentos.php?id_empresa="+params[0];
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);
            try {
                listadoDescuentos = descuentosJSON(jsonStr);
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
                if(listadoDescuentos == null){
                    //android.util.Log.d("Develop", "datos incorectos");
                    new AlertDialog.Builder(Salida.this)
                            .setTitle("Error")
                            .setMessage("Ha ocurrido un error.")

                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //Login.this.finish();
                                }
                            }).create().show();
                }else{
                    descuento.setItems(listadoDescuentos);
                }
            }
            catch (Exception e){
                e.printStackTrace();
                //android.util.Log.d("Develop", "exception");
                //android.util.Log.d("Develop", e.getMessage());
                new AlertDialog.Builder(Salida.this)
                        .setTitle("Problema de Conexion")
                        .setMessage("Hubo un problema al intentar conectarse a Internet.")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Salida.this.finish();
                            }
                        }).create().show();
            }


        }
    }

    private ArrayList<String> descuentosJSON(String json) throws JSONException {
        ArrayList<String> listado = new ArrayList<String>();
        listado.add("Sin descuento");
        if (json != null) {
            JSONObject jsonObj = new JSONObject(json);
            String result = jsonObj.getString("result"); //success o no-data
            android.util.Log.d("Develop", "result->>"+result);
            JSONArray r = jsonObj.getJSONArray("registros");
            if(result.equals("success")){
                for (int i = 0; i < r.length(); i++) {
                    JSONObject emp = r.getJSONObject(i);
                    String tmp = emp.getString("nombre");
                    listado.add(tmp);
                }

            }
            return listado;
        }
        else{
            //android.util.Log.d("Develop", "json null");
            return null;
        }
    }

    private class SalidaVehiculo extends AsyncTask<String, Void, Void> {
        String response;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Salida.this);
            pDialog.setMessage("Guardando registro...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... params) {
            WebRequest webreq = new WebRequest();
            String tmp = params[5].replace(" ", "_");
            // Making a request to url and getting response
            String URL = "http://pruebas.parkea.cl/parkea/android/salidaVehiculo.php?codigo="+params[0]+"&id_parking="+params[1]+"&hora_termino="+params[2]+"&valor="+params[3]+"&id_turno="+params[4]+"&nombre="+tmp+"&id_empresa="+params[6];
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);
            try {
                response = responseJSON(jsonStr);
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
                if(response == null){
                    //android.util.Log.d("Develop", "datos incorectos");
                    new AlertDialog.Builder(Salida.this)
                            .setTitle("Error")
                            .setMessage("Ha ocurrido un error.")

                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //Login.this.finish();
                                }
                            }).create().show();
                }else{
                    Intent intent = new Intent(Salida.this, Main.class);
                    startActivity(intent);
                    Toast.makeText(Salida.this, msg, Toast.LENGTH_SHORT).show();
                    Salida.this.finish();
                }
            }
            catch (Exception e){
                e.printStackTrace();
                //android.util.Log.d("Develop", "exception");
                //android.util.Log.d("Develop", e.getMessage());
                new AlertDialog.Builder(Salida.this)
                        .setTitle("Problema de Conexion")
                        .setMessage("Hubo un problema al intentar conectarse a Internet.")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Salida.this.finish();
                            }
                        }).create().show();
            }


        }
    }

    private String responseJSON(String json) throws JSONException {
        if (json != null) {
            JSONObject jsonObj = new JSONObject(json);
            String result = jsonObj.getString("result"); //success o no-data
            android.util.Log.d("Develop", "result->>"+result);
            if(result.equals("success")){
                //android.util.Log.d("Develop", "result success");
                msg = jsonObj.getString("mensaje");
                return "true";
            } else {
                //android.util.Log.d("Develop", "result no-data");
                msg = jsonObj.getString("mensaje");
                //android.util.Log.d("Develop", "retornando mensaje");
                //android.util.Log.d("Develop", du.toString());
                return "false";
            }
        }
        else{
            //android.util.Log.d("Develop", "json null");
            return null;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
