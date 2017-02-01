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

import java.util.ArrayList;

import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class Scanner extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    String codigo = "";
    String destino = "";
    SQLiteDatabase bd;
    boolean auto_usado = false;
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
        Log.v("Develop", rawResult.getText()); // Prints scan results
        Log.v("Develop", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String codigoMovil = rawResult.getText();
        codigo = codigoMovil;
        ArrayList<String> passing = new ArrayList<String>();
        passing.add(codigoMovil);

        new setMovil().execute(passing);

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


    private class setMovil extends AsyncTask<ArrayList<String>, Void, Void> {
        String valido = "false";
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Scanner.this);
            pDialog.setMessage("Validando Código... ");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(ArrayList<String>... passing) {
            //get data from view
            ArrayList<String> passed = passing[0]; //get passed arraylist
            // Creating service handler class instance
            WebRequest webreq = new WebRequest();

            /*
            // Making a request to url and getting response
            String URL = "http://telios.cl/quivolgo/mobile/getCodigo.php?codigo="+passed.get(0);
            Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);

            try {
                valido = jsonStr;
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            valido = "true";


            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            pDialog.dismiss();
            super.onPostExecute(result);
            if(valido.equals("true")){
                Intent i;
                if(destino.equals("ingreso")){
                    i = new Intent(Scanner.this, Ingreso.class);
                }
                else{
                    i = new Intent(Scanner.this, Salida.class);
                }

                i.putExtra("codigo", codigo);
                startActivity(i);
                Scanner.this.finish();

            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(Scanner.this);
                builder.setTitle("Error");
                builder.setIcon(R.drawable.appicon);
                builder.setMessage(valido);
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

}