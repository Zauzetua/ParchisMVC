/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.net.dto.MensajeResultado;
import com.mycompany.parchismvc.net.dto.MoverCmd;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Filter;
import java.util.UUID;

/**
 *
 * @author PC SWAN PLUS
 */
public class MoverFilter implements Filter {
    @Override public void apply(Ctx ctx){
        var sala = ctx.sala();
        var cmd  = (MoverCmd) ctx.in;
        synchronized (sala.lock){
             UUID turnoAntes = sala.turnoActual();
        String r = sala.servicio.moverFicha(cmd.jugadorId, cmd.indiceFicha);
        ctx.emitToAll(new MensajeResultado(!r.toLowerCase().contains("no"), r));
        ctx.markMutated();
        // si cambió el turno, reinicia el reloj
        sala.reiniciarRelojTurnoSiCambio(turnoAntes);
        }
    }
}
