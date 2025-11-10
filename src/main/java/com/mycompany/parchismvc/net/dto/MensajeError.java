/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

/**
 *
 * @Equipo 1
 */
public class MensajeError extends Mensaje {
    private static final long serialVersionUID = 1L;
    public final String razon;

    public MensajeError(String razon){
        super(TipoMensaje.ERROR);
        this.razon = razon;
    }
}