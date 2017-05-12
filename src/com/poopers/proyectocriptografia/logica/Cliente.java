package com.poopers.proyectocriptografia.logica;

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

public class Cliente {

    private final int PUERTO_AUTORIDAD = 4321;
    private final String HOST_AUTORIDAD = "localhost";
    private final int PUERTO_SERVIDOR = 9876;
    private final String HOST_SERVIDOR = "localhost";
    private String usuario;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private CifradoRsa cifradoRsa;

    public Cliente(String usuario) {
        this.usuario = usuario;
        cifradoRsa = new CifradoRsa();
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
            respuestas.add(input.readLine());
        }
        // Cierra la conexión
        clientSocket.close();
        // Se regresan las respuestas
        return respuestas;
    }

    public void sendFile(String filename) throws IOException {
        System.out.println("Cliente(" + usuario + ").sendFile");
        String idArchivo = sendMessage(HOST_SERVIDOR, PUERTO_SERVIDOR,
                "NUEVO_ARCHIVO", 1).get(0);
        String encodedFile = FileUtils.encodeFile(filename);
        int inicio = 0;
        int fin = 117;
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
            sendMessage(HOST_SERVIDOR, PUERTO_SERVIDOR, "BLOQUE_ARCHIVO "
                    + idArchivo + " " + cifradoRsa.encrypt(bloque, privateKey),
                    1);
        }
        sendMessage(HOST_SERVIDOR, PUERTO_SERVIDOR, "ARCHIVO_TERMINADO "
                + idArchivo, 1);
    }

    public void solicitarLLaves() throws IOException {
        System.out.println("Cliente(" + usuario + ").solicitarLLaves");
        String privateFilename = "privateReceived_" + usuario;
        String publicFilename = "publicReceived_" + usuario;
        List<String> respuestas = sendMessage(HOST_AUTORIDAD, PUERTO_AUTORIDAD,
                "GENERAR_LLAVES " + usuario, 2);
        byte[] decodedPublic = FileUtils.decodeFile(respuestas.get(0));
        byte[] decodedPrivate = FileUtils.decodeFile(respuestas.get(1));
        FileUtils.writeFile(decodedPublic, publicFilename);
        FileUtils.writeFile(decodedPrivate, privateFilename);
        publicKey = (PublicKey) Serializacion.deserialize(publicFilename);
        privateKey = (PrivateKey) Serializacion.deserialize(privateFilename);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void main(String[] args) {
        try {
            Cliente cliente = new Cliente("usuario1");
            cliente.solicitarLLaves();
            cliente.sendFile("texto.txt");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
