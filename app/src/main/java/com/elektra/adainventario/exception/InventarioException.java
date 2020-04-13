package com.elektra.adainventario.exception;

import android.support.v4.media.session.PlaybackStateCompat;

public class InventarioException extends Exception {

    public InventarioException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventarioException(String message) {
        super(message);
    }
}
