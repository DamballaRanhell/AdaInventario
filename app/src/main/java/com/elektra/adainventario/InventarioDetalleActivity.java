package com.elektra.adainventario;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.elektra.adainventario.adapters.InventarioDetalleAdapter;
import com.elektra.adainventario.database.InventarioDBMethods;
import com.elektra.adainventario.objects.Inventario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Proyecto:
 * Autor: Emmanuel Rangel Reyes
 * Fecha: 10/04/2019.
 * Empresa: Elektra
 * Area: Auditoria Sistemas y Monitoreo de Alarmas
 */
public class InventarioDetalleActivity extends AppCompatActivity{

    private List<Inventario> listSKU;
    private List<String> listEstado;
    private List<String> listUbicacion;
    private InventarioDBMethods inventarioDBMethods;
    private ListView listViewSKU;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventario_detalle_layout);

        listViewSKU = findViewById(R.id.listViewSKU);

        inventarioDBMethods = new InventarioDBMethods(this);

        listEstado = new ArrayList<>();
        listEstado.add("BUEN ESTADO");
        listEstado.add("MAL ESTADO");
        listEstado.add("ETIQUETA CAMBIADA");
        listEstado.add("SIN ETIQUETA");
        listEstado.add("FALTANTE DE ACCESORIOS");

        listUbicacion = new ArrayList<>();
        listUbicacion.add("EXHIBICION");
        listUbicacion.add("BODEGA");

        listSKU = new InventarioDBMethods(InventarioDetalleActivity.this).readInventarioFisico();
        InventarioDetalleAdapter inventarioDetalleAdapter = new InventarioDetalleAdapter(listSKU,InventarioDetalleActivity.this);
        listViewSKU.setAdapter(inventarioDetalleAdapter);

        listViewSKU.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InventarioDetalleAdapter inventarioDetalleAdapter = (InventarioDetalleAdapter)listViewSKU.getAdapter();
                final Inventario inventario = (Inventario) inventarioDetalleAdapter.getItem(i);
                LayoutInflater inflater = LayoutInflater.from(InventarioDetalleActivity.this);
                View dialogLayout = inflater.inflate(R.layout.dialog_edicion_inventario_layout, null, false);

                EditText editTextSKU = dialogLayout.findViewById(R.id.editTextSKU);
                final EditText editTextCantidad = dialogLayout.findViewById(R.id.editTextCantidad);

                final Spinner spinnerEstado = dialogLayout.findViewById(R.id.spinnerNotaCargo);
                final Spinner spinnerUbicacion = dialogLayout.findViewById(R.id.spinnerUbicacion);

                Button buttonBorrar = dialogLayout.findViewById(R.id.buttonBorrar);

                addCustomItemsOnSpinner(spinnerEstado, listEstado);
                addCustomItemsOnSpinner(spinnerUbicacion, listUbicacion);

                spinnerEstado.setSelection(inventario.getClasificacion());
                spinnerUbicacion.setSelection(inventario.getUbicacion());

                editTextSKU.setText(inventario.getSku());
                editTextCantidad.setText(String.valueOf(inventario.getConteo()));

                final AlertDialog alertDialog = new AlertDialog.Builder(InventarioDetalleActivity.this)
                        .setView(dialogLayout)
                        .setPositiveButton("Guardar", null)
                        .setNegativeButton("Cancelar", null)
                        .create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                //Utils.hideKeyboard(InventarioDetalleActivity.this, editText);
                                if(!editTextCantidad.getText().toString().equals("")){
                                    int cantidad = Integer.parseInt(editTextCantidad.getText().toString());
                                    int estado = spinnerEstado.getSelectedItemPosition();
                                    int ubicacion = spinnerUbicacion.getSelectedItemPosition();
                                    if(cantidad != 0) {
                                        if (inventario.getConteo() == cantidad) {
                                            if (inventario.getClasificacion() == estado && inventario.getUbicacion() == ubicacion) {
                                                //no se hace nada
                                            } else {
                                                //se busca si existe algun registro en BD
                                                Inventario tempInventario = inventarioDBMethods.readInventarioFisico(inventario.getSku(),String.valueOf(spinnerUbicacion.getSelectedItemPosition()),String.valueOf(spinnerEstado.getSelectedItemPosition()));
                                                //se crea la diferencia de cantidades de registros nuevos
                                                if(tempInventario != null){

                                                    inventarioDBMethods.deleteInventario("sku = ? AND ubicacion = ? AND clasificacion = ?",
                                                            new String[]{inventario.getSku(),String.valueOf(inventario.getUbicacion()),String.valueOf(inventario.getClasificacion())});

                                                    inventarioDBMethods.updateInventario(tempInventario.getSku(),tempInventario.getConteo() + cantidad,
                                                            String.valueOf(tempInventario.getUbicacion()),String.valueOf(tempInventario.getClasificacion()));
                                                }else {
                                                    //se actualiza registro con los nuevos datos
                                                    ContentValues contentValues = new ContentValues();
                                                    contentValues.put("clasificacion", spinnerEstado.getSelectedItemPosition());
                                                    contentValues.put("ubicacion", spinnerUbicacion.getSelectedItemPosition());
                                                    inventarioDBMethods.updateInventario("sku = ? AND ubicacion = ? AND clasificacion = ?",
                                                            contentValues, new String[]{inventario.getSku(),String.valueOf(inventario.getUbicacion()),String.valueOf(inventario.getClasificacion())});
                                                    Toast.makeText(InventarioDetalleActivity.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } else if (inventario.getConteo() > cantidad) {
                                            if (inventario.getClasificacion() == estado && inventario.getUbicacion() == ubicacion) {
                                                //se actualiza la cantidad
                                                inventarioDBMethods.updateInventario(inventario.getSku(),cantidad,String.valueOf(inventario.getUbicacion()),String.valueOf(inventario.getClasificacion()));
                                            } else {
                                                //se resta la nueva cantidad a la cantidad anterior
                                                int diferencia = inventario.getConteo() - cantidad;
                                                //se busca si existe algun registro en BD
                                                Inventario tempInventario = inventarioDBMethods.readInventarioFisico(inventario.getSku(),String.valueOf(spinnerUbicacion.getSelectedItemPosition()),String.valueOf(spinnerEstado.getSelectedItemPosition()));
                                                //se crea la diferencia de cantidades de registros nuevos
                                                if(tempInventario != null){
                                                    inventarioDBMethods.updateInventario(inventario.getSku(),tempInventario.getConteo() + cantidad,String.valueOf(inventario.getUbicacion()),String.valueOf(inventario.getClasificacion()));
                                                }else {

                                                    inventarioDBMethods.updateInventario(inventario.getSku(),
                                                            diferencia,
                                                            String.valueOf(inventario.getUbicacion()),
                                                            String.valueOf(inventario.getClasificacion()));

                                                    Inventario inventarioNuevo = new Inventario();
                                                    inventarioNuevo.setSku(inventario.getSku());
                                                    inventarioNuevo.setClasificacion(spinnerEstado.getSelectedItemPosition());
                                                    inventarioNuevo.setUbicacion(spinnerUbicacion.getSelectedItemPosition());
                                                    inventarioNuevo.setFechaRegistro(getDate("yyyy-MM-dd HH:mm:ss.SSS"));
                                                    inventarioNuevo.setConteo(cantidad);
                                                    inventarioDBMethods.createInventarioFisicoEdicion(inventarioNuevo);
                                                }
                                            }
                                            Toast.makeText(InventarioDetalleActivity.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                        } else if (inventario.getConteo() < cantidad) {
                                            if (inventario.getClasificacion() == estado && inventario.getUbicacion() == ubicacion) {
                                                //se actualiza la cantidad
                                                inventarioDBMethods.updateInventario(inventario.getSku(),cantidad,String.valueOf(inventario.getUbicacion()),String.valueOf(inventario.getClasificacion()));
                                            } else {
                                                //se resta la cantidad anterior a la cantidad nueva
                                                int diferencia = cantidad - inventario.getConteo();
                                                //se busca si existe algun registro en BD
                                                Inventario tempInventario = inventarioDBMethods.readInventarioFisico(inventario.getSku(),String.valueOf(spinnerUbicacion.getSelectedItemPosition()),String.valueOf(spinnerEstado.getSelectedItemPosition()));
                                                //se crea la diferencia de cantidades de registros nuevos
                                                if(tempInventario != null){
                                                    inventarioDBMethods.updateInventario(inventario.getSku(),tempInventario.getConteo() + cantidad,String.valueOf(inventario.getUbicacion()),String.valueOf(inventario.getClasificacion()));
                                                }else {
                                                    Inventario inventarioNuevo = new Inventario();
                                                    inventarioNuevo.setSku(inventario.getSku());
                                                    inventarioNuevo.setClasificacion(spinnerEstado.getSelectedItemPosition());
                                                    inventarioNuevo.setUbicacion(spinnerUbicacion.getSelectedItemPosition());
                                                    inventarioNuevo.setFechaRegistro(getDate("yyyy-MM-dd HH:mm:ss.SSS"));
                                                    inventarioNuevo.setConteo(cantidad);
                                                    inventarioDBMethods.createInventarioFisicoEdicion(inventarioNuevo);
                                                }
                                            }
                                            Toast.makeText(InventarioDetalleActivity.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        //borrar registro
                                        inventarioDBMethods.deleteInventario("sku = ? AND ubicacion = ? AND clasificacion = ?",
                                                new String[]{inventario.getSku(),String.valueOf(inventario.getUbicacion()),String.valueOf(inventario.getClasificacion())});
                                        Toast.makeText(InventarioDetalleActivity.this, "SKU borrado", Toast.LENGTH_SHORT).show();
                                    }
                                    //actualiza adapter
                                    listSKU = new InventarioDBMethods(InventarioDetalleActivity.this).readInventarioFisico();
                                    InventarioDetalleAdapter inventarioDetalleAdapter = new InventarioDetalleAdapter(listSKU,InventarioDetalleActivity.this);
                                    listViewSKU.setAdapter(inventarioDetalleAdapter);
                                    alertDialog.dismiss();
                                }else{
                                    Toast.makeText(InventarioDetalleActivity.this, "Debe especificar una cantidad", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    //alertDialog.dismiss();
                });
                alertDialog.show();

                buttonBorrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final AlertDialog alertDialogBorrar = new AlertDialog.Builder(InventarioDetalleActivity.this)
                                .setTitle("Borrar registro")
                                .setMessage("¿Esta seguro de querer borrar este registro?")
                                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        inventarioDBMethods.deleteInventario("sku = ? AND ubicacion = ? AND clasificacion = ?",
                                                new String[]{inventario.getSku(),String.valueOf(inventario.getUbicacion()),String.valueOf(inventario.getClasificacion())});
                                        listSKU = new InventarioDBMethods(InventarioDetalleActivity.this).readInventarioFisico();
                                        InventarioDetalleAdapter inventarioDetalleAdapter = new InventarioDetalleAdapter(listSKU,InventarioDetalleActivity.this);
                                        listViewSKU.setAdapter(inventarioDetalleAdapter);
                                        alertDialog.dismiss();
                                        Toast.makeText(InventarioDetalleActivity.this, "SKU borrado", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .create();
                        alertDialogBorrar.show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.itemGenerarArchivo:
                List<Inventario> listSKUArchivo = new InventarioDBMethods(InventarioDetalleActivity.this).readInventarioFisico();
                if(listSKUArchivo != null) {
                    if(listSKUArchivo.size() != 0) {

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("SKU,Cantidad,Estado,Ubicacion\n");
                        for (Inventario inventario : listSKUArchivo) {
                            stringBuilder.append(inventario.getSku()).append(",").append(inventario.getConteo()).append(",")
                                    .append(listEstado.get(inventario.getClasificacion())).append(",").append(listUbicacion.get(inventario.getUbicacion())).append("\n");
                        }
                        generateCSVFile(stringBuilder.toString());
                    }else{
                        Toast.makeText(getApplicationContext(),"No hay datos para generar el archivo",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"No hay datos para generar el archivo",Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    private boolean generateCSVFile(String texto){
        try {
            String h = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();
            File root = new File(Environment.getExternalStorageDirectory(), "ADAInventario");
            if (!root.exists()) {
                root.mkdirs();
            }
            File filepath = new File(root, h + ".csv");
            FileWriter writer = new FileWriter(filepath);
            writer.append(texto);
            writer.flush();
            writer.close();
            String m = "Archivo guardado en: " + filepath.getAbsolutePath();
            Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
            showDocumentFile(InventarioDetalleActivity.this,filepath.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "No se pudo generar el archivo", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static void showDocumentFile(Context context, String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if(android.os.Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1) {
            File file = new File(path);
            Uri tempFileUri = FileProvider.getUriForFile(context,
                    "com.inventario.provider", // As defined in Manifest
                    file);
            intent.setDataAndType(tempFileUri, "application/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else {
            intent.setDataAndType(Uri.parse("file://" + path), "application/*");
        }
        context.startActivity(intent);
    }

    private void addCustomItemsOnSpinner(Spinner spinner, List<String> list) {
        SpinnerAdapter spinnerAdapter = new com.elektra.adainventario.adapters.SpinnerAdapter(this,R.layout.spinner_item_layout,list);
        spinner.setAdapter(spinnerAdapter);
    }

    public static String getDate(String format) {
        java.text.DateFormat df = new SimpleDateFormat(format);
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }
}
