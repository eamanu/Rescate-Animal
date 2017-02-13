package com.eamanu.rescateanimal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    /**RecyclerView lista de denuncias.*/
    private RecyclerView recyclerView;

    /**Referencia a la base de datos*/
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create the recycler view
        recyclerView = (RecyclerView) findViewById(R.id.denuncias_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Denuncias");

        /*
       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


    }

    /**
     * Me voy a Denuncia_activity.xml
     */
    public void Denunciar (View view ){
        Intent intent = new Intent ( this, DenunciaActivity.class);
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onStart ( ){
        super.onStart();

        FirebaseRecyclerAdapter<Denuncia, DenunciaViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Denuncia, DenunciaViewHolder>(
                Denuncia.class,
                R.layout.denuncia_row,
                DenunciaViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(DenunciaViewHolder viewHolder, Denuncia model, int position) {
                viewHolder.setCom(model.getComentario());
                viewHolder.setDireccion(model.getDireccion());
                viewHolder.setDate(model.getTimestamp());
                //viewHolder.setImage(getApplicationContext(), model.getPathPhoto());
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    public static class DenunciaViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public DenunciaViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setCom ( String com ){
            TextView tvcoment = ( TextView ) mView.findViewById( R.id.tvComentario );
            tvcoment.setText(com);
        }

        public void setDireccion ( String dir ){
            TextView tvDir = ( TextView ) mView.findViewById( R.id.tvDireccion );
            tvDir.setText( dir );

        }

        public void setDate ( String date ){
            TextView tvDate = ( TextView ) mView.findViewById( R.id.tvFechaHora );
            tvDate.setText( date );
        }

        public void setImage (Context ctx, String imagePath ){
            ImageView imageView = (ImageView) mView.findViewById( R.id.photo );
            Picasso.with(ctx).load(imagePath).into(imageView);
        }

    }
}

