/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Repo;

import com.mycompany.parchismvc.Model.Sala;

/**
 *
 * @author Equipo 1 Parchis
 * Clase que implementa el repositorio de la sala en memoria.
 */
public class RepositorioSalaMemoria implements IRepositorioSala {
    /**
     * Sala en memoria.
     */
    private final Sala sala = new Sala();
    /**
     * Metodo que obtiene la sala en memoria.
     * @return Sala en memoria.
     */
    @Override
    public Sala obtenerSala() { return sala; }
}
