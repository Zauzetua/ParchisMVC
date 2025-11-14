/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.View.UtilsFront;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author marco
 */
public class FondoBGTablero extends ImageBackgroundPanel {

    // Variable para mantener la referencia a la ficha actualmente seleccionada
    private JButton botonFichaSeleccionada = null;

    // Timer para la animación de la ficha
    private Timer animator;

    /**
     * Clase interna para dibujar un círculo negro que representa un hueco
     * vacío. Incluye una sombra para dar un efecto de profundidad.
     */
    private class IconoDeHueco implements Icon {

        private final int width;
        private final int height;

        public IconoDeHueco(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dibuja la sombra (un círculo ligeramente desplazado y de color oscuro semitransparente)
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillOval(x + 2, y + 2, width - 4, height - 4);

            // Dibuja el círculo negro principal
            g2d.setColor(Color.BLACK);
            g2d.fillOval(x, y, width - 4, height - 4);

            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    // Mapa para guardar la referencia a cada botón por su número de casilla
    private final Map<Integer, JButton> botonesCasillas = new HashMap<>();
    // Generador de números aleatorios para la simulación
    private final Random random = new Random();
    // Mapa para rastrear la posición de cada ficha. Key: ID de la casa (101-116), Value: ID de la casilla actual.
    private final Map<Integer, Integer> mapaPosicionFichas = new HashMap<>();

    // Mapa que define la geometría del tablero. Asocia un ID de casilla a su Rectángulo (x, y, ancho, alto)
    private static final Map<Integer, Rectangle> mapaCoordenadas = new HashMap<>();

    // Bloque estático para inicializar las coordenadas de cada casilla
    static {
        // Aquí defines las coordenadas iniciales de tus botones.
        //                                   x,   y,  ancho, alt
        //Parte Amarilla
        mapaCoordenadas.put(1, new Rectangle(223, 327, 30, 14));
        mapaCoordenadas.put(2, new Rectangle(222, 310, 30, 14));
        mapaCoordenadas.put(3, new Rectangle(223, 293, 30, 14));
        mapaCoordenadas.put(4, new Rectangle(222, 278, 30, 14));
        mapaCoordenadas.put(5, new Rectangle(222, 259, 30, 14));
        mapaCoordenadas.put(6, new Rectangle(222, 243, 30, 14));
        mapaCoordenadas.put(7, new Rectangle(222, 227, 30, 14));
        mapaCoordenadas.put(8, new Rectangle(224, 211, 20, 14));
        mapaCoordenadas.put(60, new Rectangle(164, 211, 20, 14));
        mapaCoordenadas.put(61, new Rectangle(153, 227, 30, 14));
        mapaCoordenadas.put(62, new Rectangle(153, 243, 30, 14));
        mapaCoordenadas.put(63, new Rectangle(153, 259, 30, 14));
        mapaCoordenadas.put(64, new Rectangle(153, 276, 30, 14));
        mapaCoordenadas.put(65, new Rectangle(153, 293, 30, 14));
        mapaCoordenadas.put(66, new Rectangle(153, 309, 30, 14));
        mapaCoordenadas.put(67, new Rectangle(153, 326, 30, 14));
        mapaCoordenadas.put(68, new Rectangle(186, 326, 30, 14));

        //Parte Azul
        mapaCoordenadas.put(9, new Rectangle(238, 196, 14, 20));
        mapaCoordenadas.put(10, new Rectangle(254, 196, 14, 30));
        mapaCoordenadas.put(11, new Rectangle(270, 196, 14, 30));
        mapaCoordenadas.put(12, new Rectangle(287, 196, 14, 30));
        mapaCoordenadas.put(13, new Rectangle(303, 196, 14, 30));
        mapaCoordenadas.put(14, new Rectangle(319, 196, 14, 30));
        mapaCoordenadas.put(15, new Rectangle(335, 196, 14, 30));
        mapaCoordenadas.put(16, new Rectangle(354, 196, 14, 30));
        mapaCoordenadas.put(17, new Rectangle(355, 165, 14, 30));
        mapaCoordenadas.put(18, new Rectangle(355, 131, 14, 30));
        mapaCoordenadas.put(19, new Rectangle(339, 128, 14, 30));
        mapaCoordenadas.put(20, new Rectangle(321, 128, 14, 30));
        mapaCoordenadas.put(21, new Rectangle(305, 128, 14, 30));
        mapaCoordenadas.put(22, new Rectangle(289, 129, 14, 30));
        mapaCoordenadas.put(23, new Rectangle(271, 129, 14, 30));
        mapaCoordenadas.put(24, new Rectangle(253, 130, 14, 30));
        mapaCoordenadas.put(25, new Rectangle(237, 138, 14, 20));

        //Parte Roja
        mapaCoordenadas.put(26, new Rectangle(223, 126, 20, 14));
        mapaCoordenadas.put(27, new Rectangle(222, 110, 30, 14));
        mapaCoordenadas.put(28, new Rectangle(222, 96, 30, 14));
        mapaCoordenadas.put(29, new Rectangle(222, 79, 30, 14));
        mapaCoordenadas.put(30, new Rectangle(222, 63, 30, 14));
        mapaCoordenadas.put(31, new Rectangle(223, 47, 30, 14));
        mapaCoordenadas.put(32, new Rectangle(222, 31, 30, 14));
        mapaCoordenadas.put(33, new Rectangle(221, 14, 30, 14));
        mapaCoordenadas.put(34, new Rectangle(186, 13, 30, 14));
        mapaCoordenadas.put(35, new Rectangle(153, 12, 30, 14));
        mapaCoordenadas.put(36, new Rectangle(153, 28, 30, 14));
        mapaCoordenadas.put(37, new Rectangle(153, 47, 30, 14));
        mapaCoordenadas.put(38, new Rectangle(152, 63, 30, 14));
        mapaCoordenadas.put(39, new Rectangle(153, 78, 30, 14));
        mapaCoordenadas.put(40, new Rectangle(152, 95, 30, 14));
        mapaCoordenadas.put(41, new Rectangle(153, 110, 30, 14));
        mapaCoordenadas.put(42, new Rectangle(163, 126, 20, 14));

        //Parte Verde
        mapaCoordenadas.put(43, new Rectangle(153, 140, 14, 20));
        mapaCoordenadas.put(44, new Rectangle(137, 129, 14, 30));
        mapaCoordenadas.put(45, new Rectangle(121, 129, 14, 30));
        mapaCoordenadas.put(46, new Rectangle(104, 129, 14, 30));
        mapaCoordenadas.put(47, new Rectangle(88, 128, 14, 30));
        mapaCoordenadas.put(48, new Rectangle(69, 129, 14, 30));
        mapaCoordenadas.put(49, new Rectangle(53, 129, 14, 30));
        mapaCoordenadas.put(50, new Rectangle(36, 129, 14, 30));
        mapaCoordenadas.put(51, new Rectangle(35, 162, 14, 30));
        mapaCoordenadas.put(52, new Rectangle(36, 195, 14, 30));
        mapaCoordenadas.put(53, new Rectangle(50, 195, 14, 30));
        mapaCoordenadas.put(54, new Rectangle(68, 195, 14, 30));
        mapaCoordenadas.put(55, new Rectangle(86, 196, 14, 30));
        mapaCoordenadas.put(56, new Rectangle(103, 196, 14, 30));
        mapaCoordenadas.put(57, new Rectangle(120, 197, 14, 30));
        mapaCoordenadas.put(58, new Rectangle(136, 196, 14, 30));
        mapaCoordenadas.put(59, new Rectangle(153, 197, 14, 20));

        //Camino al centro parte amarilla
        mapaCoordenadas.put(69, new Rectangle(186, 309, 30, 14));
        mapaCoordenadas.put(70, new Rectangle(186, 292, 30, 14));
        mapaCoordenadas.put(71, new Rectangle(187, 275, 30, 14));
        mapaCoordenadas.put(72, new Rectangle(187, 259, 30, 14));
        mapaCoordenadas.put(73, new Rectangle(187, 242, 30, 14));
        mapaCoordenadas.put(74, new Rectangle(188, 227, 30, 14));
        mapaCoordenadas.put(75, new Rectangle(187, 211, 30, 14));
        mapaCoordenadas.put(76, new Rectangle(188, 191, 30, 14));

        //Camino al centro parte azul
        mapaCoordenadas.put(77, new Rectangle(339, 164, 14, 30));
        mapaCoordenadas.put(78, new Rectangle(321, 163, 14, 30));
        mapaCoordenadas.put(79, new Rectangle(304, 163, 14, 30));
        mapaCoordenadas.put(80, new Rectangle(286, 163, 14, 30));
        mapaCoordenadas.put(81, new Rectangle(270, 163, 14, 30));
        mapaCoordenadas.put(82, new Rectangle(254, 163, 14, 30));
        mapaCoordenadas.put(83, new Rectangle(237, 163, 14, 30));
        mapaCoordenadas.put(84, new Rectangle(217, 162, 14, 30));

        //Camino al centro parte roja
        mapaCoordenadas.put(85, new Rectangle(186, 29, 30, 14));
        mapaCoordenadas.put(86, new Rectangle(186, 45, 30, 14));
        mapaCoordenadas.put(87, new Rectangle(186, 62, 30, 14));
        mapaCoordenadas.put(88, new Rectangle(186, 78, 30, 14));
        mapaCoordenadas.put(89, new Rectangle(186, 94, 30, 14));
        mapaCoordenadas.put(90, new Rectangle(186, 110, 30, 14));
        mapaCoordenadas.put(91, new Rectangle(186, 126, 30, 14));
        mapaCoordenadas.put(92, new Rectangle(188, 146, 30, 14));

        //Camino al centro parte verde
        mapaCoordenadas.put(93, new Rectangle(51, 162, 14, 30));
        mapaCoordenadas.put(94, new Rectangle(67, 163, 14, 30));
        mapaCoordenadas.put(95, new Rectangle(84, 162, 14, 30));
        mapaCoordenadas.put(96, new Rectangle(102, 163, 14, 30));
        mapaCoordenadas.put(97, new Rectangle(119, 163, 14, 30));
        mapaCoordenadas.put(98, new Rectangle(135, 163, 14, 30));
        mapaCoordenadas.put(99, new Rectangle(154, 163, 14, 30));
        mapaCoordenadas.put(100, new Rectangle(173, 163, 14, 30));

        //Fichas Rojas
        int tamañoAlturaficha = 35;
        mapaCoordenadas.put(101, new Rectangle(105, 48, 30, tamañoAlturaficha));
        mapaCoordenadas.put(102, new Rectangle(73, 49, 30, tamañoAlturaficha));
        mapaCoordenadas.put(103, new Rectangle(73, 76, 30, tamañoAlturaficha));
        mapaCoordenadas.put(104, new Rectangle(105, 76, 30, tamañoAlturaficha));

        //Fichas Azules
        mapaCoordenadas.put(105, new Rectangle(275, 47, 30, tamañoAlturaficha));
        mapaCoordenadas.put(106, new Rectangle(307, 48, 30, tamañoAlturaficha));
        mapaCoordenadas.put(107, new Rectangle(274, 76, 30, tamañoAlturaficha));
        mapaCoordenadas.put(108, new Rectangle(306, 76, 30, tamañoAlturaficha));

        //Fichas Verdes
        mapaCoordenadas.put(109, new Rectangle(74, 245, 30, tamañoAlturaficha));
        mapaCoordenadas.put(110, new Rectangle(107, 245, 30, tamañoAlturaficha));
        mapaCoordenadas.put(111, new Rectangle(74, 277, 30, tamañoAlturaficha));
        mapaCoordenadas.put(112, new Rectangle(107, 277, 30, tamañoAlturaficha));

        //Fichas Amarillas
        mapaCoordenadas.put(113, new Rectangle(271, 247, 30, tamañoAlturaficha));
        mapaCoordenadas.put(114, new Rectangle(303, 245, 30, tamañoAlturaficha));
        mapaCoordenadas.put(115, new Rectangle(271, 275, 30, tamañoAlturaficha));
        mapaCoordenadas.put(116, new Rectangle(305, 275, 30, tamañoAlturaficha));
    }

    // Constructor
    public FondoBGTablero() {
        super("");
        loadBoardFromDisk();
        setLayout(null);
        crearBotonesParaCasillas();
        agregarBotonSimulacion(); // Añadimos el botón para la simulación

        // Inicializamos el mapa de posiciones de las fichas. Al principio, todas están en su casa.
        for (int i = 101; i <= 116; i++) {
            mapaPosicionFichas.put(i, i);
        }
    }

    /**
     * Crea y añade un botón para simular la aparición de casillas.
     */
    private void agregarBotonSimulacion() {
        JButton btnSimular = new JButton("Simular");
        // Posicionamos el botón en la esquina inferior izquierda del tablero
        btnSimular.setBounds(10, 310, 100, 30);

        btnSimular.addActionListener(e -> {
            // Forzar la habilitación del panel para asegurar que los listeners de mouse funcionen.
            this.setEnabled(true);

            // 1. Generar un número aleatorio entre 1 y 5
            int numeroRandom = random.nextInt(5) + 1;
            System.out.println("Número aleatorio generado: " + numeroRandom);

            // 2. Ocultar todos los botones del tablero (pero mantener visibles las fichas)
            for (Map.Entry<Integer, JButton> entry : botonesCasillas.entrySet()) {
                if (entry.getKey() < 101) { // Solo afecta a las casillas del tablero, no a las fichas.
                    JButton botonCasilla = entry.getValue();
                    if (botonCasilla.getIcon() != null) { // Si hay una ficha en esta casilla...
                        // La mantenemos visible y HABILITADA para que el icono no se ponga gris.
                        // Simplemente la hacemos transparente y sin borde para que no parezca un destino.
                        botonCasilla.setEnabled(true);
                        botonCasilla.setContentAreaFilled(false);
                        botonCasilla.setBorderPainted(false);
                    } else {
                        // Si no hay ficha, la ocultamos completamente.
                        botonCasilla.setVisible(false);
                        botonCasilla.setEnabled(false);
                    }
                }
            }

            // 3. Mostrar solo los botones consecutivos según el número aleatorio
            System.out.println("Mostrando botones del 0 al " + (numeroRandom - 1));
            for (int i = 0; i < numeroRandom; i++) {
                JButton botonAMostrar = botonesCasillas.get(i);
                if (botonAMostrar != null) {
                    botonAMostrar.setVisible(true);
                    botonAMostrar.setEnabled(true);
                    // Nos aseguramos de que las casillas de destino se vean como tal
                    botonAMostrar.setContentAreaFilled(true);
                    botonAMostrar.setBackground(new Color(255, 255, 0, 80));
                    botonAMostrar.setBorder(javax.swing.BorderFactory.createLineBorder(Color.ORANGE, 1));
                }
            }
        });

        this.add(btnSimular);
    }

    /**
     * Determina el "grupo de color" de una ficha basado en su ID de casa. 1:
     * Rojo, 2: Azul, 3: Verde, 4: Amarillo, 0: Desconocido.
     */
    private int getGrupoColor(int idFicha) {
        if (idFicha >= 101 && idFicha <= 104) {
            return 1; // Rojo
        }
        if (idFicha >= 105 && idFicha <= 108) {
            return 2; // Azul
        }
        if (idFicha >= 109 && idFicha <= 112) {
            return 3; // Verde
        }
        if (idFicha >= 113 && idFicha <= 116) {
            return 4; // Amarillo
        }
        return 0;
    }

    private Integer getFichaEnCasilla(int idCasilla) {
        return mapaPosicionFichas.entrySet().stream()
                .filter(entry -> entry.getValue().equals(idCasilla))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Crea un ActionListener unificado que maneja tanto la selección de fichas
     * como el movimiento a una nueva casilla.
     *
     * @param boton El botón al que se le aplicará este listener.
     * @return Un ActionListener con la lógica de juego.
     */
    private java.awt.event.ActionListener crearActionListenerUnificado(JButton boton) {
        return e -> {
            // Si hay una animación en curso, no hacemos nada para evitar conflictos.
            if (animator != null && animator.isRunning()) {
                return;
            }

            // --- ESCENARIO 1: SELECCIONAR UNA FICHA PROPIA ---
            // Si el botón clicado tiene una ficha (y no es un hueco)
            // Y NO tenemos una ficha ya seleccionada...
            if (botonFichaSeleccionada == null) {
                if (boton.getIcon() != null && !(boton.getIcon() instanceof IconoDeHueco)) {
                    // Seleccionamos esta nueva ficha.
                    System.out.println("Ficha seleccionada en casilla: " + boton.getActionCommand());
                    botonFichaSeleccionada = boton;
                    botonFichaSeleccionada.setBorder(javax.swing.BorderFactory.createLineBorder(Color.CYAN, 3));
                    botonFichaSeleccionada.setBorderPainted(true);
                    resaltarVictimas(); // Mostramos posibles capturas
                }
                // --- ESCENARIO 2: EJECUTAR MOVIMIENTO O CAPTURA ---
                // Si YA tenemos una ficha seleccionada...
            } else {
                // Si el usuario vuelve a hacer clic en la misma ficha, la deseleccionamos.
                if (botonFichaSeleccionada == boton) {
                    limpiarResaltados();
                    botonFichaSeleccionada = null;
                } else {
                    int idCasillaClicada = Integer.parseInt(boton.getActionCommand());
                    boolean esOtraFicha = boton.getIcon() != null && !(boton.getIcon() instanceof IconoDeHueco);
                    Integer idFichaSeleccionada = getFichaEnCasilla(Integer.parseInt(botonFichaSeleccionada.getActionCommand()));
                    Integer idFichaClicada = getFichaEnCasilla(idCasillaClicada);

                    // PRIORIDAD 1: Cambiar selección a otra ficha del MISMO color.
                    if (esOtraFicha && idFichaSeleccionada != null && idFichaClicada != null &&
                        getGrupoColor(idFichaSeleccionada) == getGrupoColor(idFichaClicada)) {
                        
                        seleccionarFicha(boton);
                        System.out.println("Selección cambiada a otra ficha del mismo color.");

                    // PRIORIDAD 2: Si se hace clic en otra ficha que está en su casa (fuera del tablero), cambiamos la selección.
                    } else if (esOtraFicha && idCasillaClicada >= 101) {
                        seleccionarFicha(boton);
                        System.out.println("Selección cambiada a ficha en casa.");
                    // PRIORIDAD 3: Si el destino es una casilla de movimiento válida (visible y habilitada), intentamos mover/comer.
                    } else if (boton.isVisible() && boton.isEnabled()) {
                        ejecutarMovimientoOCaptura(boton);
                    // PRIORIDAD 4: Si no es un destino válido, comprobamos si es cualquier otra ficha (enemiga) para cambiar la selección.
                    } else if (esOtraFicha) {
                        seleccionarFicha(boton);
                        System.out.println("Selección cambiada a otra ficha (posiblemente enemiga).");
                    }
                }
            }
        };
    }

    /**
     * Se ejecuta cuando ya hay una ficha seleccionada y el usuario hace clic en un destino.
     * @param destinoBoton El botón de destino (puede estar vacío o contener una víctima).
     */
    private void ejecutarMovimientoOCaptura(JButton destinoBoton) {
        final JButton origen = botonFichaSeleccionada;
        final JButton destino = destinoBoton;
        int idCasillaDestino = Integer.parseInt(destino.getActionCommand());

        // Identificamos la ficha que se mueve
        Integer idFichaMovida = getFichaEnCasilla(Integer.parseInt(origen.getActionCommand()));
        if (idFichaMovida == null) {
            limpiarResaltados();
            botonFichaSeleccionada = null;
            return; // No se pudo identificar la ficha, abortamos.
        }

        // Verificamos si el destino está ocupado por otra ficha (la víctima)
        Integer idFichaEnDestino = getFichaEnCasilla(idCasillaDestino);

        // Preparamos la acción de mover la ficha principal. Se ejecutará sola o después de una captura.
        Runnable moverFichaPrincipal = () -> {
            mapaPosicionFichas.put(idFichaMovida, idCasillaDestino);
            // Cuando la animación termine, el botón de destino se convertirá en la nueva ficha seleccionada.
            animarFicha(origen, destino, () -> seleccionarFicha(destino));
        };
        limpiarResaltados(); // Limpiamos resaltados de víctimas (rojo) antes de la acción.

        // Si hay una ficha en el destino (es una captura potencial)
        if (idFichaEnDestino != null) {
            // Verificamos que sea de un color diferente Y que la víctima esté en el tablero (no en su casa).
            if (getGrupoColor(idFichaMovida) != getGrupoColor(idFichaEnDestino) && idCasillaDestino < 101) {

                System.out.println("¡COMER! Ficha " + idFichaMovida + " come a " + idFichaEnDestino);
                JButton victima = botonesCasillas.get(idCasillaDestino);
                JButton casaOriginalFichaComida = botonesCasillas.get(idFichaEnDestino);

                // 1. Actualizamos el modelo: la ficha comida vuelve a su ID de casa.
                mapaPosicionFichas.put(idFichaEnDestino, idFichaEnDestino);

                // 2. Animamos a la víctima de vuelta a casa. Cuando termine, se ejecutará la animación de la ficha principal.
                animarFicha(victima, casaOriginalFichaComida, moverFichaPrincipal);
            }
            // Si es del mismo color, no hacemos nada.
        } else { // Si no hay ficha en el destino (es un movimiento a casilla vacía)
            moverFichaPrincipal.run();
        }

    }
    
    /**
     * Establece un botón de ficha como el seleccionado y le aplica el borde de resaltado.
     * @param botonFicha El botón a seleccionar.
     */
    private void seleccionarFicha(JButton botonFicha) {
        limpiarResaltados(); // Limpia cualquier selección anterior.
        botonFichaSeleccionada = botonFicha;
        botonFichaSeleccionada.setBorder(javax.swing.BorderFactory.createLineBorder(Color.CYAN, 3));
        botonFichaSeleccionada.setBorderPainted(true);
        resaltarVictimas(); // Muestra posibles víctimas para la nueva selección.
    }
    
    /**
     * Mueve una ficha instantáneamente de un botón de origen a uno de destino.
     *
     * @param origen Botón de inicio.
     * @param destino Botón final.
     * @param onMoveEnd Un Runnable que se ejecuta al finalizar el movimiento.
     */
    private void teletransportarFicha(JButton origen, JButton destino, Runnable onMoveEnd) {
        final Icon fichaIcon = origen.getIcon();
        if (fichaIcon == null || fichaIcon instanceof IconoDeHueco) { // Si no hay icono o es un hueco, no hay nada que mover.
            if (onMoveEnd != null) {
                onMoveEnd.run();
            }
            return;
        }

        // Limpiamos el origen
        int idCasillaOrigen = Integer.parseInt(origen.getActionCommand());
        if (idCasillaOrigen >= 101) { // Si el origen es una casa
            origen.setIcon(new IconoDeHueco(origen.getWidth() - 10, origen.getHeight() - 15));
        } else { // Si el origen es una casilla del tablero
            origen.setIcon(null);
        }
        origen.setBorderPainted(false);
        origen.repaint(); // Aseguramos que el origen se repinte inmediatamente

        // Movemos la ficha al destino
        destino.setIcon(fichaIcon);
        destino.setContentAreaFilled(false);
        
        repaint();

        destino.repaint(); // Aseguramos que el destino se repinte inmediatamente
        if (onMoveEnd != null) {
            onMoveEnd.run(); // Ejecutar la acción post-movimiento
        }
    }

    /**
     * Anima el movimiento de una ficha desde un botón de origen a uno de
     * destino.
     *
     * @param origen Botón de inicio.
     * @param destino Botón final.
     * @param onAnimationEnd Un Runnable que se ejecuta al finalizar la
     * animación.
     */
    private void animarFicha(JButton origen, JButton destino, Runnable onAnimationEnd) {
        final Icon fichaIcon = origen.getIcon();
        if (fichaIcon == null || fichaIcon instanceof IconoDeHueco) { // Si no hay icono o es un hueco, no hay nada que animar.
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
            return;
        }

        final JLabel fichaAnimada = new JLabel(fichaIcon);
        fichaAnimada.setBounds(origen.getBounds());
        add(fichaAnimada, 0);

        // Limpiamos el origen
        int idCasillaOrigen = Integer.parseInt(origen.getActionCommand());
        if (idCasillaOrigen >= 101) {
            origen.setIcon(new IconoDeHueco(origen.getWidth() - 10, origen.getHeight() - 15));
        } else {
            origen.setIcon(null);
        }
        origen.setBorderPainted(false);

        this.setEnabled(false); // Deshabilitamos clics durante la animación

        final long startTime = System.currentTimeMillis();
        final int duration = 400;
        final Point startPoint = origen.getLocation();
        final Point endPoint = destino.getLocation();

        animator = new Timer(10, (ae) -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double t = Math.min(1.0, (double) elapsed / duration);

            // Interpolación lineal para X
            int x = (int) (startPoint.x + t * (endPoint.x - startPoint.x));

            // Interpolación lineal para Y (movimiento base)
            int y_linear = (int) (startPoint.y + t * (endPoint.y - startPoint.y));

            // Componente de salto parabólico para Y
            int jumpHeight = 30; // Altura máxima del salto en píxeles. Ajusta este valor si quieres un salto más alto/bajo.
            int y_jump_offset = (int) (-jumpHeight * 4 * t * (1 - t)); // 4*t*(1-t) crea una parábola que va de 0 a 1 y de vuelta a 0.
            int y = y_linear + y_jump_offset;

            fichaAnimada.setLocation(x, y);

            if (t >= 1.0) {
                ((Timer) ae.getSource()).stop();
                remove(fichaAnimada);
                destino.setIcon(fichaIcon);
                destino.setContentAreaFilled(false);
                this.setEnabled(true);
                repaint();
                if (onAnimationEnd != null) {
                    onAnimationEnd.run(); // Ejecutar la acción post-animación
                }
            }
        });
        animator.start();
    }

    /**
     * Itera sobre las casillas de destino visibles y resalta las fichas
     * enemigas que encuentre.
     */
    private void resaltarVictimas() {
        if (botonFichaSeleccionada == null) {
            return;
        }
        Integer idFichaAtacante = getFichaEnCasilla(Integer.parseInt(botonFichaSeleccionada.getActionCommand()));
        if (idFichaAtacante == null) {
            return;
        }

        // REGLA: No se pueden resaltar víctimas si la ficha atacante está en su casa.
        int idCasillaAtacante = mapaPosicionFichas.get(idFichaAtacante);
        if (idCasillaAtacante >= 101) {
            return; // Salimos del método, no hay nada que resaltar.
        }

        for (Map.Entry<Integer, JButton> entry : botonesCasillas.entrySet()) {
            JButton botonCasilla = entry.getValue();
            // Si la casilla es visible y está habilitada (es un destino válido)
            if (botonCasilla.isVisible() && botonCasilla.isEnabled()) {
                Integer idVictima = getFichaEnCasilla(entry.getKey());
                // Si hay una ficha en esa casilla, es de otro color Y la víctima está en el tablero...
                if (idVictima != null && getGrupoColor(idFichaAtacante) != getGrupoColor(idVictima) && entry.getKey() < 101) {

                    // ...la resaltamos como "comible".
                    botonCasilla.setBorder(javax.swing.BorderFactory.createLineBorder(Color.RED, 3));
                    botonCasilla.setBorderPainted(true);
                }
            }
        }
    }

    /**
     * Limpia todos los resaltados de selección (cian) y de víctima (rojo).
     */
    private void limpiarResaltados() {
        // Quitamos el borde a todas las casillas.
        for (JButton boton : botonesCasillas.values()) {
            boton.setBorderPainted(false);
        }
        botonFichaSeleccionada = null;
    }

    private String getIconNameForFicha(int idFicha) {
        if (idFicha >= 101 && idFicha <= 104) {
            return "MChiquitaFRojo.png";
        } else if (idFicha >= 105 && idFicha <= 108) {
            return "MChiquitaFAzul.png";
        } else if (idFicha >= 109 && idFicha <= 112) {
            return "MChiquitaFVerde.png";
        } else if (idFicha >= 113 && idFicha <= 116) {
            return "MChiquitaFAmarillo.png";
        }
        return null;
    }

    private void crearBotonesParaCasillas() {
        for (Map.Entry<Integer, Rectangle> entry : mapaCoordenadas.entrySet()) {
            int idCasilla = entry.getKey();
            Rectangle bounds = entry.getValue();

            JButton boton = new JButton();
            boton.setActionCommand(String.valueOf(idCasilla));
            boton.setBounds(bounds);

            // --- MODO DE ARRASTRE PARA DEPURACIÓN ---
            final ActionListener actionListener = crearActionListenerUnificado(boton);
            MouseAdapter dragListener = new MouseAdapter() {
                private Point startPoint;
                private boolean isDragging = false;

                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    isDragging = false;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (startPoint == null) {
                        return;
                    }

                    isDragging = true; // Si se detecta un drag, se marca como tal.
                    Point currentLocation = boton.getLocation();
                    int newX = currentLocation.x + e.getX() - startPoint.x;
                    int newY = currentLocation.y + e.getY() - startPoint.y;
                    boton.setLocation(newX, newY);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging) {
                        // Si fue un arrastre, imprimimos las coordenadas.
                        System.out.println(
                                "mapaCoordenadas.put(" + boton.getActionCommand()
                                + ", new Rectangle(" + boton.getX() + ", " + boton.getY() + ", "
                                + boton.getWidth() + ", " + boton.getHeight() + "));"
                        );
                    } else {
                        // Si no fue un arrastre, procesamos el clic del juego.
                        // Esto asegura que el clic normal siga funcionando si no se arrastra.
                        actionListener.actionPerformed(new java.awt.event.ActionEvent(boton, java.awt.event.ActionEvent.ACTION_PERFORMED, boton.getActionCommand()));
                    }
                    startPoint = null;
                    isDragging = false;
                }
            };
            boton.addMouseListener(dragListener);
            boton.addMouseMotionListener(dragListener);

            // --- Lógica para asignar iconos o estilo por defecto ---
            String iconFileName = null;
            if (idCasilla >= 101 && idCasilla <= 104) {
                iconFileName = "MChiquitaFRojo.png";
            } else if (idCasilla >= 105 && idCasilla <= 108) {
                iconFileName = "MChiquitaFAzul.png";
            } else if (idCasilla >= 109 && idCasilla <= 112) {
                iconFileName = "MChiquitaFVerde.png";
            } else if (idCasilla >= 113 && idCasilla <= 116) {
                iconFileName = "MChiquitaFAmarillo.png";
            }

            if (iconFileName != null) { // Es un botón de FICHA
                ImageIcon icon = getScaledIcon(iconFileName, bounds.width, bounds.height);
                if (icon != null) {
                    boton.setIcon(icon);
                }
                boton.setOpaque(false);
                boton.setContentAreaFilled(false);
                boton.setBorderPainted(false);

            } else { // Es un botón de CASILLA del tablero
                boton.setOpaque(false);
                boton.setContentAreaFilled(true);
                boton.setBackground(new Color(255, 255, 0, 80));
                boton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.ORANGE, 1));
            }

            // --- Estado Inicial: Fichas visibles, resto oculto ---
            boton.setVisible(idCasilla >= 101); // Solo las fichas son visibles al inicio
            boton.setEnabled(idCasilla >= 101); // Solo las fichas se pueden clickear al inicio

            this.add(boton);
            botonesCasillas.put(idCasilla, boton);
        }
    }

    private ImageIcon getScaledIcon(String fileName, int targetWidth, int targetHeight) {
        try {
            File imgFile = new File("src/main/resources/Assets/" + fileName);
            if (imgFile.exists()) {
                ImageIcon originalIcon = new ImageIcon(imgFile.getAbsolutePath());
                Image originalImage = originalIcon.getImage();
                int originalWidth = originalIcon.getIconWidth();
                int originalHeight = originalIcon.getIconHeight();
                double aspectRatio = (double) originalWidth / originalHeight;
                int newWidth = targetWidth;
                int newHeight = (int) (newWidth / aspectRatio);
                if (newHeight > targetHeight) {
                    newHeight = targetHeight;
                    newWidth = (int) (newHeight * aspectRatio);
                }
                Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } else {
                System.err.println("Archivo de icono no encontrado: " + imgFile.getAbsolutePath());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error al cargar y escalar icono desde disco: " + e.getMessage());
            return null;
        }
    }

    private void loadBoardFromDisk() {
        try {
            File imgFile = new File("src/main/resources/Assets/TABLERO.png");
            if (imgFile.exists()) {
                super.setImage(javax.imageio.ImageIO.read(imgFile));
            } else {
                System.err.println("Archivo de tablero no encontrado: " + imgFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error al cargar tablero desde disco: " + e.getMessage());
        }
    }
}
