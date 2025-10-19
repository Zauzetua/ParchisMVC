/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Controller;

import com.mycompany.parchismvc.Model.ColorJugador;
import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.Sala;
import com.mycompany.parchismvc.Service.ServicioJuego;
import com.mycompany.parchismvc.View.Vista;
import com.mycompany.parchismvc.net.Client.ClienteRedMin;
import com.mycompany.parchismvc.net.dto.Mensaje;
import com.mycompany.parchismvc.net.dto.MensajeError;
import com.mycompany.parchismvc.net.dto.MensajeEstado;
import com.mycompany.parchismvc.net.dto.MensajeUnido;
import com.mycompany.parchismvc.net.dto.SolicitudUnirse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Equipo 1 Parchis
 */
public class Controlador {

    private ServicioJuego servicio;
    private com.mycompany.parchismvc.net.Client.ClienteRedMin red;
    private java.util.UUID miId;
    private String salaId;
    private volatile Sala salaCache;       // último snapshot del servidor
    private volatile UUID turnoCache;      // último turno reportado

    public Controlador(ServicioJuego servicio) {
        this.servicio = servicio;
    } // modo local

    public Controlador() {
    }
    private CompletableFuture<com.mycompany.parchismvc.net.dto.MensajeResultado> pendingResultado;
    private CompletableFuture<com.mycompany.parchismvc.net.dto.MensajeDado> pendingDado;
    private com.mycompany.parchismvc.View.Vista vista;

    public void setVista(Vista v) {
        this.vista = v;
    }

    // Esperas síncronas (para que tu Vista funcione igual que local)
    private CompletableFuture<MensajeUnido> pendingUnido;

    /**
     * Constructor del controlador que recibe una instancia del servicio de
     * juego.
     *
     * @param servicio Instancia del servicio de juego.
     */
    /**
     * Registra un nuevo jugador en el juego.
     *
     * @param nombre Nombre del jugador.
     * @param avatar Avatar del jugador.
     * @return El jugador registrado.
     */
    public UUID registrar(String nombre, String avatar) {
        try {
            pendingUnido = new CompletableFuture<>();
            red.enviar(new SolicitudUnirse(salaId, nombre, avatar));
            var unido = pendingUnido.get(5, TimeUnit.SECONDS);  // espera respuesta
            this.miId = unido.jugadorId;
            Vista.mostrarInfo("Servidor asigno miId=" + miId);
            return unido.jugadorId;
        } catch (Exception e) {
            Vista.mostrarError("Fallo al registrar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Permite a un jugador elegir un color.
     *
     * @param jugadorId ID del jugador.
     * @param color Color elegido por el jugador.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String elegirColor(UUID jugadorId, ColorJugador color) {
        if (servicio != null) {
            return servicio.elegirColor(jugadorId, color); // local
        }
        try {
            pendingResultado = new CompletableFuture<>();
            red.enviar(new com.mycompany.parchismvc.net.dto.ElegirColorCmd(jugadorId, color));
            var r = pendingResultado.get(5, TimeUnit.SECONDS);
            return r.mensaje;
        } catch (Exception e) {
            return "Error al elegir color: " + e.getMessage();
        }
    }

    /**
     * Marca a un jugador como listo para comenzar el juego.
     *
     * @param jugadorId ID del jugador.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String listo(UUID jugadorId) {
        if (servicio != null) {
            return servicio.marcarListo(jugadorId);
        }
        try {
            pendingResultado = new CompletableFuture<>();
            red.enviar(new com.mycompany.parchismvc.net.dto.ListoCmd(jugadorId));
            var r = pendingResultado.get(5, TimeUnit.SECONDS);
            return r.mensaje;
        } catch (Exception e) {
            return "Error al marcar listo: " + e.getMessage();
        }
    }

    /**
     * Cancela el estado de listo de un jugador.
     *
     * @param jugadorId ID del jugador.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String cancelar(UUID jugadorId) {
        if (servicio != null) {
            return servicio.cancelarListo(jugadorId);
        }
        try {
            pendingResultado = new CompletableFuture<>();
            red.enviar(new com.mycompany.parchismvc.net.dto.CancelarListoCmd(jugadorId));
            var r = pendingResultado.get(5, TimeUnit.SECONDS);
            return r.mensaje;
        } catch (Exception e) {
            return "Error al cancelar listo: " + e.getMessage();
        }
    }

    /**
     * Inicia el juego si todos los jugadores están listos.
     *
     * @return Mensaje indicando el resultado de la operación.
     */
    public String iniciarSiListos() {
        if (servicio != null) {
            return servicio.iniciarSiTodosListos();
        }
        try {
            pendingResultado = new CompletableFuture<>();
            red.enviar(new com.mycompany.parchismvc.net.dto.IniciarCmd());
            var r = pendingResultado.get(5, TimeUnit.SECONDS);
            return r.mensaje;
        } catch (Exception e) {
            return "Error al iniciar: " + e.getMessage();
        }
    }

    /**
     * Forza el inicio del juego independientemente del estado de los jugadores.
     *
     * @return Mensaje indicando el resultado de la operación.
     */
    public String forzarIniciar() {
        return servicio.forzarIniciar();
    }

    /**
     * Comienza el juego.
     */
    public void comenzarJuego() {
        servicio.comenzarJuego();
    }

    /**
     * Permite a un jugador tirar el dado.
     *
     * @param jugadorId ID del jugador.
     * @return Resultado de la tirada.
     */
    public int tirar(UUID jugadorId) {
        if (servicio != null) {
            return servicio.tirarDado(jugadorId);
        }
        try {
            pendingDado = new CompletableFuture<>();
            red.enviar(new com.mycompany.parchismvc.net.dto.TirarDadoCmd(jugadorId));
            var d = pendingDado.get(5, TimeUnit.SECONDS);
            return d.valor;
        } catch (Exception e) {
            Vista.mostrarError(e.getMessage());
            return 0;
        }
    }

    /**
     * Mueve una ficha de un jugador.
     *
     * @param jugadorId ID del jugador.
     * @param indiceFicha Índice de la ficha a mover.
     * @return Mensaje indicando el resultado de la operación.
     */
    public String mover(UUID jugadorId, int indiceFicha) {
        if (servicio != null) {
            return servicio.moverFicha(jugadorId, indiceFicha);
        }
        try {
            pendingResultado = new CompletableFuture<>();
            red.enviar(new com.mycompany.parchismvc.net.dto.MoverCmd(jugadorId, indiceFicha));
            var r = pendingResultado.get(5, TimeUnit.SECONDS);
            return r.mensaje;
        } catch (Exception e) {
            return "Error al mover: " + e.getMessage();
        }
    }

    /**
     * Obtiene el estado actual del juego.
     *
     * @return Estado del juego.
     */
    public String estado() {
        var s = salaCache;
        if (s == null) {
            return "(sin estado aun; usa 'registro' primero o espera 'ESTADO')";
        }
        // Reutiliza el mismo render que imprime la Vista
        Vista.actualizarEstado(s, turnoCache, miId);
        return ""; // ya imprimimos arriba
    }

    /**
     * Establece el tiempo por turno en segundos.
     *
     * @param segundos Tiempo por turno en segundos.
     */
    public void setTiempo(int segundos) {
        if (servicio != null) {
            servicio.setTiempoPorTurno(segundos);
            return;
        }
        try {
            red.enviar(new com.mycompany.parchismvc.net.dto.SetTiempoCmd(segundos));
        } catch (Exception ignored) {
        }
    }

    /**
     * Obtiene la sala de juego actual.
     *
     * @return La sala de juego.
     */
    public Sala sala() {
        return salaCache;
    }

    public void iniciarRed() {
        try {
            red = new com.mycompany.parchismvc.net.Client.ClienteRedMin("127.0.0.1", 5000);
            red.onMensaje(this::alLlegarMensaje);
            red.conectar();

            // Unirse a una sala (idSala = "sala1" por ejemplo)
            var join = new com.mycompany.parchismvc.net.dto.SolicitudUnirse("sala1", "Kike", "avatar.png");
            red.enviar(join);
        } catch (Exception e) {
            Vista.mostrarError("No se pudo conectar al servidor: " + e.getMessage());
        }
    }

    private void alLlegarMensaje(com.mycompany.parchismvc.net.dto.Mensaje m) {
        switch (m.tipo) {
            case UNIDO -> {
                var ok = (com.mycompany.parchismvc.net.dto.MensajeUnido) m;
                miId = ok.jugadorId;
                Vista.mostrarInfo("Conectado. Mi ID: " + miId);
            }
            case ESTADO -> {
                var est = (com.mycompany.parchismvc.net.dto.MensajeEstado) m;
                // Redibuja la UI con est.sala (snapshot) y el turno (est.turnoDe)
                Vista.actualizarEstado(est.sala, est.turnoDe, miId);
            }
            case ERROR -> {
                var er = (com.mycompany.parchismvc.net.dto.MensajeError) m;
                Vista.mostrarError(er.razon);
            }
            default -> {
                /* aún no implementado en el servidor mínimo */ }
        }
    }
    // === Conectar al servidor ===

    public void conectarRed(String host, int puerto, String salaId) {
        try {
            this.salaId = salaId;
            red = new ClienteRedMin(host, puerto);
            red.onMensaje(this::onMensaje);
            red.conectar();
            Vista.mostrarInfo("Conectado a " + host + ":" + puerto + " sala=" + salaId);
        } catch (Exception e) {
            Vista.mostrarError("No se pudo conectar: " + e.getMessage());
        }
    }

    // === Recepcion de mensajes del servidor ===
    private void onMensaje(Mensaje m) {
        try {
            switch (m.tipo) {
                case UNIDO -> {
                    var ok = (MensajeUnido) m;
                    if (pendingUnido != null && !pendingUnido.isDone()) {
                        pendingUnido.complete(ok);
                    }
                }
                case CUENTA_ATRAS -> {
                    var cta = (com.mycompany.parchismvc.net.dto.MensajeCuentaAtras) m;
                    var s = salaCache;
                    String donde = (s != null && s.estado == com.mycompany.parchismvc.Model.EstadoSala.JUGANDO)
                            ? "Turno" : "Inicio";
                    if (vista != null) {
                        vista.mostrarInfo(donde + " en " + cta.segundosRestantes + "s…");
                    }
                }
                case RESULTADO -> {
                    var r = (com.mycompany.parchismvc.net.dto.MensajeResultado) m;
                    if (pendingResultado != null && !pendingResultado.isDone()) {
                        pendingResultado.complete(r);
                    }
                    // opcional we
                    if (r.ok) {
                        Vista.mostrarInfo(r.mensaje);
                    } else {
                        Vista.mostrarError(r.mensaje);
                    }
                }
                case DADO -> {
                    var d = (com.mycompany.parchismvc.net.dto.MensajeDado) m;
                    if (pendingDado != null && !pendingDado.isDone()) {
                        pendingDado.complete(d);
                    }
                    if (vista != null) {
                        vista.repintarPromptTurno();
                    }

                }
                case ESTADO -> {
                    var est = (MensajeEstado) m;
                    this.salaCache = est.sala;
                    this.turnoCache = est.turnoDe;
                    Vista.actualizarEstado(salaCache, turnoCache, miId);

                    if (vista != null) {
                        if (est.sala.estado == com.mycompany.parchismvc.Model.EstadoSala.JUGANDO) {
                            // Muestra la ayuda del modo juego automáticamente al iniciar la partida
                            vista.mostrarComandosModoJuegoSiHaceFalta();
                        } else {
                            // Volvemos a permitir que se muestre la ayuda la próxima vez
                            vista.resetAyudaModo();
                        }
                    }

                }

                case ERROR -> {
                    var er = (MensajeError) m;
                    Vista.mostrarError(er.razon);
                }
                default -> {
                }
            }
        } catch (Exception ex) {
            Vista.mostrarError("Error procesando mensaje: " + ex.getMessage());
        }
    }

}
