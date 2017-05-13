package com.poopers.proyectocriptografia.comunicacion;

import com.poopers.proyectocriptografia.fileutils.CodificadorArchivo;
import com.poopers.proyectocriptografia.fileutils.GestorArchivo;
import com.poopers.proyectocriptografia.fileutils.SerializacionObjetos;
import com.poopers.proyectocriptografia.cifrado.CifradoRsa;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.bind.DatatypeConverter;

public class Cliente {

    private final int PUERTO_AUTORIDAD = 4321;
    private final String HOST_AUTORIDAD;
    private final int PUERTO_SERVIDOR = 9876;
    private final String HOST_SERVIDOR;
    private String usuario;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private CifradoRsa cifradoRsa;

    public Cliente(String usuario, String hostAutoridad, String hostServidor) {
        this.usuario = usuario;
        cifradoRsa = new CifradoRsa();
        HOST_AUTORIDAD = hostAutoridad;
        HOST_SERVIDOR = hostServidor;
    }

    private List<String> sendMessage(String host, int puerto, String message,
            int expectedResponses) throws IOException {
        // Lista donde se almacenarán las respuestas
        List<String> respuestas = new ArrayList<>();
        // Socket de cliente para conectarse a un servidor
        Socket clientSocket = new Socket(host, puerto);
        // Reader para leer las respuestas del servidor
        BufferedReader input = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        // Writer para mandar mensajes al servidor
        PrintStream output = new PrintStream(clientSocket.getOutputStream());
        // Envía un mensaje al servidor
        output.println(message);
        // Se leen todas las respuestas del servidor
        for (int i = 0; i < expectedResponses; i++) {
            // Agrega la respuesta actual a la lista
            boolean error = true;
            while (error) {
                try {
                    respuestas.add(input.readLine());
                    error = false;
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
            // Verifica si la respuesta es de error, para dejar de recibirlas
            if (respuestas.get(i).equals("ERROR")) {
                break;
            }
        }
        // Cierra la conexión
        clientSocket.close();
        // Se regresan las respuestas
        return respuestas;
    }

    public void sendFile(String filename) throws IOException {
        System.out.println("Cliente> Enviará archivo " + filename);
        if (publicKey == null) {
            System.err.println("No tiene llaves");
            return;
        }
        String idArchivo = null;
        boolean error = true;
        while (error) {
            try {
                idArchivo = sendMessage(HOST_SERVIDOR, PUERTO_SERVIDOR,
                        "NUEVO_ARCHIVO", 1).get(0);
                error = false;
            } catch (Exception ex) {
            }
        }
        String encodedFile = CodificadorArchivo.encodeFile(filename);
        int inicio = 0;
        int fin = 117;
        int countBloques = 0;
        List<String> bloques = new ArrayList<>();
        while (fin <= encodedFile.length()) {
            bloques.add(encodedFile.substring(inicio, fin));
            inicio += 117;
            fin += 117;
        }
        int restantes = encodedFile.length() - inicio;
        if (restantes < 117) {
            bloques.add(encodedFile.substring(
                    encodedFile.length() - restantes, encodedFile.length()));
        }
        for (String bloque : bloques) {
            error = true;
            while (error) {
                try {
                    String cifrado = DatatypeConverter.printHexBinary(
                            cifradoRsa.encrypt(bloque, privateKey));
                    sendMessage(HOST_SERVIDOR, PUERTO_SERVIDOR, "BLOQUE_ARCHIVO "
                            + idArchivo + " " + cifrado, 1);
                    error = false;
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
//            System.out.println(countBloques++ + " bloques de " + encodedFile.length() / 117);
        }
//        System.out.println("Original de " + encodedFile.length() + " bytes");
        error = true;
        while (error) {
            try {
                sendMessage(HOST_SERVIDOR, PUERTO_SERVIDOR, "ARCHIVO_TERMINADO "
                        + idArchivo, 1);
                error = false;
            } catch (Exception ex) {

            }
        }

    }

    public void solicitarLLaves() throws IOException {
//        System.out.println("Cliente(" + usuario + ").solicitarLLaves");
        String privateFilename = "privateReceived_" + usuario;
        String publicFilename = "publicReceived_" + usuario;
        List<String> respuestas = sendMessage(HOST_AUTORIDAD, PUERTO_AUTORIDAD,
                "GENERAR_LLAVES " + usuario, 2);
        if (!respuestas.get(0).equals("ERROR")) {
            byte[] decodedPublic = CodificadorArchivo.decodeFile(respuestas.get(0));
            byte[] decodedPrivate = CodificadorArchivo.decodeFile(respuestas.get(1));
            GestorArchivo.writeBytes(decodedPublic, "generated_files/" + publicFilename);
            GestorArchivo.writeBytes(decodedPrivate, "generated_files/" + privateFilename);
            publicKey = (PublicKey) SerializacionObjetos.deserialize("generated_files/" + publicFilename);
            privateKey = (PrivateKey) SerializacionObjetos.deserialize("generated_files/" + privateFilename);
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Inserta el nombre del cliente");
            Cliente cliente = new Cliente(scanner.nextLine(), "localhost",
                    "localhost");
            cliente.solicitarLLaves();
            System.out.println("Inserta el nombre del archivo");
            cliente.sendFile(scanner.nextLine());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
