package com.poopers.proyectocriptografia.comunicacion;

import com.poopers.proyectocriptografia.fileutils.CodificadorArchivo;
import com.poopers.proyectocriptografia.fileutils.GestorArchivo;
import com.poopers.proyectocriptografia.fileutils.SerializacionObjetos;
import com.poopers.proyectocriptografia.fileutils.VerificadorArchivo;
import com.poopers.proyectocriptografia.cifrado.CifradoRsa;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 * Entidad cuya responsabilidad es recibir archivos cifrados enviados por los
 * clientes, después de esto prueba con las llaves públicas registradas en la
 * autoridad certificadora para ver con cuál es posible regresar al mensaje
 * original, lo que autentica además quien fue el remitente.
 */
public class Servidor {

    private final int PUERTO_AUTORIDAD = 4321;
    private final String HOST_AUTORIDAD;
    private final int PUERTO_AS_SERVER = 9876;
    private List<Integer> idsArchivos;
    private List<List<String>> bloquesArchivos;
    private CifradoRsa cifradoRsa;
    private ServerSocket serverSocket;
    private UploadStartedListener startedListener;
    private UploadProgressListener progressListener;
    private UploadFinishedListener finishListener;

    public Servidor(String hostAutoridad) throws IOException {
        idsArchivos = new ArrayList<>();
        bloquesArchivos = new ArrayList<>();
        cifradoRsa = new CifradoRsa();
        // El socket se abre aquí para que siempre acepte mensajes
        serverSocket = new ServerSocket(PUERTO_AS_SERVER);
        HOST_AUTORIDAD = hostAutoridad;
    }

    public void receiveMessage() throws IOException {
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
        String[] partes = message.split(" ");
        output.flush();
        if (message.equals("NUEVO_ARCHIVO")) {
            System.out.println("Servidor> Nuevo archivo");
            idsArchivos.add(idsArchivos.size());
            bloquesArchivos.add(new ArrayList<>());
            output.println(idsArchivos.get(idsArchivos.size() - 1));
            if (startedListener != null) {
                startedListener.onUploadStart(idsArchivos.get(idsArchivos.size() - 1));
            }
        } else if (message.startsWith("ARCHIVO_TERMINADO")) {
            int idArchivo = Integer.parseInt(partes[1]);
            System.out.println("Servidor> Archivo terminado " + idArchivo);
            // Se obtiene una lista con las entidades certificadas
            String response = sendMessage(HOST_AUTORIDAD, PUERTO_AUTORIDAD,
                    "OBTENER_ENTIDADES_CERTIFICADAS", 1).get(0);
            byte[] decodedFile = CodificadorArchivo.decodeFile(response);
            GestorArchivo.writeBytes(decodedFile, "generated_files/entidadesCertificadasReceived");
            List<EntidadCertificada> entidades
                    = (List<EntidadCertificada>) SerializacionObjetos
                    .deserialize("generated_files/entidadesCertificadasReceived");
            output.println("Se recibieron todos los bloques");
            // Se desencriptarán los bloques y se verificará cuál llave lo hace
            // correctamente
            List<String> bloquesDesencriptados = new ArrayList<>();
            // Se busca la entidad que mandó el archivo
            String entidadChida = null;
            for (EntidadCertificada entidad : entidades) {
                bloquesDesencriptados.clear();
                for (String bloque : bloquesArchivos.get(idArchivo)) {
                    try {
                        String descifrado = cifradoRsa.decrypt(
                                DatatypeConverter.parseHexBinary(bloque),
                                entidad.getLlavePublica());
                        bloquesDesencriptados.add(descifrado);
                    } catch (Exception ex) {
                        break;
                    }
                }
                String decryptedFile = String.join("", bloquesDesencriptados);
                String filename = "generated_files/Decrypted_" + entidad.getNombre();
                GestorArchivo.writeBytes(CodificadorArchivo.decodeFile(decryptedFile), filename);
                if (bloquesDesencriptados.size() > 0
                        && VerificadorArchivo.verificar(filename)) {
                    entidadChida = entidad.getNombre();
                    break;
                }
            }
            if (finishListener != null) {
                finishListener.onUploadFinish(idArchivo, entidadChida);
            }
        } else if (message.startsWith("BLOQUE_ARCHIVO")) {
            int idArchivo = Integer.parseInt(partes[1]);
            bloquesArchivos.get(idArchivo).add(partes[2]);
            output.println("Bloque recibido");
            if (progressListener != null) {
                progressListener.onUploadProgress(idArchivo, bloquesArchivos.get(idArchivo).size());
            }
        }
    }

    public void setUploadStartedListener(UploadStartedListener startedListener) {
        this.startedListener = startedListener;
    }

    public void setUploadProgressListener(UploadProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void setUploadFinishedListener(UploadFinishedListener finishListener) {
        this.finishListener = finishListener;
    }

    public interface UploadStartedListener {

        void onUploadStart(int idArchivo);
    }

    public interface UploadFinishedListener {

        void onUploadFinish(int idArchivo, String entidadEmisora);
    }

    public interface UploadProgressListener {

        void onUploadProgress(int idArchivo, int bloques);
    }

    public static void main(String[] args) {
        try {
            Servidor servidor = new Servidor("localhost");
            while (true) {
                servidor.receiveMessage();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
