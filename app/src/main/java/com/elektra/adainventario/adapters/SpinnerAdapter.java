package com.elektra.adainventario.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.elektra.adainventario.R;

import java.util.List;

/**
 * Proyecto: ADA INVENTARIO
 * Autor: Emmanuel Rangel Reyes
 * Fecha: 18/10/2017.
 * Empresa: Elektra
 * Area: Auditoria Sistemas y Monitoreo de Alarmas
 */

public class SpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> listItems;
    private int idResource;
    private LayoutInflater inflater;

    public SpinnerAdapter(Context context, int resource,
                          List objects) {
        super(context,resource,0,objects);
        this.context = context;
        this.idResource = resource;
        this.listItems = objects;
        this.inflater = LayoutInflater.from(context);
    }

    public List<String> getListItems() {
        return listItems;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        View view = null;
        try {
            view = inflater.inflate(idResource, parent, false);
            TextView textViewItem = view.findViewById(R.id.textViewItem);
            String nombre = listItems.get(position);
            textViewItem.setText(nombre);
        }catch (Exception e){
            Toast.makeText(context,"Error en la aplicación",Toast.LENGTH_LONG).show();
        }
        return view;
    }
}
