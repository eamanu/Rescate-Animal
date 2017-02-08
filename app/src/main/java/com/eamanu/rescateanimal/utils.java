package com.eamanu.rescateanimal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.Manifest;

/**
 * Created by eamanu on 2/1/17.
 */

public class utils  {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermissionImagesStorages(final Context context) {
        int currentVerionAPI = Build.VERSION.SDK_INT;

        /*pregunto si la version actual es mayor que la M (supongo que es MarshMallow)*/
        if (android.os.Build.VERSION_CODES.M <= currentVerionAPI) {
            // Si no tengo permiso para leer datos de la memoria
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    /*Creo un alertDialog para mostrar el pedido de permiso*/
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setTitle("Permisos necesarios");
                    alertBuilder.setCancelable(true);
                    alertBuilder.setMessage("Es necesario que la app cuente con permisos para leer la memoria del dispositivo");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                        }
                    });

                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false; // Devuelve falso y se genera un alertdialog para adquirir permiso
            }else return true;  // Devuelve true si lo permisos ya están dados
        }else return true; // Devuelve true si la version es menor que M, ya que no se permite dar permisos en tiempo real
    }
}

