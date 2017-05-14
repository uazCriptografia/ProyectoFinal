package com.poopers.proyectocriptografia;

import com.poopers.proyectocriptografia.comunicacion.AutoridadCertificadora;
import com.poopers.proyectocriptografia.pantallas.EmisorArchivoForm;
import com.poopers.proyectocriptografia.pantallas.ReceptorArchivoForm;

/**
 * Esta clase de prueba inicializa la autoridad certificadora, el formulario del
 * servidor receptor de archivos y una instancia del formulario del cliente
 * emisor de archivos para ejecutarlos todos en una misma máquina.
 */
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
                EmisorArchivoForm.main(args);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ReceptorArchivoForm.main(args);
            }
        }).start();
    }

}
