package com.elektra.adainventario.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.elektra.adainventario.objects.Inventario;

import java.util.ArrayList;
import java.util.List;

/**
 * Proyecto:
 * Autor: Emmanuel Rangel Reyes
 * Fecha: 09/04/2019.
 * Empresa: Elektra
 * Area: Auditoria Sistemas y Monitoreo de Alarmas
 */
public class InventarioDBMethods {

    private SQLiteOpenHelper sqLiteOpenHelper;
    private Context context;
    public static final String INVENTARIO_FISICO_TABLE = "inventario_fisico";

    public InventarioDBMethods(Context context){
        this.context = context;
    }

    public static String QUERY_CREATE_TABLE_INVENTARIO_FISICO = "CREATE TABLE " + INVENTARIO_FISICO_TABLE + " (" +
            "sku TEXT, " +
            "fecha_registro TEXT, " +
            "ubicacion INTEGER, " +
            "conteo INTEGER, " +
            "clasificacion INTEGER, " +
            "PRIMARY KEY (sku,ubicacion,clasificacion))";

    public void createInventarioFisico(Inventario inventario){
        SQLiteDatabase db = context.openOrCreateDatabase("InventarioDB",context.MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        values.put("sku",inventario.getSku());
        values.put("fecha_registro",inventario.getFechaRegistro());
        values.put("ubicacion",inventario.getUbicacion());
        values.put("clasificacion",inventario.getClasificacion());
        values.put("conteo",1);
        db.insertWithOnConflict(INVENTARIO_FISICO_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void createInventarioFisicoEdicion(Inventario inventario){
        SQLiteDatabase db = context.openOrCreateDatabase("InventarioDB",context.MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        values.put("sku",inventario.getSku());
        values.put("fecha_registro",inventario.getFechaRegistro());
        values.put("ubicacion",inventario.getUbicacion());
        values.put("clasificacion",inventario.getClasificacion());
        values.put("conteo",inventario.getConteo());
        db.insertWithOnConflict(INVENTARIO_FISICO_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public Inventario readInventarioFisico(String sku,String ubicacion,String estado){
        SQLiteDatabase db = context.openOrCreateDatabase("InventarioDB",context.MODE_PRIVATE,null);
        Inventario inventario = null;
        String query = "SELECT sku,fecha_registro,ubicacion,clasificacion,conteo FROM " + INVENTARIO_FISICO_TABLE +
                " WHERE sku = ? AND ubicacion = ? AND clasificacion = ?" ;//*/
        Cursor cursor = db.rawQuery(query,new String[]{sku,ubicacion,estado});
        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    inventario = new Inventario();
                    inventario.setSku(cursor.getString(0));
                    inventario.setFechaRegistro(cursor.getString(1));
                    inventario.setUbicacion(cursor.getInt(2));
                    inventario.setClasificacion(cursor.getInt(3));
                    inventario.setConteo(cursor.getInt(4));
                }while(cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return inventario;
    }

    public List<Inventario> readInventarioFisico(){
        SQLiteDatabase db = context.openOrCreateDatabase("InventarioDB",context.MODE_PRIVATE,null);
        List<Inventario> listSKU = new ArrayList<>();
        String query = "SELECT sku,fecha_registro,ubicacion,clasificacion,conteo FROM " + INVENTARIO_FISICO_TABLE + " ORDER BY sku";//*/
        Cursor cursor = db.rawQuery(query,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    Inventario inventario = new Inventario();
                    inventario.setSku(cursor.getString(0));
                    inventario.setFechaRegistro(cursor.getString(1));
                    inventario.setUbicacion(cursor.getInt(2));
                    inventario.setClasificacion(cursor.getInt(3));
                    inventario.setConteo(cursor.getInt(4));
                    listSKU.add(inventario);
                }while(cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return listSKU;
    }

    public void updateInventario(String condition,ContentValues contentValues,String[] args){
        SQLiteDatabase db = context.openOrCreateDatabase("InventarioDB",context.MODE_PRIVATE,null);
        db.update(INVENTARIO_FISICO_TABLE,contentValues,condition,args);
        db.close();
    }

    public void updateInventario(String sku,int conteo,String ubicacion,String estado){
        SQLiteDatabase db = context.openOrCreateDatabase("InventarioDB",context.MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        values.put("conteo",conteo);
        db.update(INVENTARIO_FISICO_TABLE,values,"sku = ? AND ubicacion = ? AND clasificacion = ?",
                new String[]{sku,ubicacion,estado});
        db.close();
    }

    public void deleteInventario(String condition,String[] args){
        SQLiteDatabase db = context.openOrCreateDatabase("InventarioDB",context.MODE_PRIVATE,null);
        db.delete(INVENTARIO_FISICO_TABLE, condition,args);
        db.close();
    }
}
