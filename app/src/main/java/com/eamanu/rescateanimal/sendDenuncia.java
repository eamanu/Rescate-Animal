package com.eamanu.rescateanimal;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class sendDenuncia extends BaseActivity implements NewDenunciaUploadTaskFragment.TaskCallbacks {

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

    /**Imagen pequeña*/
    private Bitmap mThumbnail;

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

    private NewDenunciaUploadTaskFragment mTaskFragment;

    public static final String TAG_TASK_FRAGMENT = "newPostUploadTaskFragment";

    private static final int THUMBNAIL_MAX_DIMENSION = 640;

    private static final int FULL_SIZE_MAX_DIMENSION = 1280;

    private static final String TAG = "sendDenuncia";

    /**Button enviar.*/
    private Button btnEnviar;

    /**Mensaje final.*/
    private TextView mensajeFinal;

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

        FragmentManager fm = getSupportFragmentManager();

        mTaskFragment = (NewDenunciaUploadTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        if(mTaskFragment == null){
            mTaskFragment = new NewDenunciaUploadTaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        //seteo la foto
        try {
            imageBitmap = decodeSampledBitmapFromUri(this.uri, FULL_SIZE_MAX_DIMENSION,FULL_SIZE_MAX_DIMENSION);
            imageBitmap = this.getRightAngleImageBitmap(this.uri);

            mThumbnail = decodeSampledBitmapFromUri(this.uri, THUMBNAIL_MAX_DIMENSION, THUMBNAIL_MAX_DIMENSION);
            mThumbnail = this.getRightAngleImageThunm(this.uri);
            photo.setImageBitmap(mThumbnail);

        } catch (IOException e) {
            Log.e(TAG, "Error:" + e.getMessage() );
        }

        // seteo direccion
        textDireccion.setText( this.Direccion );

        // btn Enviar
        btnEnviar = (Button) findViewById(R.id.btnEnviar);
        btnEnviar.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Medium.ttf"));
        btnEnviar.setText("Enviar");

        // Mensaje final
        mensajeFinal = ( TextView ) findViewById(R.id.tvMensajeEnviar);
        mensajeFinal.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoTTF/Roboto-Regular.ttf"));
        mensajeFinal.setText("Recuerda que realizar una denuncia representa una gran responsabilidad. Por favor, sé reponsable con tus publicaciones. Gracias.");
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

    public void enviarInformacion (View view ) {

        showProgressDialog("Se está mandando el pedido de rescate!!!");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mTaskFragment.upLoadData(imageBitmap, mThumbnail, user.getUid(), this.editComentario.getText().toString(), this.Latitude, this.Longitude,
                this.Pais, this.Provincia, this.Direccion);

        //startActivity(new Intent(sendDenuncia.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    /**
     * Function that give the correct angle.
     *
     * @param imageUri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Bitmap getRightAngleImageBitmap (final Uri imageUri ){
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
     * Function that give the correct angle.
     *
     * @param imageUri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Bitmap getRightAngleImageThunm (final Uri imageUri ){
        try {
            String path = getPath(this.getApplicationContext(), imageUri);
            ExifInterface ei = new ExifInterface(path);

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(this.mThumbnail, 90);

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(this.mThumbnail, 180);

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(this.mThumbnail, 270);

                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    return rotateImage(this.mThumbnail,90);

                case ExifInterface.ORIENTATION_NORMAL:

                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("RIGTHANGLE", e.getMessage());
        }

        return rotateImage(this.mThumbnail, 0);
    }

    /**
     * Function to rotate image
     *
     * @param bit
     * @param angle
     * @return
     */
    public Bitmap rotateImage ( Bitmap bit, float angle){
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


    @Override
    public void onBitmapResized(Bitmap resizedBitmap, int mMaxDimension) {

        if (resizedBitmap == null){
            Log.e(TAG, "No se pudo redimensionar la imagen en la tarea de background: ");
            Toast.makeText(this, "Error con la imagen", Toast.LENGTH_SHORT).show();
            return;
        }
        if( mMaxDimension == FULL_SIZE_MAX_DIMENSION){
            imageBitmap = resizedBitmap;
            //imageBitmap = scalePic();
           // imageBitmap = this.getRightAngleImage(this.uri);
            photo.setImageBitmap(imageBitmap);

        }
        if(mMaxDimension == THUMBNAIL_MAX_DIMENSION){
            mThumbnail = resizedBitmap;
        }
    }

    @Override
    public void onPostUpload(final String error) {
        sendDenuncia.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (error == null) {
                    //Toast.makeText(sendDenuncia.this, "Se envió el pedido de rescate!", Toast.LENGTH_SHORT).show();
                    dismissProgressDialog();
                    startActivity(new Intent(sendDenuncia.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                } else {
                    Toast.makeText(sendDenuncia.this, error, Toast.LENGTH_SHORT).show();
                    dismissProgressDialog();
                }
            }
        });
    }

    /**
     * Calcula cuanto hay que redimensionar.
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return int
     */
    public static int calculateInSampleSize ( BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth ){
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ( ( halfHeight / inSampleSize ) > reqHeight
                    && ( halfWidth / inSampleSize) > reqWidth ){
                inSampleSize *= 2;
            }

        }

        return inSampleSize;
    }

    /**
     * Redimensiona la imagen.
     *
     * @param fileUri
     * @param reqWidth
     * @param reqHeight
     * @return Bitmap
     * @throws IOException
     */
    public Bitmap decodeSampledBitmapFromUri(Uri fileUri, int reqWidth, int reqHeight) throws IOException {
        InputStream stream = new BufferedInputStream(this.getContentResolver().openInputStream(fileUri));
        stream.mark(stream.available());
        BitmapFactory.Options options = new BitmapFactory.Options();
        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        stream.reset();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        BitmapFactory.decodeStream(stream, null, options);
        // Decode bitmap with inSampleSize set
        stream.reset();
        return BitmapFactory.decodeStream(stream, null, options);
    }



}


