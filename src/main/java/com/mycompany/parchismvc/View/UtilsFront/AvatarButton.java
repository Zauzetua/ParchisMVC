/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchismvc.View.UtilsFront;

import javax.swing.*;
import java.awt.*;

public class AvatarButton extends JToggleButton {
    private final String avatarId;
    private final String resourcePath;

    public AvatarButton(String avatarId, String resourcePath, int sizePx) {
        this.avatarId = avatarId;
        this.resourcePath = resourcePath;
        setActionCommand(resourcePath); // Guardamos la ruta, no el ID
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOpaque(false);
        setPreferredSize(new Dimension(sizePx, sizePx));

        // Cargar el recurso de forma robusta desde el classpath
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) {
            String rp = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            url = Thread.currentThread().getContextClassLoader().getResource(rp);
        }
        if (url == null) {
            System.err.println("AvatarButton: recurso no encontrado -> " + resourcePath);
            // Fallback visual para no romper la UI
            setText("?");
        } else {
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(sizePx, sizePx, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(scaled));
        }
    }

    public String getAvatarId() {
        return avatarId;
    }
    
    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // aro de selección
        if (isSelected()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 200, 180));
            g2.setStroke(new BasicStroke(4f));
            int pad = 4;
            g2.drawOval(pad, pad, getWidth() - pad * 2, getHeight() - pad * 2);
            g2.dispose();
        }
    }
}