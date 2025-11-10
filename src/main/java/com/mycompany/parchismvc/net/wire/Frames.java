/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.wire;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author PC SWAN PLUS
 */
public final class Frames {
  public static byte[] leerFrame(InputStream in) throws IOException {
    byte[] hdr = in.readNBytes(4);
    if (hdr.length < 4) throw new EOFException("Conexion cerrada");
    int len = ((hdr[0]&0xFF)<<24)|((hdr[1]&0xFF)<<16)|((hdr[2]&0xFF)<<8)|(hdr[3]&0xFF);
    if (len < 0 || len > 32*1024*1024) throw new IOException("Frame invalido: " + len);
    return in.readNBytes(len);
  }
  public static void escribirFrame(OutputStream out, byte[] payload) throws IOException {
    int len = payload.length;
    out.write(new byte[]{ (byte)(len>>>24),(byte)(len>>>16),(byte)(len>>>8),(byte)len });
    out.write(payload);
    out.flush();
  }
  private Frames(){}
}
