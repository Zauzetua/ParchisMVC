/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.dto.MensajeError;
import com.mycompany.parchismvc.net.dto.TirarDadoCmd;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;
import com.mycompany.parchismvc.net.dto.MensajeDado;


/**
 *
 * @author PC SWAN PLUS
 */
public class TirarDadoFilter implements Filter {
    @Override public void apply(Ctx ctx){
        if (!(ctx.in instanceof TirarDadoCmd)) return;
        var sala = ctx.sala();
        var cmd  = (TirarDadoCmd) ctx.in;
        synchronized (sala.lock){
            int v = sala.servicio.tirarDado(cmd.jugadorId);
            if (v == 0) ctx.emitToSelf(new MensajeError("No es tu turno o ya tiraste."));
            else        ctx.emitToAll(new MensajeDado(cmd.jugadorId, v));
        }
    }
}