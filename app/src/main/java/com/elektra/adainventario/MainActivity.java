package com.elektra.adainventario;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.elektra.adainventario.database.InventarioDBMethods;
import com.elektra.adainventario.database.InventarioDataBase;
import com.elektra.adainventario.exception.InventarioException;
import com.elektra.adainventario.objects.Inventario;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int COMANDO_VOZ_INTENT = 100;
    private Button buttonCodigoBarra;
    private Button buttonComandoVoz;
    private Button buttonManual;
    private Button buttonLimpiar;
    private EditText editTextSKU;
    private EditText editTextCantidad;
    private Spinner spinnerEstado;
    private Spinner spinnerUbicacion;
    private List<String> listEstado;
    private List<String> listUbicacion;
    private InventarioDBMethods inventarioDBMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventario_captura_layout);

        new InventarioDataBase(MainActivity.this);
        inventarioDBMethods = new InventarioDBMethods(MainActivity.this);

        buttonCodigoBarra = findViewById(R.id.buttonCodigoBarras);
        buttonComandoVoz = findViewById(R.id.buttonComandoVoz);
        buttonManual = findViewById(R.id.buttonManual);
        buttonLimpiar = findViewById(R.id.buttonLimpiar);
        editTextSKU = findViewById(R.id.editTextSKU);
        editTextCantidad = findViewById(R.id.editTextCantidad);
        spinnerEstado = findViewById(R.id.spinnerNotaCargo);
        spinnerUbicacion = findViewById(R.id.spinnerUbicacion);
        FloatingActionButton floatingActionButton = findViewById(R.id.fabVerDetalleInventario);

        editTextCantidad.setText("1");

        listEstado = new ArrayList<>();
        listEstado.add("BUEN ESTADO");
        listEstado.add("MAL ESTADO");
        listEstado.add("ETIQUETA CAMBIADA");
        listEstado.add("SIN ETIQUETA");
        listEstado.add("FALTANTE DE ACCESORIOS");
        addCustomItemsOnSpinner(spinnerEstado, listEstado);

        listUbicacion = new ArrayList<>();
        listUbicacion.add("EXHIBICION");
        listUbicacion.add("BODEGA");
        addCustomItemsOnSpinner(spinnerUbicacion, listUbicacion);

        /*String query = "SELECT sku,ubicacion,clasificacion,conteo FROM " + InventarioDBMethods.INVENTARIO_FISICO_TABLE;
        //String query = "SELECT sku,conteo FROM EE" + InventarioDBMethods.INVENTARIO_FISICO_TABLE;
        String objeto = "com.elektra.adainventario.objects.Inventario";
        //String objeto = "Inventario";

        List<Inventario> objetos = null;

        try {
            objetos = select(MainActivity.this,query,null,objeto);
            for(Inventario inv:objetos){
                System.out.println("SKU: " + inv.getSku());
            }
        } catch (InventarioException e) {
            e.printStackTrace();
        }//*/

        editTextSKU.requestFocus();

        editTextSKU.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if(checkPermission(MainActivity.this)) {
                        validaSKU(editTextSKU.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });

        buttonComandoVoz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission(MainActivity.this)) {
                    startVoiceInput();
                }
            }
        });

        buttonCodigoBarra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission(MainActivity.this)) {
                    startBarcodeScan();
                }
            }
        });

        buttonManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission(MainActivity.this)) {
                    validaSKU(editTextSKU.getText().toString());
                }
            }
        });

        buttonLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartValues();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,InventarioDetalleActivity.class);
                startActivity(intent);
            }
        });

        checkPermission(MainActivity.this);
    }

    private void startVoiceInput() {
        Intent intentActionRecognizeSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-MX");
        intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT, "Escuchando código SKU");
        intentActionRecognizeSpeech.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);

        try {
            startActivityForResult(intentActionRecognizeSpeech, COMANDO_VOZ_INTENT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this,
                    "Tu dispositivo no soporta el reconocimiento por voz",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void restartValues(){
        editTextSKU.setText("");
        editTextCantidad.setText("1");
        editTextSKU.requestFocus();
    }

    private void startBarcodeScan(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Escanear Producto");
        integrator.setCameraId(0);
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult resultScan = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (resultScan != null) {
            if (resultScan.getContents() == null) {
                System.out.println("Escaneo Cancelado");
            } else {
                validaSKU(resultScan.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            System.out.println("Escaneo Cancelado");
        }

        switch (requestCode) {
            case COMANDO_VOZ_INTENT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    validaSKU(result.get(0));
                }
                break;
            }
        }
    }

    private void validaSKU(String lectura){
        if(!lectura.equals("")) {
            hideKeyboard();
            String skuValidacion = lectura.replaceAll("\\s+", "");
            if(validaExpresion(skuValidacion,"^[0-9]+$")){
                editTextSKU.setText(skuValidacion);
                if(!editTextCantidad.getText().toString().equals("")){
                    int validaCantidad = Integer.parseInt(editTextCantidad.getText().toString());
                    if(validaCantidad != 0){
                        Inventario inventario = inventarioDBMethods.readInventarioFisico(skuValidacion,String.valueOf(spinnerUbicacion.getSelectedItemPosition()),
                                String.valueOf(spinnerEstado.getSelectedItemPosition()));
                        if(inventario != null){
                            int conteo = inventario.getConteo();
                            inventarioDBMethods.updateInventario(inventario.getSku(),conteo+validaCantidad,String.valueOf(spinnerUbicacion.getSelectedItemPosition()),
                                    String.valueOf(spinnerEstado.getSelectedItemPosition()));
                            Toast.makeText(this,"SKU "  + skuValidacion +" agregado",Toast.LENGTH_SHORT).show();
                        }else{
                            Inventario inventarioNuevo = new Inventario();
                            inventarioNuevo.setSku(skuValidacion);
                            inventarioNuevo.setUbicacion(spinnerUbicacion.getSelectedItemPosition());
                            inventarioNuevo.setClasificacion(spinnerEstado.getSelectedItemPosition());
                            inventarioNuevo.setFechaRegistro(getDate("yyyy-MM-dd HH:mm:ss.SSS"));
                            inventarioNuevo.setConteo(validaCantidad);
                            inventarioDBMethods.createInventarioFisicoEdicion(inventarioNuevo);
                            Toast.makeText(this,"SKU " + skuValidacion + " agregado",Toast.LENGTH_SHORT).show();
                        }
                        //restartValues();
                        timer();
                    }else{
                        Toast.makeText(this,"La cantidad no debe ser cero",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this,"Debe especificar una cantidad",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this,"SKU no válido",Toast.LENGTH_SHORT).show();
            }
            /*boolean flag = true;
            String skuValidacion = "";
            try {
                skuValidacion = lectura.replaceAll("\\s+", "");
                long sku = Long.parseLong(skuValidacion);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
                flag = false;
            }

            if(flag) {
                hideKeyboard();
                long sku = 0;
                String skuLimpio = "";
                try {
                    skuLimpio = lectura.replaceAll("\\s+", "");
                    sku = Long.parseLong(skuLimpio);
                    editTextSKU.setText(skuLimpio);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                editTextSKU.setText(skuValidacion);
                Toast.makeText(this,"SKU no válido",Toast.LENGTH_SHORT).show();
            }//*/
        }else{
            Toast.makeText(this,"Debe capturar un SKU",Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextSKU.getWindowToken(), 0);
    }

    private boolean validaExpresion(String cadena,String regex){
        Pattern patron = Pattern.compile(regex);
        return patron.matcher(cadena).matches();
    }

    private void addCustomItemsOnSpinner(Spinner spinner, List<String> list) {
        SpinnerAdapter spinnerAdapter = new com.elektra.adainventario.adapters.SpinnerAdapter(this,R.layout.spinner_item_layout,list);
        spinner.setAdapter(spinnerAdapter);
    }

    public static String getDate(String format) {
        DateFormat df = new SimpleDateFormat(format);
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    public void timer (){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        restartValues();
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1500);
    }

    public boolean checkPermission(final Activity activity){
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                + ContextCompat.checkSelfPermission(
                activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            // Do something, when permissions not granted
            /*if(ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,Manifest.permission.CAMERA)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)){//*/
            // If we should give explanation of requested permissions

            // Show an alert dialog here with request explanation

            /*LayoutInflater li = LayoutInflater.from(activity);
            LinearLayout layoutDialog = (LinearLayout) li.inflate(R.layout.permission_layout, null);

            Button buttonAceptar = (Button) layoutDialog.findViewById(R.id.buttonAceptar);
            LinearLayout linearLayoutCamera = (LinearLayout) layoutDialog.findViewById(R.id.linearLayoutCameraPermission);
            LinearLayout linearLayoutLocation = (LinearLayout) layoutDialog.findViewById(R.id.linearLayoutLocationPermission);
            LinearLayout linearLayoutStorage = (LinearLayout) layoutDialog.findViewById(R.id.linearLayoutStoragePermission);

            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                linearLayoutStorage.setVisibility(View.GONE);
            }

            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                linearLayoutCamera.setVisibility(View.GONE);
            }

            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                linearLayoutLocation.setVisibility(View.GONE);
            }

            final android.support.v7.app.AlertDialog builder = new android.support.v7.app.AlertDialog.Builder(activity)
                    .setView(layoutDialog)
                    .show();

            buttonAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{
                                    //Manifest.permission.CAMERA,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            200
                    );
                    builder.dismiss();
                }
            });//*/

            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    200
            );

            /*}else{
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        200
                );
            }//*/
        }else {
            // Do something, when permissions are already granted
            return true;
        }
        return false;
    }

    private List select(Context context,@NonNull String query,String[] args,@NonNull String objectResult) throws InventarioException {
        if(objectResult == null){
            throw new InventarioException("El campo objectResult es requerido");
        }
        if(query == null){
            throw new InventarioException("El campo query es requerido");
        }

        String exceptionMessage = "Error al consultar BD";
        SQLiteDatabase db = context.openOrCreateDatabase("InventarioDB",context.MODE_PRIVATE,null);
        List listObjetos = new ArrayList<>();
        //nombres de variables del objeto deben llamarse igual que en la tabla BD
        //String query = "SELECT sku,ubicacion,clasificacion,conteo FROM " + InventarioDBMethods.INVENTARIO_FISICO_TABLE;// + " WHERE sku = ?";
        Cursor cursor = null;
        String[] columnas = null;
        Field field = null;
        try {
            Class<?> clazz = Class.forName(objectResult);
            cursor = db.rawQuery(query, args);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    columnas = cursor.getColumnNames();
                    do {
                        Object instance = clazz.newInstance();
                        for(int i=0;i<columnas.length;i++){
                            field = clazz.getDeclaredField(columnas[i]);
                            field.setAccessible(true);
                            if(field.getType().toString().contains("String")){
                                field.set(instance,cursor.getString(i));
                            }else if(field.getType().toString().contains("int")){
                                field.set(instance,cursor.getInt(i));
                            }else if(field.getType().toString().contains("double")){
                                field.set(instance,cursor.getDouble(i));
                            }else if(field.getType().toString().contains("float")){
                                field.set(instance,cursor.getFloat(i));
                            }
                        }
                        listObjetos.add(instance);
                    } while (cursor.moveToNext());
                }
            }
        } catch (SQLiteException e){
            throw new InventarioException(exceptionMessage,e);
        } catch (IllegalAccessException e) {
            throw new InventarioException(exceptionMessage,e);
        } catch (InstantiationException e) {
            throw new InventarioException(exceptionMessage,e);
        } catch (ClassNotFoundException e) {
            throw new InventarioException(exceptionMessage,e);
        } catch (NoSuchFieldException e) {
            throw new InventarioException(exceptionMessage,e);
        } finally {
            if(cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return listObjetos;
    }

    public void printContentValues(ContentValues vals) {
        Set<Map.Entry<String, Object>> s=vals.valueSet();
        Iterator itr = s.iterator();

        while(itr.hasNext())
        {
            Map.Entry me = (Map.Entry)itr.next();
            String key = me.getKey().toString();
            Object value =  me.getValue();

            //Log.d("DatabaseSync", "Key:"+key+", values:"+(String)(value == null?null:value.toString()));
        }
    }
}
