package com.mundial.app.controller;

import com.mundial.app.dao.LogDao;
import com.mundial.app.model.LogModel;
import java.util.ArrayList;
import java.util.List;

public class LogController {

    //instancia del dao para mandar a llamar la bd
    private final LogDao dao = new LogDao();

    //cuantos logs voy a cargar por pagina //esto lo hice para no traer todo de golpe
    private static final int LIMITE = 20;

    //pagina actual que estoy viendo
    private int paginaActual = 0;

    //registrar accion de usuario//login//Logiut//fallido
    public boolean registrar(Integer usuarioId, String accion, String ip) {

        try {

            //si devuelve mayor a 0 si se inserto
            int id = dao.registrar(usuarioId, accion, ip);

            return id > 0;

        } catch (Exception e) {

            System.err.println("Error al registrar log: " + e.getMessage());

            return false;
        }
    }

    //cargar logs de la pagina actual//esto usa limit y offset en sql
    public List<LogModel> listarActual() {

        try {

            //offset = desde donde empieza
            int offset = paginaActual * LIMITE;

            return dao.listar(LIMITE, offset);

        } catch (Exception e) {

            System.err.println("Error al listar logs: " + e.getMessage());

            return new ArrayList<>();
        }
    }

    //ir a la siguiente pagina
    //solo avanza si todavia hay mas registros
    public List<LogModel> siguientePagina() {

        int total = dao.contar();

        //si todavia no llegamos al final avanzamos
        if ((paginaActual + 1) * LIMITE < total) {
            paginaActual++;
        }

        return listarActual();
    }

    //volver a la pagina anterior
    //evita que quede en negativos
    public List<LogModel> anteriorPagina() {

        if (paginaActual > 0) {
            paginaActual--;
        }

        return listarActual();
    }

    //reiniciar paginacion
    //esto sirve cuando entras otra vez a la vista //o cuando haces algun filtro nuevo
    public void reiniciar() {
        paginaActual = 0;
    }

    //devuelve en que pagina voy
    public int getPaginaActual() {
        return paginaActual;
    }

    //total de logs que hay en la bd
    public int contar() {

        try {

            return dao.contar();

        } catch (Exception e) {

            System.err.println("Error al contar logs: " + e.getMessage());

            return 0;
        }
    }

    //para saber cuantas paginas hay en total
    //uso math.ceil para redondear hacia arriba
    public int totalPaginas() {

        int total = contar();

        return (int) Math.ceil((double) total / LIMITE);
    }
}
