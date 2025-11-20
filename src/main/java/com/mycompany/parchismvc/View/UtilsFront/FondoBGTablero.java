/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.View.UtilsFront;

import com.mycompany.parchismvc.Model.ColorJugador;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
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
    
    // Listener para notificar cuando se completa un movimiento
    private java.util.function.BiConsumer<Integer, Integer> onMoveListener;

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

    /**
     * Clase interna para dibujar dos iconos de ficha en una misma casilla.
     */
    private class IconoCompuesto implements Icon {
        private final Icon icon1;
        private final Icon icon2;
        private final int width;
        private final int height;

        public IconoCompuesto(Icon icon1, Icon icon2, int width, int height) {
            this.icon1 = icon1;
            this.icon2 = icon2;
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Desplazamiento global para centrar mejor el par de fichas.
            int offsetX = -6;

            // Si la casilla es más alta que ancha (pasillos horizontales del tablero)
            if (height > width) {
                int halfHeight = height / 3;
                // Dibuja el primer icono en la mitad superior
                icon1.paintIcon(c, g, x + offsetX, y);
                // Dibuja el segundo icono en la mitad inferior
                icon2.paintIcon(c, g, x + offsetX, y + halfHeight - 5);
            } else { // Si la casilla es más ancha que alta (pasillos verticales)
                int halfWidth = width / 2;
                icon1.paintIcon(c, g, x + offsetX, y);
                icon2.paintIcon(c, g, x + offsetX + halfWidth - 5, y);
            }
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

    /**
     * Clase interna que envuelve un Icon para permitir un desplazamiento (offset)
     * al dibujarlo. Esto es útil para centrar visualmente los iconos en botones
     * que no son cuadrados.
     */
    private class IconoWrapper implements Icon {
        private final Icon original;
        private final int offsetX;

        public IconoWrapper(Icon original, int offsetX) {
            this.original = original;
            this.offsetX = offsetX;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Aplica el desplazamiento en el eje X al dibujar el icono original.
            original.paintIcon(c, g, x + offsetX, y);
        }

        @Override
        public int getIconWidth() {
            return original.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return original.getIconHeight();
        }
    }

    // Mapa para guardar la referencia a cada botón por su número de casilla
    private final Map<Integer, JButton> botonesCasillas = new HashMap<>();
    // Generador de números aleatorios para la simulación
    // Mapa para rastrear la posición de cada ficha. Key: ID de la casa (101-116), Value: ID de la casilla actual.
    private final Map<UUID, Integer> mapaPosicionFichas = new HashMap<>();
    private final Map<UUID, ColorJugador> mapaColorDeFicha = new HashMap<>();
    private Map<UUID, List<com.mycompany.parchismvc.Model.Ficha>> fichasPorJugadorActuales = new HashMap<>();
    private final Map<UUID, ColorJugador> mapaColoresJugadores = new HashMap<>();

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
        // La inicialización se mueve a un método separado para asegurar el orden correcto.
        initComponents();
    }

    /**
     * Crea y añade un botón para simular la aparición de casillas. (Método obsoleto)
     */
    private void agregarBotonSimulacion() { }

    public void setOnMoveListener(java.util.function.BiConsumer<Integer, Integer> listener) {
        this.onMoveListener = listener;
    }

    /**
     * Oculta todas las casillas del tablero y resalta las que se pasen como parámetro.
     * @param cantidadDeCasillas La cantidad de casillas a resaltar.
     * @param colorDelJugador El color del jugador en turno para determinar la casilla de inicio.
     */
    public void mostrarCasillasDestino(int cantidadDeCasillas, ColorJugador colorDelJugador) {
        // Iterar sobre las fichas del jugador actual para determinar y resaltar sus posibles movimientos.
        fichasPorJugadorActuales.values().stream()
            .flatMap(List::stream)
            .filter(ficha -> getColorDeFicha(ficha.id) == colorDelJugador && esMovible(ficha, cantidadDeCasillas))
            .forEach(ficha -> {
                int posActual = ficha.posicion;
                int entradaPasillo = getCasillaEntradaPasillo(colorDelJugador);
                int meta = getMeta(colorDelJugador);

                if (posActual == -1) { // Desde la base
                    resaltarBoton(getCasillaDeSalida(colorDelJugador));
                } else if (posActual > 0 && posActual <= 68) { // Desde el tablero
                    if (posActual == entradaPasillo) {
                        int destino = getPrimerPasoPasillo(colorDelJugador) + cantidadDeCasillas - 1;
                        resaltarBoton(destino <= meta ? destino : meta - (destino - meta));
                    } else {
                        resaltarBoton(((posActual - 1 + cantidadDeCasillas) % 68) + 1);
                    }
                } else if (posActual > 68) { // Desde el pasillo
                    int destino = posActual + cantidadDeCasillas;
                    resaltarBoton(destino <= meta ? destino : meta - (destino - meta));
                }
            });
        repaint();
    }
    
    private boolean esMovible(com.mycompany.parchismvc.Model.Ficha ficha, int valorDado) {
        // Si la ficha ya está en la meta, no se puede mover.
        ColorJugador color = getColorDeFicha(ficha.id);
        if (color != null && ficha.posicion == getMeta(color)) {
            return false;
        }
        
        if (ficha.posicion == -1) {
            return valorDado == 5;
        }
        return true; // Para fichas en tablero o pasillo, asumimos que se pueden mover.
    }

    private void resaltarBoton(int idCasillaAMostrar) {
                JButton botonAMostrar = botonesCasillas.get(idCasillaAMostrar);
                if (botonAMostrar != null) {
                    System.out.println("Mostrando casilla: " + idCasillaAMostrar);
                    botonAMostrar.setVisible(true);
                    botonAMostrar.setEnabled(true);
                    botonAMostrar.setOpaque(true); 
                    botonAMostrar.setContentAreaFilled(true); // Necesario para que el fondo se pinte
                    botonAMostrar.setBackground(new Color(255, 255, 0, 180)); // Amarillo más opaco
                    botonAMostrar.setBorder(javax.swing.BorderFactory.createLineBorder(Color.ORANGE, 1));
                }
            }
    
    /**
     * Restaura una casilla a su estado visual por defecto (transparente y sin borde).
     * @param idCasilla El ID de la casilla a limpiar.
     */
    private void limpiarBoton(int idCasilla) {
        JButton botonALimpiar = botonesCasillas.get(idCasilla);
        if (botonALimpiar != null) {
            // No ocultamos el botón, solo revertimos su estado visual.
            // Si no tiene ficha, se quedará como un área transparente.
            // Si va a recibir una ficha, necesita estar visible y habilitado.
            botonALimpiar.setEnabled(true); // Lo dejamos habilitado para poder hacer clic en las fichas
            botonALimpiar.setOpaque(false); // Lo volvemos transparente
            botonALimpiar.setContentAreaFilled(false);
            botonALimpiar.setBackground(new Color(0, 0, 0, 0)); // Fondo completamente transparente
            botonALimpiar.setBorder(null); // Quitamos el borde
        }
    }

    private int getCasillaDeSalida(ColorJugador color) {
        return switch (color) {
            case ROJO -> 39;
            case AZUL -> 22;
            case VERDE -> 56;
            case AMARILLO -> 5;
        };
    }

    private int getCasillaEntradaPasillo(ColorJugador color) {
        return switch (color) {
            case ROJO -> 34;
            case AZUL -> 17;
            case VERDE -> 51;
            case AMARILLO -> 68;
        };
    }

    private int getPrimerPasoPasillo(ColorJugador color) {
        return switch (color) {
            case ROJO -> 85;
            case AZUL -> 77;
            case VERDE -> 93;
            case AMARILLO -> 69;
        };
    }

    private int getMeta(ColorJugador color) {
        return switch (color) {
            case ROJO -> 92;
            case AZUL -> 84;
            case VERDE -> 100;
            case AMARILLO -> 76;
        };
    }


    /**
     * Actualiza la posición de todas las fichas en el tablero basándose en el estado
     * proporcionado por el servidor. Este método es clave para la sincronización visual
     * entre jugadores.
     *
     * @param fichasPorJugador Un mapa que contiene la lista de fichas para cada ID de jugador.
     * @param nuevosColores Un mapa opcional para actualizar los colores de los jugadores.
     */
    public void actualizarEstadoFichas(Map<UUID, List<com.mycompany.parchismvc.Model.Ficha>> fichasPorJugador, Map<UUID, ColorJugador> nuevosColores, UUID turnoDe, UUID miId) {
        SwingUtilities.invokeLater(() -> {
            limpiarResaltados(); // Limpia cualquier selección o resaltado activo.

            // Actualiza el mapa de colores interno si se proporciona uno nuevo.
            if (nuevosColores != null && !nuevosColores.isEmpty()) this.mapaColoresJugadores.putAll(nuevosColores);

            // Mapa para saber qué casillas del tablero (1-100) estarán ocupadas
            Map<Integer, java.util.List<Icon>> casillasOcupadas = new HashMap<>();
            // Mapa para saber qué casas (101-116) estarán ocupadas
            Map<Integer, Icon> casasOcupadas = new HashMap<>();

            // Mapa para rastrear las posiciones anteriores y poder limpiar solo lo necesario
            // Map<Integer, JButton> botonesParaLimpiar = new HashMap<>(botonesCasillas);

            // 1. Pre-calcular el estado final sin tocar la UI todavía
            for (Map.Entry<UUID, List<com.mycompany.parchismvc.Model.Ficha>> entry : fichasPorJugador.entrySet()) {
                ColorJugador color = this.mapaColoresJugadores.get(entry.getKey());
                List<com.mycompany.parchismvc.Model.Ficha> fichas = entry.getValue();
                for (int i = 0; i < fichas.size(); i++) {
                    com.mycompany.parchismvc.Model.Ficha ficha = fichas.get(i);
                    if (ficha == null) continue;

                    int idCasillaActual = ficha.posicion;
                    int idBotonCasa = getBotonCasaIdDeFicha(ficha.id, i, color);

                    Icon icon = getIconForFicha(idBotonCasa);
                    if (icon == null) continue; // Si no se puede obtener el icono, no se puede dibujar.

                    if (idCasillaActual == -1) { // La ficha está en casa (posición -1)
                        casasOcupadas.put(idBotonCasa, icon);
                    } else { // La ficha está en el tablero
                        casillasOcupadas.computeIfAbsent(idCasillaActual, k -> new java.util.ArrayList<>()).add(icon);

                    }
                }
            }

            // 2. Limpiar completamente el tablero antes de redibujar.
            for (JButton boton : botonesCasillas.values()) {
                int idBoton = Integer.parseInt(boton.getActionCommand());
                boolean esCasa = idBoton >= 101;
                // Pone un hueco en las bases y null (vacío) en el resto.
                boton.setIcon(esCasa ? new IconoDeHueco(boton.getWidth() - 10, boton.getHeight() - 15) : null);
                boton.setBorderPainted(false); // También limpiamos cualquier borde residual.
                
                // Aseguramos que todas las casillas del tablero sean transparentes por defecto.
                if (!esCasa) {
                    limpiarBoton(idBoton);
                }
            }

            // 3. Dibujar las fichas en sus nuevas posiciones
            casillasOcupadas.forEach((id, icons) -> {
                JButton boton = botonesCasillas.get(id);
                if (boton != null) {
                    if (icons.size() == 1) {
                        boton.setIcon(icons.get(0));
                    } else if (icons.size() >= 2) {
                        // Si hay dos o más fichas, crea un icono compuesto con las dos primeras.
                        Icon iconoCompuesto = new IconoCompuesto(icons.get(0), icons.get(1), boton.getWidth(), boton.getHeight());
                        boton.setIcon(iconoCompuesto);
                        // Al ser un bloqueo, le añadimos un borde verde.
                        boton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                        boton.setBorderPainted(true);
                    }
                    boton.setVisible(true); // Aseguramos que sea visible
                    boton.setEnabled(true); // Aseguramos que se pueda clickear
                    boton.setOpaque(false);
                    boton.setContentAreaFilled(false);

                }
            });
            casasOcupadas.forEach((id, icon) -> {
                JButton boton = botonesCasillas.get(id);
                if (boton != null) boton.setIcon(icon);
            });

            // 3. Actualizar los mapas de estado internos
            if (fichasPorJugador != null) {
                this.fichasPorJugadorActuales = fichasPorJugador;
            } else {
                this.fichasPorJugadorActuales.clear();
            }
            mapaPosicionFichas.clear(); // Limpiamos el mapa de posiciones para reconstruirlo
            mapaColorDeFicha.clear(); // Limpiamos el mapa de colores para reconstruirlo
            for (Map.Entry<UUID, List<com.mycompany.parchismvc.Model.Ficha>> entry : fichasPorJugador.entrySet()) {
                UUID idJugador = entry.getKey();
                for (com.mycompany.parchismvc.Model.Ficha ficha : entry.getValue()) {
                    if (ficha == null) continue;
                    mapaPosicionFichas.put(ficha.id, ficha.posicion);
                    mapaColorDeFicha.put(ficha.id, this.mapaColoresJugadores.get(idJugador));
                }
            }

            repaint();
        });
    }

    private Icon getIconForFicha(int idBotonCasa, int idCasillaDestino) {
        // Usamos la casilla de destino para determinar el tamaño, no la casa original.
        JButton botonDestino = botonesCasillas.get(idCasillaDestino);
        if (botonDestino == null) {
            System.err.println("getIconForFicha: No se encontró el botón para el ID de casilla " + idCasillaDestino);
            return null; // Devolver null para evitar NullPointerException
        }
        // Para las casillas del tablero, las fichas deben ser más pequeñas para que quepan dos.
        boolean esCasillaDeTablero = idCasillaDestino < 101;
        int iconWidth = esCasillaDeTablero ? botonDestino.getWidth() / 2 : botonDestino.getWidth();
        int iconHeight = botonDestino.getHeight();

        ImageIcon icon = getScaledIcon(getIconNameForFicha(idBotonCasa), iconWidth, iconHeight);

        // Si es una casilla del tablero, aplicamos un desplazamiento para centrar mejor la ficha.
        if (esCasillaDeTablero) {
            // Movemos la ficha 2 píxeles a la izquierda para que se vea más centrada.
            return new IconoWrapper(icon, -2);
        } else {
            return icon;
        }

    }

    private Icon getIconForFicha(int idBotonCasa) {
        JButton casaOriginal = botonesCasillas.get(idBotonCasa);
        if (casaOriginal == null) {
            System.err.println("getIconForFicha: No se encontró el botón para el ID de casa " + idBotonCasa);
            return null; // Devolver null para evitar NullPointerException
        }
        // Para las casillas del tablero, las fichas deben ser más pequeñas para que quepan dos.
        boolean esCasillaDeTablero = casaOriginal.getWidth() < 30;
        int iconWidth = esCasillaDeTablero ? casaOriginal.getWidth() / 2 : casaOriginal.getWidth();
        int iconHeight = casaOriginal.getHeight();

        ImageIcon icon = getScaledIcon(getIconNameForFicha(idBotonCasa), iconWidth, iconHeight);

        return icon;

    }

    private int getBotonCasaIdDeFicha(UUID idFicha, int indiceFicha, ColorJugador color) {
        if (color == null || indiceFicha < 0 || indiceFicha > 3) return 0; // Guarda de seguridad
        return switch (color) {
            case ROJO -> 101 + indiceFicha;
            case AZUL -> 105 + indiceFicha;
            case VERDE -> 109 + indiceFicha;
            case AMARILLO -> 113 + indiceFicha;
        };
    }

    private ColorJugador getColorDeFicha(UUID idFicha) {
        return mapaColorDeFicha.get(idFicha);
    }

    /**
     * Versión simple que solo busca la primera ficha en una casilla o casa. No tiene lógica recursiva.
     */
    private UUID getFichaEnCasillaSimple(int idCasilla) {
        // Busca la primera ficha en esa casilla del tablero.
        UUID fichaId = mapaPosicionFichas.entrySet().stream()
                .filter(entry -> entry.getValue().equals(idCasilla))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        // Si no se encuentra y la casilla es una casa (ID >= 101), busca qué ficha corresponde a esa casa.
        if (fichaId == null && idCasilla >= 101) {
            for (Map.Entry<UUID, List<com.mycompany.parchismvc.Model.Ficha>> entry : fichasPorJugadorActuales.entrySet()) {
                List<com.mycompany.parchismvc.Model.Ficha> fichas = entry.getValue();
                for (int i = 0; i < fichas.size(); i++) {
                    com.mycompany.parchismvc.Model.Ficha ficha = fichas.get(i);
                    ColorJugador color = mapaColoresJugadores.get(entry.getKey());
                    if (getBotonCasaIdDeFicha(ficha.id, i, color) == idCasilla) return ficha.id;
                }
            }
        }
        return fichaId;
    }

    private UUID getFichaEnCasilla(int idCasilla) {
        // Busca todas las fichas en esa casilla.
        java.util.List<UUID> fichasEnCasilla = mapaPosicionFichas.entrySet().stream()
                .filter(entry -> entry.getValue().equals(idCasilla))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());

        UUID fichaId = null;
        if (!fichasEnCasilla.isEmpty()) {
            // Si hay una ficha seleccionada, priorizamos encontrar la otra ficha (la víctima).
            if (botonFichaSeleccionada != null) {
                // Usamos la versión simple para evitar la recursión infinita.
                UUID fichaSeleccionadaId = getFichaEnCasillaSimple(Integer.parseInt(botonFichaSeleccionada.getActionCommand()));
                
                // Si hay más de una ficha, devolvemos la que NO está seleccionada.
                if (fichasEnCasilla.size() > 1) {
                    fichaId = fichasEnCasilla.stream()
                            .filter(id -> !id.equals(fichaSeleccionadaId))
                            .findFirst()
                            .orElse(fichasEnCasilla.get(0)); // Fallback por si algo va mal
                } else {
                    fichaId = fichasEnCasilla.get(0);
                }
            } else {
                // Si no hay nada seleccionado, devolvemos la primera que encontremos.
                fichaId = fichasEnCasilla.get(0);
            }
        }

        // Si no se encuentra y la casilla es una casa (ID >= 101),
        // busca qué ficha corresponde a esa casa.
        if (fichaId == null && idCasilla >= 101) {
            for (Map.Entry<UUID, List<com.mycompany.parchismvc.Model.Ficha>> entry : fichasPorJugadorActuales.entrySet()) {
                List<com.mycompany.parchismvc.Model.Ficha> fichas = entry.getValue();
                for (int i = 0; i < fichas.size(); i++) {
                    com.mycompany.parchismvc.Model.Ficha ficha = fichas.get(i);
                    ColorJugador color = mapaColoresJugadores.get(entry.getKey());
                    if (getBotonCasaIdDeFicha(ficha.id, i, color) == idCasilla) return ficha.id;
                }
            }
        }
        return fichaId;
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
            if (controlador == null) return;
            UUID miId = controlador.getMiId();
            UUID turnoId = controlador.getTurnoCache();

            // Solo permitir acciones si es mi turno
            if (miId == null || turnoId == null || !miId.equals(turnoId)) {
                return; // No es mi turno, no hago nada.
            }

            // --- ESCENARIO 1: SELECCIONAR UNA FICHA PROPIA ---
            if (botonFichaSeleccionada == null) {
                if (boton.getIcon() != null && !(boton.getIcon() instanceof IconoDeHueco) && !(boton.getIcon() instanceof IconoCompuesto)) {
                    UUID fichaId = getFichaEnCasilla(Integer.parseInt(boton.getActionCommand()));
                    // Solo permite seleccionar fichas propias
                    ColorJugador colorFicha = getColorDeFicha(fichaId);
                    ColorJugador miColor = mapaColoresJugadores.get(miId);
                    if (fichaId != null && colorFicha == miColor) {
                        seleccionarFicha(boton);
                    }
                }
                // Si es un icono compuesto, no permitimos la selección directa por ahora
                // para simplificar la lógica. El servidor se encargará de gestionar los bloqueos.
                else if (boton.getIcon() instanceof IconoCompuesto) {
                    // Podrías implementar una lógica más compleja aquí si fuera necesario,
                    // como un menú contextual para elegir qué ficha mover.
                    // Por ahora, no hacemos nada para evitar movimientos ambiguos.
                }

            } else {
                // --- ESCENARIO 2: DESELECCIONAR O MOVER ---
                // PRIORIDAD 1: Comprobar si se ha hecho clic en una casilla de destino válida.
                // Un destino es válido si está visible y habilitado (resaltado en amarillo o rojo).
                if (boton.isVisible() && boton.isEnabled()) {
                    ejecutarMovimientoOCaptura(boton);
                } else {
                    // PRIORIDAD 2: Si no es un destino, comprobar si es otra ficha propia para cambiar la selección.
                    UUID fichaClicadaId = getFichaEnCasilla(Integer.parseInt(boton.getActionCommand()));
                    ColorJugador miColor = mapaColoresJugadores.get(miId);

                    if (fichaClicadaId != null && miColor == getColorDeFicha(fichaClicadaId)) {
                        // Si es la misma ficha, se deselecciona, pero se mantienen los destinos.
                        if (botonFichaSeleccionada == boton) {
                            boton.setBorderPainted(false);
                            botonFichaSeleccionada = null;
                        } else {
                            // Si es otra ficha propia, se cambia la selección.
                            seleccionarFicha(boton);
                        }
                    } else {
                        // Si se hace clic en cualquier otro lugar, se limpia todo.
                        limpiarResaltados();
                    }
                }
            }
        };
    }

    private com.mycompany.parchismvc.Controller.Controlador controlador;
    private UUID miId;

    /**
     * Se ejecuta cuando ya hay una ficha seleccionada y el usuario hace clic en un destino.
     * @param destinoBoton El botón de destino (puede estar vacío o contener una víctima).
     */
    private void ejecutarMovimientoOCaptura(JButton destinoBoton) {
        final JButton origenBoton = botonFichaSeleccionada;
        final JButton destinoBotonFinal = destinoBoton;
        final int idCasillaOrigen = Integer.parseInt(origenBoton.getActionCommand());
        final int idCasillaDestino = Integer.parseInt(destinoBotonFinal.getActionCommand());
    
        // Identificamos la ficha que se mueve
        final UUID idFichaMovida = getFichaEnCasilla(idCasillaOrigen);
        if (idFichaMovida == null) {
            limpiarResaltados();
            botonFichaSeleccionada = null;
            return; // No se pudo identificar la ficha, abortamos.
        }
    
        // Verificamos si el destino está ocupado por otra ficha (la víctima)
        final UUID idFichaEnDestino = getFichaEnCasilla(idCasillaDestino);
        final ColorJugador colorFichaMovida = getColorDeFicha(idFichaMovida);
    
        // Preparamos la acción de mover la ficha principal. Se ejecutará sola o después de una captura.
        Runnable moverFichaPrincipal = () -> {
            List<Integer> path = calcularRuta(idCasillaOrigen, idCasillaDestino, colorFichaMovida);
            animarFicha(origenBoton, destinoBotonFinal, path, () -> {
                // Cuando la animación termina, notificamos al controlador para que actualice el estado del juego.
                if (onMoveListener != null) {
                    int indiceFicha = getIndiceDeFicha(idFichaMovida, fichasPorJugadorActuales);
                    onMoveListener.accept(indiceFicha, idCasillaDestino);
                }
                limpiarResaltados();
            });
        };
    
        // Si hay una ficha en el destino (es una captura potencial)
        if (idFichaEnDestino != null) {
            ColorJugador colorFichaEnDestino = getColorDeFicha(idFichaEnDestino);
            // Si la ficha en el destino es de un color diferente (es una captura)
            if (colorFichaMovida != colorFichaEnDestino) {
                System.out.println("¡COMER! Ficha " + idFichaMovida + " come a " + idFichaEnDestino);
                JButton victimaBoton = botonesCasillas.get(idCasillaDestino);
                if (victimaBoton == null) { return; } // Seguridad
                
                // Obtenemos el botón de la casa de la víctima para la animación de retorno.
                ColorJugador colorVictima = getColorDeFicha(idFichaEnDestino);
                JButton casaOriginalFichaComida = botonesCasillas.get(getBotonCasaIdDeFicha(idFichaEnDestino, getIndiceDeFicha(idFichaEnDestino, fichasPorJugadorActuales), colorVictima));

                // Animamos a la víctima de vuelta a casa (sin ruta, movimiento simple).
                animarFicha(victimaBoton, casaOriginalFichaComida, null, () -> {
                    // Cuando la víctima llega a casa, movemos la ficha principal.
                    moverFichaPrincipal.run();
                });
            } else { // Si es del mismo color, es un movimiento para formar un bloqueo.
                
                // CASO ESPECIAL: Salir de casa a una casilla de salida ya ocupada por una ficha propia.
                boolean esSalidaDeCasa = idCasillaOrigen >= 101;
                boolean esCasillaDeSalidaPropia = idCasillaDestino == getCasillaDeSalida(colorFichaMovida);

                if (esSalidaDeCasa && esCasillaDeSalidaPropia) { // Si es el caso especial de salir de casa...
                    // Contamos cuántas fichas hay ya en la casilla de destino.
                    long fichasEnDestino = mapaPosicionFichas.values().stream()
                            .filter(pos -> pos.equals(idCasillaDestino))
                            .count();

                    // El menú solo aparece si hay exactamente UNA ficha en el destino.
                    if (fichasEnDestino == 1) {
                        // Mostramos un menú para que el jugador elija.
                        JPopupMenu menuOpciones = new JPopupMenu();
                        
                        JMenuItem opcionBloqueo = new JMenuItem("Hacer bloqueo");
                        opcionBloqueo.addActionListener(e -> {
                            moverFichaPrincipal.run(); // Ejecuta el movimiento para formar el bloqueo.
                        });
                        menuOpciones.add(opcionBloqueo);

                        JMenuItem opcionSeleccionar = new JMenuItem("Seleccionar ficha");
                        opcionSeleccionar.addActionListener(e -> {
                            limpiarResaltados();
                            seleccionarFicha(destinoBotonFinal);
                        });
                        menuOpciones.add(opcionSeleccionar);

                        menuOpciones.show(destinoBotonFinal, destinoBotonFinal.getWidth() / 2, destinoBotonFinal.getHeight() / 2);
                    } // Si hay 0 o 2+ fichas, no hacemos nada, el movimiento no es válido o no aplica el menú.
                } else {
                    // Si es un bloqueo en cualquier otra casilla, se forma automáticamente.
                    moverFichaPrincipal.run();
                }
            }
        } else { // Si no hay ficha en el destino (es un movimiento a casilla vacía)
            moverFichaPrincipal.run();
        }
    }
    
    /**
     * Busca el índice de una ficha a partir de su UUID.
     * Este método es un placeholder y asume que el servidor enviará el índice.
     * Una implementación más robusta podría buscarlo en una estructura de datos local.
     * @param idFicha El UUID de la ficha.
     * @return El índice de la ficha (0-3), o -1 si no se encuentra.
     */
    private int getIndiceDeFicha(UUID idFicha, Map<UUID, List<com.mycompany.parchismvc.Model.Ficha>> fichasPorJugador) {
        for (List<com.mycompany.parchismvc.Model.Ficha> fichas : fichasPorJugador.values()) {
            for (int i = 0; i < fichas.size(); i++) {
                com.mycompany.parchismvc.Model.Ficha ficha = fichas.get(i);
                if (ficha != null && ficha.id.equals(idFicha)) {
                    return i; // Encontramos la ficha, devolvemos su índice en la lista.
                }
            }
        }
        return -1; // No se encontró la ficha.
    }
    
    /**
     * Establece un botón de ficha como el seleccionado y le aplica el borde de resaltado.
     * @param botonFicha El botón a seleccionar.
     */
    private void seleccionarFicha(JButton botonFicha) {
        // Limpiamos solo los bordes de las fichas, pero no las casillas de destino.
        for (JButton boton : botonesCasillas.values()) {
            boton.setBorderPainted(false);
        }
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
        if (fichaIcon == null || fichaIcon instanceof IconoDeHueco || fichaIcon instanceof IconoCompuesto) { // Si no hay icono o es un hueco, no hay nada que mover.
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
        destino.setContentAreaFilled(false); // Siempre transparente para mostrar el tablero
        
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
     * @param path La lista de IDs de casillas a seguir. Si es null, se anima en línea recta.
     */
    private void animarFicha(JButton origen, JButton destino, List<Integer> path, Runnable onAnimationEnd) {
        final Icon fichaIcon = origen.getIcon();
        if (fichaIcon == null || fichaIcon instanceof IconoDeHueco || fichaIcon instanceof IconoCompuesto) {
            if (onAnimationEnd != null) {
                onAnimationEnd.run(); // Si no hay ficha, ejecutar el callback y salir.
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

        // Si no hay una ruta definida (o es un movimiento a casa), hacer una animación simple.
        if (path == null || path.isEmpty()) {
            animarMovimientoSimple(fichaAnimada, origen.getLocation(), destino.getLocation(), () -> {
                finalizarAnimacion(fichaAnimada, destino, fichaIcon, onAnimationEnd);
            });
        } else {
            // Si hay una ruta, animar paso por paso.
            animarRutaPasoAPaso(fichaAnimada, path, 0, onAnimationEnd);
        }
    }

    private void animarRutaPasoAPaso(JLabel fichaAnimada, List<Integer> path, int pathIndex, Runnable onAnimationEnd) {
        if (pathIndex >= path.size() - 1) {
            // Hemos llegado al final de la ruta.
            JButton destinoFinal = botonesCasillas.get(path.get(path.size() - 1));
            finalizarAnimacion(fichaAnimada, destinoFinal, (Icon) fichaAnimada.getIcon(), onAnimationEnd);
            return;
        }

        Point startPoint = botonesCasillas.get(path.get(pathIndex)).getLocation();
        Point endPoint = botonesCasillas.get(path.get(pathIndex + 1)).getLocation();

        // Aseguramos que la ficha esté en la posición de inicio del segmento actual.
        fichaAnimada.setLocation(startPoint);

        animarMovimientoSimple(fichaAnimada, startPoint, endPoint, () -> {
            // Cuando el segmento termina, llamamos recursivamente para el siguiente.
            animarRutaPasoAPaso(fichaAnimada, path, pathIndex + 1, onAnimationEnd);
        });
    }

    private void animarMovimientoSimple(JLabel fichaAnimada, Point startPoint, Point endPoint, Runnable onSegmentEnd) {
        final long startTime = System.currentTimeMillis();
        final int durationPerStep = 150; // Duración de la animación para un solo paso.

        Timer stepAnimator = new Timer(10, null);
        stepAnimator.addActionListener(ae -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double t = Math.min(1.0, (double) elapsed / durationPerStep);

            int x = (int) (startPoint.x + t * (endPoint.x - startPoint.x));
            int y_linear = (int) (startPoint.y + t * (endPoint.y - startPoint.y));

            // Efecto de salto
            int jumpHeight = 20;
            int y_jump_offset = (int) (-jumpHeight * 4 * t * (1 - t));
            int y = y_linear + y_jump_offset;

            fichaAnimada.setLocation(x, y);

            if (t >= 1.0) {
                stepAnimator.stop();
                if (onSegmentEnd != null) {
                    onSegmentEnd.run();
                }
            }
        });
        stepAnimator.start();
    }

    private void finalizarAnimacion(JLabel fichaAnimada, JButton destino, Icon fichaIcon, Runnable onAnimationEnd) {
        remove(fichaAnimada);
        destino.setIcon(fichaIcon);
        destino.setContentAreaFilled(false);
        this.setEnabled(true);
        repaint();
        if (onAnimationEnd != null) {
            onAnimationEnd.run();
        }
    }
    
    private List<Integer> calcularRuta(int idOrigen, int idDestino, ColorJugador color) {
        java.util.List<Integer> path = new java.util.ArrayList<>();
        path.add(idOrigen);

        // Si la ficha sale de casa, el origen es una casilla > 100.
        if (idOrigen > 100) {
            path.add(idDestino);
            return path;
        }

        int actual = idOrigen;
        while (actual != idDestino) {
            if (actual > 68) { // En pasillo
                actual++;
            } else if (actual == getCasillaEntradaPasillo(color)) { // Entrada a pasillo
                actual = getPrimerPasoPasillo(color);
            } else { // Tablero normal
                actual = (actual % 68) + 1;
            }
            
            // Control para evitar bucles infinitos en caso de lógica inesperada
            if (path.contains(actual)) break;
            
            path.add(actual);
        }

        return path;
    }

     /**
     * Itera sobre las casillas de destino visibles y resalta las fichas
     * enemigas que encuentre.
     */
    private void resaltarVictimas() {
        if (botonFichaSeleccionada == null) {
            return;
        }
        UUID idFichaAtacante = getFichaEnCasilla(Integer.parseInt(botonFichaSeleccionada.getActionCommand()));
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
                UUID idVictima = getFichaEnCasilla(entry.getKey());
                // Si hay una ficha en esa casilla, es de otro color Y la víctima está en el tablero...
                if (idVictima != null && getColorDeFicha(idFichaAtacante) != getColorDeFicha(idVictima) && entry.getKey() < 101) {

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
    public void limpiarResaltados() {
        // Quitamos el borde a todas las casillas.
        for (JButton boton : botonesCasillas.values()) {
            boton.setBorderPainted(false);
        }
        limpiarCasillasDeTablero(); // Ocultamos y limpiamos las casillas de destino.
        botonFichaSeleccionada = null;
    }

    /**
     * Oculta y deshabilita todas las casillas del tablero que no tienen una ficha.
     * Esto se usa para limpiar las casillas de destino resaltadas después de un movimiento.
     */
    public void limpiarCasillasDeTablero() {
        for (Map.Entry<Integer, JButton> entry : botonesCasillas.entrySet()) {
            int idCasilla = entry.getKey();
            JButton boton = entry.getValue();

            // Solo afecta a las casillas del tablero (ID < 101) que estén vacías.
            if (idCasilla < 101 && boton.isVisible()) {
                limpiarBoton(idCasilla);
            }
        }
        repaint();
    }

    public void resaltarFichasMovibles(int valorDado, ColorJugador colorDelTurno) {
        limpiarResaltados();
        fichasPorJugadorActuales.values().stream()
            .flatMap(List::stream)
            .filter(ficha -> getColorDeFicha(ficha.id) == colorDelTurno)
            .filter(ficha -> {
                // Filtramos las fichas que ya han llegado a la meta.
                if (ficha.posicion == getMeta(colorDelTurno)) {
                    return false;
                }
                return true;
            })
            .forEach(ficha -> {
                boolean puedeMover = false;
                if (ficha.posicion == -1) { // En base
                    if (valorDado == 5) puedeMover = true;
                } else { // En tablero o pasillo
                    puedeMover = true; // Asumimos que siempre puede moverse, la lógica de bloqueo está en el servidor.
                }

                if (puedeMover) {
                    int pos = ficha.posicion;
                    int idBoton = (pos == -1) ? getBotonCasaIdDeFicha(ficha.id, getIndiceDeFicha(ficha.id, fichasPorJugadorActuales), colorDelTurno) : pos;
                    JButton botonFicha = botonesCasillas.get(idBoton);
                    if (botonFicha != null) {
                        Color glowColor = switch(colorDelTurno) {
                            case ROJO -> new Color(255, 100, 100);
                            case AZUL -> new Color(100, 150, 255);
                            case VERDE -> new Color(100, 255, 150);
                            case AMARILLO -> new Color(255, 255, 100);
                        };
                        botonFicha.setBorder(BorderFactory.createLineBorder(glowColor, 3));
                        botonFicha.setBorderPainted(true);
                    }
                }
            });
        repaint();
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

    private void initComponents() {
        loadBoardFromDisk();
        setLayout(null);
        crearBotonesParaCasillas();
    }
    
    public void setControlador(com.mycompany.parchismvc.Controller.Controlador controlador) {
        this.controlador = controlador;
        if (controlador != null) this.miId = controlador.getMiId();
    }
    

    private void crearBotonesParaCasillas() {
        for (Map.Entry<Integer, Rectangle> entry : mapaCoordenadas.entrySet()) {
            int idCasilla = entry.getKey();
            Rectangle bounds = entry.getValue();

            JButton boton = new JButton();
            boton.setActionCommand(String.valueOf(idCasilla));
            boton.setBounds(bounds);

            // Se asigna el listener unificado para manejar los clics del juego.
            final ActionListener actionListener = crearActionListenerUnificado(boton);
            boton.addActionListener(actionListener);

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
                // Las casillas deben ser transparentes e invisibles por defecto.
                boton.setOpaque(false);
                boton.setContentAreaFilled(false);
                boton.setBorderPainted(false);
            }

            // --- Estado Inicial: Fichas visibles, resto oculto ---
            boton.setVisible(idCasilla >= 101); // Solo las fichas (ID >= 101) son visibles al inicio
            boton.setEnabled(idCasilla >= 101); // Solo las fichas se pueden clickear al inicio

            this.add(boton);
            botonesCasillas.put(idCasilla, boton);
        }
    }

    private ImageIcon getScaledIcon(String fileName, int targetWidth, int targetHeight) {
        try {
            File imgFile = new File("src/main/resources/Assets/" + fileName);
            if (imgFile.exists()) {
                BufferedImage originalImage = javax.imageio.ImageIO.read(imgFile);

                // Si el ancho objetivo es muy pequeño, no intentamos mantener el aspect ratio
                // y simplemente forzamos el tamaño. Esto es para los iconos dobles.
                if (targetWidth < 20) {
                    Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImage);
                }

                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();
                double aspectRatio = (double) originalWidth / originalHeight;

                int newWidth, newHeight;
                
                // Si la casilla es más alta que ancha (horizontal en el tablero),
                // escalamos basándonos en el ancho para mantener la proporción.
                if (targetHeight > targetWidth) {
                    newWidth = targetWidth;
                    newHeight = (int) (newWidth / aspectRatio);
                } else { // Para casillas verticales o cuadradas, escalamos por altura.
                    newHeight = targetHeight;
                    newWidth = (int) (newHeight * aspectRatio);
                    if (newWidth > targetWidth) { // Si se pasa de ancho, reajustamos por ancho.
                        newWidth = targetWidth;
                        newHeight = (int) (newWidth / aspectRatio);
                    }
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
