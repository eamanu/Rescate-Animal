package com.eamanu.rescateanimal;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by eamanu on 3/1/17.
 */

public class ChangeStateDenunciaTaskFragment extends Fragment {

    private static final String TAG = "CHANGESTATEDENUNCIATASKFRAGMENT";
    private TaskCallback taskCallback;
    private Context mApplicationContext;
    /**
     *  necessary construct empty
     */
    public ChangeStateDenunciaTaskFragment () {
        // empty constructor
    }

    public static ChangeStateDenunciaTaskFragment newInstance ( ){
        return new ChangeStateDenunciaTaskFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public interface TaskCallback {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof TaskCallback){
            taskCallback = (TaskCallback) context;
        }else{
            throw new RuntimeException(context.toString() + "must implements callbacks");
        }
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        taskCallback = null;
    }

    public void addNewRescateToUser ( String userKey, String denunciaKey){
        UploadRescateToUser task = new UploadRescateToUser(userKey, denunciaKey);
        task.execute();
        task.changeStatusOfDenuncia();

    }

    class UploadRescateToUser extends AsyncTask <Void, Void, Void>{

        /**User ID*/
        private String User;
        /**Denuncia ID*/
        private String DenunciaKey;
        /**database reference*/
        private DatabaseReference mDatabase;

        /**
         * Constructor.
         *
         * @param userID
         * @param denunciaKey
         */
        public UploadRescateToUser ( String userID, String denunciaKey ){
            User = userID;
            DenunciaKey = denunciaKey;
            mDatabase = FirebaseUtil.getRescatesFromCurrentRecatistaRef( userID );
        }
        @Override
        protected Void doInBackground(Void... params) {
                mDatabase  = FirebaseUtil.getCurrentRecatista(this.User);

            // if not exist userid create a new
            if ( mDatabase != null ){
                final String key = mDatabase.push().getKey();

                Map<String, Object> updateData = new HashMap<>();
                Map<String, Object> datas = new HashMap<>();
                //updateData.put(key, true);
                datas.put("DenunciaID", DenunciaKey);
                datas.put("Timestamp", utils.setTimeStampToNameFile());

                updateData.put(key,datas);

                mDatabase.updateChildren(updateData, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null){
                            /*TODO: all ok*/
                        }else{
                            Log.e(TAG, "Error en la actualización de la denuncia" + databaseError.getMessage());
                            FirebaseCrash.report(databaseError.toException());
                        }
                    }
                });
            }else{
                mDatabase = FirebaseUtil.getRecatistaRef();
                final String key = mDatabase.push().getKey();

                Map<String, Object> updateData = new HashMap<>();
                //updateData.put(this.User + "/" + key, true);
                updateData.put(this.User + "/" + key + "/Rescates", DenunciaKey);
                updateData.put(this.User + "/" + key + "/Rescates", utils.setTimeStampToNameFile());

                mDatabase.updateChildren(updateData, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if ( databaseError == null){
                            // TODO: all ok
                        }else{
                            Log.e(TAG, "Error en la actualización de la denuncia de un nuevo recatista" + databaseError.getMessage());
                            FirebaseCrash.report(databaseError.toException());
                        }
                    }
                });
            }

          return null;
        }

        public void changeStatusOfDenuncia ( ){
            mDatabase = FirebaseUtil.getDenunciasRef().child(this.DenunciaKey);

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Denuncia denuncia = dataSnapshot.getValue(Denuncia.class);
                    if ( denuncia.getEstado() == 0)
                        denuncia.setEstado(1);
                    else
                        Log.w(TAG, "Puede estar ocurriendo un problema");

                    Map<String, Object> updateData = new HashMap<>();

                    updateData.put(denuncia.getId(), true);
                    updateData.put(denuncia.getId(), denuncia);

                    DatabaseReference dr = FirebaseUtil.getDenunciasRef();
                    dr.updateChildren(updateData, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                // TODO: all ok
                            }else{
                                Log.e(TAG, "Error en el cambio de estado" + databaseError.getMessage());
                                FirebaseCrash.report(databaseError.toException());
                            }
                        }
                    });
                    dr.child(DenunciaKey).removeValue();

                    Map<String, Object> clearDenuncia = new HashMap<String, Object>();

                    DatabaseReference finDenuncia = FirebaseUtil.getFinDenuncia();

                    clearDenuncia.put(denuncia.getId(), true);
                    clearDenuncia.put(denuncia.getId(), denuncia);

                    finDenuncia.updateChildren(clearDenuncia, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                // TODO: all ok
                            }else{
                                Log.e(TAG, "Error en el cierre de denuncia" + databaseError.getMessage());
                                FirebaseCrash.report(databaseError.toException());
                            }
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error en la lectura de la denuncia para realizar cambio en el estado" + databaseError.getMessage());
                    FirebaseCrash.report(databaseError.toException());
                }

            });


        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
