package com.eamanu.rescateanimal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;


public class ViewDenuncia extends AppCompatActivity implements ChangeStateDenunciaTaskFragment.TaskCallback {

    /**comentario*/
    private String strComentario;

    /**Direccion*/
    private String strDireccion;

    /**Lat*/
    private double lat;

    /**Lng*/
    private double lng;

    /**picture*/
    private Bitmap photo;

    /**Animal's Picture*/
    private ImageView Photo;

    /**Address' Animal*/
    private TextView tvDireccion;

    /**comment's Animal*/
    private TextView tvComentario;

    /**Rescue Button*/
    private Button btnRescate;

    /**Button to open map*/
    private ImageView positionMap;

    /**Path to download image.*/
    private String url;

    /**TAG to fragment.*/
    private static final String TAG_Task_Fragment = "Change_Status_Denuncia";

    /**Fragmet to update denuncia status*/
    private ChangeStateDenunciaTaskFragment changeStateDenunciaTaskFragment;

    FirebaseUser user;

    /**denuncia key*/
    private String denunciakey;

    /*title view denuncia*/
    private TextView tvViewDenuncia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_denuncia);

        Intent intent  = getIntent();
        strComentario = intent.getStringExtra("Comentario");
        strDireccion = intent.getStringExtra("Direccion");
        lat = intent.getDoubleExtra("Latitude", 0.0);
        lng = intent.getDoubleExtra("Longitude", 0.0);
        url = intent.getStringExtra("URL");
        denunciakey = intent.getStringExtra("DenunciaID");

        /**init */
        Photo = ( ImageView ) findViewById( R.id.fotoDenuncia );
        tvDireccion = ( TextView ) findViewById( R.id.tvDireccionDenuncia );
        tvComentario = ( TextView ) findViewById( R.id.tvComentarioDenuncia );
        btnRescate = ( Button ) findViewById( R.id.btnRescatado );
        positionMap = ( ImageView ) findViewById( R.id.positionMap);
        user = FirebaseAuth.getInstance().getCurrentUser();

        putData();

        //manager del fragmento
        FragmentManager fragmentManager = getSupportFragmentManager();
        // create the fragment
        changeStateDenunciaTaskFragment = ( ChangeStateDenunciaTaskFragment )
                fragmentManager.findFragmentByTag(TAG_Task_Fragment);

        if (changeStateDenunciaTaskFragment == null){
            // create fragment
            changeStateDenunciaTaskFragment = changeStateDenunciaTaskFragment.newInstance();
            // commit
            fragmentManager.beginTransaction().add(changeStateDenunciaTaskFragment, TAG_Task_Fragment).commit();
        }
        /**
         *  Set on clickListener to icon of map position
         */
        positionMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = "geo:" + lat + "," + lng + "?z=" + Integer.toString(15) +  "&q=" + lat + "," + lng + "(Aquí estoy)";
                Uri  gmmIntentUri = Uri.parse( query );
                Intent mapIntent = new Intent( Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }else{
                    Toast.makeText(ViewDenuncia.this, "No existen apps para ver la posición del animalito", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /**
         * Set onclicklistener to Rescatado button
         */
        btnRescate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStateDenunciaTaskFragment.addNewRescateToUser(user.getUid(), denunciakey);
                // go to main activity
                startActivity(new Intent(ViewDenuncia.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        tvViewDenuncia = (TextView) findViewById(R.id.tvViewDenuncia);
        tvViewDenuncia.setText("Info de la denuncia");
        tvViewDenuncia.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Regular.ttf"));
    }

    /**
     * Put data in layout
     */
    private void putData ( ){
        tvDireccion.setText(strDireccion);
        tvComentario.setText(strComentario);
        Picasso.with(getApplicationContext()).load(this.url).fit().into(Photo);
    }
}

