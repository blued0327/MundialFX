/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
// MIGRACION: package cambiado de "controller" a "com.mundial.app.controller"
package com.mundial.app.controller;

// MIGRACION: import ajustado de "dao.ClienteDao" a "com.mundial.app.dao.ClienteDao"
import com.mundial.app.dao.ClienteDao;
// MIGRACION: import ajustado de "model.ClienteModel" a "com.mundial.app.model.ClienteModel"
import com.mundial.app.model.ClienteModel;
import java.util.List;

public class ClientesController {

    private final ClienteDao dao = new ClienteDao();

    // Listar todos
    public List<ClienteModel> obtenerClientes() {
        return dao.listarClientes();
    }

    // Registrar
    public void registrarCliente(String nombre, String apellido, String telefono, String email, String direccion) {
        ClienteModel cm = new ClienteModel();
        cm.setNombre(nombre);
        cm.setApellido(apellido);
        cm.setTelefono(telefono);
        cm.setEmail(email);
        cm.setDireccion(direccion);
        dao.registrarCliente(cm);
    }

    // Actualizar
    public void actualizarCliente(int id, String nombre, String apellido, String telefono, String email, String direccion) {
        ClienteModel cm = new ClienteModel();
        cm.setId(id);
        cm.setNombre(nombre);
        cm.setApellido(apellido);
        cm.setTelefono(telefono);
        cm.setEmail(email);
        cm.setDireccion(direccion);
        dao.actualizarCliente(cm);
    }

    // Cambiar estado (activo/inactivo)
    public void cambiarEstado(int id) {
        dao.cambiarEstado(id);
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
