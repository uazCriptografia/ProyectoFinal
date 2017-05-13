package com.poopers.proyectocriptografia.comunicacion;

import com.poopers.proyectocriptografia.fileutils.CodificadorArchivo;
import com.poopers.proyectocriptografia.fileutils.SerializacionObjetos;
import com.poopers.proyectocriptografia.cifrado.CifradoRsa;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta entidad tiene la responsabilidad de generar las llaves públicas y
 * privadas de las entidades que lo soliciten, así como almacenar las llaves
 * públicas de las entidades para así poder dárselas a cualquier entidad que lo
 * solicite y que la entidad solicitadora y la entidad involucrada puedan tener
 * un intercambio de mensajes autenticados.
 */
public class AutoridadCertificadora {

    private List<EntidadCertificada> entidadesCertificadas;
    private CifradoRsa cifradoRsa;
    private static final int PUERTO = 4321;

    public AutoridadCertificadora() {
        entidadesCertificadas = new ArrayList<>();
        cifradoRsa = new CifradoRsa();
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
        // Cierra el cliente
        sourceSocket.close();
        // Cierra el servidor
        serverSocket.close();
    }

    private void procesarMensaje(PrintStream output, String mensaje) {
        System.out.println("AutoridadCertificadora.procesarMensaje");
        String[] partes = mensaje.split(" ");
        if (mensaje.startsWith("GENERAR_LLAVES")) {
            System.out.println(">> Procesar GENERAR_LLAVES");
            String privateFilename = "generated_files/privateSent_" + partes[1];
            String publicFilename = "generated_files/publicSent_" + partes[1];
            KeyPair keyPair = generarClaves(partes[1]);
            output.flush();
            if (keyPair == null) {
                output.println("ERROR");
            } else {
                SerializacionObjetos.serialize(keyPair.getPublic(), publicFilename);
                SerializacionObjetos.serialize(keyPair.getPrivate(), privateFilename);
                output.println(CodificadorArchivo.encodeFile(publicFilename));
                output.println(CodificadorArchivo.encodeFile(privateFilename));
            }
        } else if (mensaje.startsWith("OBTENER_ENTIDADES_CERTIFICADAS")) {
            SerializacionObjetos.serialize(entidadesCertificadas,
                    "generated_files/entidadesCertificadas");
            output.flush();
            output.println(CodificadorArchivo.encodeFile("generated_files/entidadesCertificadas"));
        }
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
        if (buscarEntidad(nombreEntidad) == null) {
            KeyPair keyPair = new CifradoRsa().generateKey();
            EntidadCertificada nuevaEntidad = new EntidadCertificada(nombreEntidad,
                    keyPair.getPublic());
            entidadesCertificadas.add(nuevaEntidad);
            return keyPair;
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            AutoridadCertificadora autoridad = new AutoridadCertificadora();
            while (true) {
                autoridad.receiveMessage();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
