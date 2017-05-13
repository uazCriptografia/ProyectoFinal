package com.poopers.proyectocriptografia;

import com.poopers.proyectocriptografia.comunicacion.AutoridadCertificadora;
import com.poopers.proyectocriptografia.comunicacion.Servidor;
import com.poopers.proyectocriptografia.pantallas.EmisorArchivoForm;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AutoridadCertificadora.main(args);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Servidor.main(args);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                EmisorArchivoForm.main(args);
            }
        }).start();
    }

}
