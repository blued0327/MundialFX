/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mundial.app.controller;

import com.mundial.app.dao.LogDao;
import com.mundial.app.model.LogModel;
/**
 *
 * @author rchar
 */
public class LogController {
    private final LogDao dao = new LogDao();
 
    public int registrarLogin(int usuarioId, String ip) {
        return registrar(usuarioId, LogModel.ACCION_LOGIN, ip);
    }
 
    public int registrarLogout(int usuarioId, String ip) {
        return registrar(usuarioId, LogModel.ACCION_LOGOUT, ip);
    }
 
    public int registrarLoginFallido(int usuarioId, String ip) {
        return registrar(usuarioId, LogModel.ACCION_LOGIN_FALLIDO, ip);
    }
 
    private int registrar(int usuarioId, String accion, String ip) {
        try {
            LogModel log = new LogModel(usuarioId, accion, ip);
            return dao.registrarLog(log);
        } catch (Exception e) {
            System.err.println("Error al registrar log [" + accion + "]: " + e.getMessage());
            return -1;
        }
    }
}
