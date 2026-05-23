// MIGRACION: package cambiado de "controller" a "com.mundial.app.controller"
package com.mundial.app.controller;

// MIGRACION: import ajustado de "model.UsuarioModel" a "com.mundial.app.model.UsuarioModel"
import com.mundial.app.model.UsuarioModel;
// MIGRACION: import ajustado de "util.PasswordUtil" a "com.mundial.app.util.PasswordUtil"
import com.mundial.app.util.PasswordUtil;
import java.util.List;
// MIGRACION: import ajustado de "dao.UsuarioDao" a "com.mundial.app.dao.UsuarioDao"
import com.mundial.app.dao.UsuarioDao;

public class UsuarioController {

    private final UsuarioDao dao = new UsuarioDao();

    //login
    public UsuarioModel login(String username, String password) {
        UsuarioModel usuario = dao.buscarPorUsername(username);
        if (usuario != null && PasswordUtil.verificar(password, usuario.getPassword())) {
            return usuario;
        }

        return null;
    }

    //insertar
    public boolean insertarUsuario(String username, String password, String rol) {
        String hash = PasswordUtil.hashear(password);
        UsuarioModel user = new UsuarioModel(username, hash, rol);
        return dao.insertar(user) > 0;
    }

    //actualizar
    public boolean actualizarUsuario(int id, String username, String password, String rol, boolean estado) {
        String hash = PasswordUtil.hashear(password);
        UsuarioModel user = new UsuarioModel(username, hash, rol, estado);
        user.setId(id);
        return dao.actualizar(user);
    }

    //eliminar(cambiar estado)
    public boolean cambiarEstadoUsuario(int id, boolean estado) {
        return dao.cambiarEstado(id, estado);
    }

    //listar
    public List<UsuarioModel> listarUsuarios() {
        return dao.listar();
    }

}
