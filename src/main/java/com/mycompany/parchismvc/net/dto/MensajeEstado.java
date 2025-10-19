/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

import com.mycompany.parchismvc.Model.Sala;
import java.util.UUID;

/**
 *
 * @author PC SWAN PLUS
 */
public class MensajeEstado extends Mensaje {
    private static final long serialVersionUID = 1L;
    public final Sala sala;      // snapshot completo (ya es Serializable)
    public final UUID turnoDe;   // jugador en turno (puede ser null)

    public MensajeEstado(Sala sala, UUID turnoDe){
        super(TipoMensaje.ESTADO);
        this.sala = sala;
        this.turnoDe = turnoDe;
    }
}
