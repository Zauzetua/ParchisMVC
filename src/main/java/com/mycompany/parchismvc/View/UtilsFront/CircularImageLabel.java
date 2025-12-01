package com.mycompany.parchismvc.View.UtilsFront;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * JLabel que muestra una imagen recortada en círculo.
 */
public class CircularImageLabel extends JLabel {
    private BufferedImage image;
    private int diameter;

    public CircularImageLabel(int diameter) {
        this.diameter = diameter;
        setPreferredSize(new Dimension(diameter, diameter));
        setMinimumSize(new Dimension(diameter, diameter));
        setOpaque(false);
    }

    public void setResourcePath(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                // intentar sin slash inicial
                String rp = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                url = Thread.currentThread().getContextClassLoader().getResource(rp);
            }
            if (url == null) {
                System.err.println("Recurso no encontrado: " + resourcePath);
                return;
            }
            image = ImageIO.read(url);
            repaint();
        } catch (IOException e) {
            System.err.println("Error cargando recurso: " + resourcePath + " -> " + e.getMessage());
        }
    }

    public void setDiameter(int diameter) {
        this.diameter = diameter;
        setPreferredSize(new Dimension(diameter, diameter));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;

        int size = Math.min(getWidth(), getHeight());
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape circle = new Ellipse2D.Double(x, y, size, size);
        g2.setClip(circle);

        // Escalar la imagen para cubrir el círculo totalmente (cover)
        double iw = image.getWidth();
        double ih = image.getHeight();
        double s = Math.max(size / iw, size / ih);
        int drawW = (int) Math.round(iw * s);
        int drawH = (int) Math.round(ih * s);
        int dx = x + (size - drawW) / 2;
        int dy = y + (size - drawH) / 2;
        g2.drawImage(image, dx, dy, drawW, drawH, null);

        g2.setClip(null);
        // Borde
        g2.setColor(new Color(255, 255, 255));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(circle);

        g2.dispose();
    }
}
