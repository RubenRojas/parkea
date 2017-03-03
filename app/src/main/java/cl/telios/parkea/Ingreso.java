package cl.telios.parkea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Date;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static cl.telios.parkea.R.id.start;
import static cl.telios.parkea.R.id.textView;

public class Ingreso extends AppCompatActivity {
    TextView hora;
    Button continuar;
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
        hora = (TextView) findViewById(R.id.hora);
        String minutos = "";
        if(new Date().getMinutes()<10){
            minutos = "0"+String.valueOf(new Date().getMinutes());
        }
        else{
            minutos = String.valueOf(new Date().getMinutes());
        }
        String horas = String.valueOf(new Date().getHours());
        String currentDateTimeString = horas+":"+minutos;
        hora.setText(currentDateTimeString);

        continuar = (Button)findViewById(R.id.continuar);
        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Ingreso.this, Main.class);
                startActivity(intent);
                Toast.makeText(Ingreso.this, "Ingreso Registrado", Toast.LENGTH_SHORT).show();
                Ingreso.this.finish();
            }
        });

    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
