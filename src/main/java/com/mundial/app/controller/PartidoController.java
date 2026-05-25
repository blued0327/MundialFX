// MIGRACION: package cambiado de "controller" a "com.mundial.app.controller"
package com.mundial.app.controller;

// MIGRACION: import ajustado de "dao.PartidoDao" a "com.mundial.app.dao.PartidoDao"
import com.mundial.app.dao.PartidoDao;
// MIGRACION: import ajustado de "model.PartidoModel" a "com.mundial.app.model.PartidoModel"
import com.mundial.app.model.PartidoModel;

import java.time.LocalDateTime;
import java.util.List;

public class PartidoController {

    private final PartidoDao dao = new PartidoDao();

    // INSERTAR
    public boolean insertarPartido(String equipoLocal, String equipoVisitante,
            LocalDateTime fecha, String estadio,
            String ciudad, int capacidad, String estado) {
        PartidoModel p = new PartidoModel(
                equipoLocal, equipoVisitante,
                fecha, estadio, ciudad, capacidad, estado
        );
        return dao.insertar(p) > 0;
    }

    // ACTUALIZAR
    public boolean actualizarPartido(int id, String equipoLocal, String equipoVisitante,
            LocalDateTime fecha, String estadio,
            String ciudad, int capacidad, String estado) {
        PartidoModel p = new PartidoModel(
                id, equipoLocal, equipoVisitante,
                fecha, estadio, ciudad, capacidad, estado, null
        );
        return dao.actualizar(p);
    }

    // ELIMINAR pone cancelado vía SP
    /*
    public boolean eliminarPartido(int id) {
        return dao.eliminar(id);
    }*/
    public boolean cambiarEstado(int id, String estado) {
        return dao.cambiarEstado(id, estado);
    }

    // LISTAR TODOS
    public List<PartidoModel> listarPartidos() {
        return dao.listarTodos();
    }

    // BUSCAR POR ID
    public PartidoModel buscarPorId(int id) {
        return dao.buscarPorId(id);
    }

    // BUSCAR POR EQUIPO
    public List<PartidoModel> buscarPorEquipo(String texto) {
        return dao.buscarPorEquipo(texto);
    }

    // LISTAR SOLO DISPONIBLES — útil para el módulo de Tickets y Ventas
    public List<PartidoModel> listarDisponibles() {
        return dao.listarTodos()
                .stream()
                .filter(p -> PartidoDao.ESTADO_DISPONIBLE.equals(p.getEstado()))
                .toList();
    }
}
