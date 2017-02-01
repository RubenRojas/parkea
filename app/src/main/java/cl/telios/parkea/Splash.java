package cl.telios.parkea;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.CheckNetwork;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Splash extends AppCompatActivity {

    SQLiteDatabase bd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,
                "PARKEA", null, Integer.parseInt(getString(R.string.database_version)));
        bd = admin.getWritableDatabase();


        if(CheckNetwork.isInternetAvailable(Splash.this)){

            continuar();
        }
        else{
            new AlertDialog.Builder(this)
                    .setTitle("Sin Conexión a internet.")
                    .setMessage("Esta aplicación necesita una conexión a internet para funcionar.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Splash.this.finish();
                        }
                    }).create().show();
        }

    }
    private void continuar(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //reviso si hay un usuario logueado
                //si existe, redireccion a dash, sino, a login
                final Intent mainIntent;
                Log.d("Develop", String.valueOf(Operador.someoneLogued(bd)));
                if(Operador.someoneLogued(bd)){
                    mainIntent = new Intent(Splash.this, Main.class);
                    mainIntent.putExtra("refer", "SPLASH");
                }
                else{
                    //LogViewer.d("Develop", "Usuario NO Logueado");
                    mainIntent = new Intent(Splash.this, Login.class);
                }
                Splash.this.startActivity(mainIntent);
                //overridePendingTransition(R.transition.left_in, R.transition.left_out);
                Splash.this.finish();

            }
        }, 1500);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
