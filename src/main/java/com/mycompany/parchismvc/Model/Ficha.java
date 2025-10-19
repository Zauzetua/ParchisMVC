/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Model;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author Equipo 1 Parchis
 * Clase que representa una ficha en el juego de Parchis.
 */
public class Ficha implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID unico de la ficha.
     */
    public final UUID id = UUID.randomUUID();
    /**
     * ID del jugador dueño de la ficha.
     */
    public final UUID dueñoId;
    /**
     * Posición actual de la ficha en el tablero. -1 indica que está en la base.
     * 0-68 indica la posición en el tablero.
     */
    public int posicion = -1; // -1 = base; >=0 posicion absoluta
    /**
     * Estado actual de la ficha (en base, en tablero, en casa).
     */
    public EstadoFicha estado = EstadoFicha.BASE;

    /**
     * Constructor de la ficha que asigna el dueño.
     * @param dueñoId ID del jugador dueño de la ficha.
     */
    public Ficha(UUID dueñoId) {
        this.dueñoId = dueñoId;
    }

    /**
     * Devuelve una representacion en cadena de la ficha.
     * @return Cadena que representa la ficha.
     */
    @Override
    public String toString() {
        return "Ficha{" + id.toString().substring(0,6) + " pos=" + posicion + " estado=" + estado + "}";
    }
}