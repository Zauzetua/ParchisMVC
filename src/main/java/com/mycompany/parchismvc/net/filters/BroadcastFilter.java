/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;

/**
 *
 * @author PC SWAN PLUS
 */
public class BroadcastFilter implements Filter {
    @Override public void apply(Ctx ctx){
        // al emisor
        for (var m : ctx.toSender) ctx.sesion.enviar(m);
        // a todos en la sala
        var sala = ctx.sala();
        for (var m : ctx.toAll) sala.broadcast(m);
    }
}