/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

/**
 *
 * @author PC SWAN PLUS
 */
public class MensajeCuentaAtras extends Mensaje {
    private static final long serialVersionUID = 1L;
    public final int segundosRestantes;
    public MensajeCuentaAtras(int s){ super(TipoMensaje.CUENTA_ATRAS); this.segundosRestantes = s; }
}