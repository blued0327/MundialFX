
package com.mundial.app.util;

import java.net.InetAddress;//libreria para optener mi ip

public class IpUtil {

    //sacar ip del equipo actual
    public static String obtenerIp() {

        try {

            //obtiene la ip de la pc
            return InetAddress
                    .getLocalHost()
                    .getHostAddress();

        } catch (Exception e) {

            e.printStackTrace();

            return "Descopnocida";
        }
    }
}
