package cl.telios.parkea.Classes;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by rubro on 01-02-2017.
 */

public class Operador {
    public final String id;
    public final String id_emp;
    public final String rut;
    public final String nombre;
    public final String pass;
    public final String id_parking;
    public final String id_turno;
    public final String rol;
    public final String nombre_emp;

    public Operador(String id, String id_emp, String rut, String nombre, String pass, String id_parking, String id_turno, String rol, String nombre_emp) {
        this.id = id;
        this.id_emp = id_emp;
        this.rut = rut;
        this.nombre = nombre;
        this.pass = pass;
        this.id_parking = id_parking;
        this.id_turno = id_turno;
        this.rol = rol;
        this.nombre_emp = nombre_emp;
    }

    public String getId() {
        return id;
    }

    public String getId_emp() {
        return id_emp;
    }

    public String getRut() {
        return rut;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPass() {
        return pass;
    }

    public String getId_parking() {
        return id_parking;
    }

    public String getId_turno() { return id_turno; }

    public String getRol() { return rol; }

    public String getNombre_emp() {
        return nombre_emp;
    }

    @Override
    public String toString() {
        return "Operador{" +
                "id='" + id + '\'' +
                ", id_emp='" + id_emp + '\'' +
                ", rut='" + rut + '\'' +
                ", nombre='" + nombre + '\'' +
                ", pass='" + pass + '\'' +
                ", id_parking='" + id_parking + '\'' +
                ", id_turno='" + id_turno + '\'' +
                ", rol='" + rol + '\'' +
                ", nombre_emp='" + nombre_emp + '\'' +
                '}';
    }

    /*************************************
     FUNCIONES DE BASE DE DATSOS
     *************************************/
    public static boolean someoneLogued(SQLiteDatabase bd){
        String query = "select rut from operador limit 1";
        Cursor fila = bd.rawQuery(query, null);
        ////LogViewer.d("Develop", fila.getString(0));
        if (fila.moveToFirst()) {
            //LogViewer.d("Develop", fila.getString(0));
            //android.util.Log.d("Develop","operador logueado->"+fila.getString(0));
            return true;
        }
        else{
            return false;
        }
    }
    public boolean insertOperador(SQLiteDatabase bd){
        Boolean pass;
        ContentValues registro = new ContentValues();
        registro.put("id", this.id);
        registro.put("id_emp", this.id_emp);
        registro.put("rut", this.rut);
        registro.put("nombre", this.nombre);
        registro.put("pass", this.pass);
        registro.put("id_parking", this.id_parking);
        registro.put("id_turno", this.id_turno);
        registro.put("rol", this.rol);
        registro.put("nombre_emp", this.nombre_emp);
        try{
            bd.insert("operador", null, registro);
            pass= true;
        }
        catch (Exception e){
            //LogViewer.d("Develop", e.toString());
            pass = false;
        }
        return pass;
    }
    public boolean updateOperador(SQLiteDatabase bd){
        Boolean pass;
        ContentValues registro = new ContentValues();
        registro.put("id", this.id);
        registro.put("id_emp", this.id_emp);
        registro.put("rut", this.rut);
        registro.put("nombre", this.nombre);
        registro.put("pass", this.pass);
        registro.put("id_parking", this.id_parking);
        registro.put("id_turno", this.id_turno);
        registro.put("rol", this.rol);
        registro.put("nombre_emp", this.nombre_emp);

        try{
            bd.update("operador", registro,null, null);
            pass = true;
        }
        catch (Exception e){
            pass = false;
        }
        return true;
    }
    public boolean deleteOperador(SQLiteDatabase bd){
        Boolean pass;
        try{
            bd.delete("operador", "id='" + this.id+"'", null);
            pass = true;
        }
        catch (Exception e){
            pass = false;
        }
        return pass;
    }
    public static Operador getOperador(SQLiteDatabase bd) {
        String query = "select id ,id_emp ,rut ,nombre ,pass ,id_parking, id_turno, rol, nombre_emp from operador limit 1";
        Cursor fila = bd.rawQuery(query, null);
        Operador us;
        //android.util.Log.d("Develop","id fila->"+fila.getString(0));
        if (fila.moveToFirst()) {
            android.util.Log.d("Develop","retornando operador");
            us = new Operador(fila.getString(0),fila.getString(1),fila.getString(2),fila.getString(3),fila.getString(4),fila.getString(5), fila.getString(6), fila.getString(7), fila.getString(8));
            return us;
        }
        else{
            android.util.Log.d("Develop","retornando null");
            return null;
        }
    }



}
