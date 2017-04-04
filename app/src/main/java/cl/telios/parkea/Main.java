package cl.telios.parkea;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import cl.telios.parkea.Classes.DatosMain;
import cl.telios.parkea.Classes.Operador;
import cl.telios.parkea.Helpers.AdminSQLiteOpenHelper;
import cl.telios.parkea.Helpers.WebRequest;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Button ingreso, salida, terminar_turno, ver_ocupados;
    TextView ocupados, libres, recaudado, autos_estacionados, nombreOperador, rutOperador;
    SQLiteDatabase bd;
    Operador op;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Pantalla Principal");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);


        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,
                "PARKEA", null, Integer.parseInt(getString(R.string.database_version)));
        bd = admin.getWritableDatabase();

        op = Operador.getOperador(bd);
        android.util.Log.d("Develop","operador->" + op.toString());

        nombreOperador = (TextView)header.findViewById(R.id.nombreOperador);
        nombreOperador.setText(op.getNombre());
        rutOperador = (TextView)header.findViewById(R.id.rutOperador);
        rutOperador.setText(op.getRut());

        ocupados = (TextView) findViewById(R.id.ocupados);
        libres = (TextView) findViewById(R.id.libres);
        recaudado = (TextView) findViewById(R.id.recaudado);
        autos_estacionados = (TextView) findViewById(R.id.autos_estacionados);

        ingreso = (Button)findViewById(R.id.ingreso);
        ingreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, Scanner.class);
                i.putExtra("tipo", "ingreso");
                startActivity(i);
            }
        });

        salida = (Button)findViewById(R.id.salida);
        salida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, Scanner.class);
                i.putExtra("tipo", "salida");
                startActivity(i);
            }
        });

        ver_ocupados = (Button) findViewById(R.id.ver_ocupados);
        ver_ocupados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Main.this, ListadoOcupados.class);
                startActivity(i);
            }
        });

        terminar_turno = (Button)findViewById(R.id.terminar_turno);
        terminar_turno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Main.this);
                alertDialogBuilder.setMessage("¿Desea terminar su turno?");
                        alertDialogBuilder.setPositiveButton("Si",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        android.util.Log.d("Develop","operador dentro dialogo->" + op.toString());
                                        new TerminarTurno().execute(op);
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
        new CargarDatos().execute(op);
    }

    private class TerminarTurno extends AsyncTask<Operador, Void, Void> {
        Operador operador;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Main.this);
            pDialog.setMessage("Terminando turno...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(Operador... op) {
            operador = op[0];
            android.util.Log.d("Develop","op[0]->" + op[0]);
            WebRequest webreq = new WebRequest();
            // Making a request to url and getting response
            String URL = "http://pruebas.parkea.cl/parkea/android/terminarTurno.php?id="+operador.getId_turno();
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();

            operador.deleteOperador(bd);
            Intent intent = new Intent(Main.this, Splash.class);
            startActivity(intent);
            Main.this.finish();
        }
    }

    private class CargarDatos extends AsyncTask<Operador, Void, Void> {
        Operador operador;
        ProgressDialog pDialog;
        DatosMain data;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Main.this);
            pDialog.setMessage("Cargando Datos...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(Operador... op) {
            operador = op[0];
            android.util.Log.d("Develop","op[0]->" + op[0]);
            WebRequest webreq = new WebRequest();
            // Making a request to url and getting response
            String URL = "http://pruebas.parkea.cl/parkea/android/cargarDatos.php?id_turno="+operador.getId_turno()+"&id_parking="+operador.getId_parking();
            android.util.Log.d("Develop", URL);
            //retorna un json con los datos de usuario(result: success) , sino, un json con un "result: no data";     <-- OJO A ESTO!!!
            String jsonStr = webreq.makeWebServiceCall(URL, WebRequest.GET);
            try {
                data = parseJSON(jsonStr);
            }catch(JSONException e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.dismiss();

            if(data != null){
                ocupados.setText(data.getOcupados());
                libres.setText(data.getLibres());
                recaudado.setText(data.getRecaudado());
                autos_estacionados.setText(data.getAutos_estacionados());
            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                builder.setTitle("Error");
                builder.setIcon(R.drawable.appicon);
                builder.setMessage("Ha ocurrido un error al cargar los datos");
                String positiveText = getString(android.R.string.ok);
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // positive button logic
                                finish();
                            }
                        });

                AlertDialog dialog = builder.create();
                // display dialog
                dialog.show();
            }
        }
    }

    private DatosMain parseJSON(String json) throws JSONException {
        if (json != null) {
            JSONObject jsonObj = new JSONObject(json);
            String result = jsonObj.getString("result"); //success o no-data
            android.util.Log.d("Develop", "result->>" + result);
            if (result.equals("success")) {
                //android.util.Log.d("Develop", "result success");
                DatosMain dm = new DatosMain();
                dm.setOcupados(jsonObj.getString("ocupados"));
                dm.setLibres(jsonObj.getString("libres"));
                dm.setRecaudado(jsonObj.getString("recaudado"));
                dm.setAutos_estacionados(jsonObj.getString("autos_estacionados"));
                return dm;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.prod_fecha) {
            // Handle the camera action
        } else if (id == R.id.espacios_ocupados) {

        } else if (id == R.id.autorizados) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
