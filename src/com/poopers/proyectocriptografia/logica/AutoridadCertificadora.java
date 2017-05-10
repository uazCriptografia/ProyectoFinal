package com.poopers.proyectocriptografia.logica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta entidad tiene la responsabilidad de generar las llaves públicas y
 * privadas de las entidades que lo soliciten, así como almacenar las llaves
 * públicas de las entidades para así poder dárselas a cualquier entidad que lo
 * solicite y que la entidad solicitadora y la entidad involucrada puedan tener
 * un intercambio de mensajes autenticados.
 */
public class AutoridadCertificadora {

    private List<EntidadCertificada> entidadesCertificadas;
    private static final int PUERTO = 4321;

    public AutoridadCertificadora() {
        entidadesCertificadas = new ArrayList<>();
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
        System.out.println("Destino> Recibiendo el mensaje...");
        // Lee mensaje del cliente
        String message = clientInput.readLine();
        // Limpia la entrada
        output.flush();
        // Envía la respuesta al cliente
        output.println("Destino> Mensaje recibido");
        System.out.println("Destino> Mensaje recibido");
        // Cierra la conexión
        sourceSocket.close();
    }

    public EntidadCertificada buscarEntidad(String nombreEntidad) {
        for (EntidadCertificada entidad : entidadesCertificadas) {
            if (entidad.getNombre().equals(nombreEntidad)) {
                return entidad;
            }
        }
        return null;
    }

    /**
     * Genera las claves pública y privada para una nueva entidad.
     *
     * @param nombreEntidad Es el nombre de la entidad a certificar.
     * @return Las claves en una sola cadena separadas por una coma, por
     * ejemplo: "123321312,312321321" el primer número es la clave pública y el
     * segundo es la privada.
     */
    public KeyPair generarClaves(String nombreEntidad) {
        KeyPair keyPair = new CifradoRsa().generateKey();
        EntidadCertificada nuevaEntidad = new EntidadCertificada(nombreEntidad,
                keyPair.getPublic());
        entidadesCertificadas.add(nuevaEntidad);
        return keyPair;
    }

    public static void main(String[] args) {
        try {
            AutoridadCertificadora autoridad = new AutoridadCertificadora();
            autoridad.receiveMessage();
        } catch (IOException ex) {
            Logger.getLogger(AutoridadCertificadora.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
