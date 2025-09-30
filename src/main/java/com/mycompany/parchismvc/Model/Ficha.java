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
public class Ficha {
    public final UUID id = UUID.randomUUID();
    public final UUID dueñoId;
    public int posicion = -1; // -1 = base; >=0 posición absoluta
    public EstadoFicha estado = EstadoFicha.BASE;

    public Ficha(UUID dueñoId) {
        this.dueñoId = dueñoId;
    }

    @Override
    public String toString() {
        return "Ficha{" + id.toString().substring(0,6) + " pos=" + posicion + " estado=" + estado + "}";
    }
}