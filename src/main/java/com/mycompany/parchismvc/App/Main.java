/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.App;

import com.mycompany.parchismvc.Controller.Controlador;
import com.mycompany.parchismvc.Repo.IRepositorioSala;
import com.mycompany.parchismvc.Repo.RepositorioSalaMemoria;
import com.mycompany.parchismvc.Service.ServicioJuego;
import com.mycompany.parchismvc.Util.Dado;
import com.mycompany.parchismvc.Util.DadoAleatorio;
import com.mycompany.parchismvc.View.Vista;

/**
 *
 * @author jesus
 */
public class Main {
    public static void main(String[] args) {
        IRepositorioSala repo = new RepositorioSalaMemoria();
        Dado dado = new DadoAleatorio();
        ServicioJuego servicio = new ServicioJuego(repo, dado);
        Controlador controlador = new Controlador(servicio);
        Vista vista = new Vista(controlador);

        System.out.println("PARCHÍS - Demo MVC consola (reglas simplificadas)");
        System.out.println("Comienza registrando jugadores con: registro <nombre> [avatar]");
        System.out.println("Recomendado: crear al menos 2 jugadores y que elijan color antes de 'listo'.\n");

        vista.iniciar();
    }
}
