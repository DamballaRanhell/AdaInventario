package com.elektra.adainventario.objects;

/**
 * Proyecto:
 * Autor: Emmanuel Rangel Reyes
 * Fecha: 09/04/2019.
 * Empresa: Elektra
 * Area: Auditoria Sistemas y Monitoreo de Alarmas
 */
public class Inventario {

    private String sku;
    private String descripcion;
    private String fechaRegistro;
    private int ubicacion;
    private int clasificacion;
    private int conteo;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(int ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(int clasificacion) {
        this.clasificacion = clasificacion;
    }

    public int getConteo() {
        return conteo;
    }

    public void setConteo(int conteo) {
        this.conteo = conteo;
    }
}
