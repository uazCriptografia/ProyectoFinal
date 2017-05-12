package com.poopers.proyectocriptografia.logica;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * Esta clase representa un registro de una entidad que fue certificada por la
 * autoridad certificadora, que almacena su nombre y su llave pública.
 */
public class EntidadCertificada implements Serializable {

    private String nombre;
    private PublicKey llavePublica;

    public EntidadCertificada(String nombre, PublicKey llavePublica) {
        this.nombre = nombre;
        this.llavePublica = llavePublica;
    }

    public String getNombre() {
        return nombre;
    }

    public PublicKey getLlavePublica() {
        return llavePublica;
    }

    @Override
    public String toString() {
        return "EntidadCertificada(" + nombre + ")";
    }
}
