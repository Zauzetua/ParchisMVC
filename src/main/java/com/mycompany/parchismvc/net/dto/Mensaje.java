/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

import java.io.Serializable;

/**
 *
 * @Equipo 1
 */
public abstract class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    public final TipoMensaje tipo;
    protected Mensaje(TipoMensaje tipo){ this.tipo = tipo; }
}
