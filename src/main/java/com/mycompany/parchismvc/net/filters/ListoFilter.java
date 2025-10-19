/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.Model.EstadoSala;
import com.mycompany.parchismvc.Service.ServicioJuego;
import com.mycompany.parchismvc.net.dto.ListoCmd;
import com.mycompany.parchismvc.net.dto.MensajeCuentaAtras;
import com.mycompany.parchismvc.net.dto.MensajeEstado;
import com.mycompany.parchismvc.net.dto.MensajeResultado;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author PC SWAN PLUS
 */
public class ListoFilter implements Filter {
    @Override public void apply(Ctx ctx){
        if (!(ctx.in instanceof ListoCmd)) return;
        var sala = ctx.sala();
        var cmd  = (ListoCmd) ctx.in;

        synchronized (sala.lock){
            String r = sala.servicio.marcarListo(cmd.jugadorId);
            ctx.emitToAll(new MensajeResultado(true, r));
            ctx.markMutated();

            // ¿Todos listos?
            if (sala.servicio.todosListosMinimos()
                && sala.sala().estado != EstadoSala.JUGANDO) {

                // Pone estado INICIANDO
                String msg = sala.servicio.iniciarSiTodosListos();
                ctx.emitToAll(new MensajeResultado(true, msg));
                ctx.emitToAll(new MensajeEstado(sala.sala(), sala.turnoActual()));

                // Limpia contadores previos si hubiera
                if (sala.tareaInicio != null && !sala.tareaInicio.isDone()) sala.tareaInicio.cancel(false);
                if (sala.tareaTick   != null && !sala.tareaTick.isDone())   sala.tareaTick.cancel(false);

                // Inicializa cuenta atrás
                sala.segundosRestantes = ServicioJuego.TIEMPO_ESPERA_INICIO;
                sala.broadcast(new MensajeCuentaAtras(sala.segundosRestantes));

                // Tick cada 1 segundo
                sala.tareaTick = sala.scheduler.scheduleAtFixedRate(() -> {
                    synchronized (sala.lock){
                        sala.segundosRestantes--;
                        if (sala.segundosRestantes > 0) {
                            sala.broadcast(new MensajeCuentaAtras(sala.segundosRestantes));
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS);

                // Arranque automático al terminar
                sala.tareaInicio = sala.scheduler.schedule(() -> {
                    synchronized (sala.lock){
                        // detener ticks
                        if (sala.tareaTick != null) sala.tareaTick.cancel(false);
                        sala.segundosRestantes = 0;

                        sala.servicio.comenzarJuego();
                        sala.iniciarRelojTurno();
                        sala.broadcast(new MensajeResultado(true, "¡Juego iniciado!"));
                        sala.broadcast(new MensajeEstado(sala.sala(), sala.turnoActual()));
                    }
                }, ServicioJuego.TIEMPO_ESPERA_INICIO, TimeUnit.SECONDS);
            }
        }
    }
}