
package com.mycompany.parchismvc.net.dto;

/**
 *
 * Equpo 1 
 */
public enum TipoMensaje {
    // conexión
    UNIRSE, UNIDO, ERROR,

    // lobby / preparación
    ELEGIR_COLOR, LISTO, CANCELAR_LISTO, INICIAR, FORZAR_INICIAR, TIEMPO,

    // juego
    TIRAR_DADO, DADO, MOVER, ESTADO, RESULTADO, CUENTA_ATRAS
}
