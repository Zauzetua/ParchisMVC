package com.mycompany.parchismvc.net.Server;

import com.mycompany.parchismvc.Model.EstadoSala;
import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.Sala;
import com.mycompany.parchismvc.Repo.IRepositorioSala;
import com.mycompany.parchismvc.Repo.RepositorioSalaMemoria;
import com.mycompany.parchismvc.Service.ServicioJuego;
import com.mycompany.parchismvc.net.dto.*;
import com.mycompany.parchismvc.net.filters.BroadcastFilter;
import com.mycompany.parchismvc.net.filters.CancelarListoFilter;
import com.mycompany.parchismvc.net.filters.DispatchFilter;
import com.mycompany.parchismvc.net.filters.ElegirColorFilter;
import com.mycompany.parchismvc.net.filters.EmitEstadoFilter;
import com.mycompany.parchismvc.net.filters.IniciarFilter;
import com.mycompany.parchismvc.net.filters.JoinFilter;
import com.mycompany.parchismvc.net.filters.ListoFilter;
import com.mycompany.parchismvc.net.filters.LogFilter;
import com.mycompany.parchismvc.net.filters.MoverFilter;
import com.mycompany.parchismvc.net.filters.RouterFilter;
import com.mycompany.parchismvc.net.filters.SalaBindingFilter;
import com.mycompany.parchismvc.net.filters.SetTiempoFilter;
import com.mycompany.parchismvc.net.filters.TirarDadoFilter;
import com.mycompany.parchismvc.net.wire.Codec;
import com.mycompany.parchismvc.net.wire.Codecs;
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Dispatcher;
import com.mycompany.parchismvc.pf.MapDispatcher;
import com.mycompany.parchismvc.pf.Pipeline;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * Servidor: - Acepta clientes - Maneja UNIRSE - Responde UNIDO y ESTADO
 * (snapshot de Sala)
 *
 * Mas adelante anadimos colores, listo, tirar dado y mover.
 */
public class ParchisServidorMin {

    public static void main(String[] args) throws Exception {
        new ParchisServidorMin(5000).iniciar();
    }

    private final ServerSocket server;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<String, SalaActiva> salas = new ConcurrentHashMap<>();

    public ParchisServidorMin(int puerto) throws IOException {
        this.server = new ServerSocket(puerto);
        System.out.println("[SERVIDOR] Escuchando en puerto " + puerto);
    }

    public void iniciar() throws IOException {
        while (true) {
            Socket s = server.accept();
            pool.submit(new SesionCliente(s));
        }
    }

    //Obtiene/crea sala por id
    public SalaActiva sala(String idSala) {
        return salas.computeIfAbsent(idSala, SalaActiva::new);
    }

    public static class SalaActiva {

        private final String id;
        private final IRepositorioSala repo;
        public final ServicioJuego servicio;

        public final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        public ScheduledFuture<?> tareaInicio;
        public java.util.concurrent.ScheduledFuture<?> tareaTick;
        public volatile int segundosRestantes;
        public java.util.concurrent.ScheduledFuture<?> tareaTurnoTick;
        public java.util.concurrent.ScheduledFuture<?> tareaTurnoFin;
        public volatile int segundosTurno;

        public final List<SesionCliente> clientes = new CopyOnWriteArrayList<>();
        public final Object lock = new Object();

        SalaActiva(String id) {
            this.id = id;
            this.repo = new RepositorioSalaMemoria();
            com.mycompany.parchismvc.Util.Dado dadoImpl = new com.mycompany.parchismvc.Util.DadoAleatorio();
            this.servicio = new ServicioJuego(repo, dadoImpl);
        }

        public Sala sala() {
            return servicio.sala();
        }

        public void broadcast(Mensaje m) {
            for (var c : clientes) {
                c.enviar(m);
            }
        }

        public void addCliente(SesionCliente c) {
            clientes.add(c);
        }

        public void removeCliente(SesionCliente c) {
            clientes.remove(c);
        }

        public UUID turnoActual() {
            Jugador j = servicio.jugadorActual();
            return (j == null) ? null : j.id;
        }
        
        public void cancelarRelojTurno() {
    if (tareaTurnoTick != null && !tareaTurnoTick.isDone()) tareaTurnoTick.cancel(false);
    if (tareaTurnoFin  != null && !tareaTurnoFin.isDone())  tareaTurnoFin.cancel(false);
}

        public void iniciarRelojTurno() {
            cancelarRelojTurno();
            segundosTurno = sala().tiempoPorTurno > 0 ? sala().tiempoPorTurno : 30; // default
            // enviamos un tick inicial para pintar en clientes
            broadcast(new com.mycompany.parchismvc.net.dto.MensajeCuentaAtras(segundosTurno));
            tareaTurnoTick = scheduler.scheduleAtFixedRate(() -> {
                synchronized (lock) {
                    segundosTurno--;
                    if (segundosTurno > 0) {
                        broadcast(new com.mycompany.parchismvc.net.dto.MensajeCuentaAtras(segundosTurno));
                    }
                }
            }, 1, 1, java.util.concurrent.TimeUnit.SECONDS);

            tareaTurnoFin = scheduler.schedule(() -> {
                synchronized (lock) {
                    cancelarRelojTurno();
                    var turnoAntes = turnoActual();
                    servicio.pasarTurnoPorTiempo();
                    broadcast(new com.mycompany.parchismvc.net.dto.MensajeResultado(true,
                            "Tiempo agotado. Turno pasado."));
                    broadcast(new com.mycompany.parchismvc.net.dto.MensajeEstado(sala(), turnoActual()));
                    // reinicia el reloj para el nuevo turno
                    iniciarRelojTurno();
                }
            }, segundosTurno, java.util.concurrent.TimeUnit.SECONDS);
        }
        
        public void reiniciarRelojTurnoSiCambio(UUID turnoAntes) {
    UUID turnoDespues = turnoActual();
    if (!java.util.Objects.equals(turnoAntes, turnoDespues)) {
        iniciarRelojTurno();
    }
}

    public void terminarSiQuedanMenosDe2() {
    long conectados = sala().jugadores.stream().filter(j -> j.conectado).count();

    // Queremos cerrar tanto si estaba JUGANDO como si estaba INICIANDO
    if ((sala().estado == EstadoSala.JUGANDO || sala().estado == EstadoSala.INICIANDO)
            && conectados < 2) {

        // apaga timers de inicio y de turno
        if (tareaInicio != null && !tareaInicio.isDone()) tareaInicio.cancel(false);
        if (tareaTick   != null && !tareaTick.isDone())   tareaTick.cancel(false);
        cancelarRelojTurno();

        // si queda 1 conectado, es el ganador; si 0, sin ganador
        UUID ganador = null;
        if (conectados == 1) {
            ganador = sala().jugadores.stream()
                    .filter(j -> j.conectado)
                    .map(j -> j.id)
                    .findFirst().orElse(null);
        }

        sala().estado  = EstadoSala.FINALIZADA;
        sala().ganador = ganador;

        String msg = (conectados == 1)
                ? "Partida finalizada: queda un solo jugador activo (se cierra para todos)."
                : "Partida finalizada: no quedan jugadores activos (se cierra para todos).";

        // Mensajes informativos + snapshot final
        broadcast(new com.mycompany.parchismvc.net.dto.MensajeResultado(true, msg));
        broadcast(new com.mycompany.parchismvc.net.dto.MensajeEstado(sala(), turnoActual()));

        // ORDEN DE CIERRE para todos los clientes (incluye UUID del ganador)
        broadcast(new com.mycompany.parchismvc.net.dto.MensajeFinPartida(msg, ganador));
    }
}
        
    }

    
    // ====== Sesion por cliente ======
    public class SesionCliente implements Runnable {

        private final Socket socket;
        private BufferedInputStream bin;
        private OutputStream bout;
        private Codec codec;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        public SalaActiva sala;
        public Jugador yo;
        
         // Pipeline (con Dispatcher)
        private final Pipeline pipeline;

        public SesionCliente(Socket socket) {
            this.socket = socket;

            // Dispatcher (tabla de rutas TipoMensaje -> filtro de dominio)
            Dispatcher dispatcher = new MapDispatcher(Map.of(
                TipoMensaje.UNIRSE,         new JoinFilter(),
                TipoMensaje.ELEGIR_COLOR,   new ElegirColorFilter(),
                TipoMensaje.LISTO,          new ListoFilter(),
                TipoMensaje.CANCELAR_LISTO, new CancelarListoFilter(),
                TipoMensaje.INICIAR,        new IniciarFilter(),
                TipoMensaje.TIEMPO,         new SetTiempoFilter(),
                TipoMensaje.TIRAR_DADO,     new TirarDadoFilter(),
                TipoMensaje.MOVER,          new MoverFilter()
            ));

            this.pipeline = Pipeline.of(
                new LogFilter(),
                new SalaBindingFilter(ParchisServidorMin.this::sala),
                new DispatchFilter(dispatcher),   // <— reemplaza a RouterFilter
                new EmitEstadoFilter(),
                new BroadcastFilter()
            );
        }
        
        private void onDisconnect() {
    try {
        if (sala == null || yo == null) return;
       synchronized (sala.lock) {
    sala.clientes.remove(this);
    yo.conectado = false;

    // ¿se termina la partida por quedar <2?
    sala.terminarSiQuedanMenosDe2();
    if (sala.sala().estado == EstadoSala.FINALIZADA) return;

    // regresar fichas y normalizar turno
    sala.servicio.regresarFichasABase(yo.id);
    UUID turnoAntes = sala.turnoActual();
    sala.servicio.pasarTurnoPorDesconexion(yo.id);

    // reloj
    sala.cancelarRelojTurno();
    if (!java.util.Objects.equals(turnoAntes, sala.turnoActual())) {
        sala.iniciarRelojTurno();
    } else {
        // si no cambió (p.ej. el que se fue no era el del turno), asegúrate de que el actual esté conectado
        // jugadorActual() ya normaliza; por claridad puedes dejar así.
        sala.iniciarRelojTurno();
    }

    sala.broadcast(new MensajeResultado(true,
        yo.nombre + " se desconectó: sus fichas volvieron a BASE y se pasó el turno si aplicaba."));
    sala.broadcast(new MensajeEstado(sala.sala(), sala.turnoActual()));
}
    } catch (Exception ignored) {}
}

        public SalaActiva getSala() { return sala; }
        public void setSala(SalaActiva sala) { this.sala = sala; }
        public Jugador getYo() { return yo; }
        public void setYo(Jugador yo) { this.yo = yo; }

        @Override
        public void run() {
            try (socket) {
                // Detectar codec por sesión (Java serialization o JSON enmarcado)
                this.bin  = new BufferedInputStream(socket.getInputStream());
                this.bout = socket.getOutputStream();
                this.codec = Codecs.detectar(bin, bout);
                System.out.println("[SERVIDOR] Cliente conectado: " + socket.getRemoteSocketAddress()
                        + " codec=" + codec.nombre());

                while (true) {
                    Mensaje m = codec.leer(bin);        // bloqueante; lanza EOFException si se cierra
                    Ctx ctx = new Ctx(this, m);
                    pipeline.run(ctx);
                }
            } catch (EOFException eof) {
                System.out.println("[SERVIDOR] Cliente cerro conexión: " + socket.getRemoteSocketAddress());
                if (sala != null) sala.removeCliente(this);
                onDisconnect();

            } catch (Exception e) {
                System.out.println("[SERVIDOR] Cliente fuera: " + e.getMessage());
                if (sala != null) sala.removeCliente(this);
                onDisconnect();
            }
        }

        public void enviar(Mensaje m) {
            try {
                codec.escribir(bout, m);
            } catch (Exception ignored) {
            }
        }

        @Override
        public String toString() {
            return "Sesion[" + socket.getRemoteSocketAddress() + "]";
        }
    }
}