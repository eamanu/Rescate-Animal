package com.eamanu.rescateanimal;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by eamanu on 3/4/17.
 */

public class HeaderNavView extends Fragment{
    private static final String TAG = "HeaderNavView";

    private onDataHeaderNavView mListener;
    /**TextView user name.*/
    private TextView tvNameUser;
    /**Profile picture.*/
    private ProfilePictureView profilePictureView;
    /**TextView email User.*/
    private TextView tvEmailUser;
    /**TextView Cantidad de denuncias Title.*/
    private TextView tvCantidadDenunciasTitle;
    /**TextView Cantidad de dencunias.*/
    private TextView tvCantidadDenuncias;
    /**TextView Cantidad de Rescates Title.*/
    private TextView tvCantidadRescatesTitle;
    /**TextView Cantidad de dencunias.*/
    private TextView tvCantidadRescates;
    /**Database Reference to Rescatistas.*/
    private DatabaseReference mDatabase;
    /**Database Reference to Denunciantes.*/
    private DatabaseReference mDatabaseDenunciantes;
    /**User of Firebase.*/
    private FirebaseUser mUser;

    /**
     * Empty constructor
     */
    public HeaderNavView ( ){
        // empty constructor
    }

    /**
     * new Instance.
     *
     * @return
     */
    public HeaderNavView newInstance(){
        return new HeaderNavView();
    }

    /**
     *
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDatabase = FirebaseUtil.getCurrentRecatista(mUser.getUid());

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( dataSnapshot == null)
                    tvCantidadRescates.setText("0");
                else
                    tvCantidadRescates.setText(Long.toString(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
                FirebaseCrash.report(databaseError.toException());
            }
        });

        mDatabaseDenunciantes = FirebaseUtil.getCurrentDenunciantesRef(mUser.getUid());

        mDatabaseDenunciantes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( dataSnapshot == null)
                    tvCantidadDenuncias.setText("0");
                else
                    tvCantidadDenuncias.setText(Long.toString(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error: " + databaseError.getMessage());
                FirebaseCrash.report(databaseError.toException());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if ( container == null){
            return null;
        }

        View rootView = inflater.inflate(R.layout.header_navview, container, false);
        rootView.setTag(TAG);

        // textview of name user
        tvNameUser = (TextView) rootView.findViewById(R.id.NameUser);
        profilePictureView = (ProfilePictureView) rootView.findViewById(R.id.PhotoUser);
        tvEmailUser = (TextView) rootView.findViewById(R.id.EmailUser);

        tvCantidadDenunciasTitle = (TextView) rootView.findViewById(R.id.CantidadDenunciasTitle);
        tvCantidadDenunciasTitle.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "RobotoTTF/Roboto-Medium.ttf"));
        tvCantidadDenunciasTitle.setText("Cantidad de denuncias");

        tvCantidadRescatesTitle = (TextView) rootView.findViewById(R.id.CantidadRescatesTitle);
        tvCantidadRescatesTitle.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "RobotoTTF/Roboto-Medium.ttf"));
        tvCantidadRescatesTitle.setText("Cantidad de rescates");

        tvCantidadDenuncias = (TextView) rootView.findViewById(R.id.CantidadDenuncias);

        tvCantidadRescates = (TextView) rootView.findViewById(R.id.CantidadRescates);

        mListener.setData(tvNameUser, profilePictureView, tvEmailUser, tvCantidadDenuncias, tvCantidadRescates);


        return rootView;
    }

    public interface onDataHeaderNavView{
        void setData (TextView nameUser, ProfilePictureView picture, TextView emailUser, TextView cantidadDenuncias, TextView cantidadRescates);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onDataHeaderNavView){
            mListener = (onDataHeaderNavView) context;
        }else
            throw new RuntimeException(context.toString()  + " must implement onDataHeaderNavView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



}
