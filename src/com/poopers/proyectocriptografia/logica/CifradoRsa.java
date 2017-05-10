/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.poopers.proyectocriptografia.logica;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

/**
 *
 * @author porfirio
 */
public class CifradoRsa {

    /**
     * Genera el KeyPair que contiene las llaves pública y privada usando 1024
     * bytes.
     *
     * @return El objeto KeyPair con las llaves pública y privada.
     */
    public KeyPair generateKey() {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();
            return key;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CifradoRsa.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        return null;
    }

    /**
     * Cifra el texto en claro..
     *
     * @param text Es el texto en claro a cifrar.
     * @param key Es la llave con la que se cifrará.
     * @return Arreglo de bytes con el texto cifrado.
     */
    public byte[] encrypt(String text, Key key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * Descifra el texto cifrado.
     *
     * @param text Es el texto cifrado a descifrar.
     * @param key Es la llave con la que se descifrará.
     * @return Cadena con el resultado del descifrado.
     */
    public String decrypt(byte[] text, Key key) {
        byte[] dectyptedText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            dectyptedText = cipher.doFinal(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new String(dectyptedText);
    }

    /**
     * Test the EncryptionUtil
     */
    public static void main(String[] args) {
        CifradoRsa cifradoRsa = new CifradoRsa();
        cifradoRsa.generateKey();
        KeyPair keyPair = cifradoRsa.generateKey();
        String originalText = "Texto que se cifrará con clave pública";
        byte[] cipherText = cifradoRsa.encrypt(originalText,
                keyPair.getPublic());
        String plainText = cifradoRsa.decrypt(cipherText,
                keyPair.getPrivate());
        // Printing the Original, Encrypted and Decrypted Text
        System.out.println(" Original: " + originalText);
        System.out.println("Encrypted: " + cipherText.toString());
        System.out.println("Decrypted: " + plainText);

        originalText = "Texto que se cifrará con clave privada";
        cipherText = cifradoRsa.encrypt(originalText,
                keyPair.getPrivate());
        plainText = cifradoRsa.decrypt(cipherText,
                keyPair.getPublic());
        // Printing the Original, Encrypted and Decrypted Text
        System.out.println(" Original: " + originalText);
        System.out.println("Encrypted: " + cipherText.toString());
        System.out.println("Decrypted: " + plainText);
    }
}
