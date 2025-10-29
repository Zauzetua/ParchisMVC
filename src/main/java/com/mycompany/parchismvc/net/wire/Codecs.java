/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.wire;
import java.io.*;
/**
 *
 * @author PC SWAN PLUS
 */
public final class Codecs {
  public static Codec detectar(BufferedInputStream bin, OutputStream out) throws Exception {
    bin.mark(8);
    int b0 = skipWs(bin);
    bin.reset();

    // Firma de ObjectStream: AC ED ...
    if (b0 == 0xAC) {
      byte[] sig = bin.readNBytes(2);
      bin.reset();
      if (sig.length==2 && (sig[0]&0xFF)==0xAC && (sig[1]&0xFF)==0xED) {
        return new JavaSerCodec(bin, out);
      }
    }
    // Si no es Java, asumimos JSON con framing
    return new JsonCodec();
  }
  private static int skipWs(BufferedInputStream bin) throws IOException {
    int c;
    do { bin.mark(1); c = bin.read(); } while (c==' '||c=='\n'||c=='\r'||c=='\t');
    bin.reset();
    return c;
  }
  private Codecs(){}
}