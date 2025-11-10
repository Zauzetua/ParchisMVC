/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

import com.mycompany.parchismvc.Model.ColorJugador;
import java.util.UUID;

/**
 *
 * @author PC SWAN PLUS
 */
public class ElegirColorCmd extends Mensaje {
    private static final long serialVersionUID = 1L;
    public final UUID jugadorId;
    public final ColorJugador color;

    public ElegirColorCmd(UUID jugadorId, ColorJugador color){
        super(TipoMensaje.ELEGIR_COLOR);
        this.jugadorId = jugadorId;
        this.color = color;
    }
}