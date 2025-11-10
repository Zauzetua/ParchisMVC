/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.pf;

import com.mycompany.parchismvc.net.dto.MensajeError;
import com.mycompany.parchismvc.net.dto.TipoMensaje;
import java.util.Map;

/**
 *
 * @author PC SWAN PLUS
 */
public class MapDispatcher implements Dispatcher{

     private final Map<TipoMensaje, Filter> handlers;
    public MapDispatcher(Map<TipoMensaje, Filter> handlers){ this.handlers = handlers; }
    
    @Override
    public void dispatch(Ctx ctx) throws Exception {
        var f = handlers.get(ctx.in.tipo);
        if (f == null) {
            ctx.emitToSelf(new MensajeError("Mensaje no soportado: " + ctx.in.tipo));
            return;
        }
        f.apply(ctx);
    }
    
}
