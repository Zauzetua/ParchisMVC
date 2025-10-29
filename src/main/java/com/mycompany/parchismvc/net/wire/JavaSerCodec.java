/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.wire;

import com.mycompany.parchismvc.net.dto.Mensaje;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 *
 * @author PC SWAN PLUS
 */
public final class JavaSerCodec implements Codec {
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    public JavaSerCodec(InputStream in, OutputStream out) throws IOException {
        this.oos = new ObjectOutputStream(out);
        this.ois = new ObjectInputStream(in);
    }
    @Override public Mensaje leer(InputStream ignored) throws Exception {
        return (Mensaje) ois.readObject();
    }
    @Override public void escribir(OutputStream ignored, Mensaje m) throws Exception {
        oos.reset();               // <- importante para snapshots frescos
        oos.writeObject(m);
        oos.flush();
    }
    @Override public String nombre() { return "java-ser"; }
}
