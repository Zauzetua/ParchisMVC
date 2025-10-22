/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.Model.EstadoSala;
import com.mycompany.parchismvc.net.dto.CancelarListoCmd;
import com.mycompany.parchismvc.net.dto.MensajeEstado;
import com.mycompany.parchismvc.net.dto.MensajeResultado;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;

/**
 *
 * @author PC SWAN PLUS
 */
public class CancelarListoFilter implements Filter {
    @Override public void apply(Ctx ctx){
        if (!(ctx.in instanceof CancelarListoCmd)) return;
        var sala = ctx.sala();
        var cmd  = (CancelarListoCmd) ctx.in;

        synchronized (sala.lock){
            String r = sala.servicio.cancelarListo(cmd.jugadorId);
            // deten la cuenta atras si estaba corriendo
            if (sala.tareaInicio != null && !sala.tareaInicio.isDone()) sala.tareaInicio.cancel(false);
            if (sala.tareaTick   != null && !sala.tareaTick.isDone())   sala.tareaTick.cancel(false);
            sala.segundosRestantes = 0;
            // vuelve a ESPERANDO si se habia puesto INICIANDO
            sala.sala().estado = EstadoSala.ESPERANDO;

            ctx.emitToAll(new MensajeResultado(true, r + ". Cuenta atrás cancelada."));
            ctx.emitToAll(new MensajeEstado(sala.sala(), sala.turnoActual()));
            ctx.markMutated();
        }
    }
}