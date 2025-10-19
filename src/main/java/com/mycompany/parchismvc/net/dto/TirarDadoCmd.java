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
public class TirarDadoCmd extends Mensaje {
    private static final long serialVersionUID = 1L;
    public final UUID jugadorId;
    public TirarDadoCmd(UUID jugadorId){
        super(TipoMensaje.TIRAR_DADO);
        this.jugadorId = jugadorId;
    }
}
