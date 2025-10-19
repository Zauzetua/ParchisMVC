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
public class LogFilter implements Filter {

    @Override
    public void apply(Ctx ctx){
        System.out.println("[FILTRO] " + ctx.sesion + " -> " + ctx.in.tipo);
    }
    
}
