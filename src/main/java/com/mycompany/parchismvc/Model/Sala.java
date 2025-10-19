package com.mycompany.parchismvc.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Equipo 1 Parchis
 */
public class Sala implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Lista de jugadores en la sala
     */
    public final List<Jugador> jugadores = new ArrayList<>();
    /**
     * Mapa que asocia a cada jugador (por su UUID) la lista de sus fichas
     * (objetos Ficha)
     */
    public final Map<UUID, List<Ficha>> fichasPorJugador = new HashMap<>();
    /**
     * Estado actual de la sala
     */
    public EstadoSala estado = EstadoSala.ESPERANDO;
    /**
     * Índice del jugador cuyo turno es actualmente
     */
    public int indiceTurno = 0;
    /**
     * Tiempo por turno en segundos
     */
    public int tiempoPorTurno = 30;
    /**
     * UUID del jugador que ha ganado la partida, o null si no hay ganador aún
     */
    public UUID ganador = null;
}
