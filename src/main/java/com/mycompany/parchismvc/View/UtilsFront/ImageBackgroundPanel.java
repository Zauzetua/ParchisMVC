/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.View.UtilsFront;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ImageBackgroundPanel extends JPanel {
    private BufferedImage img;
    private boolean preserveAspect = true;

    public ImageBackgroundPanel(String resourcePath) {
        setOpaque(false);
        try {
            img = ImageIO.read(getClass().getResource(resourcePath));
        } catch (Exception e) {
            System.err.println("No se pudo cargar: " + resourcePath + " -> " + e.getMessage());
        }
    }

    public void setPreserveAspect(boolean preserve) {
        this.preserveAspect = preserve;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img == null) return;

        int w = getWidth(), h = getHeight();
        if (!preserveAspect) {
            g.drawImage(img, 0, 0, w, h, this);
            return;
        }
        double rImg = (double) img.getWidth() / img.getHeight();
        double rPan = (double) w / h;
        int dw, dh;
        if (rImg > rPan) { dw = w; dh = (int) (w / rImg); }
        else { dh = h; dw = (int) (h * rImg); }
        int x = (w - dw) / 2, y = (h - dh) / 2;
        g.drawImage(img, x, y, dw, dh, this);
    }
}