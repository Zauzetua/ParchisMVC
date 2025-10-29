/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.parchismvc.net.wire;

import com.mycompany.parchismvc.net.dto.Mensaje;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author 
 */
public interface Codec {
    Mensaje leer(InputStream in) throws Exception;         // lee 1 mensaje
    void escribir(OutputStream out, Mensaje m) throws Exception; // envía 1 mensaje
    String nombre();
}

