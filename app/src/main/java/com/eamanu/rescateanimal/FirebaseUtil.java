package com.eamanu.rescateanimal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by eamanu on 2/20/17.
 */

public class FirebaseUtil {

    public static DatabaseReference getDataBaseRef ( ){
        return FirebaseDatabase.getInstance().getReference();
    }

    public static StorageReference getStorageRef ( ){
        return FirebaseStorage.getInstance().getReference();
    }

    public static String getCurrentID ( ){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            return user.getUid();
        else
            return null;
    }

    public static DatabaseReference getCurrentDenuncia ( String key){
        if ( key != null ){
            return getDataBaseRef().child("Denuncia").child(key);
        }else
            return null;
    }

    public static DatabaseReference getDenunciasRef ( ){
        return getDataBaseRef().child("Denuncias");
    }

    public static String getDenunciaPath ( ){
        return "Denuncias/";
    }

    public static DatabaseReference getRecatistaRef ( ) {
        return getDataBaseRef().child("Recatistas/");
    }

    public static String getRecatistaPath(String user) { return "Recatistas/"; }

    public static DatabaseReference getCurrentRecatista ( String userKey ){
        if ( userKey != null)
            return getRecatistaRef().child(userKey);
        else
            return null;
    }

    public static DatabaseReference getRescatesFromCurrentRecatistaRef ( String userKey ){
        return getCurrentRecatista(userKey).child("Rescates");
    }

    public static DatabaseReference getFinDenuncia ( ) {
        return getDataBaseRef().child("FinDenuncias/");
    }

    public static DatabaseReference getDenunciantesRef ( ){
        return getDataBaseRef().child("Denunciantes");
    }

    public static DatabaseReference getCurrentDenunciantesRef ( String userID ){
        if ( userID != null )
            return getDenunciantesRef().child(userID);
        else
            return null;
    }

    public static DatabaseReference getFrases ( ){
        return getDataBaseRef().child("Frases");
    }

}



