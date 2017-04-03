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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static cl.telios.parkea.R.id.confirmar;
import static cl.telios.parkea.R.id.start;
import static cl.telios.parkea.R.id.textView;

public class Ingreso extends AppCompatActivity {
    TextView hora, ficha;
    Button confirmar;
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

        hora = (TextView) findViewById(R.id.hora);
        hora.setText(getIntent().getStringExtra("hora_inicio"));
        /*String minutos = "";
        if(new Date().getMinutes()<10){
            minutos = "0"+String.valueOf(new Date().getMinutes());
        }
        else{
            minutos = String.valueOf(new Date().getMinutes());
        }
        String horas = String.valueOf(new Date().getHours());
        String currentDateTimeString = horas+":"+minutos;*/

        ficha = (TextView) findViewById(R.id.ficha);
        ficha.setText(getIntent().getStringExtra("codigo"));

        confirmar = (Button)findViewById(R.id.confirmar);
        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new IngresoVehiculo().execute(getIntent().getStringExtra("codigo"),op.getId_parking(),op.getId_turno());
            }
        });

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
            String URL = "http://pruebas.parkea.cl/parkea/android/ingresoVehiculo.php?codigo="+params[0]+"&id_parking="+params[1]+"&id_turno="+params[2];
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
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
