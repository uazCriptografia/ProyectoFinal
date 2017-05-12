package com.poopers.proyectocriptografia.logica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {

    private final int PUERTO_AUTORIDAD = 4321;
    private final String HOST_AUTORIDAD = "localhost";
    private static final int PUERTO = 9876;
    private List<Integer> idsArchivos;
    private List<List<String>> bloquesArchivos;

    public Servidor() {
        idsArchivos = new ArrayList<>();
        bloquesArchivos = new ArrayList<>();
    }

    public void receiveMessage() throws IOException {
        // Socket de servidor para recibir mensajes
        ServerSocket serverSocket = new ServerSocket(PUERTO);
        // Socket del cliente que se conecta al servidor
        Socket sourceSocket = serverSocket.accept();
        // Reader para leer los mensajes del cliente
        BufferedReader clientInput = new BufferedReader(
                new InputStreamReader(sourceSocket.getInputStream()));
        // Writer para enviar respuestas al cliente
        PrintStream output = new PrintStream(sourceSocket.getOutputStream());
        // Lee mensaje del cliente
        String message = clientInput.readLine();
        // Se manda procesar el mensaje
        procesarMensaje(output, message);
        // Cierra la conexión
        sourceSocket.close();
        serverSocket.close();
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

    private void procesarMensaje(PrintStream output, String message)
            throws IOException {
        System.out.println("Servidor.procesarMensaje");
        String[] partes = message.split(" ");
        output.flush();
        if (message.equals("NUEVO_ARCHIVO")) {
            System.out.println(">> Procesar NUEVO_ARCHIVO");
            idsArchivos.add(idsArchivos.size());
            bloquesArchivos.add(new ArrayList<>());
            output.println(idsArchivos.get(idsArchivos.size() - 1));
        } else if (message.startsWith("ARCHIVO_TERMINADO")) {
            System.out.println(">> Procesar ARCHIVO_TERMINADO");
            String response = sendMessage(HOST_AUTORIDAD, PUERTO_AUTORIDAD,
                    "OBTENER_ENTIDADES_CERTIFICADAS", 1).get(0);
            byte[] decodedFile = FileUtils.decodeFile(response);
            FileUtils.writeFile(decodedFile, "entidadesCertificadasReceived");
            List<EntidadCertificada> entidades
                    = (List<EntidadCertificada>) Serializacion
                    .deserialize("entidadesCertificadasReceived");
            System.out.println(entidades);
            output.println("Se recibieron todos los bloques");
            // TODO Probar descifrar los archivos con cada una de las llaves 
            // públicas de las entidades certificadas para determinar cuál
            // fue el remitente.
        } else if (message.startsWith("BLOQUE_ARCHIVO")) {
            System.out.println(">> Procesar BLOQUE_ARCHIVO");
            int idArchivo = Integer.parseInt(partes[1]);
            bloquesArchivos.get(idArchivo).add(partes[2]);
            output.println("Bloque recibido");
        }
    }

    public static void main(String[] args) {
        try {
            Servidor servidor = new Servidor();
            while (true) {
                servidor.receiveMessage();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
