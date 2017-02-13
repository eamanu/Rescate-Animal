package com.eamanu.rescateanimal;

import android.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.path;


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
    private static Uri uri;

    /**Variable para indicar que se devolvió una imagen de la galera.*/
    private static final int SELECT_FILE = 2;

    /**Variable para almacenar la selección del usuario (camara o galeria).*/
    private String userChoise;

    /**
     *  Crea la actividad.
     *  @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);
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
                // TODO: Almacenar direccion latitude longtiude provincia pais
            }
        }
        /** Datos proveniente de la actividad de la camera.*/
        if ( requestCode == REQUEST_IMAGE_CAPUTRE ) {
            if (resultCode == RESULT_OK){
                captureImageFromCamera ( data );
                /*TODO: aquí debería guardar la foto para enviarla a firebase*/
            }
        }

        /** Datos provenientes de la Galería.*/
        if (requestCode == SELECT_FILE ){
            if ( resultCode == RESULT_OK){
                SelectImageFromGallery ( data ) ;
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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, REQUEST_IMAGE_CAPUTRE );
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
            //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            // En la memoria externa
            path = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/MyPictures/");
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
        imageBitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);

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
            os.write(byteArrayOutputStream.toByteArray());

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
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
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
        Intent intent = new Intent(DenunciaActivity.this, sendDenuncia.class);

        intent.putExtra("Direccion", this.Direccion);
        intent.putExtra("Latitude", this.Latitude);
        intent.putExtra("Longitude", this.Longitude);
        intent.putExtra("Pais", this.Pais);
        intent.putExtra("Provincia", this.Provincia);
        //intent.putExtra("Uri", this.uri.toString());
        intent.setData(this.uri);

        startActivity(intent);
    }
}

