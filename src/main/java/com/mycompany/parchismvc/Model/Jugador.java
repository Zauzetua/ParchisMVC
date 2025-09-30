/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Model;

import java.util.UUID;

/**
 *
 * @author jesus
 */
public class Jugador {
    public final UUID id = UUID.randomUUID();
    public final String nombre;
    public String avatar;
    public ColorJugador color = null;
    public boolean listo = false;
    public boolean conectado = true;

    public Jugador(String nombre, String avatar) {
        this.nombre = nombre;
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return nombre + " (" + (color == null ? "sin color" : color) + ") " + (listo ? "[LISTO]" : "");
    }
}
