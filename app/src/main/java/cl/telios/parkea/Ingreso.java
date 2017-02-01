package cl.telios.parkea;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Ingreso extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);
        getSupportActionBar().setTitle("ingreso");
    }
}
