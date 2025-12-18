package pongai;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ping Pong AI - Samograjek");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);

            frame.pack();
            frame.setLocationRelativeTo(null); // wyśrodkowanie okna
            frame.setVisible(true);
        });
    }
}