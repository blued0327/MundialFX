/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.controller;

import com.mundial.app.dao.ClienteDao;
import com.mundial.app.model.ClienteModel;
import java.util.List;

public class ClienteController {

    private final ClienteDao dao = new ClienteDao();

    // Listar todos
    public List<ClienteModel> obtenerClientes() {
        return dao.listarClientes();
    }

    // Registrar
    public boolean registrarCliente(String nombre, String apellido, String telefono, String email, String direccion) {
        ClienteModel cm = new ClienteModel();
        cm.setNombre(nombre);
        cm.setApellido(apellido);
        cm.setTelefono(telefono);
        cm.setEmail(email);
        cm.setDireccion(direccion);
        return dao.registrarCliente(cm);
    }

    // Actualizar
    public boolean actualizarCliente(int id, String nombre, String apellido, String telefono, String email, String direccion) {
        ClienteModel cm = new ClienteModel();
        cm.setId(id);
        cm.setNombre(nombre);
        cm.setApellido(apellido);
        cm.setTelefono(telefono);
        cm.setEmail(email);
        cm.setDireccion(direccion);
        return dao.actualizarCliente(cm);
    }

    // Cambiar estado (activo/inactivo)
    public boolean cambiarEstado(int id, boolean estado) {
        return dao.cambiarEstado(id, estado);
    }

    // Buscar por ID
    public ClienteModel buscarPorId(int id) {
        return dao.buscarPorId(id);
    }

    // Buscar por nombre
    public List<ClienteModel> buscarPorNombre(String texto) {
        return dao.buscarPorNombre(texto);
    }
}
