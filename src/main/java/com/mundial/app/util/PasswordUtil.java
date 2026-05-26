
package com.mundial.app.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hashear(String passwordNormal) {
        return BCrypt.hashpw(passwordNormal, BCrypt.gensalt(12));//el 12 es costo cuantas veces se repite entre mas mejor seguridad pero mas lento
    }

    //verificar la password
    //agarra el hash//extrae el salt/ vuelve a hashear la passowrd con el mismo salt/ compara hasehs devuelve true si coinciden
    public static boolean verificar(String passwordNormal, String hash) {
        if (passwordNormal == null || hash == null) {
            return false;
        }
        //sino es un hash lo que se pasa, tira el error
        try {
            boolean resultado = BCrypt.checkpw(passwordNormal, hash); //esta funcion devuelve un bool
            return resultado;

        } catch (IllegalArgumentException e) {
            System.out.println("hash invalido: " + e.getMessage());
            return false;
        }

    }
}
