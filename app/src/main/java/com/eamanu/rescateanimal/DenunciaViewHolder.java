
package com.eamanu.rescateanimal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;

/**
 * Created by eamanu on 2/21/17 .
 */

public class DenunciaViewHolder extends RecyclerView.ViewHolder{

    /**View.*/
    View mView;
    /**TAG.*/
    private static final String TAG = "DENUNCIAVIEWHOLDER";
    /**Denuncia object.*/
    public Denuncia den;

    /**
     * Constructor.
     *
     * @param itemView
     */
    public DenunciaViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( mView.getContext(), ViewDenuncia.class);
                intent.putExtra("Comentario", den.getComentario());
                intent.putExtra("Direccion", den.getDireccion());
                intent.putExtra("Latitude", den.getLatitude());
                intent.putExtra("Longitude", den.getLongitude());
                intent.putExtra("URL", den.getPathPhoto());
                intent.putExtra("DenunciaID", den.getId());
                mView.getContext().startActivity(intent);
            }
        });

    }

    /**
     * Set Denuncia Object.
     *
     * @param d
     */
    public void setDenuncia  ( Denuncia d ){
        den = (Denuncia) d;
    }

    /**
     * Set Cometary.
     * @param com
     */
    public void setCom ( String com ){
        TextView tvcoment = ( TextView ) mView.findViewById( R.id.tvComentario );
        tvcoment.setText(com);
    }

    /**
     * Set Address.
     *
     * @param dir
     */
    public void setDireccion ( String dir ){
        TextView tvDir = ( TextView ) mView.findViewById( R.id.tvDireccion );
        tvDir.setText( dir );
    }

    /**
     * Set date.
     *
     * @param date
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void setDate (String date ){
        TextView tvDate = ( TextView ) mView.findViewById( R.id.tvFechaHora );
        try {
            tvDate.setText( utils.getTimeStampFromNameFile(date) );
        } catch (ParseException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    /**
     * Set image.
     *
     * @param ctx
     * @param imagePath
     */
    public void setImage (Context ctx, String imagePath ){
        ImageView imageView = (ImageView) mView.findViewById( R.id.photo );
        Picasso.with(ctx).load(imagePath).fit().into(imageView);
    }
}
