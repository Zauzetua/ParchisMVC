/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.Util;

import java.util.Random;

/**
 *
 * @author jesus
 */
public class DadoAleatorio implements Dado {
    private final Random rng = new Random();
    @Override
    public int tirar() { return rng.nextInt(6) + 1; }
}