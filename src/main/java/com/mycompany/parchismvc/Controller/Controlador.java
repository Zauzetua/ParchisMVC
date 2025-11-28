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
import com.mycompany.parchismvc.View.GameEvents;
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
    private volatile Sala salaCache;       // ultimo snapshot del servidor
    private volatile UUID turnoCache;      // ultimo turno reportado
    private int ultimoValorDado = 0;

    public Controlador(ServicioJuego servicio) {
        this.servicio = servicio;
    } // modo local

    public Controlador() {
    }
    private CompletableFuture<com.mycompany.parchismvc.net.dto.MensajeResultado> pendingResultado;
    private CompletableFuture<com.mycompany.parchismvc.net.dto.MensajeDado> pendingDado;
    private com.mycompany.parchismvc.View.Vista vista;
    private GameEvents events; // callbacks UI opcionales

    public void setVista(Vista v) {
        this.vista = v;
    }
    public void setEvents(GameEvents e){
        this.events = e;
    }

    public UUID getMiId(){ return miId; }
    public Sala getSalaCache(){ return salaCache; }
    public UUID getTurnoCache(){ return turnoCache; }
    public int getUltimoValorDado() {
        return ultimoValorDado;
    }

    /**
     * Obtiene el valor actual del dado desde la caché del controlador.
     * @return El último valor obtenido en el dado.
     */
    public int getValorDado() {
        return this.ultimoValorDado;
    }

    // Esperas sincronas (para que tu Vista funcione igual que local)
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
            if(events!=null) events.onRegistrado(miId);
            return unido.jugadorId;
        } catch (Exception e) {
            Vista.mostrarError("Fallo al registrar: " + e.getMessage());
            if(events!=null) events.onError("Fallo al registrar: " + e.getMessage());
            return null;
        }
    }

    // Variante asincrona para UI Swing
    public void registrarAsync(String nombre, String avatar){
        if(red == null || !red.isReady()){
            if(events!=null) events.onError("No conectado al servidor (inicia o reintenta conexión)");
            return;
        }
        try {
            red.enviar(new SolicitudUnirse(salaId, nombre, avatar));
        } catch(Exception ex){
            if(events!=null) events.onError("Error enviando solicitud: "+ex.getMessage());
        }
    }

    /**
     * Permite a un jugador elegir un color.
     *
     * @param jugadorId ID del jugador.
     * @param color Color elegido por el jugador.
     * @return Mensaje indicando el resultado de la operacion.
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
     * @return Mensaje indicando el resultado de la operacion.
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
     * @return Mensaje indicando el resultado de la operacion.
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
     * @return Mensaje indicando el resultado de la operacion.
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
     * @return Mensaje indicando el resultado de la operacion.
     */
    public String mover(UUID jugadorId, int indiceFicha) {
        if (servicio != null) {
            return servicio.moverFicha(jugadorId, indiceFicha);
        }
        // Se ejecuta en un hilo separado para no bloquear la UI de Swing.
        new Thread(() -> {
            try {
                red.enviar(new com.mycompany.parchismvc.net.dto.MoverCmd(jugadorId, indiceFicha));
            } catch (Exception e) {
                if(events!=null) events.onError("Error al mover: " + e.getMessage());
            }
        }).start();
        return "Movimiento enviado..."; // Devolvemos un mensaje inmediato.
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
                /* aun no implementado en el servidor minimo */ }
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
            if(events!=null) events.onConectado(host, puerto, salaId);
        } catch (Exception e) {
            Vista.mostrarError("No se pudo conectar: " + e.getMessage());
            if(events!=null) events.onError("No se pudo conectar: " + e.getMessage());
        }
    }

    private volatile int segundosRestantes = -1;

    public int getSegundosRestantes() {
        return segundosRestantes;
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
                    this.miId = ok.jugadorId;
                    if(events!=null) events.onRegistrado(miId);
                }
                case CUENTA_ATRAS -> {
                    var cta = (com.mycompany.parchismvc.net.dto.MensajeCuentaAtras) m;
                    var s = salaCache;
                    if (salaCache != null
                            && salaCache.estado == com.mycompany.parchismvc.Model.EstadoSala.INICIANDO) {

                        if (vista != null) {
                            vista.mostrarInfo("Iniciando en " + cta.segundosRestantes + "…");
                        }

                    } else {
                        // Estamos en JUGANDO (timer de turno) no imprimir segun
                        this.segundosRestantes = cta.segundosRestantes;

                        // esta madre para despues, es para el promp
                        if (vista != null) {
                        }
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
                    if(events!=null) events.onResultado(r.ok, r.mensaje);
                }
                case DADO -> {
                    var d = (com.mycompany.parchismvc.net.dto.MensajeDado) m;
                    if (pendingDado != null && !pendingDado.isDone()) {
                        pendingDado.complete(d);
                    }
                    this.ultimoValorDado = d.valor;
                    if (vista != null) {
                    }
                    if(events!=null) events.onDado(d.jugadorId, d.valor);

                }
                case ESTADO -> {
                    var est = (MensajeEstado) m;
                    this.salaCache = est.sala;
                    this.turnoCache = est.turnoDe;                    
                    if(events!=null) events.onEstado(salaCache, turnoCache, miId);

                    if (vista != null) {
                        if (est.sala.estado == com.mycompany.parchismvc.Model.EstadoSala.JUGANDO) {
                            vista.mostrarComandosModoJuegoSiHaceFalta(); 
                        } else {
                            vista.resetAyudaModo();                      
                        }
                    }

                }

                case ERROR -> {
                    var er = (MensajeError) m;
                    Vista.mostrarError(er.razon);
                    if(events!=null) events.onError(er.razon);
                }
                default -> {
                }
            }
        } catch (Exception ex) {
            Vista.mostrarError("Error procesando mensaje: " + ex.getMessage());
        }
    }

    // Desconectar/Salir de la sala (cierra socket)
    public void desconectar(){
        try { if(red!=null) red.close(); } catch(Exception ignored) {}
    }

    
    
}
