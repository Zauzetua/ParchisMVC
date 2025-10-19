/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.dto.Mensaje;
import com.mycompany.parchismvc.net.dto.TipoMensaje;
import java.util.UUID;

/**
 *
 * @author PC SWAN PLUS
 */
public class MensajeDado extends Mensaje {
    private static final long serialVersionUID = 1L;
    public final UUID jugadorId;
    public final int valor;

    public MensajeDado(UUID jugadorId, int valor){
        super(TipoMensaje.DADO);
        this.jugadorId = jugadorId;
        this.valor = valor;
    }
}