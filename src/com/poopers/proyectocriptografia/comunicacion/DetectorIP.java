package com.poopers.proyectocriptografia.comunicacion;

import com.poopers.proyectocriptografia.pantallas.EmisorArchivoForm;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetectorIP {

    public static String detectarIP() {
        try {
            Enumeration< NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface actual = interfaces.nextElement();
                if (actual.isLoopback()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : actual.getInterfaceAddresses()) {
                    InetAddress inetAddress = interfaceAddress.getAddress();
                    if (!(inetAddress instanceof Inet4Address)) {
                        continue;
                    }
                    if(actual.getName().equals("wlan0")) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(EmisorArchivoForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
