package com.mycompany.parchismvc.View;

import com.mycompany.parchismvc.Controller.Controlador;
import com.mycompany.parchismvc.Model.ColorJugador;
import com.mycompany.parchismvc.Model.EstadoSala;
import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.Sala;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
        ctl.setVista(this);
    }
    
    // Esto es para que los jugadores le piquen a juagr para cuando todos esten listo 
    //private boolean avisoModoMostrado = false;
    
    private volatile boolean enModoJuegoFlag = false;
    
    private boolean ayudaModoMostrada = false;
   
    
// checando 
//public void sugerirEntrarModoJuego() {
//    if (!avisoModoMostrado) {
//        System.out.println("Juego iniciado. Escribe 'jugar' para entrar al modo juego.");
//        avisoModoMostrado = true;
//    }
//}

//public void resetSugerenciaModo() {
//    avisoModoMostrado = false;
//}
    

    /**
     * Inicia la interfaz de linea de comandos.
     * 
     */

     

    public void iniciar() {
         // Conexion al servidor (ajustar host/puerto/sala)
    ctl.conectarRed("127.0.0.1", 5000, "sala1");
        
        imprimirInstrucciones();
        boolean salir = false;
        Map<String, UUID> aliasJugadores = new HashMap<>();

         while (!salir) {

        // === AUTO-ENTRAR A MODO JUEGO CUANDO EL SERVIDOR ESTE JUGANDO WE===
       try {
            Sala st = ctl.sala();
            if (!enModoJuegoFlag && st != null && st.estado == EstadoSala.JUGANDO) {
                enModoJuegoFlag = true;
                try { modoJuego(aliasJugadores, null); } finally { enModoJuegoFlag = false; }
                continue; // volvemos al menu tras salir del modo
            }
        } catch (Exception ignored) {}


        System.out.print("> ");
        String linea = sc.nextLine().trim();
        if (linea.isEmpty()) {
            continue;
        }
        Sala stNow = ctl.sala();
        if (stNow != null && stNow.estado == EstadoSala.JUGANDO) {
            String lower = linea.toLowerCase(Locale.ROOT);
            if (lower.startsWith("tirar") || lower.startsWith("mover") || lower.equals("estado")) {
                enModoJuegoFlag = true;
                try { modoJuego(aliasJugadores, linea); } finally { enModoJuegoFlag = false; }
                continue;
            }
        }
        String[] partes = linea.split("\\s+");
        String cmd = partes[0].toLowerCase(Locale.ROOT);

        try {
            switch (cmd) {
                case "help", "ayuda" -> imprimirInstrucciones();

                case "registro" -> {
                    if (partes.length < 2) {
                        System.out.println("Uso: registro <nombre> [avatar]");
                        break;
                    }
                    String nombre = partes[1];
                    String avatar = (partes.length >= 3 ? partes[2] : "avatar.png");
                    UUID id = ctl.registrar(nombre, avatar);
                    aliasJugadores.put(nombre, id);
                    System.out.println("Registrado: " + nombre + " id=" + id);
                }

                case "elegircolor" -> {
                    if (partes.length < 3) {
                        System.out.println("Uso: elegircolor <nombre> <COLOR>");
                        break;
                    }
                    String nombre = partes[1];
                    String colorTxt = partes[2].toUpperCase(Locale.ROOT);
                    UUID id = aliasJugadores.get(nombre);
                    if (id == null) { System.out.println("Jugador no registrado: " + nombre); break; }
                    try {
                        ColorJugador color = ColorJugador.valueOf(colorTxt);
                        System.out.println(ctl.elegirColor(id, color));
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Color invalido");
                    }
                }

                case "listo" -> {
                    if (partes.length < 2) {
                        System.out.println("Uso: listo <nombre>");
                        break;
                    }
                    UUID id = aliasJugadores.get(partes[1]);
                    if (id == null) { System.out.println("Jugador no registrado"); break; }
                    // El servidor se encarga de la cuenta atras e inicio automatico
                    System.out.println(ctl.listo(id));
                }

                case "cancelar" -> {
                    if (partes.length < 2) {
                        System.out.println("Uso: cancelar <nombre>");
                        break;
                    }
                    UUID id = aliasJugadores.get(partes[1]);
                    if (id == null) { System.out.println("Jugador no registrado"); break; }
                    System.out.println(ctl.cancelar(id));
                }

                case "tiempo" -> {
                    if (partes.length < 2) {
                        System.out.println("Uso: tiempo <segundos>");
                        break;
                    }
                    int seg = Integer.parseInt(partes[1]);
                    ctl.setTiempo(seg); // el servidor lo difundira por ESTADO/RESULTADO
                    System.out.println("Tiempo por turno ajustado a " + seg + "s");
                }

                case "iniciar" -> {
                    // Solicita el inicio si todos estan listos; el servidor hara cuenta atras y empezar
                    System.out.println(ctl.iniciarSiListos());
                }

                case "estado" -> System.out.println(ctl.estado());

                case "jugar" -> {
                    // Plan b si no jala alv
                }

                case "salir" -> salir = true;

                default -> System.out.println("Comando no reconocido. 'help' para ver comandos.");
            }
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    exe.shutdownNow();
    System.out.println("Saliendo...");
}

    
    /*
    Calando imprimir instrucciones para jugar alv
    */
    public void mostrarComandosModoJuegoSiHaceFalta() {
  if (ayudaModoMostrada) return;
    imprimirAyudaModo();
    ayudaModoMostrada = true;
}
    
    private void imprimirAyudaModo() {
    System.out.println("Modo juego interactivo:");
    System.out.println("  tirar <nombre>");
    System.out.println("  mover <nombre> <indiceFicha>");
    System.out.println("  estado");
    System.out.println("  salirmodo");
}
    
    public void resetAyudaModo(){
        ayudaModoMostrada = false; 
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
     * Inicia una cuenta regresiva y comienza el juego si la sala esta en estado
     * INICIANDO.
     */
  
   private void modoJuego(Map<String, UUID> aliasJugadores, String primeraLinea) {
    Sala s = ctl.sala();
    if (s == null || s.estado != EstadoSala.JUGANDO) {
        System.out.println("El juego no ha iniciado aún.");
        return;
    }
    
     if (!ayudaModoMostrada) {        // <--- evita el doble "Modo juego..."
        imprimirAyudaModo();
        ayudaModoMostrada = true;
    }

    System.out.println("Modo juego interactivo:");
    System.out.println("  tirar <nombre>");
    System.out.println("  mover <nombre> <indiceFicha>");
    System.out.println("  estado");
    System.out.println("  salirmodo");

    enModoJuegoFlag = true;
    try {
    String linea = primeraLinea; // <- arrancamos con la que ya tecleo el usuario
        boolean modo = true;

        while (modo) {
            Jugador turnoJugador = ctl.sala().jugadores.get(ctl.sala().indiceTurno);
            if (linea == null) {
                System.out.print("[Turno: " + turnoJugador.nombre + "] > ");
                linea = sc.nextLine();
            }
            if (linea == null) continue;

            // Aceptar "<nombre> <comando>" y reordenar si aplica
            String[] partes = linea.trim().split("\\s+");
            if (partes.length == 0 || partes[0].isEmpty()) { linea = null; continue; }
            Map<String, UUID> aliasJugadoresLower = new HashMap<>();
            for (var e : aliasJugadores.entrySet()) aliasJugadoresLower.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue());
            if (partes.length == 2 && aliasJugadoresLower.containsKey(partes[0].toLowerCase(Locale.ROOT))) {
                String nombre = partes[0];
                String comando = partes[1].toLowerCase(Locale.ROOT);
                partes = new String[]{ comando, nombre };
            }

            String cmd = partes[0].toLowerCase(Locale.ROOT);
            try {
                switch (cmd) {
                    case "tirar" -> {
                        if (partes.length < 2) { System.out.println("Uso: tirar <nombre>"); break; }
                        UUID id = aliasJugadoresLower.get(partes[1].toLowerCase(Locale.ROOT));
                        if (id == null) { System.out.println("Jugador no registrado"); break; }
                        if (!turnoJugador.id.equals(id)) { System.out.println("No es tu turno"); break; }
                        int v = ctl.tirar(id);
                        if (v == 0) System.out.println("No es tu turno o ya tiraste.");
                        else System.out.println("Resultado del dado: " + v + (v == 6 ? " (¡6! turno extra si mueves)" : ""));
                    }
                    case "mover" -> {
                        if (partes.length < 3) { System.out.println("Uso: mover <nombre> <indiceFicha>"); break; }
                        UUID id = aliasJugadoresLower.get(partes[1].toLowerCase(Locale.ROOT));
                        if (id == null) { System.out.println("Jugador no registrado"); break; }
                        if (!turnoJugador.id.equals(id)) { System.out.println("No es tu turno"); break; }
                        int idx;
                        try { idx = Integer.parseInt(partes[2]); }
                        catch (NumberFormatException e) { System.out.println("Indice invalido"); break; }
                        String res = ctl.mover(id, idx);
                        System.out.println(res);
                        if (ctl.sala().estado == EstadoSala.FINALIZADA) {
                            String ganador = ctl.sala().jugadores.stream()
                                    .filter(j -> j.id.equals(ctl.sala().ganador))
                                    .findFirst().map(j -> j.nombre).orElse("n/a");
                            System.out.println("Juego finalizado. Ganador: " + ganador);
                            modo = false;
                        }
                    }
                    case "estado" -> System.out.println(ctl.estado());
                    case "salirmodo" -> modo = false;
                    default -> System.out.println("Comando de modo juego no reconocido.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }

            linea = null; // a partir de aqui leemos normalmente del teclado
        }
    } finally {
        enModoJuegoFlag = false;
        System.out.println("(Has salido del modo juego. Vuelves al menú.)");
    }
}

     public void repintarPromptTurno() {
    if (!enModoJuegoFlag) return;
    try {
        var s = ctl.sala();
        var turnoJugador = s.jugadores.get(s.indiceTurno);
        System.out.print("\n[Turno: " + turnoJugador.nombre + "] > ");
        System.out.flush();
    } catch (Exception ignored) {}
}
    
// ====== GANCHOS PARA RED (mensajes simples) ======
public static void mostrarError(String msg) {
    System.err.println("[ERROR] " + msg);
}
public static void mostrarInfo(String msg) {
    System.out.println("[INFO] " + msg);
}
public void mostrarChat(String txt) {
    System.out.println("[CHAT] " + txt);
}
public void mostrarDado(UUID jugadorId, int valor) {
    System.out.println("[DADO] Jugador " + jugadorId + " sacó: " + valor);
}
/** Render rapido del snapshot que envia el servidor */
public static void actualizarEstado(Sala sala, UUID turnoDe, UUID miId) {
    if (sala == null) { System.out.println("[ESTADO] (sin sala)"); return; }
    System.out.println("======= ESTADO DE SALA =======");
    System.out.println("Estado: " + sala.estado + " | Tiempo por turno: " + sala.tiempoPorTurno + "s");
    System.out.println("Jugadores (" + sala.jugadores.size() + "):");
    for (var j : sala.jugadores) {
        String soy = (miId != null && miId.equals(j.id)) ? " <- YO" : "";
        String turno = (turnoDe != null && turnoDe.equals(j.id)) ? " [EN TURNO]" : "";
        String listo = j.listo ? " [LISTO]" : "";
        String color = (j.color == null) ? "sin color" : j.color.name();
        System.out.println(" - " + j.nombre + " | id=" + j.id + " | " + color + listo + turno + soy);
        var mis = sala.fichasPorJugador.get(j.id);
        if (mis != null) {
            for (int i = 0; i < mis.size(); i++) {
                var f = mis.get(i);
                String pos = switch (f.estado) {
                    case BASE -> "BASE";
                    case CASA -> "CASA";
                    default -> "pos=" + f.posicion;
                };
                System.out.println("     [" + i + "] " + f.estado + " (" + pos + ")");
            }
        }
    }
    if (sala.ganador != null) {
        var g = sala.jugadores.stream().filter(x -> x.id.equals(sala.ganador)).findFirst().orElse(null);
        System.out.println("GANADOR: " + (g == null ? sala.ganador : g.nombre));
    }
    System.out.println("================================\n");
    }

   
}
