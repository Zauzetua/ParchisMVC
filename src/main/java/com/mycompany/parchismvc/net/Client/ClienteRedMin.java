/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.net.Client;

import com.mycompany.parchismvc.net.dto.Mensaje;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Cliente de red mínimo:
 * - conecta al servidor (host/puerto)
 * - envía Mensaje
 * - recibe Mensaje y dispara callback
 */
public class ClienteRedMin implements Closeable {
    private final String host;
    private final int puerto;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread lector;
    private Consumer<Mensaje> onMensaje = m -> {};

    public ClienteRedMin(String host, int puerto){
        this.host = host; this.puerto = puerto;
    }

    public void onMensaje(Consumer<Mensaje> handler){ this.onMensaje = handler; }

    public void conectar() throws IOException {
        socket = new Socket(host, puerto);
        out = new ObjectOutputStream(socket.getOutputStream());
        in  = new ObjectInputStream(socket.getInputStream());
        lector = new Thread(this::bucleLectura, "lector-red");
        lector.start();
    }

    private void bucleLectura(){
        try {
            while (!Thread.currentThread().isInterrupted()){
                Object o = in.readObject();
                if (o instanceof Mensaje m) onMensaje.accept(m);
            }
        } catch (Exception ignored) { /* socket cerrado */ }
    }

    public synchronized void enviar(Mensaje m) throws IOException {
        out.writeObject(m); out.flush();
    }

    @Override public void close() throws IOException {
        if (lector != null) lector.interrupt();
        if (socket != null) socket.close();
    }
}