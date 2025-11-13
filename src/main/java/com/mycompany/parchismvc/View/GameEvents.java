package com.mycompany.parchismvc.View;

import com.mycompany.parchismvc.Model.Sala;
import java.util.UUID;

/**
 * Eventos que el Controlador emite hacia cualquier UI (Swing, consola, etc.).
 */
public interface GameEvents {
    void onConectado(String host, int puerto, String salaId);
    void onRegistrado(UUID jugadorId);
    void onEstado(Sala sala, UUID turnoDe, UUID yo);
    void onCuentaAtras(int segundos, Sala sala);
    void onResultado(boolean ok, String mensaje);
    void onDado(UUID jugadorId, int valor);
    void onError(String razon);
}
