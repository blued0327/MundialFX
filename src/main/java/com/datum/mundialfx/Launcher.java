package com.datum.mundialfx;

//esta clase existe SOLO para arrancar la app desde el fat-jar
//javafx no permite que la clase con main extienda Application directamente
//cuando se empaqueta con shade -- por eso este intermediario
public class Launcher {

    public static void main(String[] args) {
        App.main(args);
    }
}
