package com.eamanu.rescateanimal;

/**
 * Created by eamanu on 4/23/17.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 *  Actividad de com.eamanu.rescateanimal.Denuncia.
 *  Esta actividad permite lanzar una actividad que registra la posicón del animal y saca una foto.
 *  @author eamanu
 *  @version 0.9.2 Beta
 */
public class DenunciaActivity extends AppCompatActivity{

    private static String TAG = "DenunciaActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_pic, CameraFragment.newInstance())
                    .commit();

        }
    }
}
