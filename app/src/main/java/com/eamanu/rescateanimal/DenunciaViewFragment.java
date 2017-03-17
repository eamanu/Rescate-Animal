package com.eamanu.rescateanimal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by eamanu on 2/21/17.
 */

/**
 * Class DenunciaViewFragment
 */
public class DenunciaViewFragment extends Fragment {
    /**TAG.*/
    public final String TAG = "DenunciaViewFragment";
    /**RecyclerPosition.*/
    private int mRecyclerViewPostion = 0 ;
    /**RecyclerView.*/
    private RecyclerView recyclerView;
    /**some adapter.*/
    private RecyclerView.Adapter<DenunciaViewHolder> mAdapter;
    /**Referencia a la base de datos*/
    private DatabaseReference mDatabase;
    /**Layout for new goods*/
    private RelativeLayout relativeLayoutGooNews;
    /**interface*/
    private OnPostSelectedListener mListener;
    /**Textview GoodNews*/
    private TextView tvGoodNews;

    /**
     * Necessary construct
     */
    public DenunciaViewFragment ( ){
        // require empty constructor
    }
    /**
     * Crete an instance of DenunciaViewFragmet
     * @return DenunciViewFragment
     */
    public static DenunciaViewFragment newInstance( ){
        DenunciaViewFragment fragment = new DenunciaViewFragment();
        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        if ( ! isConnectionInternet( ) ){
            AlertDialog.Builder builder = new AlertDialog.Builder( getActivity());
            builder.setTitle( "Imposible conectarse a Internet" );
            builder.setMessage( "Por favor conéctate a la red");
            builder.setPositiveButton( "OK", new DialogInterface.OnClickListener ( ){
                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    // TODO: ver que onda.
                }
            });
            builder.show( );
            return;
        }

        // Creo la ref a la DB
        mDatabase = FirebaseUtil.getDenunciasRef();

        //set adapter
        mAdapter = getFirebaseRecylerAdapter();


        mListener.onDataDownloading(true);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    /**There aren't data to download*/
                    recyclerView.setVisibility(View.VISIBLE);
                    relativeLayoutGooNews.setVisibility(View.INVISIBLE);

                    for ( DataSnapshot Snapshot : dataSnapshot.getChildren() ){
                        Log.w(TAG,Snapshot.getKey().toString());
                    }
                    mListener.onDataDownloading(false);

                }else{
                    /**ther aren't data to download */
                    recyclerView.setVisibility(View.INVISIBLE);
                    relativeLayoutGooNews.setVisibility(View.VISIBLE);
                    mListener.onDataDownloading(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error en la base de datos");
                FirebaseCrash.report(databaseError.toException());
                FirebaseCrash.log("Error en: " + TAG + " -> " + databaseError.getMessage());
            }
        });

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        View rootView = inflater.inflate(R.layout.denuncia_view_fragment, container, false);
        rootView.setTag(TAG);

        // creo el manager del LinearLayout
        recyclerView = (RecyclerView) rootView.findViewById(R.id.denuncias_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager layoutManager = new LinearLayoutManager (getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        // good news
        relativeLayoutGooNews = (RelativeLayout) rootView.findViewById(R.id.goodNews);
        tvGoodNews = (TextView) rootView.findViewById(R.id.textViewGoodNews);
        tvGoodNews.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "RobotoTTF/Roboto-Bold.ttf"));
        tvGoodNews.setText("Buenas noticias: No hay animalitos para rescatar");
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private FirebaseRecyclerAdapter<Denuncia, DenunciaViewHolder> getFirebaseRecylerAdapter(){
     return new FirebaseRecyclerAdapter<Denuncia, DenunciaViewHolder>(
             Denuncia.class,
             R.layout.denuncia_row,
             DenunciaViewHolder.class,
             mDatabase
             //query
             )
     {
         @Override
         protected void populateViewHolder(DenunciaViewHolder denunciaViewHolder, Denuncia denuncia , int position) {
            setupDenunciaPost( denunciaViewHolder, denuncia , position);
         }

         @Override
         public void onViewRecycled(DenunciaViewHolder holder) {
             super.onViewRecycled(holder);
         }
     };
    }


    private void setupDenunciaPost ( DenunciaViewHolder denunciaViewHolder, Denuncia denuncia, int position ){
        denunciaViewHolder.setCom(denuncia.getComentario());
        denunciaViewHolder.setDireccion(denuncia.getDireccion());
        denunciaViewHolder.setDate(denuncia.getTimestamp());
        denunciaViewHolder.setImage(getActivity().getApplicationContext(), denuncia.getPathThumbail());
        denunciaViewHolder.setDenuncia( denuncia );
    }

    /**
     * interface of DenunviaViewFragment
     */
    public interface  OnPostSelectedListener{

        /**
         * Send if the data is donwloading.
         *
         * @param isDataDownload
         */
        void onDataDownloading ( Boolean isDataDownload);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPostSelectedListener) {
            mListener = (OnPostSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPostSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * check if internet is connected
     * @return true if connected
     */
    private boolean isConnectionInternet ( ){
        ConnectivityManager connectivityManager  = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }
}
