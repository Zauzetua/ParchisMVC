package com.mycompany.parchismvc.Service;

import com.mycompany.parchismvc.Model.ColorJugador;
import com.mycompany.parchismvc.Model.EstadoFicha;
import com.mycompany.parchismvc.Model.EstadoSala;
import com.mycompany.parchismvc.Model.Ficha;
import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.Sala;
import com.mycompany.parchismvc.Repo.IRepositorioSala;
import com.mycompany.parchismvc.Repo.RepositorioSalaMemoria;
import com.mycompany.parchismvc.Util.DadoAleatorio;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase ServicioJuego
 *
 * @author jesus
 */
public class ServicioJuegoTest {

    private ServicioJuego servicio;
    private IRepositorioSala repositorio;
    private DadoAleatorio dado;

    /**
     * Configuracion inicial antes de cada prueba
     */
    @Before
    public void setUp() {
        repositorio = new RepositorioSalaMemoria();
        dado = new DadoAleatorio();
        servicio = new ServicioJuego(repositorio, dado);
    }

    /**
     * Prueba que la sala se obtiene correctamente
     */
    @Test
    public void testSala() {
        Sala sala = servicio.sala();
        assertNotNull("La sala no debe ser nula", sala);
        assertEquals("El estado inicial debe ser ESPERANDO", EstadoSala.ESPERANDO, sala.estado);
    }

    /**
     * Prueba el registro de un jugador
     */
    @Test
    public void testRegistrarJugador() {
        String nombre = "Jugador1";
        String avatar = "avatar1.png";

        Jugador jugador = servicio.registrarJugador(nombre, avatar);

        assertNotNull("El jugador no debe ser nulo", jugador);
        assertEquals("El nombre debe coincidir", nombre, jugador.nombre);
        assertEquals("El avatar debe coincidir", avatar, jugador.avatar);
        assertNotNull("El ID del jugador no debe ser nulo", jugador.id);

        // Verificar que el jugador se agrego a la sala
        Sala sala = servicio.sala();
        assertEquals("Debe haber un jugador en la sala", 1, sala.jugadores.size());
        assertTrue("La sala debe contener al jugador", sala.jugadores.contains(jugador));

        // Verificar que se crearon las fichas
        List<Ficha> fichas = sala.fichasPorJugador.get(jugador.id);
        assertNotNull("Las fichas no deben ser nulas", fichas);
        assertEquals("Debe haber 4 fichas por jugador", ServicioJuego.FICHAS_POR_JUGADOR, fichas.size());

        // Verificar que todas las fichas estan en BASE
        for (Ficha ficha : fichas) {
            assertEquals("La ficha debe estar en BASE", EstadoFicha.BASE, ficha.estado);
            assertEquals("La posicion debe ser -1", -1, ficha.posicion);
        }
    }

    /**
     * Prueba el registro de multiples jugadores
     */
    @Test
    public void testRegistrarMultiplesJugadores() {
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        Jugador j3 = servicio.registrarJugador("Jugador3", "avatar3.png");

        Sala sala = servicio.sala();
        assertEquals("Debe haber 3 jugadores", 3, sala.jugadores.size());

        // Verificar que los IDs son unicos
        assertNotEquals("Los IDs deben ser diferentes", j1.id, j2.id);
        assertNotEquals("Los IDs deben ser diferentes", j1.id, j3.id);
        assertNotEquals("Los IDs deben ser diferentes", j2.id, j3.id);
    }

    /**
     * Prueba elegir un color disponible
     */
    @Test
    public void testElegirColorDisponible() {
        Jugador jugador = servicio.registrarJugador("Jugador1", "avatar1.png");

        String resultado = servicio.elegirColor(jugador.id, ColorJugador.ROJO);

        assertTrue("Debe contener confirmacion", resultado.contains("asignado"));
        assertEquals("El color debe ser ROJO", ColorJugador.ROJO, jugador.color);
    }

    /**
     * Prueba elegir un color ya ocupado
     */
    @Test
    public void testElegirColorOcupado() {
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");

        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        String resultado = servicio.elegirColor(j2.id, ColorJugador.ROJO);

        assertEquals("Debe indicar que el color esta ocupado", "Color ya elegido por otro jugador", resultado);
        assertNull("El jugador 2 no debe tener color", j2.color);
    }

    /**
     * Prueba elegir color con jugador inexistente
     */
    @Test
    public void testElegirColorJugadorInexistente() {
        UUID idInexistente = UUID.randomUUID();

        String resultado = servicio.elegirColor(idInexistente, ColorJugador.AZUL);

        assertEquals("Debe indicar que el jugador no existe", "Jugador no encontrado", resultado);
    }

    /**
     * Prueba marcar un jugador como listo
     */
    @Test
    public void testMarcarListo() {
        Jugador jugador = servicio.registrarJugador("Jugador1", "avatar1.png");
        servicio.elegirColor(jugador.id, ColorJugador.VERDE);

        String resultado = servicio.marcarListo(jugador.id);

        assertTrue("Debe confirmar que esta listo", resultado.contains("listo"));
        assertTrue("El jugador debe estar listo", jugador.listo);
    }

    /**
     * Prueba marcar listo sin elegir color
     */
    @Test
    public void testMarcarListoSinColor() {
        Jugador jugador = servicio.registrarJugador("Jugador1", "avatar1.png");

        String resultado = servicio.marcarListo(jugador.id);
        String expResult = "Elige un color antes de marcar listo";
        assertEquals(expResult, resultado);
    }

    /**
     * Test of cancelarListo method, of class ServicioJuego.
     */
    @Test
    public void testCancelarListo() {
        System.out.println("cancelarListo");
        Jugador jugador = servicio.registrarJugador("Jugador1", "avatar1.png");
        servicio.elegirColor(jugador.id, ColorJugador.ROJO);
        servicio.marcarListo(jugador.id);
        String expResult = "cancelo listo";
        String result = servicio.cancelarListo(jugador.id);
        assertTrue(result.contains(expResult));
    }

    /**
     * Test of todosListosMinimos method, of class ServicioJuego.
     */
    @Test
    public void testTodosListosMinimos() {
        System.out.println("todosListosMinimos");
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        Jugador j3 = servicio.registrarJugador("Jugador3", "avatar3.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.elegirColor(j3.id, ColorJugador.VERDE);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.marcarListo(j3.id);
        boolean expResult = true;
        boolean result = servicio.todosListosMinimos();
        assertEquals(expResult, result);
    }

    /**
     * Test of iniciarSiTodosListos method, of class ServicioJuego.
     */
    @Test
    public void testIniciarSiTodosListos() {
        System.out.println("iniciarSiTodosListos");
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        assertEquals(EstadoSala.ESPERANDO, servicio.sala().estado);
        servicio.iniciarSiTodosListos();
        assertEquals(EstadoSala.INICIANDO, servicio.sala().estado);

    }

    /**
     * Test of forzarIniciar method, of class ServicioJuego.
     */
    @Test
    public void testForzarIniciar() {
        System.out.println("forzarIniciar");

        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        assertEquals(EstadoSala.ESPERANDO, servicio.sala().estado);
        servicio.forzarIniciar();
        assertEquals(EstadoSala.INICIANDO, servicio.sala().estado);
    }

    /**
     * Test of comenzarJuego method, of class ServicioJuego.
     */
    @Test
    public void testComenzarJuego() {
        System.out.println("comenzarJuego");
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.iniciarSiTodosListos();
        servicio.comenzarJuego();
        assertEquals(EstadoSala.JUGANDO, servicio.sala().estado);

    }

    /**
     * Test of buscarJugador method, of class ServicioJuego.
     */
    @Test
    public void testBuscarJugador() {
        System.out.println("buscarJugador");
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        UUID jugadorId = j1.id;
        Jugador expResult = j1;
        Jugador result = servicio.buscarJugador(jugadorId);
        assertEquals(expResult, result);
    }

    /**
     * Test of jugadorActual method, of class ServicioJuego.
     */
    @Test
    public void testJugadorActual() {
        System.out.println("jugadorActual");
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.iniciarSiTodosListos();
        Jugador expResult = j1;
        Jugador result = servicio.jugadorActual();
        assertEquals(expResult, result);
    }

    /**
     * Test of tirarDado method, of class ServicioJuego.
     */
    @Test
    public void testTirarDado() {
        System.out.println("tirarDado");
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.iniciarSiTodosListos();
        int expResult = 0;
        int result = servicio.tirarDado(j1.id);
        assertEquals(expResult, result);
    }

    /**
     * Test of moverFicha method, of class ServicioJuego.
     */
    @Test
    public void testMoverFicha() {
        System.out.println("moverFicha");
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.comenzarJuego();
        int numeroFicha = 0;
        //Se necesita un 5 para sacar ficha de base
        servicio.ultimoValorTirado.put(j1.id, 5); 
        String expResult = "Ficha sacada a tablero en casilla";
        String result = servicio.moverFicha(j1.id, numeroFicha);
        assertTrue(result.contains(expResult));
    }

    /**
     * Test of pasarTurnoPorTiempo method, of class ServicioJuego.
     */
    @Test
    public void testPasarTurnoPorTiempo() {
        System.out.println("pasarTurnoPorTiempo");
        // Configurar el juego con dos jugadores
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.comenzarJuego();
        
        // Verificar jugador actual antes de pasar turno
        Jugador jugadorInicial = servicio.jugadorActual();
        
        // Pasar turno por tiempo
        servicio.pasarTurnoPorTiempo();
        
        // Verificar que el turno pasó al siguiente jugador
        Jugador siguienteJugador = servicio.jugadorActual();
        assertNotEquals("El jugador actual debe ser diferente después de pasar turno", jugadorInicial, siguienteJugador);
    }

    /**
     * Test of pasarTurno method, of class ServicioJuego.
     */
    @Test
    public void testPasarTurno() {
        System.out.println("pasarTurno");
        // Configurar el juego con dos jugadores
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.comenzarJuego();
        
        // Verificar jugador actual antes de pasar turno
        Jugador jugadorInicial = servicio.jugadorActual();
        
        // Pasar turno manualmente
        servicio.pasarTurno(j1.id);
        
        // Verificar que el turno pasó al siguiente jugador
        Jugador siguienteJugador = servicio.jugadorActual();
        assertNotEquals("El jugador actual debe ser diferente después de pasar turno", jugadorInicial, siguienteJugador);
        assertEquals("El siguiente jugador debe ser j2", j2, siguienteJugador);
    }

    /**
     * Test of volcarEstado method, of class ServicioJuego.
     */
    @Test
    public void testVolcarEstado() {
        System.out.println("volcarEstado");
        // Configurar el juego con dos jugadores
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.comenzarJuego();
        
        // Obtener el estado del juego
        String estado = servicio.volcarEstado();
        
        // Verificar que el estado contiene información relevante
        assertNotNull("El estado no debe ser nulo", estado);
        assertTrue("El estado debe contener información de los jugadores", estado.contains("Jugador1") && estado.contains("Jugador2"));
        assertTrue("El estado debe contener información de los colores", estado.contains("ROJO") && estado.contains("AZUL"));
        assertTrue("El estado debe contener información del estado de la sala", estado.contains("JUGANDO"));
    }

    /**
     * Test of setTiempoPorTurno method, of class ServicioJuego.
     */
    @Test
    public void testSetTiempoPorTurno() {
        System.out.println("setTiempoPorTurno");
        // Configurar un tiempo por turno válido
        int segundos = 30;
        servicio.setTiempoPorTurno(segundos);
        
        // Verificar que el tiempo se estableció correctamente iniciando una partida
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.comenzarJuego();
        
        // El tiempo por turno debería estar configurado en la sala
        assertEquals("El tiempo por turno debe ser 30 segundos", 30, servicio.sala().tiempoPorTurno);
    }

    /**
     * Test of fichasMovibles method, of class ServicioJuego.
     */
    @Test
    public void testFichasMovibles() {
        System.out.println("fichasMovibles");
        // Configurar el juego con dos jugadores
        Jugador j1 = servicio.registrarJugador("Jugador1", "avatar1.png");
        Jugador j2 = servicio.registrarJugador("Jugador2", "avatar2.png");
        servicio.elegirColor(j1.id, ColorJugador.ROJO);
        servicio.elegirColor(j2.id, ColorJugador.AZUL);
        servicio.marcarListo(j1.id);
        servicio.marcarListo(j2.id);
        servicio.comenzarJuego();
        
        // Configurar un valor de dado que permita sacar fichas (5)
        servicio.ultimoValorTirado.put(j1.id, 5);
        
        // Obtener las fichas movibles
        List<Integer> fichasMovibles = servicio.fichasMovibles(j1.id);
        
        // Verificar que hay fichas movibles (deberían ser todas las fichas en BASE)
        assertNotNull("La lista de fichas movibles no debe ser nula", fichasMovibles);
        assertFalse("Debe haber fichas movibles", fichasMovibles.isEmpty());
        assertEquals("Deben haber 4 fichas movibles al inicio con un 5", 4, fichasMovibles.size());
    }

}
