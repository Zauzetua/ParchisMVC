/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Util;

import java.util.Random;

/**
 *
 * @author Equipo 1 Parchis
 */
public class DadoAleatorio implements Dado {
    /**
     * Generador de numeros aleatorios
     */
    private final Random rng = new Random();
    /**
     * Metodo que simula el lanzamiento de un dado
     */
    @Override
    public int tirar() { return rng.nextInt(6) + 1; }
}