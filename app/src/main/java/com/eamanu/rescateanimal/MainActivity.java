package com.eamanu.rescateanimal;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class MainActivity extends BaseActivity implements DenunciaViewFragment.OnPostSelectedListener, HeaderNavView.onDataHeaderNavView{
    /**TAG.*/
    private static final String TAG = "MAINACTIVITY";

    /**TAG for DenunciaViewFragment indentification*/
    private static final String TAG_TASK_FRAGMENT= "DenunciaViewFragment";

    /**TAG for HeaderNavView Fragment indentification.*/
    private static final String TAG_TASK_HEADER_NAV_VIEW = "HeaderNavView";

    /**font.*/
    private Typeface font;

    /**Image view for photo user*/
    private ProfilePictureView ivPhotoUser;

    private DrawerLayout mDrawerLayout;

    private ImageButton btnOpenMyConfig;

    private TextView tvTituloApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.Drawer);
        btnOpenMyConfig = ( ImageButton ) findViewById(R.id.btn_menu);

        btnOpenMyConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });


        // picture of profile user
        ivPhotoUser = (ProfilePictureView) findViewById(R.id.PhotoUser);

        if (user == null)
            goToLoginActivity();
        else{
            // Manager de fragmento
            FragmentManager fragmentManager = getSupportFragmentManager();
            // Creo el objeto fragmento
            DenunciaViewFragment denunciaViewFragment =  (DenunciaViewFragment) fragmentManager.findFragmentByTag(TAG_TASK_FRAGMENT);

            //Creo el fragmento y lo asigno y ejecuto
            if ( denunciaViewFragment == null){
                denunciaViewFragment = new DenunciaViewFragment();
                //add the fragment
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                // Asigno el contenedor al fragmento
                fragmentTransaction.add(R.id.content_main,denunciaViewFragment);
                // envío
                fragmentTransaction.commit();
            }
            // set userData
            setUserData();
        }


        //set title
        tvTituloApp = (TextView) findViewById(R.id.tvTituloApp);
        tvTituloApp.setText("Rescate Animal");
        tvTituloApp.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Medium.ttf"));
    }

    private void setUserData() {
        //manager de fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        // creo el objeto fragmento
        HeaderNavView headerNavView = (HeaderNavView) fragmentManager.findFragmentByTag(TAG_TASK_HEADER_NAV_VIEW);

        //creo el fragmento y lo asigno y ejecuto
        if( headerNavView == null){
            headerNavView = new HeaderNavView();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.navview, headerNavView);
            fragmentTransaction.commit();
        }
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Me voy a Denuncia_activity.xml
     */
    public void Denunciar (View view ){
        Intent intent = new Intent ( this, DenunciaActivity.class);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                goToLoginActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onDataDownloading(Boolean isDataDownload) {
        if  (isDataDownload)
            showProgressDialog( "Descargando información ");
        else
            dismissProgressDialog();
    }

    @Override
    public void setData(TextView nameUser, ProfilePictureView picture, TextView emailUser, TextView cantidadDenuncias, TextView cantidadRescates) {
        Profile profile = Profile.getCurrentProfile();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Name user
        nameUser.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Medium.ttf"));
        nameUser.setText(profile.getName());
        //Email user
        emailUser.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Regular.ttf"));
        emailUser.setText(user.getEmail());
        //Picture user
        picture.setProfileId(profile.getId());
        // Denuncias cant
        cantidadDenuncias.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Medium.ttf"));
        // Rescates cant
        cantidadRescates.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Medium.ttf"));

    }
}

