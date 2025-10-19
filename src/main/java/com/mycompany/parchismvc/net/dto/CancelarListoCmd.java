/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

import java.util.UUID;

/**
 *
 * @author PC SWAN PLUS
 */
public class CancelarListoCmd extends Mensaje {
    private static final long serialVersionUID = 1L;
    public final UUID jugadorId;
    public CancelarListoCmd(UUID jugadorId){
        super(TipoMensaje.CANCELAR_LISTO);
        this.jugadorId = jugadorId;
    }
}