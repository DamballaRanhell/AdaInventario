package com.elektra.adainventario.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.elektra.adainventario.R;
import com.elektra.adainventario.objects.Inventario;

import java.util.ArrayList;
import java.util.List;

/**
 * Proyecto:
 * Autor: Emmanuel Rangel Reyes
 * Fecha: 10/04/2019.
 * Empresa: Elektra
 * Area: Auditoria Sistemas y Monitoreo de Alarmas
 */
public class InventarioDetalleAdapter extends BaseAdapter {

    private List<Inventario> listSKU;
    private Activity activity;

    public InventarioDetalleAdapter(List<Inventario> listSKU,Activity activity){
        this.listSKU = listSKU;
        this.activity = activity;
    }

    public List<Inventario> getListSKU() {
        return listSKU;
    }

    @Override
    public int getCount() {
        return listSKU.size();
    }

    @Override
    public Object getItem(int i) {
        return listSKU.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Inventario inventario = listSKU.get(i);

        View item = new View(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        item = inflater.inflate(R.layout.inventario_detalle_item_layout, viewGroup, false);

        List<String> listEstado = new ArrayList<>();
        listEstado.add("BUEN ESTADO");
        listEstado.add("MAL ESTADO");
        listEstado.add("ETIQUETA CAMBIADA");
        listEstado.add("SIN ETIQUETA");
        listEstado.add("FALTANTE DE ACCESORIOS");

        List<String> listUbicacion = new ArrayList<>();
        listUbicacion.add("EXHIBICION");
        listUbicacion.add("BODEGA");

        TextView textViewSKU = item.findViewById(R.id.textViewSKU);
        TextView textViewCantidad = item.findViewById(R.id.textViewCantidad);
        TextView textViewUbicacion = item.findViewById(R.id.textViewUbicacion);
        TextView textViewEstado = item.findViewById(R.id.textViewEstado);

        textViewSKU.setText(inventario.getSku());
        textViewCantidad.setText(String.valueOf(inventario.getConteo()));
        textViewUbicacion.setText(listUbicacion.get(inventario.getUbicacion()));
        textViewEstado.setText(listEstado.get(inventario.getClasificacion()));

        return item;
    }
}
