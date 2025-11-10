/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.filters;

import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Dispatcher;
import com.mycompany.parchismvc.pf.Filter;

/**
 *
 * @author PC SWAN PLUS
 */
public final class DispatchFilter implements Filter {
    private final Dispatcher dispatcher;
    public DispatchFilter(Dispatcher dispatcher){ this.dispatcher = dispatcher; }
    @Override public void apply(Ctx ctx) throws Exception { dispatcher.dispatch(ctx); }
}