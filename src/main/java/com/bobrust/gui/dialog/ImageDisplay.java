package com.bobrust.gui.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageDisplay {

    private static JFrame frame;


    public static void showImage(BufferedImage image) {
        showImage(image, -1);
    }


    public static void showImage(BufferedImage image, int xPosition) {

        if (frame != null) {
            frame.dispose();
        }


        frame = new JFrame("Image Display");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(image.getWidth(), image.getHeight());


        ImageIcon icon = new ImageIcon(image);


        JLabel label = new JLabel(icon);


        frame.getContentPane().add(label);


        frame.pack();


        if (xPosition >= 0) {

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int yPosition = (screenSize.height - frame.getHeight()) / 2;
            frame.setLocation(xPosition, yPosition);
        } else {
            frame.setLocationRelativeTo(null);
        }


        frame.setVisible(true);
    }


    public static void closeImage() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
    }
}
