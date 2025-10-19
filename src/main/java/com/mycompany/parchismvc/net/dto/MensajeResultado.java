/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

import java.io.Serializable;

public class MensajeResultado extends Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    public final boolean ok;
    public final String mensaje;

    public MensajeResultado(boolean ok, String mensaje){
        super(TipoMensaje.RESULTADO);
        this.ok = ok;
        this.mensaje = mensaje;
    }
}