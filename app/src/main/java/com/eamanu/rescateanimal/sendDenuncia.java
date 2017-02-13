package com.eamanu.rescateanimal;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class sendDenuncia extends AppCompatActivity {

    /**variable para almacenar la Dirección.*/
    private  String Direccion;

    /**Variable para almacenar la latitud. */
    private  double Latitude;

    /**Variable para almacenar la longitude.*/
    private  double Longitude;

    /**Variable para almacenar el nombre del país. */
    private  String Pais;

    /**Variable para almacenar el nombre de la localidad.*/
    private  String Provincia;

    /**Imagen para ser enviada.*/
    private Bitmap imageBitmap;

    /**Uri to image to send*/
    private  Uri uri;

    /**Comentario.*/
    private EditText editComentario;

    /**Para mostrar la direccion.*/
    private TextView textDireccion;

    /** Para el progres bar.*/
    ProgressDialog progress;

    /**Para mostrar la foto.*/
    ImageView photo;

    /**TimeStamp*/
    private String tStamp;

    // Referencia a la base de datos
    private DatabaseReference databaseReference;

    // Referencia al almacenamiento de fotos
    private StorageReference mStorage;

    private static final String userid = "eamanu";


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_denuncia);

        // Referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Denuncias");
        // Referencia al almacenamiento de fotos
        mStorage = FirebaseStorage.getInstance().getReference();

        /**Imagen.*/
        photo = (ImageView) findViewById(R.id.imgViewPhoto);
        /**Comentario*/
        editComentario = (EditText) findViewById(R.id.editTextDescripcion);
        /**Direccion.*/
        textDireccion = (TextView) findViewById(R.id.editTextDireccion);

        progress = new ProgressDialog(this);

        receiveData();

        try {
            // seteo la foto
            imageBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),this.uri);
            imageBitmap = this.getRightAngleImage(this.uri);
            photo.setImageBitmap(imageBitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // seteo direccion
        textDireccion.setText( this.Direccion );
    }

    /**
     * Recive los datos de la actividad anterior.
     */

    private void receiveData ( ){
        Intent i = this.getIntent();
        this.Direccion = i.getStringExtra("Direccion");
        this.Latitude = i.getDoubleExtra("Latitude", 0.0 );
        this.Longitude = i.getDoubleExtra("Longitude", 0.0 );
        this.Pais = i.getStringExtra("Pais");
        this.Provincia = i.getStringExtra("Provincia");
        //this.uri = Uri.parse(i.getStringExtra("Uri"));
        this.uri = i.getData();

    }

    public void enviarInformacion ( View view ){


        tStamp= new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        progress.setMessage("Subiendo información ...");
        progress.show();

        // Subier datos a FirebaseStorage
        StorageReference filepath = mStorage.child("Photos").child(userid + "_" + tStamp);
        filepath.putFile(this.uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // traigo la URL de descarga de la imagen
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                // creo el objeto denuncia
                Denuncia denuncia = new Denuncia(userid, tStamp, editComentario.getText().toString(),
                        Latitude,Longitude, Pais,
                        Provincia, Direccion, downloadUrl.toString());

                // Hago un push para obtener un id
                DatabaseReference newDenuncia = databaseReference.push();
                // set datas
                newDenuncia.setValue(denuncia);

                progress.dismiss();
                startActivity(new Intent(sendDenuncia.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.dismiss();
                Toast.makeText(sendDenuncia.this, "No se pudo enviar la información, vuelve a intentar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Function that give the correct angle.
     *
     * @param imageUri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Bitmap getRightAngleImage (final Uri imageUri ){
        try {
            String path = getPath(this.getApplicationContext(), imageUri);
            ExifInterface ei = new ExifInterface(path);

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(this.imageBitmap, 90);

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(this.imageBitmap, 180);

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(this.imageBitmap, 270);

                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    return rotateImage(this.imageBitmap,90);

                case ExifInterface.ORIENTATION_NORMAL:

                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("RIGTHANGLE", e.getMessage());
        }

        return rotateImage(this.imageBitmap, 0);
    }

    /**
     * Function to rotate image
     *
     * @param bit
     * @param angle
     * @return
     */
    public Bitmap rotateImage ( Bitmap bit, float angle){
        Toast.makeText(sendDenuncia.this, Float.toString(angle), Toast.LENGTH_SHORT).show();
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
    }

    /**
     * Funcion que devuelve el path de la imagen para rotar.
     * @see "http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework/20559175#20559175"
     * @param context
     * @param uri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}


