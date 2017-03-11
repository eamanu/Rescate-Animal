package com.eamanu.rescateanimal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *  Actividad de com.eamanu.rescateanimal.Denuncia.
 *  Esta actividad permite lanzar una actividad que registra la posicón del animal y saca una foto.
 *  @author eamanu
 *  @version 0.1
 */
public class DenunciaActivity extends AppCompatActivity{
    /**Boton para abrir la actividad del map.*/
    public Button GetPositionMapButton;

    /**Boton para agregar una foto.*/
    public Button GetPicture;

    /**variable para indicar que se devolvió correctamente la actividad del mapa.*/
    private static final int REQUEST_CODE = 0;

    /**variable para almacenar la Dirección.*/
    public static String Direccion;

    /**Variable para almacenar la latitud. */
    public static double Latitude;

    /**Variable para almacenar la longitude.*/
    public static double Longitude;

    /**Variable para almacenar el nombre del país. */
    public static String Pais;

    /**Variable para almacenar el nombre de la localidad.*/
    public static String Provincia;

    /**Variable para almacenar la direcion temporal.*/
    private String mCurrentPhotoPath;

    /**Variable para indicar que se devolvió correctamente actividad de la cámara.*/
    private static final int REQUEST_IMAGE_CAPUTRE = 1;

    /**Imagen para ser enviada.*/
    private Bitmap imageBitmap;

    /**Uri to image to send*/
    private Uri uri;

    /**Variable para indicar que se devolvió una imagen de la galera.*/
    private static final int SELECT_FILE = 2;

    /**Variable para almacenar la selección del usuario (camara o galeria).*/
    private String userChoise;

    /**Variable para conocer si se seleccionó una imagen*/
    private boolean isPhoto = false;

    /**Variable para conocer si se seleccionó un lugar en el mapa*/
    private boolean isDirection = false;

    /**Button Siguiente*/
    private Button btnSiguiente;

    /**Textview for topics*/
    private TextView tvFrases;

    /**Frases.*/
    private ArrayList<String> frases;

    /**Handler.*/
    private Handler handler;

    /**Runnable.*/
    private Runnable runnable;

    /**Interval time*/
    private static final int interval = 10000;

    /**
     *  Crea la actividad.
     *  @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);

        this.GetPicture = ( Button ) findViewById( R.id.BtnCamera);
        this.GetPositionMapButton = ( Button ) findViewById( R.id.BtnMap );

        //Button next
        btnSiguiente = (Button) findViewById(R.id.btnSiguiente);
        btnSiguiente.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Medium.ttf"));
        btnSiguiente.setText("Siguiente");

        // frases
        tvFrases = ( TextView ) findViewById(R.id.tvFrases);
        tvFrases.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Regular.ttf"));

        putFrases ( tvFrases );


    }

    private void putFrases(final TextView tvFrases) {
        DatabaseReference mDatabaseFrases = FirebaseUtil.getFrases();
        handler = new Handler();

        mDatabaseFrases.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for( DataSnapshot ds : dataSnapshot.getChildren()){
                   tvFrases.setText(ds.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Método que desencadena el alerDialog para elegir foto o tomar una.
     * @param view
     */
    public void getPicture (View view){
        addImageToSend ( );
    }

    /**
     * Llama a la actividad PositionAnimal
     * @param view
     */
    public void getPositionAnimal(View view){
        startActivityForResult(new Intent(this, PostionAnimal.class), REQUEST_CODE);

    }

    /**
     * Se maneja el resultado de las demás activiades
     * @param requestCode
     * @param resultCode
     * @param data
     */

    protected  void onActivityResult (int requestCode, int resultCode, Intent data){
        /** Datos proveniente de la Activiad de posición.*/
        if (requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK){
                setDataPosition(data);
                this.changeMapSrc( this.GetPositionMapButton);
                // TODO: Almacenar direccion latitude longtiude provincia pais
            }
        }
        /** Datos proveniente de la actividad de la camera.*/
        if ( requestCode == REQUEST_IMAGE_CAPUTRE ) {
            if (resultCode == RESULT_OK){
                //captureImageFromCamera ( data );
                this.changePhotoSrc( this.GetPicture );
                /*TODO: aquí debería guardar la foto para enviarla a firebase*/
            }
        }

        /** Datos provenientes de la Galería.*/
        if (requestCode == SELECT_FILE ){
            if ( resultCode == RESULT_OK){
                SelectImageFromGallery ( data ) ;
                this.changePhotoSrc( this.GetPicture );
                /*TODO: Aquí debería guardar la foto para enviarla al firebase*/
            }
        }
    }

    /**
     *  Para adjuntar una imagen de la memoria o la cámara.
     */
    public void addImageToSend(){
        /**
         * Opciones permitidas.
         */
        final String optionsImages [] = {"Tomar una foto", "Elegir una foto de la galería", "Cancelar"};

        /** Construyo el alert dialog para mostrar las opciones.*/
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DenunciaActivity.this);

        alertBuilder.setTitle("Seleccionar una opción");

        alertBuilder.setItems(optionsImages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean result = utils.checkPermissionImagesStorages(DenunciaActivity.this);
                if (optionsImages[which].equals("Tomar una foto")) {
                    /** Se quiere tomar una foto. */
                    userChoise = "Tomar una foto";
                    if (result)
                        takePhoto();
                }
                if (optionsImages[which].equals("Elegir una foto de la galería")){
                    /** Se quiere elegir una foto de la galería. */
                    userChoise = "Elegir una foto de la galería";
                    if (result)
                        takePictureFromStorage();
                }
            }
        });
        alertBuilder.show();
    }

    /**
     * Request permision result.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case utils.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (userChoise.equals("Tomar una foto")){
                        takePhoto();
                    }else if (userChoise.equals("Elegir una foto de la galería")){
                        takePictureFromStorage();
                    }else{
                        //TODO: que pasa cuando cancelo la orden
                    }
                }
                break;
        }
    }

    /**
     * Tomar una foto.
     * Llama a la cámara para que tome una foto
     */
    private void takePhoto ( ) {
        File path;

        if (this.checkExternalStorageWritable()){
            //path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MyPictures/");
            // En la memoria externa
            path = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/MyPictures/");
            Log.w("TEST",path.toString() );
        }else{
            // En la memoria Interna
            path = getFilesDir();
        }

        // Creo un string para ponerle nombre al la imagen
        String nameImage = "RescateAnimal_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg" ;

        File imageFile = new File(path, nameImage);


        try {
            imageFile.createNewFile();

            this.uri = Uri.fromFile(imageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, this.uri);

            if (intent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(intent, REQUEST_IMAGE_CAPUTRE );

        }catch (IOException e){
            Log.e("ERROR-TakePhoto", "Error: " +  e.getMessage());
        }
    }

    /**
     * Captura la imagen de la cámara y la almacena en la memoria.
     * @param data
     * @see android developer webpage
     */
    private void captureImageFromCamera ( Intent data) {
        // Path for picture
        File path;

        if (this.checkExternalStorageWritable()){
            //path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MyPictures/");
            // En la memoria externa
            path = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/MyPictures/");
            Log.w("TEST",path.toString() );
        }else{
            // En la memoria Interna
            path = getFilesDir();
        }

        /**Extraigo la foto del intent.*/
        imageBitmap = null;
        imageBitmap = (Bitmap) data.getExtras().get("data");

        // Llamo a una variable tipo array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );

        // convierto a jpg
        //imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);

        // Creo un string para ponerle nombre al la imagen
        String nameImage = "RescateAnimal_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg" ;

        // creo el file
        //File imageFile = new File(path, nameImage);
        File imageFile = new File(path, nameImage);


        try{
            // Me aseguro que la carpeta existe
            path.mkdirs();

            imageFile.createNewFile();
            FileOutputStream os = new FileOutputStream(imageFile);
            //os.write(byteArrayOutputStream.toByteArray());

            imageBitmap.compress(Bitmap.CompressFormat.JPEG,100, os);

            this.uri  = Uri.fromFile(imageFile);

            // cierro
            os.close();


        }catch (IOException e){
            Log.w("ExternalStorage", "Error Writing" + imageFile, e);
        }
    }

    /**
     * Lanza actividad que permite elegir una foto de la galería
     */
    private void takePictureFromStorage ( ){
        /**
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Por favor elige una foto"), SELECT_FILE);*/

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Por favor elige una foto"), SELECT_FILE);

    }

    @SuppressWarnings("deprecation")
    private void SelectImageFromGallery ( Intent data){
        imageBitmap = null;
        if (data != null){
            try{
                imageBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                this.uri = data.getData();

            }catch (IOException e){
                Log.w("FromGallery", "Error reading " +e );
                FirebaseCrash.report(e);
            }catch (OutOfMemoryError e){
                Log.w("FromGallery", "Error en la imagen " + e );
                Toast.makeText(this, "Error en la subida de la imágen, elige otra foto por favor", Toast.LENGTH_SHORT).show();
                FirebaseCrash.report(e);
            }
        }
    }

    /**
     * Crea un file temp.
     * @deprecated Utilizada para testing. Si no se puede utilizar FileProvider se eliminara
     * @return
     * @throws IOException
     */
    private File createTempPhoto ( ) throws IOException{
        String nameImage = "RescateAnimal_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) ;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                nameImage,  /*prefijo*/
                ".jpg",     /*sufijo*/
                storageDir /*directory*/
        );

        /*Almacena una imagen: path usado por el eAction_view intents*/
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Método que informa la presencia de memoria externa
     * @return True if External Memory Card is available.
     */
    @NonNull
    private Boolean checkExternalStorageWritable ( ){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return true; // External storage is available for write

        return false;
    }

    private void setDataPosition ( Intent data ){
        this.Direccion = data.getStringExtra("Direccion");
        this.Latitude = data.getDoubleExtra("Latitude", 0.0);
        this.Longitude = data.getDoubleExtra("Longitude", 0.0);
        this.Pais = data.getStringExtra("Pais");
        this.Provincia = data.getStringExtra("Provincia");
    }

    public void btnNext (View view){

        // check if the photo and address is right
        if (isPhoto && isDirection){
        Intent intent = new Intent(DenunciaActivity.this, sendDenuncia.class);

        intent.putExtra("Direccion", this.Direccion);
        intent.putExtra("Latitude", this.Latitude);
        intent.putExtra("Longitude", this.Longitude);
        intent.putExtra("Pais", this.Pais);
        intent.putExtra("Provincia", this.Provincia);
        //intent.putExtra("Uri", this.uri.toString());
        intent.setData(this.uri);

        startActivity(intent);
        } else{
            Toast.makeText(this, "Por favor, agregar foto y la posición del animalito", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cambia el color del boton para adjuntar la imagen
     * @param butCamera boton de la camera.
     */
    private void changePhotoSrc ( Button butCamera){
        if (this.isPhoto == false){
            this.isPhoto = true;
            butCamera.setBackgroundResource(R.drawable.ic_camera_blue);
        }
    }

    /**
     * Cambia el color del boton del mapa
     * @param butMap boton del mapa.
     */
    private void changeMapSrc ( Button butMap){
        if (this.isDirection== false){
            this.isDirection = true;
            butMap.setBackgroundResource(R.drawable.ic_google_maps_blue);
        }
    }
}

