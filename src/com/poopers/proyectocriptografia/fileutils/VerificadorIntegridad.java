package com.poopers.proyectocriptografia.fileutils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class VerificadorIntegridad {

    public static boolean verificar(String filename) {
        try {
            File file = new File(filename);
            Desktop.getDesktop().open(file);
            return true;
        } catch (IllegalArgumentException iae) {
            System.err.println("El archivo no existe");
        } catch (IOException ioe) {
            System.err.println("El archivo no se puede abrir (inválido)");
        }
        return false;
    }
}
