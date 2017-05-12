package com.poopers.proyectocriptografia.logica;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class FileUtils {

    public static String encodeFile(String filename) {
        byte[] fileBytes = readBytes(filename);
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    public static byte[] decodeFile(String encodedFile) {
        return Base64.getDecoder().decode(encodedFile);
    }

    private static byte[] readBytes(String filename) {
        File file = new File(filename);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte fileBytes[] = new byte[(int) file.length()];
            inputStream.read(fileBytes);
            return fileBytes;
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the File " + ioe);
        }
        return null;
    }

    public static void writeFile(byte[] fileBytes, String filename) {
        FileOutputStream imageOutFile = null;
        try {
            imageOutFile = new FileOutputStream(filename);
            imageOutFile.write(fileBytes);
            imageOutFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
