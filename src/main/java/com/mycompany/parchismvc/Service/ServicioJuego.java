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
    public static final int TAM_TABLERO = 52;
    /**
     * Número de fichas por jugador
     */
    public static final int FICHAS_POR_JUGADOR = 4;
    /**
     * Tiempo de espera (en segundos) para iniciar el juego una vez todos están
     * listos
     */
    public static final int TIEMPO_ESPERA_INICIO = 10;
    /**
     * Número minimo de jugadores para iniciar el juego
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
            ColorJugador.ROJO, 0,
            ColorJugador.AZUL, 13,
            ColorJugador.VERDE, 26,
            ColorJugador.AMARILLO, 39);

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
     * Comprueba si todos los jugadores están listos y hay al menos el minimo
     * 
     * @return true si todos están listos y hay al menos el mínimo, false en caso
     *         contrario
     */
    public boolean todosListosMinimos() {
        Sala s = sala();
        long listos = s.jugadores.stream().filter(p -> p.listo && p.color != null).count();
        return s.jugadores.size() >= MIN_JUGADORES && listos == s.jugadores.size();
    }

    /**
     * Inicia el juego si todos están listos y hay al menos el mínimo
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
        Sala s = sala();
        if (s.estado != EstadoSala.JUGANDO) {
            return 0;
        }
        if (actual == null || !actual.id.equals(jugadorId)) {
            return 0;
        }
        // Evitar segunda tirada si ya existe un valor pendiente
        if (ultimoValorTirado.containsKey(jugadorId)) {
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
     * @return Mensaje de resultado de la acción
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
                return "Ficha llego a CASA. Estado actualizado.";
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
     * Comprueba si una posición del tablero esta bloqueada por un rival (2 o más
     * fichas)
     * 
     * @param modulo Posicion en el tablero (0-51)
     * @param miId   ID del jugador que verifica
     * @return true si la posicion está bloqueada por un rival, false en caso
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
     * Limpia el estado de tirada/turno extra del jugador indicado.
     */
    private void limpiarEstadoTirada(UUID jugadorId) {
        ultimoValorTirado.remove(jugadorId);
        tieneTurnoExtra.remove(jugadorId);
    }

    /**
     * Pasa el turno del jugador actual por agotamiento de tiempo.
     * Limpia cualquier tirada pendiente y turno extra, y avanza el turno.
     */
    public void pasarTurnoPorTiempo() {
        Jugador actual = jugadorActual();
        if (actual == null) {
            return;
        }
        limpiarEstadoTirada(actual.id);
        avanzarTurno();
    }

    /**
     * Permite al jugador actual pasar turno voluntariamente.
     * Si no es su turno, no hace nada.
     * @param jugadorId Jugador que solicita pasar turno.
     */
    public void pasarTurno(UUID jugadorId) {
        Jugador actual = jugadorActual();
        if (actual == null || !actual.id.equals(jugadorId)) {
            return;
        }
        limpiarEstadoTirada(jugadorId);
        avanzarTurno();
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

    /**
     * Obtiene el tiempo por turno en segundos
     * 
     * @param segundos El tiempo en segundos
     */
    public void setTiempoPorTurno(int segundos) {
        sala().tiempoPorTurno = segundos;
    }

    /**
     * Devuelve los indices de fichas del jugador actual que pueden moverse con
     * el valor de dado pendiente. Si no es su turno o no hay valor pendiente,
     * devuelve lista vacia.
     * @param jugadorId Jugador a evaluar
     * @return Lista de indices de fichas movibles
     */
    public List<Integer> fichasMovibles(UUID jugadorId) {
        List<Integer> movibles = new ArrayList<>();
        Sala s = sala();
        Jugador actual = jugadorActual();
        if (s.estado != EstadoSala.JUGANDO || actual == null || !actual.id.equals(jugadorId)) {
            return movibles;
        }
        Integer valor = ultimoValorTirado.get(jugadorId);
        if (valor == null || valor == 0) {
            return movibles;
        }
        List<Ficha> mis = s.fichasPorJugador.get(jugadorId);
        if (mis == null) {
            return movibles;
        }
        for (int i = 0; i < mis.size(); i++) {
            Ficha f = mis.get(i);
            if (f.estado == EstadoFicha.CASA) {
                continue;
            }
            if (f.estado == EstadoFicha.BASE) {
                if (valor == 5) {
                    movibles.add(i);
                }
                continue;
            }
            if (f.estado == EstadoFicha.EN_TABLERO) {
                int destino = f.posicion + valor;
                int posMeta = inicioPorColor.get(actual.color) + TAM_TABLERO;
                if (destino >= posMeta) {
                    movibles.add(i);
                } else {
                    boolean bloqueado = false;
                    for (int paso = 1; paso <= valor; paso++) {
                        int inter = f.posicion + paso;
                        if (inter < posMeta) {
                            int mod = inter % TAM_TABLERO;
                            if (posicionBloqueadaPorRival(mod, actual.id)) {
                                bloqueado = true;
                                break;
                            }
                        }
                    }
                    if (!bloqueado) {
                        movibles.add(i);
                    }
                }
            }
        }
        return movibles;
    }

}
