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
public class MensajeFinPartida extends Mensaje {
    public final String motivo;
    public final UUID ganadorId; // UUID del jugador ganador, o null si no hay ganador
    
    public MensajeFinPartida(String motivo, UUID ganadorId) {
        super(TipoMensaje.FIN_PARTIDA);
        this.motivo = motivo;
        this.ganadorId = ganadorId;
    }
}