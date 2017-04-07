package cl.telios.parkea.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by rubro on 01-02-2017.
 */

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //tabla de usuario
        String query="create table operador(id varchar(25) primary key," +
                "id_emp varchar(25), "+
                "rut varchar(25), "+
                "nombre varchar(25), "+
                "pass varchar(25), "+
                "id_parking varchar(25), "+
                "id_turno varchar(25), "+
                "rol varchar(25), "+
                "nombre_emp varchar(25) "+
                ")";

        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists operador");
        String query="create table operador(id varchar(25) primary key," +
                "id_emp varchar(25), "+
                "rut varchar(25), "+
                "nombre varchar(25), "+
                "pass varchar(25), "+
                "id_parking varchar(25), "+
                "id_turno varchar(25), "+
                "rol varchar(25), "+
                "nombre_emp varchar(25) "+
                ")";

        db.execSQL(query);


    }

}