package com.eamanu.rescateanimal;

import android.*;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class PostionAnimal extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /*TAG for debug*/
    private final static String TAG = "position-animal-map";

    /*map*/
    private GoogleMap mMap;

    private static final int MY_LOCATION_REQUEST_CODE = 1;

    /*Cliente de Google para obtener posición del movil*/
    private GoogleApiClient client;

    /*Intervalo de adquisición de position normal*/
    private static final long interval_request = 10000;

    /*Intervalo rápido de adquisición normal*/
    private static final long fast_interval_request = interval_request / 2;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    /*Para guardar la posición*/
    public String myPostionLatLng;

    /*sigo el status del update*/
    public boolean mRequestingLocationUpdates;

    /*TODO: DEBUG*/
    public TextView latlngview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postion_animal);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mRequestingLocationUpdates = false;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng LR_LatLng = new LatLng(-29.4166426, -66.8797555);
        /*
        *   Muevo la camara a la Rioja
        */
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LR_LatLng, 12));
        mMap.setOnMyLocationButtonClickListener(this);

        // Permito la capa "mylocation"
        enabledMyLocation();
        // Creo el api de google
        createApiClientGoogle();


    }

    /*Permite que active la capa "myLocation" si tengo los permisos*/
    private void enabledMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); //Activo la capa "mylocation"
        } else {
            //Pido permiso. Esto llama a public void onRequestPermissionsResult()
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }

    /*Creo el API client Google*/
    private synchronized void createApiClientGoogle() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /*Creo un request location*/
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest(); //new local request
        mLocationRequest.setInterval(interval_request); // configuro el intervalo normal
        mLocationRequest.setFastestInterval(fast_interval_request); // configuro la petición rápida
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // configuro en alta prioridad

        /*me conecto a Google Service*/
        client.connect();
        mRequestingLocationUpdates = true;
    }

    /*comienzo a actualizar posición*/
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                client, mLocationRequest, this);
    }

    /*Paro de actualizar la posición*/
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        latlngview.setText(mCurrentLocation.toString());

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "connected to Google Services API");
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            //latlngview.setText(mCurrentLocation.toString());
        }

        if (mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        client.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public void getGeoLocation (View view) throws IOException {
        hideSoftKeyboard(view);

        EditText ed = (EditText) findViewById(R.id.textDireccion);
        String strLocation = ed.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(strLocation,1);
        Address add = list.get(0);
        String locality = add.getLocality();


        Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();
    }

    public void hideSoftKeyboard (View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

}

