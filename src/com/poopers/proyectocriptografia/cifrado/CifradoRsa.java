package com.poopers.proyectocriptografia.cifrado;

import com.poopers.proyectocriptografia.fileutils.CodificadorArchivo;
import com.poopers.proyectocriptografia.fileutils.GestorArchivo;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

/**
 * Esta clase se encarga del cifrado y descifrado de texto por medio del
 * algoritmo RSA.
 */
public class CifradoRsa {

    /**
     * Genera el par de llaves pública y privada para que una entidad pueda
     * cifrar y descifrar.
     *
     * @return Instancia de KeyPair que contiene las llaves.
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
     * Realiza el cifrado con el texto y llave dados.
     *
     * @param text El texto que se cifrará (no más de 117 bytes).
     * @param key La llave con la que se cifrará (si es la pública el descifrado
     * se deberá hacer con la privada, y viceversa).
     * @return Arreglo de bytes que representan el resultado del cifrado.
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
        }
        return cipherText;
    }

    /**
     * Realiza el descifrado con el arreglo de bytes y la llave dados.
     *
     * @param text Es el texto a descifrar.
     * @param key Es la llave con la que se descifrará (Si se cifró con la
     * pública, deberá pasarse la privada y viceversa).
     * @return String que representa el resultado del descifrado.
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
//            ex.printStackTrace();
        }
        return new String(dectyptedText);
    }

    /**
     * Ejecuta las pruebas para esta utilidad.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            testLongerFile();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CifradoRsa.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Caso de prueba para el cifrado y descifrado
     *
     * @throws UnsupportedEncodingException
     */
    public static void testLongerFile() throws UnsupportedEncodingException {
        CifradoRsa cifradoRsa = new CifradoRsa();
        cifradoRsa.generateKey();
        KeyPair keyPair = cifradoRsa.generateKey();

        String encodedFile = CodificadorArchivo.encodeFile("imagen.png");
        int inicio = 0;
        int fin = 117;
        int countBloques = 0;
        List<String> bloquesOriginales = new ArrayList<>();
        List<String> bloquesCifrados = new ArrayList<>();
        List<String> bloquesDescifrados = new ArrayList<>();
        while (fin <= encodedFile.length()) {
            bloquesOriginales.add(encodedFile.substring(inicio, fin));
            inicio += 117;
            fin += 117;
        }
        System.out.println("Original: " + bloquesOriginales.get(0));
        int restantes = encodedFile.length() - inicio;
        if (restantes < 117) {
            bloquesOriginales.add(encodedFile.substring(
                    encodedFile.length() - restantes, encodedFile.length()));
        }
        for (int i = 0; i < bloquesOriginales.size(); i++) {
            String cifrado = DatatypeConverter.printHexBinary(
                    cifradoRsa.encrypt(bloquesOriginales.get(i),
                            keyPair.getPrivate()));
            bloquesCifrados.add(cifrado);
        }
        System.out.println("Cifrado: " + bloquesCifrados.get(0));
        for (int i = 0; i < bloquesCifrados.size(); i++) {
            String descifrado = cifradoRsa.decrypt(
                    DatatypeConverter.parseHexBinary(bloquesCifrados.get(i)),
                    keyPair.getPublic());
            bloquesDescifrados.add(descifrado);
        }
        System.out.println("Descifrado: " + bloquesDescifrados.get(0));
        System.out.println("Correcto: " + bloquesOriginales.equals(bloquesDescifrados));
        String decryptedFile = String.join("", bloquesDescifrados);
        GestorArchivo.writeBytes(CodificadorArchivo.decodeFile(decryptedFile), "generated_files/testCifrado");
    }

    /*
    ----------------------------------------------------------------------------
    Ejemplo de cifrado de textos grandes encontrado en internet
    ----------------------------------------------------------------------------
     */
 /*
    Cipher myCipher;
    KeyPair keyPair;

    public String encrypt(String plaintext) throws Exception {
        keyPair = generateKey();
        myCipher = Cipher.getInstance("RSA");
        myCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        byte[] bytes = plaintext.getBytes("UTF-8");
        byte[] encrypted = blockCipher(bytes, Cipher.ENCRYPT_MODE);
        char[] encryptedTranspherable = DatatypeConverter.printHexBinary(encrypted).toCharArray();
        return new String(encryptedTranspherable);
    }

    public String decrypt(String encrypted) throws Exception {
        myCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] bts = DatatypeConverter.parseHexBinary(encrypted);
        byte[] decrypted = blockCipher(bts, Cipher.DECRYPT_MODE);
        return new String(decrypted, "UTF-8");
    }

    private byte[] blockCipher(byte[] bytes, int mode)
            throws IllegalBlockSizeException, BadPaddingException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];
        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE) ? 100 : 128;
        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)) {
                //execute the operation
                scrambled = myCipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn, scrambled);
                // here we calculate the length of the next buffer required
                int newlength = length;
                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newlength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newlength];
            }
            // copy byte into our buffer.
            buffer[i % length] = bytes[i];
        }
        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = myCipher.doFinal(buffer);
        // final step before we can return the modified data.
        toReturn = append(toReturn, scrambled);
        return toReturn;
    }

    private byte[] append(byte[] prefix, byte[] suffix) {
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i = 0; i < prefix.length; i++) {
            toReturn[i] = prefix[i];
        }
        for (int i = 0; i < suffix.length; i++) {
            toReturn[i + prefix.length] = suffix[i];
        }
        return toReturn;
    }
     */
}
