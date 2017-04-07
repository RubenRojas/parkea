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
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Salida extends AppCompatActivity {
    Button salida, cancelar;
    TextView ficha, hora_ingreso, hora_salida, tiempo_total, valor, fecha;
    MaterialSpinner descuento;
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

        descuento = (MaterialSpinner) findViewById(R.id.descuento);
        descuento.setItems("Promoción","Convenio","Cliente habitual");

        salida = (Button)findViewById(R.id.salida);
        salida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SalidaVehiculo().execute(getIntent().getStringExtra("codigo"),op.getId_parking(), getIntent().getStringExtra("hora_termino"), (String)tiempo_total.getText(), op.getId_turno());
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
            // Making a request to url and getting response
            String URL = "http://pruebas.parkea.cl/parkea/android/salidaVehiculo.php?codigo="+params[0]+"&id_parking="+params[1]+"&hora_termino="+params[2]+"&tiempo_total="+params[3]+"&id_turno="+params[4];
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
