/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.parchismvc.View;

import com.mycompany.parchismvc.View.UtilsFront.AvatarButton;
import com.mycompany.parchismvc.View.UtilsFront.CircularImageLabel;
// import com.mycompany.parchismvc.View.UtilsFront.ImageBackgroundPanel; // Ya no se usa directamente en esta clase
import java.awt.GridLayout;
import javax.swing.ButtonGroup;

/**
 *
 * @author galle
 */
public class RegistroJF extends javax.swing.JFrame {
    
    private final ButtonGroup grupoAvatares = new ButtonGroup();
    private String avatarSeleccionado = null;
    private CircularImageLabel previewAvatar;
    
    public RegistroJF() {
        // Crear el preview ANTES de initComponents, porque el GroupLayout lo usa dentro
        previewAvatar = new CircularImageLabel(140);
        initComponents();
        configurarAvatares();
    }
  
    // Método ya no requerido para crear el preview; se deja por si quieres ajustar propiedades luego
    private void configurarPreview(){
        // previewAvatar ya está creado en el constructor antes de initComponents()
    }
  private void configurarAvatares() {
        // jPanel1 es el panel “Avatares” (contenedor en tu diseño)
    AvataresPanel.removeAll();
    AvataresPanel.setOpaque(false);
    AvataresPanel.setLayout(new GridLayout(2, 2, 20, 20)); // 4 avatares, espacio 20px

        // Define opciones (id que guardarás y ruta del recurso)
        // NOTA: Los archivos reales en resources son image_1.png ... image_4.png (sin 'n').
        // Se usan ids lógicos (ratita, perrito, mapache, gatito) que podrás mapear en el servidor.
        Object[][] AVS = new Object[][]{
            { "ratita",  "/Assets/image_1.png"  },
            { "perrito", "/Assets/image_2.png"  },
            { "mapache", "/Assets/image_3.png"  },
            { "gatito",  "/Assets/image_4.png"  },
        };

        for (Object[] av : AVS) {
            String id = (String) av[0];
            String path = (String) av[1];

            AvatarButton b = new AvatarButton(id, path, 96);
            b.addActionListener(e -> {
                avatarSeleccionado = e.getActionCommand();
                previewAvatar.setResourcePath(path);
            });
            grupoAvatares.add(b);
            AvataresPanel.add(b);
        }

        // Selección por defecto
        if (grupoAvatares.getElements().hasMoreElements()) {
            var it = grupoAvatares.getElements();
            if(it.hasMoreElements()){
                var first = it.nextElement();
                first.setSelected(true);
                avatarSeleccionado = first.getActionCommand();
                // path del primero
                previewAvatar.setResourcePath((String) AVS[0][1]);
            }
        }

        // (Preview ya se coloca en UnirsePanel, no dentro de AvataresPanel)
        AvataresPanel.revalidate();
        AvataresPanel.repaint();
        
    }

    // Llama a esto cuando presiones “JUGAR” para saber qué avatar enviar al servidor
    public String getAvatarSeleccionado() {
        return avatarSeleccionado; // ej. "shoco"
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        FondoPanel = new com.mycompany.parchismvc.View.UtilsFront.FondoBGPanel();
        RegistroPanel = new com.mycompany.parchismvc.View.UtilsFront.RegistroBGPanel();
        jLabel1 = new javax.swing.JLabel();
        UnirsePanel = new com.mycompany.parchismvc.View.UtilsFront.UnirseBGPanel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        AvataresPanel = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        FondoPanel.setBackground(new java.awt.Color(0, 153, 153));

        RegistroPanel.setRequestFocusEnabled(false);

        jLabel1.setFont(new java.awt.Font("Showcard Gothic", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Como Jugar");

        UnirsePanel.setBackground(new java.awt.Color(0, 102, 102));

        jButton1.setText("Unirse");

        javax.swing.GroupLayout UnirsePanelLayout = new javax.swing.GroupLayout(UnirsePanel);
        UnirsePanel.setLayout(UnirsePanelLayout);
        UnirsePanelLayout.setHorizontalGroup(
            UnirsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
            .addGroup(UnirsePanelLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(UnirsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(previewAvatar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        UnirsePanelLayout.setVerticalGroup(
            UnirsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UnirsePanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(previewAvatar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Showcard Gothic", 0, 48)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("PARCHIS");

        javax.swing.GroupLayout AvataresPanelLayout = new javax.swing.GroupLayout(AvataresPanel);
        AvataresPanel.setLayout(AvataresPanelLayout);
        AvataresPanelLayout.setHorizontalGroup(
            AvataresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 358, Short.MAX_VALUE)
        );
        AvataresPanelLayout.setVerticalGroup(
            AvataresPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 346, Short.MAX_VALUE)
        );

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/parchis_icon_1.png"))); // NOI18N

        javax.swing.GroupLayout RegistroPanelLayout = new javax.swing.GroupLayout(RegistroPanel);
        RegistroPanel.setLayout(RegistroPanelLayout);
        RegistroPanelLayout.setHorizontalGroup(
            RegistroPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RegistroPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(108, 108, 108)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(37, 37, 37))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RegistroPanelLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(AvataresPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addComponent(UnirsePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(74, 74, 74))
        );
        RegistroPanelLayout.setVerticalGroup(
            RegistroPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RegistroPanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(RegistroPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addGroup(RegistroPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jLabel1)))
                .addGap(18, 18, 18)
                .addGroup(RegistroPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(UnirsePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AvataresPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(84, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout FondoPanelLayout = new javax.swing.GroupLayout(FondoPanel);
        FondoPanel.setLayout(FondoPanelLayout);
        FondoPanelLayout.setHorizontalGroup(
            FondoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FondoPanelLayout.createSequentialGroup()
                .addContainerGap(187, Short.MAX_VALUE)
                .addComponent(RegistroPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(184, 184, 184))
        );
        FondoPanelLayout.setVerticalGroup(
            FondoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FondoPanelLayout.createSequentialGroup()
                .addContainerGap(76, Short.MAX_VALUE)
                .addComponent(RegistroPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FondoPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FondoPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RegistroJF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RegistroJF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RegistroJF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RegistroJF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RegistroJF().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AvataresPanel;
    private javax.swing.JPanel FondoPanel;
    private javax.swing.JPanel RegistroPanel;
    private javax.swing.JPanel UnirsePanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
