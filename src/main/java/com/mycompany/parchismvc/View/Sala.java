/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.parchismvc.View;

import com.mycompany.parchismvc.Controller.Controlador;
import com.mycompany.parchismvc.Model.Jugador;
import com.mycompany.parchismvc.Model.ColorJugador;
import java.util.UUID;
import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class Sala extends JFrame implements GameEvents {
    private Controlador controlador;
    private UUID miId;
    private JLabel lblEstado;
    private JButton btnReady, btnSalir, btnTimerLeft, btnTimerRight;
    private GameEvents gameEvents;
    private JLabel lblTimerValue;
    private JPanel panelColors;
    private Map<ColorJugador, ColorButton> colorButtons = new EnumMap<>(ColorJugador.class);
    private PlayerSlotPanel[] playerSlots = new PlayerSlotPanel[4];
    private final int[] TIMER_OPTIONS = {30,15};
    private int timerIndex = 0;

    public Sala(){ buildUI(); }
    public Sala(Controlador controlador, UUID miId){
        this.controlador = controlador; this.miId = miId; buildUI(); configurarPostRegistro();
    }

    private void configurarPostRegistro(){
        if(controlador!=null){
            controlador.setEvents(this);
        }
    }

    private void buildUI(){
        setTitle("Sala");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBackground(new Color(8,55,70));
        getContentPane().setLayout(new BorderLayout());
        lblEstado = new JLabel("Lobby"); lblEstado.setForeground(Color.WHITE);

        JPanel center = new JPanel(); center.setBackground(new Color(12,60,80)); center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS)); center.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(lblEstado); center.add(Box.createVerticalStrut(15));

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,15,5)); timerPanel.setBackground(new Color(20,70,90));
        btnTimerLeft = new JButton("<"); btnTimerRight = new JButton(">"); lblTimerValue = new JLabel(TIMER_OPTIONS[timerIndex]+" SG"); lblTimerValue.setForeground(Color.WHITE);
        JLabel lblTimer = new JLabel("TIMER"); lblTimer.setForeground(Color.WHITE);
        timerPanel.add(btnTimerLeft); timerPanel.add(lblTimer); timerPanel.add(lblTimerValue); timerPanel.add(btnTimerRight);
        center.add(timerPanel); center.add(Box.createVerticalStrut(20));

        panelColors = new JPanel(new GridLayout(2,2,15,15)); panelColors.setBackground(new Color(20,70,90));
        for(ColorJugador cj: ColorJugador.values()){
            ColorButton b = new ColorButton(cj); colorButtons.put(cj,b); panelColors.add(b); b.addActionListener(e -> elegirColor(cj));
        }
        center.add(panelColors); center.add(Box.createVerticalStrut(20));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER,25,10)); actions.setBackground(new Color(12,60,80));
        btnReady = new JButton("Ready"); btnSalir = new JButton("Salir"); actions.add(btnReady); actions.add(btnSalir);
        center.add(actions);

        getContentPane().add(center, BorderLayout.CENTER);

        JPanel top = new JPanel(new GridLayout(1,2)); top.setBackground(new Color(8,55,70));
        JPanel bottom = new JPanel(new GridLayout(1,2)); bottom.setBackground(new Color(8,55,70));
        playerSlots[0] = new PlayerSlotPanel(); playerSlots[1] = new PlayerSlotPanel(); playerSlots[2] = new PlayerSlotPanel(); playerSlots[3] = new PlayerSlotPanel();
        top.add(playerSlots[0]); top.add(playerSlots[1]); bottom.add(playerSlots[2]); bottom.add(playerSlots[3]);
        getContentPane().add(top, BorderLayout.NORTH); getContentPane().add(bottom, BorderLayout.SOUTH);

        btnTimerLeft.addActionListener(e -> cambiarTimer(-1)); btnTimerRight.addActionListener(e -> cambiarTimer(1));
        btnReady.addActionListener(e -> toggleReady()); btnSalir.addActionListener(e -> { if(controlador!=null) controlador.desconectar(); dispose(); });

        pack(); setSize(1000,600); setLocationRelativeTo(null);
    }

    private void cambiarTimer(int delta){ timerIndex = (timerIndex + delta + TIMER_OPTIONS.length) % TIMER_OPTIONS.length; int nuevo = TIMER_OPTIONS[timerIndex]; lblTimerValue.setText(nuevo+" SG"); if(controlador!=null) controlador.setTiempo(nuevo); }
    private void elegirColor(ColorJugador c){ if(controlador==null || controlador.getMiId()==null) return; var msg = controlador.elegirColor(controlador.getMiId(), c); lblEstado.setText(msg); }
    private void toggleReady(){ if(controlador==null || controlador.getMiId()==null) return; var sala = controlador.getSalaCache(); if(sala==null) return; Jugador yoJugador = sala.jugadores.stream().filter(j -> j.id.equals(controlador.getMiId())).findFirst().orElse(null); if(yoJugador==null) return; String r = yoJugador.listo? controlador.cancelar(controlador.getMiId()) : controlador.listo(controlador.getMiId()); lblEstado.setText(r); }

    private void actualizarJugadores(com.mycompany.parchismvc.Model.Sala sala, UUID turnoDe, UUID yo){
        if(sala==null) return; int tiempo = sala.tiempoPorTurno; if(tiempo==30) timerIndex=0; else if(tiempo==15) timerIndex=1; lblTimerValue.setText(tiempo+" SG");
        
        // Si el estado de la sala cambia a JUGANDO, abrimos la ventana del juego.
        if (sala.estado == com.mycompany.parchismvc.Model.EstadoSala.JUGANDO) {
            Juego juegoFrame = new Juego(controlador, miId);
            controlador.setEvents(juegoFrame); // ¡IMPORTANTE! Transferir el control de eventos a la nueva ventana.
            juegoFrame.actualizarJugadores(sala.jugadores, turnoDe, miId); // Pasamos la lista de jugadores, el turno y el ID local
            juegoFrame.setVisible(true);
            this.dispose(); // Cerramos la ventana de la sala
            return; // Salimos del método para no actualizar la UI de la sala que ya se cerró
        }
        
        for(var cb: colorButtons.values()) cb.setTaken(false);
        for(Jugador j: sala.jugadores){ if(j.color!=null){ var btn = colorButtons.get(j.color); if(btn!=null) btn.setTaken(true); } }
        for(int i=0;i<playerSlots.length;i++){ PlayerSlotPanel slot = playerSlots[i]; if(i < sala.jugadores.size()){ Jugador j = sala.jugadores.get(i); slot.update(j, j.id.equals(yo), turnoDe!=null && j.id.equals(turnoDe)); } else { slot.clear(); } }
        Jugador yoJugador = sala.jugadores.stream().filter(j -> j.id.equals(yo)).findFirst().orElse(null); if(yoJugador!=null) btnReady.setText(yoJugador.listo?"Cancelar":"Ready");
        lblEstado.setText("Estado: "+sala.estado); boolean lobby = sala.estado == com.mycompany.parchismvc.Model.EstadoSala.ESPERANDO; setLobbyControlsEnabled(lobby);
    }
    private void setLobbyControlsEnabled(boolean enabled){ btnTimerLeft.setEnabled(enabled); btnTimerRight.setEnabled(enabled); for(var cb: colorButtons.values()) cb.setEnabled(enabled && !cb.isTaken()); btnReady.setEnabled(enabled); }
    
    public GameEvents getGameEvents() {
        return this;
    }

    @Override
    public void onConectado(String host, int puerto, String salaId) {}

    @Override
    public void onRegistrado(UUID jugadorId) {}

    @Override
    public void onEstado(com.mycompany.parchismvc.Model.Sala sala, UUID turnoDe, UUID yo) {
        SwingUtilities.invokeLater(() -> actualizarJugadores(sala, turnoDe, yo));
    }

    @Override
    public void onCuentaAtras(int segundos, com.mycompany.parchismvc.Model.Sala sala) {
        SwingUtilities.invokeLater(() -> lblEstado.setText("Cuenta atras: " + segundos));
    }

    @Override
    public void onResultado(boolean ok, String mensaje) {
        SwingUtilities.invokeLater(() -> lblEstado.setText(mensaje));
    }

    @Override
    public void onDado(UUID jugadorId, int valor) {}

    @Override
    public void onError(String razon) {
        SwingUtilities.invokeLater(() -> lblEstado.setText("Error: " + razon));
    }

    // === Inner components ===
    private static class PlayerSlotPanel extends JPanel {
        private JLabel lblNombre = new JLabel("JUGADOR"); private JLabel lblAvatar = new JLabel(); private JLabel lblEstadoJugador = new JLabel("No Listo"); private JLabel lblTurno = new JLabel();
        PlayerSlotPanel(){ setOpaque(false); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); lblNombre.setForeground(Color.WHITE); lblEstadoJugador.setForeground(Color.RED); lblTurno.setForeground(Color.YELLOW); lblAvatar.setPreferredSize(new Dimension(64,64)); lblAvatar.setOpaque(true); lblAvatar.setBackground(Color.WHITE); lblAvatar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY,2)); add(lblNombre); add(Box.createVerticalStrut(5)); add(lblAvatar); add(Box.createVerticalStrut(5)); add(lblEstadoJugador); add(lblTurno); }
        void update(Jugador j, boolean esYo, boolean esTurno){ lblNombre.setText(j.nombre); lblEstadoJugador.setText(j.listo?"Listo":"No Listo"); lblEstadoJugador.setForeground(j.listo?new Color(120,255,120):Color.RED); lblTurno.setText(esTurno?"Turno":""); if(j.avatar!=null){ try { var imgUrl = PlayerSlotPanel.class.getResource(j.avatar); if(imgUrl!=null){ var icon = new ImageIcon(new ImageIcon(imgUrl).getImage().getScaledInstance(64,64,Image.SCALE_SMOOTH)); lblAvatar.setIcon(icon); } } catch(Exception ignored) {} } lblAvatar.setBorder(BorderFactory.createLineBorder(esYo?Color.GREEN:Color.DARK_GRAY, esYo?3:2)); }
        void clear(){ lblNombre.setText("Libre"); lblEstadoJugador.setText(""); lblTurno.setText(""); lblAvatar.setIcon(null); lblAvatar.setBackground(Color.WHITE); lblAvatar.setBorder(BorderFactory.createLineBorder(Color.GRAY,1)); }
    }
    private static class ColorButton extends JButton { private final ColorJugador colorJugador; private boolean taken; ColorButton(ColorJugador c){ super(c.name()); this.colorJugador=c; setPreferredSize(new Dimension(100,70)); setFocusPainted(false); setBackground(swingColor(c)); setForeground(Color.BLACK); }
        static Color swingColor(ColorJugador c){ return switch(c){ case ROJO -> Color.RED; case AZUL -> new Color(40,90,180); case VERDE -> new Color(40,170,120); case AMARILLO -> Color.YELLOW; }; }
        void setTaken(boolean t){ this.taken=t; setEnabled(!t); repaint(); }
        boolean isTaken(){ return taken; }
        @Override protected void paintComponent(Graphics g){ super.paintComponent(g); if(taken){ g.setColor(new Color(180,0,0,160)); g.fillRect(0,0,getWidth(),getHeight()); g.setColor(Color.WHITE); g.drawLine(8,8,getWidth()-8,getHeight()-8); g.drawLine(getWidth()-8,8,8,getHeight()-8);} }
    }
}
