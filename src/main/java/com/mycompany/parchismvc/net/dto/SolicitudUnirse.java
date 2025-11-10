/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.dto;

/**
 *
 * @Equipo 1
 */


public class SolicitudUnirse extends Mensaje {
    private static final long serialVersionUID = 1L;
    public final String idSala;
    public final String nombre;
    public final String avatar;

    public SolicitudUnirse(String idSala, String nombre, String avatar){
        super(TipoMensaje.UNIRSE);
        this.idSala = idSala;
        this.nombre = nombre;
        this.avatar = avatar;
    }
}