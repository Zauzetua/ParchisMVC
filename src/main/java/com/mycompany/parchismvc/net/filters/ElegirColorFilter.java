/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.dto.ElegirColorCmd;
import com.mycompany.parchismvc.net.dto.MensajeResultado;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;

/**
 *
 * @author PC SWAN PLUS
 */
public class ElegirColorFilter implements Filter {
    @Override public void apply(Ctx ctx){
        if (!(ctx.in instanceof ElegirColorCmd)) return;
        var sala = ctx.sala();
        var cmd  = (ElegirColorCmd) ctx.in;
        synchronized (sala.lock){
            String r = sala.servicio.elegirColor(cmd.jugadorId, cmd.color);
            ctx.emitToAll(new MensajeResultado(!r.toLowerCase().contains("no"), r));
            ctx.markMutated();
        }
    }
}