/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author jesus
 */
public class Sala {

    public final List<Jugador> jugadores = new ArrayList<>();
    public final Map<UUID, List<Ficha>> fichasPorJugador = new HashMap<>();
    public EstadoSala estado = EstadoSala.ESPERANDO;
    public int indiceTurno = 0;
    public int tiempoPorTurno = 30;
    public UUID ganador = null;
}
