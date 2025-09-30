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
 * @author jesus
 */
public class Controlador {

    private final ServicioJuego servicio;

    public Controlador(ServicioJuego servicio) {
        this.servicio = servicio;
    }

    public Jugador registrar(String nombre, String avatar) {
        return servicio.registrarJugador(nombre, avatar);
    }

    public String elegirColor(UUID jugadorId, ColorJugador color) {
        return servicio.elegirColor(jugadorId, color);
    }

    public String listo(UUID jugadorId) {
        return servicio.marcarListo(jugadorId);
    }

    public String cancelar(UUID jugadorId) {
        return servicio.cancelarListo(jugadorId);
    }

    public String iniciarSiListos() {
        return servicio.iniciarSiTodosListos();
    }

    public String forzarIniciar() {
        return servicio.forzarIniciar();
    }

    public void comenzarJuego() {
        servicio.comenzarJuego();
    }

    public int tirar(UUID jugadorId) {
        return servicio.tirarDado(jugadorId);
    }

    public String mover(UUID jugadorId, int indiceFicha) {
        return servicio.moverFicha(jugadorId, indiceFicha);
    }

    public String estado() {
        return servicio.volcarEstado();
    }

    public void setTiempo(int segundos) {
        servicio.setTiempoPorTurno(segundos);
    }

    public Sala sala() {
        return servicio.sala();
    }
}
