package com.poopers.proyectocriptografia.fileutils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utilidad para la serialización y deserialización de objetos en archivos.
 */
public class SerializacionObjetos {

    /**
     * deserialize to Object from given file. We use the general Object so as
     * that it can work for any Java Class.
     */
    public static Object deserialize(String fileName) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * serialize the given object and save it to given file
     */
    public static File serialize(Object obj, String fileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
            return new File(fileName);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
