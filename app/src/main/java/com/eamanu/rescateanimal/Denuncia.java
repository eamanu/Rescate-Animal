package com.eamanu.rescateanimal;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by eamanu on 2/11/17.
 */

@IgnoreExtraProperties
public class Denuncia {
    private String Userid;
    private String Timestamp;
    private String Comentario;
    private double Latitude;
    private double Longitude;
    private String Pais;
    private String Provincia;
    private String Direccion;
    private String pathPhoto;

    public void setUserid(String userid) {
        Userid = userid;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public void setComentario(String comentario) {
        Comentario = comentario;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public void setPais(String pais) {
        Pais = pais;
    }

    public void setProvincia(String provincia) {
        Provincia = provincia;
    }

    public void setDireccion(String direccion) {
        Direccion = direccion;
    }

    public void setPathPhoto(String pathPhoto) {
        this.pathPhoto = pathPhoto;
    }


    public String getUserid() {
        return Userid;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public String getComentario() {
        return Comentario;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public String getPais() {
        return Pais;
    }

    public String getProvincia() {
        return Provincia;
    }

    public String getDireccion() {
        return Direccion;
    }

    public String getPathPhoto() {
        return pathPhoto;
    }


    /**
     * Default constructor
     */
    public Denuncia ( ){

    }

    /**
     * Constructor
     *
     * @param userid
     * @param timestamp
     * @param comentario
     * @param lat
     * @param lng
     * @param pais
     * @param provincia
     * @param direccion
     */
    public Denuncia ( String userid, String timestamp,
                      String comentario, double lat,
                      double lng, String pais,
                      String provincia, String direccion, String pathToPhoto){


        this.Userid = userid;
        this.Timestamp = timestamp;
        this.Comentario = comentario;
        this.Latitude = lat;
        this.Longitude = lng;
        this.Pais = pais;
        this.Provincia = provincia;
        this.Direccion = direccion;
        this.pathPhoto = pathToPhoto;
    }
}
