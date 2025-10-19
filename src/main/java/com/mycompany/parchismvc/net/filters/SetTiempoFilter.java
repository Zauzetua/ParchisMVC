/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.dto.MensajeResultado;
import com.mycompany.parchismvc.net.dto.SetTiempoCmd;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;

/**
 *
 * @author PC SWAN PLUS
 */
public class SetTiempoFilter implements Filter {
    @Override public void apply(Ctx ctx){
        if (!(ctx.in instanceof SetTiempoCmd)) return;
        var sala = ctx.sala();
        var cmd  = (SetTiempoCmd) ctx.in;
        synchronized (sala.lock){
            sala.servicio.setTiempoPorTurno(cmd.segundos);
            ctx.emitToAll(new MensajeResultado(true, "Tiempo por turno = " + cmd.segundos + "s"));
            ctx.markMutated();
        }
    }
}