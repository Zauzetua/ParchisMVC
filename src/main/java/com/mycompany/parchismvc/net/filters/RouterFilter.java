/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.dto.TipoMensaje;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;
import java.util.Map;

/**
 *
 * @author PC SWAN PLUS
 */
public class RouterFilter implements Filter {
    private final Map<TipoMensaje, Filter> routes;
    public RouterFilter(Map<TipoMensaje, Filter> routes){ this.routes = routes; }

    @Override public void apply(Ctx ctx) throws Exception {
        Filter f = routes.get(ctx.in.tipo);
        if (f != null) f.apply(ctx); // mensajes no mapeados = ignorados
    }
}
