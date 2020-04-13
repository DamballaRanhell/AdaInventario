package com.elektra.adainventario.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Proyecto: ADA INVENTARIO
 * Autor: Emmanuel Rangel Reyes
 * Fecha: 09/04/2019.
 * Empresa: Elektra
 * Area: Auditoria Sistemas y Monitoreo de Alarmas
 */
public class InventarioDataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "InventarioDB";
    private static final int DATABASE_VERSION = 1;
    private Context context;

    public InventarioDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(InventarioDBMethods.QUERY_CREATE_TABLE_INVENTARIO_FISICO);
    }

    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + InventarioDBMethods.INVENTARIO_FISICO_TABLE);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}