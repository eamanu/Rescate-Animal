package com.eamanu.rescateanimal;

import android.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.DialogPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class PostionAnimal extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback{
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener {


    /*TAG for debug*/
    private final static String TAG = "position-animal-map";

    /*map*/
    private GoogleMap mMap;

    private static final int MY_LOCATION_REQUEST_CODE = 1;

    /*Cliente de Google para obtener posición del movil*/
//    private GoogleApiClient client;

    /*Intervalo de adquisición de position normal*/
//    private static final long interval_request = 10000;

    /*Intervalo rápido de adquisición normal*/
//    private static final long fast_interval_request = interval_request / 2;

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

    /*String con la calle*/
    public String strDireccion;

    /*double lat to send to back activity*/
    public double dlatToSend;

    /*double lng to send to back activity*/
    public double dlngToSend;

    /*String Provincia to send to back activity*/
    public String strProvinca;

    /*String Pais to send to back activity*/
    public String strPais;

    /*zoom to the animal position*/
    private final float ZOOM_TO_GET_POSITION = 18;

    /*zoom to map*/
    private final float ZOOM_TO_MAP = 12;

    /*TODO: DEBUG*/
    public TextView latlngview;

    /*Direccion*/
    EditText editTextDireccion;

    /*Button ok*/
    protected Button btnOK;

    /*Button back*/
    protected Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postion_animal);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = ( SupportMapFragment ) getSupportFragmentManager()
                .findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );

        editTextDireccion = ( EditText ) findViewById( R.id.textDireccion ); //traigo la dirección

        /*declaro el boton de ok para el envío de datos a la activity anterior*/
        btnOK = ( Button ) findViewById ( R.id.OkPosicion );
        /*declaro el boton de back para cancelar la operación y no se guarde ningún dato. */
        btnBack = ( Button )findViewById( R.id.BtnBack );

        if ( ! isConnectionInternet( ) ){
            AlertDialog.Builder builder = new AlertDialog.Builder( PostionAnimal.this );
            builder.setTitle( "Imposible conectarse a Internet" );
            builder.setMessage( "Es necesario conectarse a Internet. Por favor checkear la conección");
            builder.setPositiveButton( "OK", new DialogInterface.OnClickListener ( ){
                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    // TODO: ver que onda.
                }
            });
            builder.show( );
        }

        /*Listener del ok*/
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent output = new Intent();

                output.putExtra("Direccion", strDireccion);
                output.putExtra("Latitude", dlatToSend);
                output.putExtra("Longitude", dlngToSend);
                output.putExtra("Pais", strPais);
                output.putExtra("Provincia", strProvinca);

                setResult(RESULT_OK,output);
                finish();

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
             finish();
            }
        });
        //mRequestingLocationUpdates = false;
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
    public void onMapReady(GoogleMap googleMap){

        mMap = googleMap;

        LatLng LR_LatLng = new LatLng(-29.4142924, -66.8907967);
        /*
        *   Muevo la camara a la Rioja
        */
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LR_LatLng, this.ZOOM_TO_MAP));
        mMap.setOnMyLocationButtonClickListener(this);

        // Permito la capa "mylocation"
        //enabledMyLocation();
        // Creo el api de google
        //createApiClientGoogle();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                goToLocation(latLng.latitude, latLng.longitude, 18);
                try {
                    giveNameLocation(latLng.latitude, latLng.longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(PostionAnimal.this, e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        });
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

//    /*Creo el API client Google*/
//    private synchronized void createApiClientGoogle() {
//        client = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        createLocationRequest();
//    }

    /*Creo un request location*/
//    private void createLocationRequest() {
//        mLocationRequest = new LocationRequest(); //new local request
//        mLocationRequest.setInterval(interval_request); // configuro el intervalo normal
//        mLocationRequest.setFastestInterval(fast_interval_request); // configuro la petición rápida
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // configuro en alta prioridad
//
//        /*me conecto a Google Service*/
//        client.connect();
//        mRequestingLocationUpdates = true;
//    }

//    /*comienzo a actualizar posición*/
//    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//                client, mLocationRequest, this);
//    }

    /*Paro de actualizar la posición*/
//    protected void stopLocationUpdates() {
//        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
//    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        latlngview.setText(mCurrentLocation.toString());

}

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

 //   @Override
 //   public void onConnected(@Nullable Bundle bundle) {
    //      Log.i(TAG, "connected to Google Services API");
//        if (mCurrentLocation == null) {
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            //latlngview.setText(mCurrentLocation.toString());
//        }

//        if (mRequestingLocationUpdates){
//            startLocationUpdates();
//        }
//    }

//    @Override
//    public void onConnectionSuspended(int i) {
//        client.connect();
//    }

//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

//    }

    /**
     * Get the location
     *
     * @param view
     * @throws IOException
     */
    public void getGeoLocation (View view) throws IOException {
        hideSoftKeyboard(view); // Una vez que escribo desaparece el teclado

        this.strDireccion = editTextDireccion.getText().toString();

        if (strDireccion.isEmpty()){
            Toast.makeText(this, "Escribe una dirección por favor", Toast.LENGTH_SHORT).show();
            return;
        }
        // Llamo al Geocoder
        Geocoder gc = new Geocoder(this);
        // Extraigo la localización
        List<Address> list = gc.getFromLocationName(strDireccion, 1, -29.459814, -66.941286,-29.361477, -66.782517);
        if(list.isEmpty()){
            Toast.makeText(this, "No encontramos esa dirección", Toast.LENGTH_LONG).show();
            return;
        }

        Address add = list.get(0);

        this.setDataToSend(this.strDireccion,
                add.getLatitude(),
                add.getLongitude(),
                add.getCountryName(),
                add.getLocality());

        /*Muevo la cámara hacia el punto*/
        goToLocation(this.dlatToSend,this.dlngToSend,this.ZOOM_TO_GET_POSITION);

    }

    /**
     * Oculto el teclado
     * @param view
     */
    public void hideSoftKeyboard (View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    /**
     * Muevo la cámara hacia el punto de interes
     * @param lat
     * @param lng
     * @param zoom
     */
    public void goToLocation ( double lat, double lng, float zoom){
        LatLng ll = new LatLng(lat,lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoom));
        putMarker(lat, lng);// pongo un marcador
    }

    /**
     * Put the marker
     * @param lat
     * @param lng
     */
    public void putMarker ( double lat, double lng){

        mMap.clear();//Limpio los otros marcadores
        mMap.addMarker(new MarkerOptions()
        .position(new LatLng(lat,lng))
        .title("Rescate")
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_maps_chiquita))
        .anchor(0.0f, 1.0f)
        .draggable(true));
    }
    /**
     * Función para extraer el nombre de la dirección
     * @param lat latitude.
     * @param lng Longitude
     *
     */
    public void giveNameLocation ( double lat, double lng) throws IOException {
        Geocoder gc = new Geocoder(this);

        List <Address> list = gc.getFromLocation(lat, lng, 1);
        Address add = list.get(0);

        // set the data to send
        this.setDataToSend(add.getAddressLine(0),
                lat,
                lng,
                add.getCountryName(),
                add.getLocality());

        editTextDireccion.setText(add.getAddressLine(0));
    }

    /**
     * Funcion para armar lo datos para enviar a la actividad de la denuncia
     * @param dir Direccion
     * @param lat latitude
     * @param lng longitude
     * @param pais country
     * @param provincia locality
     */
    private void setDataToSend ( String dir, double lat, double lng, String pais, String provincia){
        this.strDireccion = dir;
        this.dlatToSend = lat;
        this.dlngToSend = lng;
        this.strPais = pais;
        this.strProvinca = provincia;
    }

    /**
     * check if internet is connected
     * @return true if connected
     */
    private boolean isConnectionInternet ( ){
        ConnectivityManager connectivityManager  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

}
