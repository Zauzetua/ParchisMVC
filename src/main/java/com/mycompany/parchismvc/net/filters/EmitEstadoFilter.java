/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.dto.MensajeEstado;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;

/**
 *
 * @author PC SWAN PLUS
 */
public class EmitEstadoFilter implements Filter {
    @Override public void apply(Ctx ctx){
        if (ctx.mutated){
            var sala = ctx.sala();
            ctx.emitToAll(new MensajeEstado(sala.sala(), sala.turnoActual()));
        }
    }
}
