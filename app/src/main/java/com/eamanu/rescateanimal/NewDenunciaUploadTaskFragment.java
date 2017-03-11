package com.eamanu.rescateanimal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eamanu on 2/20/17.
 */

public class NewDenunciaUploadTaskFragment extends Fragment {
    private static final String TAG = "NewDenunciaUploadTaskFragment";

    public interface TaskCallbacks{
        void onBitmapResized(Bitmap resizedBitmap, int mMaxDimension);
        void onPostUpload ( String error );
    }
    private Context mApplicationContext;
    private TaskCallbacks mCallbacks;
    private Bitmap selectedBitmap;
    private Bitmap thumbnail;

    public NewDenunciaUploadTaskFragment ( ){
        //necessary construct empty
    }


    public static NewDenunciaUploadTaskFragment newInstance ( ){
        return new NewDenunciaUploadTaskFragment();
    }

    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

    }

    @Override
    public void onAttach (Context context){
        super.onAttach(context);

        if (context instanceof TaskCallbacks){
            mCallbacks = (TaskCallbacks) context;
        }else{
            throw new RuntimeException(context.toString() + "must implements callbacks");
        }
        mApplicationContext = context.getApplicationContext();

    }

    @Override
    public void onDetach ( ){
        super.onDetach();
        mCallbacks = null;
    }

    public void setSelectedBitmap (Bitmap bitmap){
        this.selectedBitmap = bitmap;

    }

    public Bitmap getSelectedBitmap ( ){
        return this.selectedBitmap;
    }

    public void setThumbnail ( Bitmap thumbnail ){
        this.thumbnail  = thumbnail;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void resizeBitmap ( Uri uri, int maxDimension){
        LoadResizeBitmapTaks task  = new LoadResizeBitmapTaks( maxDimension );
        task.execute(uri);
    }

    public void upLoadData (Bitmap bitmap, Bitmap t, String userid, String comentario,
                            double lat, double lng, String pais, String provincia, String direccion ){

        UploadDenunciaTask task = new UploadDenunciaTask(bitmap, t, userid, comentario,
                lat, lng, pais, provincia, direccion);

        task.execute();
    }


    class UploadDenunciaTask extends AsyncTask<Void, Void, Void>{
        private WeakReference<Bitmap> bitmapReference;
        private WeakReference<Bitmap> thumbnailReference;
        private Denuncia denuncia;
        private String Userid;
        private String Comentario;
        private double Latitude;
        private double Longitude;
        private String Pais;
        private String Provincia;
        private String Direccion;
        private String tStamp;

        // Referencia al almacenamiento de fotos
        private StorageReference mStorage;

        /**
         * Construct
         * @param bitmap
         * @param t
         */

        public UploadDenunciaTask ( Bitmap bitmap, Bitmap t, String userid, String comentario,
                                    double lat, double lng, String pais, String provincia, String direccion){

            bitmapReference = new WeakReference<Bitmap>(bitmap);
            thumbnailReference = new WeakReference<Bitmap>(t);
            this.Userid = userid;
            this.Comentario = comentario;
            this.Latitude = lat;
            this.Longitude = lng;
            this.Pais = pais;
            this.Provincia = provincia;
            this.Direccion = direccion;
            mStorage = FirebaseUtil.getStorageRef();
        }

        @Override
        protected  void onPreExecute(){
            //TODO: something
        }

        @Override
        protected Void doInBackground(Void... params) {
            final Bitmap fullsize = bitmapReference.get();
            final Bitmap thun = thumbnailReference.get();

            if ( (fullsize == null) || (thun == null)){
                return null;
            }

            tStamp = utils.setTimeStampToNameFile();

            final StorageReference filePathFullPhoto = mStorage.child("Photos").child("full").child(this.Userid + "_" + this.tStamp);
            final StorageReference filePathThumbail = mStorage.child("Photos").child("thumbail").child(this.Userid + "_" + this.tStamp);

            ByteArrayOutputStream fullSizeStream = new ByteArrayOutputStream();
            fullsize.compress(Bitmap.CompressFormat.JPEG,90, fullSizeStream);

            byte[] bytes = fullSizeStream.toByteArray();

            filePathFullPhoto.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final DatabaseReference databaseReference = FirebaseUtil.getDenunciasRef();
                    final Uri fullsizeUrl = taskSnapshot.getDownloadUrl();

                    ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
                    thun.compress(Bitmap.CompressFormat.JPEG, 40, thumbnailStream);

                    filePathThumbail.putBytes(thumbnailStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final Uri thumbnailUrl = taskSnapshot.getDownloadUrl();
                            final String newDenunciaKey = databaseReference.push().getKey();
                            Denuncia denuncia = new Denuncia(Userid, tStamp,
                                    Comentario,Latitude,Longitude, Pais, Provincia,
                                    Direccion, fullsizeUrl.toString(), thumbnailUrl.toString());

                            // Hago un push para obtener un id
                            //DatabaseReference newDenuncia = databaseReference.push();

                            // set datas
                            denuncia.setId(newDenunciaKey);

                            //newDenuncia.setValue(denuncia);

                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put(newDenunciaKey, true);
                            updateData.put(newDenunciaKey, denuncia);
                            databaseReference.updateChildren(updateData, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if ( databaseError == null){
                                        mCallbacks.onPostUpload(null);
                                    }else{
                                        Log.e(TAG, "Error al crear la denuncia" + databaseError.getMessage());
                                        FirebaseCrash.report(databaseError.toException());
                                        mCallbacks.onPostUpload("ERROR al crear la denuncia");
                                    }
                                }
                            });

                            addDenunciantes(denuncia.getUserid(), denuncia.getId(), denuncia.getTimestamp());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseCrash.logcat(Log.ERROR, TAG, "Failed to upload to database");
                            FirebaseCrash.report(e);
                            mCallbacks.onPostUpload("Error en la subida de la foto");
                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "Failed to upload to database");
                    FirebaseCrash.report(e);
                    mCallbacks.onPostUpload("Error en la subida de la foto");
                }
            });
            return null;
        }
    }


    class LoadResizeBitmapTaks extends AsyncTask<Uri,Void, Bitmap>{

        private int mMaxDimension;

        /**
         * Construct.
         *
         * @param Dimension
         */
        public LoadResizeBitmapTaks ( int Dimension ){
            this.mMaxDimension = Dimension;
        }

        /**
         * Redimensiona las imagenes.
         * @param params
         * @return Bitmap
         */
        @Override
        protected Bitmap doInBackground(Uri... params) {
            Uri uri = params[0];
            if ( uri != null){
                Bitmap bitmap = null;
                try{
                    bitmap =  decodeSampledBitmapFromUri(uri, this.mMaxDimension, this.mMaxDimension);
                }catch (FileNotFoundException ex){
                    Log.e(TAG, "No se puede encontrar para redimensionar: " + ex.getMessage());
                    FirebaseCrash.report(ex);
                }catch (IOException ex) {
                    Log.e(TAG, "Error durante la redimension: " + ex.getMessage());
                    FirebaseCrash.report(ex);
                }
                return bitmap;
            }
            return null;
        }

        @Override
        protected void onPostExecute ( Bitmap bitmap ){
            mCallbacks.onBitmapResized(bitmap, mMaxDimension);
        }
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
        InputStream stream = new BufferedInputStream(mApplicationContext.getContentResolver().openInputStream(fileUri));
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


    public void addDenunciantes ( String user, String denID, String timeStamp){
        DatabaseReference databaseReference = FirebaseUtil.getCurrentDenunciantesRef(user);

        String key = databaseReference.push().getKey();

        Map<String, Object> updateData = new HashMap<>();

        updateData.put(key, denID);
        updateData.put(key, timeStamp);

        databaseReference.updateChildren(updateData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if ( databaseError == null ){
                    Log.d(TAG, "Denunciate agregado correctamente");
                }else{
                    Log.e(TAG, "Error en el agregado de denunciates " + databaseError.getMessage() );
                    FirebaseCrash.report(databaseError.toException());
                }

            }

        });

    }


}
