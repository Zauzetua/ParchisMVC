/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Repo;

import com.mycompany.parchismvc.Model.Sala;

/**
 *
 * @author jesus
 */
public class RepositorioSalaMemoria implements IRepositorioSala {
    private final Sala sala = new Sala();
    @Override
    public Sala obtenerSala() { return sala; }
}
