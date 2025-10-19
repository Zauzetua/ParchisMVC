/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.App;

import com.mycompany.parchismvc.Controller.Controlador;
import com.mycompany.parchismvc.View.Vista;

/**
 *
 * @author PC SWAN PLUS
 */
public class MainCliente {
    public static void main(String[] args) {
        // Controlador en modo RED (sin ServicioJuego local)
        Controlador ctl = new Controlador();  // <-- ctor vacío
        Vista vista = new Vista(ctl);         // tu vista de consola
        ctl.setVista(vista);                  // enlaza vista ↔ controlador

        // Conéctate al servidor de sockets (ajusta host/puerto/sala si hace falta)
        ctl.conectarRed("127.0.0.1", 5000, "sala1");

        System.out.println("PARCHIS - Cliente (sockets)");
        System.out.println("Usa: registro <nombre> [avatar]  y luego  estado\n");

        vista.iniciar();
    }
}