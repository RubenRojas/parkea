package cl.telios.parkea;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Classes.Registro;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class Scanner extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    String codigo = "";
    String destino = "";
    SQLiteDatabase bd;
    boolean auto_usado = false;
    Operador op;
    String msg = "Ha ocurrido un error.";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkCameraPermission();
            checkLocationPermission();
        }

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,
                "PARKEA", null, Integer.parseInt(getString(R.string.database_version)));
        bd = admin.getWritableDatabase();
        op = Operador.getOperador(bd);

        destino = getIntent().getStringExtra("tipo").toString();

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Scanner.this, Main.class));
        Scanner.this.finish();
    }

    @Override
    public void handleResult(Result rawResult) {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 400 milliseconds
        v.vibrate(400);
        // Do something with the result here
        //Log.v("Develop", rawResult.getText()); // Prints scan results
        //Log.v("Develop", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String codigoMovil = rawResult.getText();
        codigo = codigoMovil;

        new setMovil().execute(codigoMovil);

        // If you would like to resume scanning, call this method below:
        // resume();
    }

    public void resumeScan(){
        mScannerView.resumeCameraPreview(this);
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 99;
    public boolean checkLocationPermission(){

        //Location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION },MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    public boolean checkCameraPermission(){

        //Location
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA },MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
            return false;
        } else {
            return true;
        }
    }


    private class setMovil extends AsyncTask<String, Void, Void> {
        Registro reg;
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Scanner.this);
            pDialog.setMessage("Validando Código... ");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... passing) {
            //get data from view
            // Creating service handler class instance
            WebRequest webreq = new WebRequest();

            String URL;
            if(destino.equals("ingreso")) {
                // Making a request to url and getting response
                URL = "http://pruebas.parkea.cl/parkea/android/validarIngreso.php?codigo=" + passing[0] + "&id_parking=" + op.getId_parking();
            }else{
                URL = "http://pruebas.parkea.cl/parkea/android/validarSalida.php?codigo=" + passing[0] + "&id_parking=" + op.getId_parking();
            }
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);

            try {
                reg = parseJSON(jsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //valido = "true";
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            pDialog.dismiss();
            super.onPostExecute(result);
            if (reg != null) {
                    Intent i;
                    if (destino.equals("ingreso")) {
                        i = new Intent(Scanner.this, Ingreso.class);
                    } else {
                        i = new Intent(Scanner.this, Salida.class);
                        i.putExtra("hora_termino", reg.getHora_termino());
                        i.putExtra("tiempo_total", reg.getTiempo_total());
                        i.putExtra("valor", reg.getValor());
                    }

                    i.putExtra("codigo", codigo);
                    i.putExtra("hora_inicio", reg.getHora_inicio());
                    i.putExtra("fecha", reg.getFecha());
                    i.putExtra("ficha", reg.getEtiqueta());
                    android.util.Log.d("Develop", "fecha en scanner->>" + reg.getFecha());
                    startActivity(i);
                    Scanner.this.finish();
            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(Scanner.this);
                builder.setTitle("Error");
                builder.setIcon(R.drawable.appicon);
                builder.setMessage(msg);
                String positiveText = getString(android.R.string.ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // positive button logic
                                resumeScan();
                            }
                        });

                AlertDialog dialog = builder.create();
                // display dialog
                dialog.show();
            }
        }
    }

    private Registro parseJSON(String json) throws JSONException {
        if (json != null) {
            JSONObject jsonObj = new JSONObject(json);
            String result = jsonObj.getString("result"); //success o no-data
            android.util.Log.d("Develop", "result->>" + result);
            if (result.equals("success")) {
                //android.util.Log.d("Develop", "result success");
                Registro r = new Registro();
                String tipo = jsonObj.getString("tipo"); //ingreso o salida
                r.setHora_inicio(jsonObj.getString("hora_inicio"));
                r.setFecha(jsonObj.getString("fecha"));
                r.setEtiqueta(jsonObj.getString("ficha"));
                if(tipo.equals("salida")){
                    r.setHora_inicio(jsonObj.getString("hora_inicio"));
                    r.setHora_termino(jsonObj.getString("hora_termino"));
                    r.setTiempo_total(jsonObj.getString("tiempo_total"));
                    r.setValor(jsonObj.getString("valor"));
                }
                return r;
            }
            else{
                msg = jsonObj.getString("mensaje");
            }
        }
        return null;
    }
}

