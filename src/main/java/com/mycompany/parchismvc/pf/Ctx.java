/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.pf;

import com.mycompany.parchismvc.net.Server.ParchisServidorMin.SalaActiva;
import com.mycompany.parchismvc.net.Server.ParchisServidorMin.SesionCliente;
import com.mycompany.parchismvc.net.dto.Mensaje;
import com.mycompany.parchismvc.net.dto.MensajeError;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PC SWAN PLUS
 */


public class Ctx {
    public final SesionCliente sesion;
    public final Mensaje in;
    
    private SalaActiva sala;
    public boolean mutated = false;
    
    public final List<Mensaje> toSender = new ArrayList<>();
    public final List<Mensaje> toAll    = new ArrayList<>();
    
    public Ctx(SesionCliente sesion, Mensaje in){this.sesion = sesion; this.in = in; }
    
    public void sala(SalaActiva s){this.sala = s;}
    public SalaActiva sala(){ return sala; }

    public void emitToSelf(Mensaje m){ toSender.add(m); }
    public void emitToAll(Mensaje m){ toAll.add(m); }
    public void markMutated(){ mutated = true; }

    public void error(Exception ex){ emitToSelf(new MensajeError(ex.getMessage())); }
    
}
