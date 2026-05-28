package com.mundial.app.util;

public class PruebaPassword {

    public static void main(String[] args) {
        // PASO 1: hashear
        String passwordOriginal = "admin123";
        String hash = PasswordUtil.hashear(passwordOriginal);

        System.out.println("Password original: " + passwordOriginal);
        System.out.println("Hash generado:     " + hash);
        System.out.println();

        // PASO 2: verificar con la password correcta
        boolean coincide = PasswordUtil.verificar("admin123", hash);
        System.out.println("'admin123' vs hash -> " + coincide);

        // PASO 3: verificar con password incorrecta
        boolean noCoincide = PasswordUtil.verificar("otraCosa", hash);
        System.out.println("'otraCosa' vs hash -> " + noCoincide);

        // PASO 4: generar dos hashes de la misma password
        String hash1 = PasswordUtil.hashear("admin123");
        String hash2 = PasswordUtil.hashear("admin123");

        System.out.println();
        System.out.println("Hash 1: " + hash1);
        System.out.println("Hash 2: " + hash2);
        System.out.println("¿Son iguales? " + hash1.equals(hash2));

        String hash3 = PasswordUtil.hashear("admin123");
        System.out.println("Hash para 'admin123':");
        System.out.println(hash3);

    }
}
