/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author PC SWAN PLUS
 */
public class MoverCmd extends Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    public final UUID jugadorId;
    public final int indiceFicha;

    public MoverCmd(UUID jugadorId, int indiceFicha){
        super(TipoMensaje.MOVER);
        this.jugadorId = jugadorId;
        this.indiceFicha = indiceFicha;
    }
}