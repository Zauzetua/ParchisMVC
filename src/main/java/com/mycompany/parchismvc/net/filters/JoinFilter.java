/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.dto.MensajeEstado;
import com.mycompany.parchismvc.net.dto.MensajeUnido;
import com.mycompany.parchismvc.net.dto.SolicitudUnirse;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;

/**
 *
 * @author PC SWAN PLUS
 */
public class JoinFilter implements Filter {
    @Override public void apply(Ctx ctx){
        var sala = ctx.sala();
        var req  = (SolicitudUnirse) ctx.in;
        synchronized (sala.lock){
            var yo = sala.servicio.registrarJugador(req.nombre, req.avatar);
            ctx.sesion.yo = yo;
            ctx.sesion.sala = sala;
            sala.clientes.add(ctx.sesion);

            ctx.emitToSelf(new MensajeUnido(yo.id));
            ctx.emitToAll(new MensajeEstado(sala.sala(), sala.turnoActual()));
        }
    }
}
