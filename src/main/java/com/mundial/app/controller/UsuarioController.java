package com.mundial.app.controller;

import com.mundial.app.model.UsuarioModel;
import com.mundial.app.util.PasswordUtil;
import java.util.List;
import com.mundial.app.dao.UsuarioDao;
import com.mundial.app.dao.LogDao;

public class UsuarioController {

    private final UsuarioDao dao = new UsuarioDao();

    //dao para registrar logs del sistema
    private final LogDao logDao = new LogDao();

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

    //insertar con estado
    //sobrecarga para respetar lo que diga el checkbox del formulario
    //antes el insert siempre quedaba inactivo porque el constructor de 3 args
    //no setea estado y por default un boolean en java arranca en false
    public boolean insertarUsuario(String username, String password, String rol, boolean estado) {

        //hashear la password con bcrypt antes de mandarla a la BD
        String hash = PasswordUtil.hashear(password);

        //usamos el constructor de 4 args que si setea el estado
        UsuarioModel user = new UsuarioModel(username, hash, rol, estado);

        return dao.insertar(user) > 0;
    }

    //actualizar
    /*
    public boolean actualizarUsuario(int id, String username, String password, String rol, boolean estado) {
        String hash = PasswordUtil.hashear(password);
        UsuarioModel user = new UsuarioModel(username, hash, rol, estado);
        user.setId(id);
        return dao.actualizar(user);
    }*/
    //actualizar
    public boolean actualizarUsuario(int id, String username, String password, String rol, boolean estado) {

        //si password viene vacia
        //significa que no quiere cambiarla
        if (password == null || password.isBlank()) {

            //buscar usuario actual
            UsuarioModel actual = dao.buscarPorId(id);

            //si no existe retornar false
            if (actual == null) {
                return false;
            }

            //usar password vieja(hasheada)
            password = actual.getPassword();

        } else {

            //si escribio nueva password
            //entonces encriptar
            password = PasswordUtil.hashear(password);
        }

        UsuarioModel user = new UsuarioModel(
                username,
                password,
                rol,
                estado
        );

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
//tuve que a;adir aqui lo de los logs ya que aqui se maneja eso

    //login
    public UsuarioModel login(String username, String password, String ip) {

        //buscar usuario por username
        UsuarioModel usuario = dao.buscarPorUsername(username);

        //si existe y la password coincide entra
        if (usuario != null && PasswordUtil.verificar(password, usuario.getPassword())) {

            //registrar login correcto
            logDao.registrar(usuario.getId(), "LOGIN", ip
            );

            return usuario;
        }

        //si falla el login guardamos intento fallido
        logDao.registrar(null, "LOGIN_FALLIDO", ip
        );

        return null;
    }

    //logout
    public void logout(int usuarioId, String ip) {

        //guardar salida del sistema
        logDao.registrar(usuarioId, "LOGOUT", ip
        );
    }
}
