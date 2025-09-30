/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Service;

import com.mycompany.parchismvc.Model.ColorJugador;
import com.mycompany.parchismvc.Model.EstadoFicha;
import com.mycompany.parchismvc.Model.EstadoSala;
import com.mycompany.parchismvc.Model.Ficha;
import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.Sala;
import com.mycompany.parchismvc.Repo.IRepositorioSala;
import com.mycompany.parchismvc.Util.Dado;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author jesus
 */
public class ServicioJuego {

    public static final int TAM_TABLERO = 52;
    public static final int FICHAS_POR_JUGADOR = 4;
    public static final int TIEMPO_ESPERA_INICIO = 10;
    public static final int MIN_JUGADORES = 2;

    private final IRepositorioSala repo;
    private final Dado dado;

    // mapa: color -> posición inicial (simplificada)
    private final Map<ColorJugador, Integer> inicioPorColor = Map.of(
            ColorJugador.ROJO, 0,
            ColorJugador.AZUL, 13,
            ColorJugador.VERDE, 26,
            ColorJugador.AMARILLO, 39
    );

    // estado temporal
    private final Map<UUID, Integer> ultimoValorTirado = new HashMap<>();
    private final Map<UUID, Boolean> tieneTurnoExtra = new HashMap<>();

    public ServicioJuego(IRepositorioSala repo, Dado dado) {
        this.repo = repo;
        this.dado = dado;
    }

    public Sala sala() {
        return repo.obtenerSala();
    }

    public Jugador registrarJugador(String nombre, String avatar) {
        Jugador j = new Jugador(nombre, avatar);
        Sala s = sala();
        s.jugadores.add(j);
        List<Ficha> lista = new ArrayList<>();
        for (int i = 0; i < FICHAS_POR_JUGADOR; i++) {
            lista.add(new Ficha(j.id));
        }
        s.fichasPorJugador.put(j.id, lista);
        return j;
    }

    public String elegirColor(UUID jugadorId, ColorJugador color) {
        Sala s = sala();
        Jugador j = buscarJugador(jugadorId);
        if (j == null) {
            return "Jugador no encontrado";
        }
        boolean ocupado = s.jugadores.stream().anyMatch(p -> color.equals(p.color));
        if (ocupado) {
            return "Color ya elegido por otro jugador";
        }
        j.color = color;
        return "Color " + color + " asignado a " + j.nombre;
    }

    public String marcarListo(UUID jugadorId) {
        Jugador j = buscarJugador(jugadorId);
        if (j == null) {
            return "Jugador no encontrado";
        }
        if (j.color == null) {
            return "Elige un color antes de marcar listo";
        }
        j.listo = true;
        return j.nombre + " está listo";
    }

    public String cancelarListo(UUID jugadorId) {
        Jugador j = buscarJugador(jugadorId);
        if (j == null) {
            return "Jugador no encontrado";
        }
        j.listo = false;
        return j.nombre + " canceló listo";
    }

    public boolean todosListosMinimos() {
        Sala s = sala();
        long listos = s.jugadores.stream().filter(p -> p.listo && p.color != null).count();
        return s.jugadores.size() >= MIN_JUGADORES && listos == s.jugadores.size();
    }

    public String iniciarSiTodosListos() {
        if (!todosListosMinimos()) {
            return "No todos están listos o no hay suficientes jugadores";
        }
        Sala s = sala();
        s.estado = EstadoSala.INICIANDO;
        return "Iniciando en " + TIEMPO_ESPERA_INICIO + " segundos...";
    }

    public String forzarIniciar() {
        Sala s = sala();
        if (s.jugadores.size() < MIN_JUGADORES) {
            return "No hay suficientes jugadores para iniciar";
        }
        s.estado = EstadoSala.INICIANDO;
        return "Forzando inicio...";
    }

    public void comenzarJuego() {
        Sala s = sala();
        s.estado = EstadoSala.JUGANDO;
        s.indiceTurno = 0;
        ultimoValorTirado.clear();
        tieneTurnoExtra.clear();
        s.ganador = null;
    }

    public Jugador buscarJugador(UUID id) {
        return sala().jugadores.stream().filter(j -> j.id.equals(id)).findFirst().orElse(null);
    }

    public Jugador jugadorActual() {
        Sala s = sala();
        if (s.jugadores.isEmpty()) {
            return null;
        }
        return s.jugadores.get(s.indiceTurno);
    }

    public int tirarDado(UUID jugadorId) {
        Sala s = sala();
        Jugador actual = jugadorActual();
        if (actual == null || !actual.id.equals(jugadorId)) {
            return 0;
        }
        int valor = dado.tirar();
        ultimoValorTirado.put(jugadorId, valor);
        if (valor == 6) {
            tieneTurnoExtra.put(jugadorId, true);
        } else {
            tieneTurnoExtra.put(jugadorId, false);
        }
        return valor;
    }

    public String moverFicha(UUID jugadorId, int indiceFicha) {
        Sala s = sala();
        Jugador actual = jugadorActual();
        if (actual == null || !actual.id.equals(jugadorId)) {
            return "No es tu turno";
        }
        Integer valor = ultimoValorTirado.get(jugadorId);
        if (valor == null || valor == 0) {
            return "Aún no has tirado el dado";
        }
        List<Ficha> mis = s.fichasPorJugador.get(jugadorId);
        if (mis == null || indiceFicha < 0 || indiceFicha >= mis.size()) {
            return "Índice de ficha inválido";
        }
        Ficha f = mis.get(indiceFicha);

        if (f.estado == EstadoFicha.BASE) {
            if (valor != 5) {
                ultimoValorTirado.remove(jugadorId);
                boolean extra = tieneTurnoExtra.getOrDefault(jugadorId, false);
                if (!extra) {
                    avanzarTurno();
                }
                tieneTurnoExtra.put(jugadorId, false);
                return "Necesitas un 5 para salir de base. Turno pasado.";
            } else {
                int posInicio = inicioPorColor.get(actual.color);
                f.posicion = posInicio;
                f.estado = EstadoFicha.EN_TABLERO;
                aplicarCapturaYBloqueoAlColocar(f);
                boolean extra = tieneTurnoExtra.getOrDefault(jugadorId, false);
                ultimoValorTirado.remove(jugadorId);
                if (!extra) {
                    avanzarTurno();
                } else {
                    tieneTurnoExtra.put(jugadorId, false);
                }
                return "Ficha sacada a tablero en casilla " + f.posicion;
            }
        }

        if (f.estado == EstadoFicha.EN_TABLERO) {
            int destino = f.posicion + valor;
            int posMeta = inicioPorColor.get(actual.color) + TAM_TABLERO;
            if (destino >= posMeta) {
                f.posicion = posMeta;
                f.estado = EstadoFicha.CASA;
                ultimoValorTirado.remove(jugadorId);
                boolean extra = tieneTurnoExtra.getOrDefault(jugadorId, false);
                if (!extra) {
                    avanzarTurno();
                } else {
                    tieneTurnoExtra.put(jugadorId, false);
                }
                comprobarVictoria(actual);
                return "Ficha llegó a CASA. Estado actualizado.";
            } else {
                // comprobar bloqueo en el camino
                for (int paso = 1; paso <= valor; paso++) {
                    int inter = f.posicion + paso;
                    if (inter < posMeta) {
                        int mod = inter % TAM_TABLERO;
                        if (posicionBloqueadaPorRival(mod, actual.id)) {
                            ultimoValorTirado.remove(jugadorId);
                            boolean extra = tieneTurnoExtra.getOrDefault(jugadorId, false);
                            if (!extra) {
                                avanzarTurno();
                            } else {
                                tieneTurnoExtra.put(jugadorId, false);
                            }
                            return "Movimiento bloqueado por bloqueo rival en casilla " + mod + ". Turno terminado.";
                        }
                    }
                }
                f.posicion = destino;
                aplicarCapturaYBloqueoAlColocar(f);
                ultimoValorTirado.remove(jugadorId);
                boolean extra = tieneTurnoExtra.getOrDefault(jugadorId, false);
                if (!extra) {
                    avanzarTurno();
                } else {
                    tieneTurnoExtra.put(jugadorId, false);
                }
                comprobarVictoria(actual);
                return "Ficha movida a posición " + f.posicion;
            }
        }

        return "Movimiento no permitido";
    }

    private void aplicarCapturaYBloqueoAlColocar(Ficha fichaColocada) {
        Sala s = sala();
        if (fichaColocada.estado != EstadoFicha.EN_TABLERO) {
            return;
        }
        int modulo = fichaColocada.posicion % TAM_TABLERO;
        UUID propietario = fichaColocada.dueñoId;

        Map<UUID, List<Ficha>> porCasilla = new HashMap<>();
        for (Map.Entry<UUID, List<Ficha>> kv : s.fichasPorJugador.entrySet()) {
            List<Ficha> enEsta = kv.getValue().stream()
                    .filter(f -> f.estado == EstadoFicha.EN_TABLERO && f.posicion % TAM_TABLERO == modulo)
                    .collect(Collectors.toList());
            if (!enEsta.isEmpty()) {
                porCasilla.put(kv.getKey(), enEsta);
            }
        }

        for (Map.Entry<UUID, List<Ficha>> kv : porCasilla.entrySet()) {
            UUID otro = kv.getKey();
            if (otro.equals(propietario)) {
                continue;
            }
            List<Ficha> fichasOponente = kv.getValue();
            if (fichasOponente.size() >= 2) {
                continue;
            } else {
                for (Ficha f : fichasOponente) {
                    f.posicion = -1;
                    f.estado = EstadoFicha.BASE;
                }
            }
        }

        List<Ficha> mias = s.fichasPorJugador.get(propietario).stream()
                .filter(f -> f.estado == EstadoFicha.EN_TABLERO && f.posicion % TAM_TABLERO == modulo)
                .collect(Collectors.toList());
        // si mias.size()>=2 -> bloqueo implícito
    }

    private boolean posicionBloqueadaPorRival(int modulo, UUID miId) {
        Sala s = sala();
        for (Map.Entry<UUID, List<Ficha>> kv : s.fichasPorJugador.entrySet()) {
            if (kv.getKey().equals(miId)) {
                continue;
            }
            long cnt = kv.getValue().stream()
                    .filter(f -> f.estado == EstadoFicha.EN_TABLERO && f.posicion % TAM_TABLERO == modulo)
                    .count();
            if (cnt >= 2) {
                return true;
            }
        }
        return false;
    }

    private void avanzarTurno() {
        Sala s = sala();
        if (s.jugadores.isEmpty()) {
            return;
        }
        s.indiceTurno = (s.indiceTurno + 1) % s.jugadores.size();
    }

    private void comprobarVictoria(Jugador jugador) {
        Sala s = sala();
        List<Ficha> mis = s.fichasPorJugador.get(jugador.id);
        boolean todosEnCasa = mis.stream().allMatch(f -> f.estado == EstadoFicha.CASA);
        if (todosEnCasa) {
            s.estado = EstadoSala.FINALIZADA;
            s.ganador = jugador.id;
        }
    }

    public String volcarEstado() {
        Sala s = sala();
        StringBuilder sb = new StringBuilder();
        sb.append("=== Estado de la Sala ===\n");
        sb.append("Estado: ").append(s.estado).append("\n");
        sb.append("Tiempo por turno: ").append(s.tiempoPorTurno).append("s\n");
        sb.append("Jugadores (" + s.jugadores.size() + "):\n");
        for (Jugador j : s.jugadores) {
            sb.append(" - ").append(j.toString()).append("\n");
            List<Ficha> ps = s.fichasPorJugador.get(j.id);
            if (ps != null) {
                for (int i = 0; i < ps.size(); i++) {
                    sb.append("     [" + i + "] " + ps.get(i).toString() + "\n");
                }
            }
        }
        if (s.estado == EstadoSala.JUGANDO) {
            Jugador act = jugadorActual();
            sb.append("Turno actual: ").append(act == null ? "n/a" : act.nombre).append("\n");
        }
        if (s.estado == EstadoSala.FINALIZADA) {
            Jugador g = buscarJugador(s.ganador);
            sb.append("GANADOR: ").append(g == null ? "n/a" : g.nombre).append("\n");
        }
        sb.append("=========================\n");
        return sb.toString();
    }

    public void setTiempoPorTurno(int segundos) {
        sala().tiempoPorTurno = segundos;
    }

}
