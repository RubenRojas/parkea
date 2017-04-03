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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login extends AppCompatActivity {
    Button ingresar;
    EditText rut, pass;
    SQLiteDatabase bd;
    String msg, tMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        rut = (EditText)findViewById(R.id.rut);
        pass = (EditText)findViewById(R.id.pass);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,
                "PARKEA", null, Integer.parseInt(getString(R.string.database_version)));
        bd = admin.getWritableDatabase();

        ingresar = (Button)findViewById(R.id.ingresar);
        ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strRut = rut.getText().toString();
                String strPass = pass.getText().toString();

                if(strRut.equals("") || strPass.equals("")){
                    Toast.makeText(Login.this, "Debe ingresar su rut y su clave.", Toast.LENGTH_LONG).show();
                    return;
                }
                new Validar().execute(strRut,strPass);

                /*Intent i = new Intent(Login.this, Main.class);
                startActivity(i);
                Login.this.finish();*/
            }
        });
    }

    private class Validar extends AsyncTask<String, Void, Void> {
        Operador operador;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Comprobando sus datos...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... params) {
            WebRequest webreq = new WebRequest();
            // Making a request to url and getting response
            String URL = "http://pruebas.parkea.cl/parkea/android/login.php?rut="+params[0]+"&pass="+params[1];
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);
            try {
                operador = ParseJSON(jsonStr);
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
                if(operador == null){
                    android.util.Log.d("Develop", "datos incorectos");
                    new AlertDialog.Builder(Login.this)
                            .setTitle(tMsg)
                            .setMessage(msg)

                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //Login.this.finish();
                                }
                            }).create().show();
                }else{
                    android.util.Log.d("Develop", "datos correctos");
                    Intent i = new Intent(Login.this,Main.class);
                    operador.insertOperador(bd);
                    startActivity(i);
                    Login.this.finish();
                }
            }
            catch (Exception e){
                e.printStackTrace();
                //android.util.Log.d("Develop", "exception");
                //android.util.Log.d("Develop", e.getMessage());
                new AlertDialog.Builder(Login.this)
                        .setTitle("Problema de Conexion")
                        .setMessage("Hubo un problema al intentar conectarse a Internet.")

                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Login.this.finish();
                            }
                        }).create().show();
            }


        }
    }


    private Operador ParseJSON(String json) throws JSONException {
        if (json != null) {
            JSONObject jsonObj = new JSONObject(json);
            String result = jsonObj.getString("result"); //success o no-data
            android.util.Log.d("Develop", "result->>"+result);
            if(result.equals("success")){
                JSONObject datos_usuario = jsonObj.getJSONObject("datos_usuario");
                android.util.Log.d("Develop", "result success");
                Operador op = new Operador(datos_usuario.getString("id"),datos_usuario.getString("id_emp"), datos_usuario.getString("rut"), datos_usuario.getString("nombre"), datos_usuario.getString("pass"), datos_usuario.getString("id_parking"),datos_usuario.getString("id_turno"));
                android.util.Log.d("Develop", "op->"+op.toString());
                return op;
            } else {
                android.util.Log.d("Develop", "result no-data");
                tMsg = jsonObj.getString("titulo_mensaje");
                msg = jsonObj.getString("mensaje");
                //android.util.Log.d("Develop", "retornando mensaje");
                //android.util.Log.d("Develop", du.toString());
                return null;
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
