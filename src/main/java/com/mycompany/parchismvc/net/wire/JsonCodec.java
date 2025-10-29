/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.wire;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.parchismvc.net.dto.*;
import java.io.*;
/**
 *
 * @author PC SWAN PLUS
 */
public final class JsonCodec implements Codec {
  private final ObjectMapper om = new ObjectMapper();

  @Override public Mensaje leer(InputStream in) throws Exception {
    byte[] frame = Frames.leerFrame(in);
    JsonNode root = om.readTree(frame);
    String tipoTxt = root.get("tipo").asText();
    TipoMensaje tipo = TipoMensaje.valueOf(tipoTxt);
    Class<? extends Mensaje> clazz = switch (tipo) {
       case UNIRSE       -> SolicitudUnirse.class;
      case ELEGIR_COLOR -> ElegirColorCmd.class;
      case LISTO        -> ListoCmd.class;
      case CANCELAR_LISTO -> CancelarListoCmd.class;
      case INICIAR      -> IniciarCmd.class;
      case TIEMPO       -> SetTiempoCmd.class;
      case TIRAR_DADO   -> TirarDadoCmd.class;
      case MOVER        -> MoverCmd.class;
      default           -> Mensaje.class; // fallback
    };
    return om.treeToValue(root, clazz);
  }

  @Override public void escribir(OutputStream out, Mensaje m) throws Exception {
    byte[] json = om.writeValueAsBytes(m);
    Frames.escribirFrame(out, json);
  }

  @Override public String nombre() { return "json"; }
}
