package com.mycompany.parchismvc.Model;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author Equipo 1 Parchis
  * Clase que representa a un jugador en el juego.
 */
/*
Sockets
*/
public class Jugador implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    /**
     * Identificador unico del jugador.
     */
    public final UUID id = UUID.randomUUID();
    /**
     * Nombre del jugador.
     */
    public final String nombre;
    /**
     * Avatar del jugador (Ruta).
     */
    public String avatar;
    /**
     * Color asignado al jugador. Pertenece al Enum ColorJugador.
     */
    public ColorJugador color = null;
    /**
     * Indica si el jugador esta listo para iniciar la partida.
     */
    public boolean listo = false;
    /**
     * Indica si el jugador esta conectado.
     */
    public boolean conectado = true;

    /**
     * Constructor de la clase Jugador.
     * @param nombre Nombre del jugador.
     * @param avatar Avatar del jugador (Ruta).
     */
    public Jugador(String nombre, String avatar) {
        this.nombre = nombre;
        this.avatar = avatar;
    }

    /**
     * Representacion en cadena del jugador.
     * @return Cadena con el nombre, color y estado de listo del jugador.
     */
    @Override
    public String toString() {
        return nombre + " (" + (color == null ? "sin color" : color) + ") " + (listo ? "[LISTO]" : "");
    }
}
