package com.eamanu.rescateanimal;

import android.*;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.content.Intent.*;


public class DenunciaActivity extends AppCompatActivity{
    public Button GetPositionMapButton;
    private static final int REQUEST_CODE = 0;
    public static String Direccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

    }

    public void getPositionAnimal(View view){
        startActivityForResult(new Intent(this, PostionAnimal.class), REQUEST_CODE);

    }

    protected  void onActivityResult (int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, data.getStringExtra("Direccion"), Toast.LENGTH_SHORT).show();
            }
        }
    }

}

