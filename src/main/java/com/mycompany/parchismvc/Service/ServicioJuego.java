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
 * @author Equipo 1 Parchis
 */
public class ServicioJuego {

    /**
     * Constantes del juego
     */
    public static final int TAM_TABLERO = 68;
    /**
     * Numero de fichas por jugador
     */
    public static final int FICHAS_POR_JUGADOR = 4;
    /**
     * Tiempo de espera (en segundos) para iniciar el juego una vez todos estan
     * listos
     */
    public static final int TIEMPO_ESPERA_INICIO = 10;
    /**
     * Numero minimo de jugadores para iniciar el juego
     */
    public static final int MIN_JUGADORES = 2;

    private final IRepositorioSala repo;
    /**
     * Generador de numeros aleatorios para el dado
     */
    private final Dado dado;

    /**
     * Posicion inicial en el tablero para cada color. Es decir, la posicion en la
     * que empiezan las fichas al salir de la base.
     */
    private final Map<ColorJugador, Integer> inicioPorColor = Map.of(
            ColorJugador.ROJO, 39,
            ColorJugador.AZUL, 22,
            ColorJugador.VERDE, 56,
            ColorJugador.AMARILLO, 5);

    /**
     * Ultimo valor tirado por cada jugador en su turno (si ya ha tirado)
     */
    private final Map<UUID, Integer> ultimoValorTirado = new HashMap<>();
    /**
     * Indica si el jugador tiene un turno extra (por haber sacado un 6)
     */
    private final Map<UUID, Boolean> tieneTurnoExtra = new HashMap<>();

    /**
     * Constructor del servicio de juego
     * 
     * @param repo Repositorio de la sala
     * @param dado Generador de numeros aleatorios para el dado
     */
    public ServicioJuego(IRepositorioSala repo, Dado dado) {
        this.repo = repo;
        this.dado = dado;
    }

    /**
     * Obtiene la sala de juego (si no existe, la crea)
     * 
     * @return La sala de juego
     */
    public Sala sala() {
        return repo.obtenerSala();
    }

    /**
     * Registra un nuevo jugador en la sala
     * 
     * @param nombre Nombre del jugador
     * @param avatar Avatar del jugador
     * @return El jugador registrado
     */
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

    /**
     * Elige un color para el jugador
     * 
     * @param jugadorId ID del jugador
     * @param color     Color elegido
     * @return Mensaje de confirmacion o error
     */
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

    /**
     * Marca al jugador como listo para iniciar el juego
     * 
     * @param jugadorId ID del jugador
     * @return El mensaje de confirmacion o error
     */
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

    /**
     * Cancela el estado de listo del jugador
     * 
     * @param jugadorId ID del jugador
     * @return Mensaje de confirmacion o error
     */
    public String cancelarListo(UUID jugadorId) {
        Jugador j = buscarJugador(jugadorId);
        if (j == null) {
            return "Jugador no encontrado";
        }
        j.listo = false;
        return j.nombre + " canceló listo";
    }

    /**
     * Comprueba si todos los jugadores estan listos y hay al menos el minimo
     * 
     * @return true si todos estan listos y hay al menos el minimo, false en caso
     *         contrario
     */
    public boolean todosListosMinimos() {
        Sala s = sala();
        long listos = s.jugadores.stream().filter(p -> p.listo && p.color != null).count();
        return s.jugadores.size() >= MIN_JUGADORES && listos == s.jugadores.size();
    }

    /**
     * Inicia el juego si todos estan listos y hay al menos el minimo
     * 
     * @return El mensaje de confirmacion o error
     */
    public String iniciarSiTodosListos() {
        if (!todosListosMinimos()) {
            return "No todos están listos o no hay suficientes jugadores";
        }
        Sala s = sala();
        s.estado = EstadoSala.INICIANDO;
        return "Iniciando en " + TIEMPO_ESPERA_INICIO + " segundos...";
    }

    /**
     * Forzar el inicio del juego (sin comprobar si todos estan listos o hay el
     * minimo)
     * 
     * @return El mensaje de confirmacion o error
     */
    public String forzarIniciar() {
        Sala s = sala();
        if (s.jugadores.size() < MIN_JUGADORES) {
            return "No hay suficientes jugadores para iniciar";
        }
        s.estado = EstadoSala.INICIANDO;
        return "Forzando inicio...";
    }

    /**
     * Comienza el juego (debe llamarse tras un retardo desde iniciarSiTodosListos o
     * forzarIniciar)
     */
    public void comenzarJuego() {
        Sala s = sala();
        s.estado = EstadoSala.JUGANDO;
        s.indiceTurno = 0;
        ultimoValorTirado.clear();
        tieneTurnoExtra.clear();
        s.ganador = null;
        
        for (Jugador j : sala().jugadores) j.listo = false;
    }

    /**
     * Busca un jugador por su ID
     * 
     * @param id ID del jugador
     * @return El jugador encontrado o null si no existe
     */
    public Jugador buscarJugador(UUID id) {
        return sala().jugadores.stream().filter(j -> j.id.equals(id)).findFirst().orElse(null);
    }

    /**
     * Obtiene el jugador cuyo turno es actualmente
     * 
     * @return El jugador actual o null si no hay jugadores
     */
    public Jugador jugadorActual() {
        Sala s = sala();
        if (s.jugadores.isEmpty()) {
            return null;
        }
        return s.jugadores.get(s.indiceTurno);
    }

    /**
     * Tira el dado para el jugador actual (si es su turno)
     * 
     * @param jugadorId ID del jugador que tira
     * @return El valor del dado o 0 si no es su turno
     */
    public int tirarDado(UUID jugadorId) {
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

    /**
     * Mueve una ficha del jugador actual (si es su turno y ha tirado el dado)
     * 
     * @param jugadorId   ID del jugador que mueve la ficha
     * @param indiceFicha Indice de la ficha a mover
     * @return Mensaje de resultado de la accion
     */
    public String moverFicha(UUID jugadorId, int indiceFicha) {
        Sala s = sala();
        Jugador actual = jugadorActual();
        if (actual == null || !actual.id.equals(jugadorId)) {
            return "No es tu turno";
        }
        Integer valor = ultimoValorTirado.get(jugadorId);
        if (valor == null || valor == 0) {
            return "Aun no has tirado el dado";
        }
        List<Ficha> mis = s.fichasPorJugador.get(jugadorId);
        if (mis == null || indiceFicha < 0 || indiceFicha >= mis.size()) {
            return "Indice de ficha invalido";
        }
        Ficha f = mis.get(indiceFicha);

        if (f.estado == EstadoFicha.BASE) {
            if (valor != 5) {
                pasarTurno();
                return "Necesitas un 5 para salir de base. Turno pasado.";
            } else {
                int posInicio = inicioPorColor.get(actual.color);
                f.posicion = posInicio;
                f.estado = EstadoFicha.EN_TABLERO;
                aplicarCapturaYBloqueoAlColocar(f);
                boolean extra = tieneTurnoExtra.getOrDefault(jugadorId, false);
                pasarTurno();
                return "Ficha sacada a tablero en casilla " + f.posicion;
            }
        }

        if (f.estado == EstadoFicha.EN_TABLERO) {
            int destino = (f.posicion - 1 + valor) % TAM_TABLERO + 1;
            int casillaEntradaMeta = (inicioPorColor.get(actual.color) - 2 + TAM_TABLERO) % TAM_TABLERO + 1;

            // Comprobar si la ficha pasa por su casilla de entrada a la meta
            boolean pasaPorMeta = false;
            for (int i = 1; i <= valor; i++) {
                if (((f.posicion - 1 + i) % TAM_TABLERO + 1) == casillaEntradaMeta) {
                    pasaPorMeta = true;
                    break;
                }
            }

            if (pasaPorMeta) {
                // La ficha ha completado una vuelta y ha llegado a su fin.
                // En una implementación completa, aquí se calcularía la entrada al pasillo final.
                // Por ahora, la marcaremos como que ha llegado a casa.
                f.estado = EstadoFicha.CASA;
                f.posicion = -1; // O una posición especial para la casa.
                pasarTurno();
                comprobarVictoria(actual);
                return "Ficha llego a CASA. Estado actualizado.";
            } else {
                // comprobar bloqueo en el camino
                for (int paso = 1; paso <= valor; paso++) {
                    int inter = f.posicion + paso;
                    // La posición en el tablero se calcula con el módulo
                    int mod = (inter - 1) % TAM_TABLERO + 1;
                    if (posicionBloqueadaPorRival(mod, actual.id)) {
                        pasarTurno();
                        return "Movimiento bloqueado por bloqueo rival en casilla " + mod + ". Turno terminado.";
                    }
                }
                f.posicion = destino;
                aplicarCapturaYBloqueoAlColocar(f);
                pasarTurno();
                comprobarVictoria(actual);
                return "Ficha movida a posición " + f.posicion;
            }
        }

        return "Movimiento no permitido";
    }

    private void pasarTurno() {
        Jugador actual = jugadorActual();
        if (actual == null) return;
        ultimoValorTirado.remove(actual.id);
        boolean extra = tieneTurnoExtra.getOrDefault(actual.id, false);
        if (!extra) {
            avanzarTurno();
        }
        tieneTurnoExtra.put(actual.id, false); // El turno extra se consume o se pierde
    }

    /**
     * Aplica las reglas de captura y bloqueo al colocar una ficha en el tablero
     * 
     * @param fichaColocada
     */
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
        if (mias.size() >= 2) {
            // bloqueo
        } else {
            // no bloqueo
        }

    }

    /**
     * Comprueba si una posicion del tablero esta bloqueada por un rival (2 o mas
     * fichas)
     * 
     * @param modulo Posicion en el tablero (0-51)
     * @param miId   ID del jugador que verifica
     * @return true si la posicion esta bloqueada por un rival, false en caso
     *         contrario
     */
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

    /**
     * Avanza el turno al siguiente jugador
     */
    private void avanzarTurno() {
        Sala s = sala();
        if (s.jugadores.isEmpty()) {
            return;
        }
        s.indiceTurno = (s.indiceTurno + 1) % s.jugadores.size();
    }

    /**
     * Comprueba si el jugador ha ganado (todas sus fichas en casa) y actualiza el
     * estado de la sala
     * 
     * @param jugador El jugador a comprobar
     */
    private void comprobarVictoria(Jugador jugador) {
        Sala s = sala();
        List<Ficha> mis = s.fichasPorJugador.get(jugador.id);
        boolean todosEnCasa = mis.stream().allMatch(f -> f.estado == EstadoFicha.CASA);
        if (todosEnCasa) {
            s.estado = EstadoSala.FINALIZADA;
            s.ganador = jugador.id;
        }
    }

    /**
     * Vuelca el estado actual de la sala en un String para depuracion. Es decir,
     * muestra datos generales, jugadores, fichas, turno actual, ganador, etc.
     * 
     * @return El estado de la sala en formato String
     */
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
            if (ps != null) { // ps es la lista de fichas del jugador j
                for (int i = 0; i < ps.size(); i++) {
                    Ficha f = ps.get(i);
                    String pos = switch (f.estado) {
                        case BASE -> "BASE";
                        case CASA -> "CASA";
                        // Para EN_TABLERO, mostramos la posición numérica
                        default -> "pos=" + f.posicion;
                    };
                    sb.append("     [").append(i).append("] ").append(f.estado).append(" (").append(pos).append(")\n");
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
    
    
    public void pasarTurnoPorTiempo() {
    Jugador actual = jugadorActual();
    if (actual == null) return;
    // si habia un valor de dado pendiente, lo descartamos
    ultimoValorTirado.remove(actual.id);
    tieneTurnoExtra.put(actual.id, false);
    avanzarTurno(); // metodo privado de la misma clase
}

    /**
     * Obtiene el tiempo por turno en segundos
     * 
     * @param segundos El tiempo en segundos
     */
    public void setTiempoPorTurno(int segundos) {
        sala().tiempoPorTurno = segundos;
    }

    /**
     * Desconecta a un jugador de la sala, eliminándolo a él y a sus fichas.
     * Si el juego está en curso y era su turno, lo avanza.
     * @param jugadorId El ID del jugador a desconectar.
     * @return Un mensaje indicando el resultado.
     */
    public String desconectarJugador(UUID jugadorId) {
        Sala s = sala();
        Jugador jugadorADesconectar = buscarJugador(jugadorId);
        if (jugadorADesconectar == null) {
            return "Jugador no encontrado.";
        }

        boolean eraTurnoDelDesconectado = false;
        if (s.estado == EstadoSala.JUGANDO && jugadorActual() != null) {
            eraTurnoDelDesconectado = jugadorActual().id.equals(jugadorId);
        }

        // Eliminar al jugador y sus fichas
        s.jugadores.remove(jugadorADesconectar);
        s.fichasPorJugador.remove(jugadorId);

        if (s.estado == EstadoSala.JUGANDO) {
            // Si el índice de turno ahora está fuera de los límites, ajústalo.
            if (s.indiceTurno >= s.jugadores.size()) {
                s.indiceTurno = 0;
            } else if (eraTurnoDelDesconectado) {
                // Si era el turno del que se fue, no necesitamos incrementar el índice,
                // porque el siguiente jugador ahora ocupa el `indiceTurno` actual.
                // El estado se refrescará y mostrará al nuevo jugador en turno.
            }
        }
        return "Jugador " + jugadorADesconectar.nombre + " se ha desconectado.";
    }
}
