/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.parchismvc.View;

import com.mycompany.parchismvc.Controller.Controlador;
import com.mycompany.parchismvc.Model.Jugador;
import java.util.UUID;

public class Sala extends javax.swing.JFrame {
    private Controlador controlador;
    private javax.swing.JTextArea jugadoresTA;
    private javax.swing.JScrollPane scrollJugadores;
    private javax.swing.JButton btnSalir;
    private javax.swing.JLabel lblEstado;

    public Sala(){
        initComponents();
    }

    public Sala(Controlador controlador, UUID jugadorId){
        this();
        this.controlador = controlador;
        configurarPostRegistro();
    }

    private void configurarPostRegistro(){
        if(controlador!=null){
            controlador.setEvents(new GameEvents(){
                @Override public void onConectado(String host,int puerto,String salaId){}
                @Override public void onRegistrado(UUID jugadorId){}
                @Override public void onEstado(com.mycompany.parchismvc.Model.Sala sala, UUID turnoDe, UUID yo){ actualizarJugadores(sala, turnoDe, yo); }
                @Override public void onCuentaAtras(int segundos, com.mycompany.parchismvc.Model.Sala sala){ lblEstado.setText("Cuenta atras: "+segundos); }
                @Override public void onResultado(boolean ok,String mensaje){ lblEstado.setText(mensaje); }
                @Override public void onDado(UUID jugadorId,int valor){}
                @Override public void onError(String razon){ lblEstado.setText("Error: "+razon); }
            });
        }
    }

    private void actualizarJugadores(com.mycompany.parchismvc.Model.Sala sala, UUID turnoDe, UUID yo){
        if(sala==null) return;
        StringBuilder sb = new StringBuilder();
        for(Jugador j: sala.jugadores){
            sb.append(j.nombre);
            if(j.id.equals(yo)) sb.append(" (yo)");
            if(turnoDe!=null && j.id.equals(turnoDe)) sb.append(" <-- turno");
            if(j.listo) sb.append(" [LISTO]");
            sb.append('\n');
        }
        jugadoresTA.setText(sb.toString());
        lblEstado.setText("Estado: "+sala.estado);
    }

    private void initComponents(){
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(21,79,102));
        setSize(800,500);
        jugadoresTA = new javax.swing.JTextArea();
        jugadoresTA.setEditable(false);
        jugadoresTA.setFont(new java.awt.Font("Monospaced",0,14));
        scrollJugadores = new javax.swing.JScrollPane(jugadoresTA);
        btnSalir = new javax.swing.JButton("Salir");
        lblEstado = new javax.swing.JLabel("Lobby");
        lblEstado.setForeground(java.awt.Color.WHITE);
        getContentPane().setLayout(new java.awt.BorderLayout(8,8));
        javax.swing.JPanel top = new javax.swing.JPanel(new java.awt.BorderLayout());
        top.setBackground(new java.awt.Color(21,79,102));
        top.add(lblEstado, java.awt.BorderLayout.WEST);
        getContentPane().add(top, java.awt.BorderLayout.NORTH);
        getContentPane().add(scrollJugadores, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel south = new javax.swing.JPanel();
        south.setBackground(new java.awt.Color(21,79,102));
        south.add(btnSalir);
        getContentPane().add(south, java.awt.BorderLayout.SOUTH);
        btnSalir.addActionListener(e->{ if(controlador!=null) controlador.desconectar(); dispose(); });
        pack();
    }
}
