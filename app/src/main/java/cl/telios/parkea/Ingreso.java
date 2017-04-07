package cl.telios.parkea;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Classes.TipoAuto;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static cl.telios.parkea.R.id.confirmar;
import static cl.telios.parkea.R.id.start;
import static cl.telios.parkea.R.id.textView;

public class Ingreso extends AppCompatActivity {
    TextView hora, ficha, fecha;
    EditText patente;
    Button confirmar, cancelar;
    MaterialSpinner tipos_vehiculo;
    Operador op;
    SQLiteDatabase bd;
    String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);
        getSupportActionBar().hide();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,
                "PARKEA", null, Integer.parseInt(getString(R.string.database_version)));
        bd = admin.getWritableDatabase();
        op = Operador.getOperador(bd);

        ficha = (TextView) findViewById(R.id.ficha);
        ficha.setText(getIntent().getStringExtra("ficha"));

        fecha = (TextView)findViewById(R.id.fecha);
        fecha.setText(getIntent().getStringExtra("fecha"));
        android.util.Log.d("Develop", "fecha en intent->>" + getIntent().getStringExtra("fecha"));

        hora = (TextView) findViewById(R.id.hora);
        hora.setText(getIntent().getStringExtra("hora_inicio"));

        tipos_vehiculo = (MaterialSpinner) findViewById(R.id.tipo_vehiculo);
        //tipos_vehiculo.setItems("Auto", "Camión", "Motocicleta");
        /*tipos_vehiculo.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
            }
        });*/

        patente = (EditText) findViewById(R.id.patente);

        confirmar = (Button)findViewById(R.id.confirmar);
        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new IngresoVehiculo().execute(getIntent().getStringExtra("codigo"),op.getId_parking(), patente.getText().toString(), tipos_vehiculo.getText().toString());
            }
        });

        cancelar = (Button) findViewById(R.id.cancelar);
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Ingreso.this);
                alertDialogBuilder.setMessage("¿Desea cancelar el ingreso?");
                alertDialogBuilder.setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(Ingreso.this, Main.class);
                                startActivity(intent);
                                Ingreso.this.finish();
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
        new CargarTiposVehiculos().execute(op.getId_emp());
    }

    private class CargarTiposVehiculos extends AsyncTask<String, Void, Void> {
        ArrayList<String> listadoTiposAuto;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Ingreso.this);
            pDialog.setMessage("Cargando...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... params) {
            WebRequest webreq = new WebRequest();
            // Making a request to url and getting response
            String URL = "http://pruebas.parkea.cl/parkea/android/getTiposAuto.php?id_empresa="+params[0];
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);
            try {
                listadoTiposAuto = tiposAutoJSON(jsonStr);
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
                if(listadoTiposAuto == null){
                    //android.util.Log.d("Develop", "datos incorectos");
                    new AlertDialog.Builder(Ingreso.this)
                            .setTitle("Error")
                            .setMessage("Ha ocurrido un error.")

                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //Login.this.finish();
                                }
                            }).create().show();
                }else{
                    if(listadoTiposAuto.isEmpty()){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Ingreso.this);
                        alertDialogBuilder.setMessage(msg);
                        alertDialogBuilder.setPositiveButton("Aceptar",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Intent intent = new Intent(Ingreso.this, Main.class);
                                        startActivity(intent);
                                        Ingreso.this.finish();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                    else{
                        tipos_vehiculo.setItems(listadoTiposAuto);
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                //android.util.Log.d("Develop", "exception");
                //android.util.Log.d("Develop", e.getMessage());
                new AlertDialog.Builder(Ingreso.this)
                        .setTitle("Problema de Conexion")
                        .setMessage("Hubo un problema al intentar conectarse a Internet.")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Ingreso.this.finish();
                            }
                        }).create().show();
            }


        }
    }

    private class IngresoVehiculo extends AsyncTask<String, Void, Void> {
        String response;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Ingreso.this);
            pDialog.setMessage("Guardando registro...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... params) {
            WebRequest webreq = new WebRequest();
            // Making a request to url and getting response
            android.util.Log.d("Develop","patente->"+params[2]);
            String urlPatente = "";
            if(!params[2].isEmpty()){
                urlPatente = "&patente="+params[2];
            }
            String URL = "http://pruebas.parkea.cl/parkea/android/ingresoVehiculo.php?codigo="+params[0]+"&id_parking="+params[1]+"&tipo_vehiculo="+params[3];
            URL += urlPatente;
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
                    new AlertDialog.Builder(Ingreso.this)
                            .setTitle("Error")
                            .setMessage("Ha ocurrido un error.")

                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //Login.this.finish();
                                }
                            }).create().show();
                }else{
                    Intent intent = new Intent(Ingreso.this, Main.class);
                    startActivity(intent);
                    Toast.makeText(Ingreso.this, msg, Toast.LENGTH_SHORT).show();
                    Ingreso.this.finish();
                }
            }
            catch (Exception e){
                e.printStackTrace();
                //android.util.Log.d("Develop", "exception");
                //android.util.Log.d("Develop", e.getMessage());
                new AlertDialog.Builder(Ingreso.this)
                        .setTitle("Problema de Conexion")
                        .setMessage("Hubo un problema al intentar conectarse a Internet.")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Ingreso.this.finish();
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

    private ArrayList<String> tiposAutoJSON(String json) throws JSONException {
        ArrayList<String> listado = new ArrayList<String>();
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

            } else {
                //android.util.Log.d("Develop", "result no-data");
                msg = jsonObj.getString("mensaje");
            }
            return listado;
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
