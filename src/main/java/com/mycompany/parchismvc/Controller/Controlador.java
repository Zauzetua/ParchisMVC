/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Controller;

import com.mycompany.parchismvc.Model.ColorJugador;
import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.Sala;
import com.mycompany.parchismvc.Service.ServicioJuego;
import java.util.UUID;

/**
 *
 * @author Equipo 1 Parchis
 */
public class Controlador {

    private final ServicioJuego servicio;

    /**
     * Constructor del controlador que recibe una instancia del servicio de juego.
     * @param servicio Instancia del servicio de juego.
     */
    public Controlador(ServicioJuego servicio) {
        this.servicio = servicio;
    }

    /**
     * Registra un nuevo jugador en el juego.
     * @param nombre Nombre del jugador.
     * @param avatar Avatar del jugador.
     * @return El jugador registrado.
     */
    public Jugador registrar(String nombre, String avatar) {
        return servicio.registrarJugador(nombre, avatar);
    }

    /**
     * Permite a un jugador elegir un color.
     * @param jugadorId ID del jugador.
     * @param color Color elegido por el jugador.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String elegirColor(UUID jugadorId, ColorJugador color) {
        return servicio.elegirColor(jugadorId, color);
    }

    /**
     * Marca a un jugador como listo para comenzar el juego.
     * @param jugadorId ID del jugador.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String listo(UUID jugadorId) {
        return servicio.marcarListo(jugadorId);
    }

    /**
     * Cancela el estado de listo de un jugador.
     * @param jugadorId ID del jugador.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String cancelar(UUID jugadorId) {
        return servicio.cancelarListo(jugadorId);
    }

    /**
     * Inicia el juego si todos los jugadores están listos.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String iniciarSiListos() {
        return servicio.iniciarSiTodosListos();
    }

    /**
     * Forza el inicio del juego independientemente del estado de los jugadores.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String forzarIniciar() {
        return servicio.forzarIniciar();
    }

    /**
     * Comienza el juego.
     */
    public void comenzarJuego() {
        servicio.comenzarJuego();
    }

    /**
     * Permite a un jugador tirar el dado.
     * @param jugadorId ID del jugador.
     * @return Resultado de la tirada.
     */
    public int tirar(UUID jugadorId) {
        return servicio.tirarDado(jugadorId);
    }

    /**
     * Mueve una ficha de un jugador.
     * @param jugadorId ID del jugador.
     * @param indiceFicha Índice de la ficha a mover.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String mover(UUID jugadorId, int indiceFicha) {
        return servicio.moverFicha(jugadorId, indiceFicha);
    }

    /**
     * Obtiene el estado actual del juego.
     * @return Estado del juego.
     */
    public String estado() {
        return servicio.volcarEstado();
    }

    /**
     * Establece el tiempo por turno en segundos.
     * @param segundos Tiempo por turno en segundos.
     */
    public void setTiempo(int segundos) {
        servicio.setTiempoPorTurno(segundos);
    }

    /**
     * Obtiene la sala de juego actual.
     * @return La sala de juego.
     */
    public Sala sala() {
        return servicio.sala();
    }
}
