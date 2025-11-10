/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.pf;

import java.util.List;

/**
 *
 * @author PC SWAN PLUS
 */
public class Pipeline {
    private final List<Filter> filters;
    public Pipeline(List<Filter> filters){ this.filters = filters;}
    public static Pipeline of(Filter... fs){return new Pipeline(java.util.Arrays.asList(fs)); }
    public void run(Ctx ctx){
        for (Filter f : filters){
            
            try { f.apply(ctx); }
            catch(Exception ex) {ctx.error(ex);
            
            break;
            }
        }
    }
    
}
