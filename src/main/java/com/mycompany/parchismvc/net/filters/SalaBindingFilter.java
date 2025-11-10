/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.Server.ParchisServidorMin.SalaActiva;
import com.mycompany.parchismvc.net.dto.SolicitudUnirse;
import com.mycompany.parchismvc.net.dto.TipoMensaje;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;
import java.util.function.Function;

/**
 *
 * @author PC SWAN PLUS
 */
public class SalaBindingFilter implements Filter {
    private final Function<String, SalaActiva> getSala;
    public SalaBindingFilter(Function<String, SalaActiva> getSala){ this.getSala = getSala; }

    @Override public void apply(Ctx ctx){
        if (ctx.in.tipo == TipoMensaje.UNIRSE){
            String id = ((SolicitudUnirse)ctx.in).idSala;
            ctx.sala(getSala.apply(id));
        } else {
            ctx.sala(ctx.sesion.sala);
        }
    }
}