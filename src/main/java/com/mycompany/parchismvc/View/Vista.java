package com.mycompany.parchismvc.View;

import com.mycompany.parchismvc.Controller.Controlador;
import com.mycompany.parchismvc.Model.ColorJugador;
import com.mycompany.parchismvc.Model.EstadoSala;
import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.Sala;
import com.mycompany.parchismvc.Service.ServicioJuego;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Equipo 1 Parchis
 */
public class Vista {

    /**
     * Controlador del juego
     */
    private final Controlador ctl;
    /**
     * Scanner para leer entrada del usuario
     */
    private final Scanner sc = new Scanner(System.in);
    /**
     * Executor para manejar tiempo de espera en turnos
     */
    private final ExecutorService exe = Executors.newSingleThreadExecutor();
    /**
     * Tiempo de espera en segundos antes de iniciar el juego tras estar todos
     */
    private static final int TIEMPO_ESPERA_INICIO = 10;

    /**
     * Constructor de la vista
     * 
     * @param ctl Controlador del juego
     */
    public Vista(Controlador ctl) {
        this.ctl = ctl;
    }

    /**
     * Inicia la interfaz de línea de comandos.
     * 
     */
    public void iniciar() {
        imprimirInstrucciones();
        boolean salir = false;
        Map<String, UUID> aliasJugadores = new HashMap<>();

        while (!salir) {
            System.out.print("> ");
            String linea = sc.nextLine().trim();
            if (linea.isEmpty()) {
                continue;
            }
            // dividir la línea en comando y argumentos
            String[] partes = linea.split("\\s+");
            String cmd = partes[0].toLowerCase(Locale.ROOT);

            try {
                switch (cmd) {
                    /**
                     * Mostrar instrucciones de uso
                     */
                    case "help", "ayuda":
                        imprimirInstrucciones();
                        break;
                    /**
                     * Registrar un nuevo jugador en la sala.
                     */
                    case "registro": {
                        if (partes.length < 2) {
                            System.out.println("Uso: registro <nombre> [avatar]");
                            break;
                        }
                        String nombre = partes[1];
                        String avatar = partes.length >= 3 ? partes[2] : "avatar.png";
                        Jugador j = ctl.registrar(nombre, avatar);
                        aliasJugadores.put(nombre, j.id);
                        System.out.println("Registrado: " + j.nombre + " id=" + j.id);
                        break;
                    }
                    /**
                     * Elegir color para un jugador registrado.
                     */
                    case "elegircolor": {
                        if (partes.length < 3) {
                            System.out.println("Uso: elegircolor <nombre> <COLOR>");
                            break;
                        }
                        String nombre = partes[1];
                        String colorTxt = partes[2].toUpperCase(Locale.ROOT);
                        UUID id = aliasJugadores.get(nombre);
                        if (id == null) {
                            System.out.println("Jugador no registrado: " + nombre);
                            break;
                        }
                        try {
                            ColorJugador color = ColorJugador.valueOf(colorTxt);
                            System.out.println(ctl.elegirColor(id, color));
                        } catch (IllegalArgumentException ex) {
                            System.out.println("Color invalido");
                        }
                        break;
                    }
                    /**
                     * Marcar un jugador como listo para iniciar el juego.
                     */
                    case "listo": {
                        if (partes.length < 2) {
                            System.out.println("Uso: listo <nombre>");
                            break;
                        }
                        UUID id = aliasJugadores.get(partes[1]);
                        if (id == null) {
                            System.out.println("Jugador no registrado");
                            break;
                        }
                        System.out.println(ctl.listo(id));
                        // si todos listos, iniciar cuenta regresiva
                        if (ctl.sala().estado == EstadoSala.ESPERANDO
                                && ctl.sala().jugadores.size() >= ServicioJuego.MIN_JUGADORES) {
                            if (ctl.iniciarSiListos().startsWith("Iniciando")) {
                                cuentaRegresivaYComienza();
                            }
                        }
                        break;
                    }
                    /**
                     * Cancelar el estado de listo de un jugador
                     */
                    case "cancelar": {
                        if (partes.length < 2) {
                            System.out.println("Uso: cancelar <nombre>");
                            break;
                        }
                        UUID id = aliasJugadores.get(partes[1]);
                        if (id == null) {
                            System.out.println("Jugador no registrado");
                            break;
                        }
                        System.out.println(ctl.cancelar(id));
                        break;
                    }
                    /**
                     * Establecer el tiempo por turno antes de iniciar el juego
                     */
                    case "tiempo":
                        if (partes.length < 2) {
                            System.out.println("Uso: tiempo <segundos>");
                            break;
                        }
                        int seg = Integer.parseInt(partes[1]);
                        ctl.setTiempo(seg);
                        System.out.println("Tiempo por turno ajustado a " + seg + "s");
                        break;
                    /**
                     * Forzar el inicio del juego si hay al menos 2 jugadores
                     */
                    case "iniciar":
                        System.out.println(ctl.forzarIniciar());
                        cuentaRegresivaYComienza();
                        break;
                    /**
                     * Mostrar el estado actual de la sala
                     */
                    case "estado":
                        System.out.println(ctl.estado());
                        break;
                    /**
                     * Entrar al modo juego interactivo
                     */
                    case "jugar":
                        modoJuego(aliasJugadores);
                        break;
                    /**
                     * Salir del programa
                     */
                    case "salir":
                        salir = true;
                        break;
                    default:
                        System.out.println("Comando no reconocido. 'help' para ver comandos.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }

        exe.shutdownNow();
        System.out.println("Saliendo...");
    }

    /**
     * Imprime las instrucciones de uso.
     */
    private void imprimirInstrucciones() {
        System.out.println("COMANDOS:");
        System.out.println("  registro <nombre> [avatar]     - registrar un jugador");
        System.out.println("  elegircolor <nombre> <COLOR>   - elegir ROJO, AZUL, VERDE, AMARILLO");
        System.out.println("  tiempo <segundos>              - establecer tiempo por turno (antes de iniciar)");
        System.out.println("  listo <nombre>                 - marcar listo");
        System.out.println("  cancelar <nombre>              - cancelar listo");
        System.out.println("  iniciar                        - forzar inicio (si hay >=2 jugadores)");
        System.out.println("  estado                         - mostrar estado de la sala");
        System.out.println("  jugar                          - entrar al modo juego (solo si ya inició)");
        System.out.println("  salir                          - salir del programa");
        System.out.println("");
    }

    /**
     * Inicia una cuenta regresiva y comienza el juego si la sala está en estado
     * INICIANDO.
     */
    private void cuentaRegresivaYComienza() {
        Sala s = ctl.sala();
        if (s.estado != EstadoSala.INICIANDO) {
            return;
        }
        new Thread(() -> {
            try {
                for (int i = TIEMPO_ESPERA_INICIO; i >= 1; i--) {
                    System.out.println("Iniciando en " + i + "...");
                    Thread.sleep(1000);
                }
                ctl.comenzarJuego();
                System.out.println("Juego iniciado. Usa comando 'jugar' para entrar al modo jugador y jugar.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void modoJuego(Map<String, UUID> aliasJugadores) {
        Sala s = ctl.sala();
        if (s.estado != EstadoSala.JUGANDO) {
            System.out.println("El juego no ha iniciado aún.");
            return;
        }
        System.out.println("Modo juego interactivo:");
        System.out.println("  tirar <nombre>");
        System.out.println("  mover <nombre> <indiceFicha>");
        System.out.println("  estado");
        System.out.println("  salirmodo");
        boolean modo = true;
        while (modo) {
            Jugador turnoJugador = ctl.sala().jugadores.get(ctl.sala().indiceTurno);
            System.out.print("[Turno: " + turnoJugador.nombre + "] > ");
            String linea = leerLineaConTimeout(s.tiempoPorTurno);
            if (linea == null) {
                System.out.println("Tiempo agotado. Turno pasado.");
                ctl.sala().indiceTurno = (ctl.sala().indiceTurno + 1) % ctl.sala().jugadores.size();
                continue;
            }
            String[] partes = linea.trim().split("\\s+");
            if (partes.length == 0) {
                continue;
            }
            String cmd = partes[0].toLowerCase(Locale.ROOT);
            try {
                switch (cmd) {
                    case "tirar": {
                        if (partes.length < 2) {
                            System.out.println("Uso: tirar <nombre>");
                            break;
                        }
                        UUID id = aliasJugadores.get(partes[1]);
                        if (id == null) {
                            System.out.println("Jugador no registrado");
                            break;
                        }
                        if (!turnoJugador.id.equals(id)) {
                            System.out.println("No es tu turno");
                            break;
                        }
                        int v = ctl.tirar(id);
                        if (v == 0) {
                            System.out.println("No es tu turno o error");
                        } else {
                            System.out.println("Resultado del dado: " + v
                                    + (v == 6 ? " (¡6! Turno extra si mueves correctamente)" : ""));
                        }
                        break;
                    }
                    case "mover": {
                        if (partes.length < 3) {
                            System.out.println("Uso: mover <nombre> <indiceFicha>");
                            break;
                        }
                        UUID id = aliasJugadores.get(partes[1]);
                        if (id == null) {
                            System.out.println("Jugador no registrado");
                            break;
                        }
                        if (!turnoJugador.id.equals(id)) {
                            System.out.println("No es tu turno");
                            break;
                        }
                        int idx = Integer.parseInt(partes[2]);
                        String res = ctl.mover(id, idx);
                        System.out.println(res);
                        if (ctl.sala().estado == EstadoSala.FINALIZADA) {
                            System.out.println("Juego finalizado. Ganador: "
                                    + ctl.sala().jugadores.stream().filter(j -> j.id.equals(ctl.sala().ganador))
                                            .findFirst().map(j -> j.nombre).orElse("n/a"));
                            modo = false;
                        }
                        break;
                    }
                    case "estado":
                        System.out.println(ctl.estado());
                        break;
                    case "salirmodo":
                        modo = false;
                        break;
                    default:
                        System.out.println("Comando de modo juego no reconocido.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private String leerLineaConTimeout(int segundos) {
        Future<String> futuro = exe.submit(() -> {
            try {
                return sc.nextLine();
            } catch (NoSuchElementException e) {
                return null;
            }
        });
        try {
            return futuro.get(segundos, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            futuro.cancel(true);
            return null;
        } catch (Exception e) {
            futuro.cancel(true);
            return null;
        }
    }
}
