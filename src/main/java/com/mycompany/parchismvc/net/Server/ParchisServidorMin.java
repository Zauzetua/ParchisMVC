package com.mycompany.parchismvc.net.Server;

import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.Sala;
import com.mycompany.parchismvc.Repo.IRepositorioSala;
import com.mycompany.parchismvc.Repo.RepositorioSalaMemoria;
import com.mycompany.parchismvc.Service.ServicioJuego;
import com.mycompany.parchismvc.net.dto.*;
import com.mycompany.parchismvc.net.filters.BroadcastFilter;
import com.mycompany.parchismvc.net.filters.CancelarListoFilter;
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
import com.mycompany.parchismvc.pf.Ctx;
import com.mycompany.parchismvc.pf.Pipeline;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * Servidor mínimo: - Acepta clientes - Maneja UNIRSE - Responde UNIDO y ESTADO
 * (snapshot de Sala)
 *
 * Más adelante añadimos colores, listo, tirar dado y mover.
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

    // ====== Obtiene/crea sala por id======
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
        
    }

    
    // ====== Sesion por cliente ======
    public class SesionCliente implements Runnable {

        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        public SalaActiva sala;
        public Jugador yo;

        // --------- Pipeline (pipes & filters) ----------
        private final Pipeline pipeline = Pipeline.of(
                new LogFilter(),
                // Resuelve sala en el contexto: si es UNIRSE usa el id del mensaje; si no, la ya asignada a la sesión
                new SalaBindingFilter(ParchisServidorMin.this::sala),
                // Enrutamiento a filtros por tipo de mensaje
                new RouterFilter(Map.of(
                        TipoMensaje.UNIRSE, new JoinFilter(),
                        TipoMensaje.ELEGIR_COLOR, new ElegirColorFilter(),
                        TipoMensaje.LISTO, new ListoFilter(),
                        TipoMensaje.CANCELAR_LISTO, new CancelarListoFilter(),
                        TipoMensaje.INICIAR, new IniciarFilter(),
                        TipoMensaje.TIEMPO, new SetTiempoFilter(),
                        TipoMensaje.TIRAR_DADO, new TirarDadoFilter(),
                        TipoMensaje.MOVER, new MoverFilter()
                // Aquí puedes añadir: ELEGIR_COLOR, LISTO, INICIAR, etc.
                )),
                // Si algún filtro marcó mutación del estado -> emitir snapshot
                new EmitEstadoFilter(),
                // Finalmente, enviar acumulados a emisor y a todos
                new BroadcastFilter()
        );

        public SesionCliente(Socket socket) {
            this.socket = socket;
        }

        public SalaActiva getSala() {
            return sala;
        }

        public void setSala(SalaActiva sala) {
            this.sala = sala;
        }

        public Jugador getYo() {
            return yo;
        }

        public void setYo(Jugador yo) {
            this.yo = yo;
        }

        @Override
        public void run() {
            try (socket) {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                System.out.println("[SERVIDOR] Cliente conectado: " + socket.getRemoteSocketAddress());

                while (true) {
                    Object o = in.readObject();
                    if (o instanceof Mensaje m) {
                        Ctx ctx = new Ctx(this, m);
                        pipeline.run(ctx);
                    }
                }
            } catch (Exception e) {
                System.out.println("[SERVIDOR] Cliente fuera: " + e.getMessage());
                if (sala != null) {
                    sala.clientes.remove(this);
                }
            }
        }

        public void enviar(Mensaje m) {
            try {
                out.reset();
                out.writeObject(m);
                out.flush();
            } catch (IOException ignored) {
            }
        }

        @Override
        public String toString() {
            return "Sesion[" + socket.getRemoteSocketAddress() + "]";
        }
    }
}
