package com.eamanu.rescateanimal;

import android.*;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class DenunciaActivity extends AppCompatActivity{
    public Button GetPositionMapButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        GetPositionMapButton = (Button) findViewById(R.id.PositionMap);

    }

    public void getPositionAnimal(View view){
        Intent intent = new Intent(this, PostionAnimal.class);
        startActivity(intent);
    }

}

