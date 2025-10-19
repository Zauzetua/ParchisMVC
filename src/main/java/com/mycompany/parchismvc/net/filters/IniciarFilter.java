/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.Service.ServicioJuego;
import com.mycompany.parchismvc.net.dto.IniciarCmd;
import com.mycompany.parchismvc.net.dto.MensajeEstado;
import com.mycompany.parchismvc.net.dto.MensajeResultado;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author PC SWAN PLUS
 */
public class IniciarFilter implements Filter {
    @Override public void apply(Ctx ctx){
        if (!(ctx.in instanceof IniciarCmd)) return;
        var sala = ctx.sala();
        synchronized (sala.lock){
            String r = sala.servicio.iniciarSiTodosListos();
            boolean ok = !r.toLowerCase().contains("no");
            ctx.emitToAll(new MensajeResultado(ok, r));
            if (ok){
                if (sala.tareaInicio != null && !sala.tareaInicio.isDone()) sala.tareaInicio.cancel(false);
                sala.tareaInicio = sala.scheduler.schedule(() -> {
                    synchronized (sala.lock){
                        sala.servicio.comenzarJuego();
                        sala.broadcast(new MensajeResultado(true, "¡Juego iniciado!"));
                        sala.broadcast(new MensajeEstado(sala.sala(), sala.turnoActual()));
                    }
                }, ServicioJuego.TIEMPO_ESPERA_INICIO, TimeUnit.SECONDS);
            }
            ctx.markMutated();
        }
    }
}