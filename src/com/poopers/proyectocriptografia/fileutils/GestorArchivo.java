package com.poopers.proyectocriptografia.fileutils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utilidad para la escritura y lectura de archivos por medio de sus bytes.
 */
public class GestorArchivo {

    public static void writeBytes(byte[] fileBytes, String filename) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filename);
            fos.write(fileBytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
