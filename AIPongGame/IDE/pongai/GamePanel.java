package pongai;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.sound.sampled.*;

public class GamePanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDLE_WIDTH = 20;
    private static final int PADDLE_HEIGHT = 100;
    private static final int BALL_SIZE = 20;
    private static final int DELAY = 10; // ms

    private Timer timer;
    private int ballX, ballY;
    private int ballVelX = 9, ballVelY = 7; // szybsza startowa prędkość
    private int leftPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int rightPaddleY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int leftScore = 0;
    private int rightScore = 0;

    private Clip paddleClip;
    private Clip scoreClip;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        loadSounds();
        resetBall();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    private void loadSounds() {
        try {
            File paddleFile = new File("AIPongSounds/paddlebounce.wav");
            File scoreFile = new File("AIPongSounds/score.wav");

            AudioInputStream paddleStream = AudioSystem.getAudioInputStream(paddleFile);
            paddleClip = AudioSystem.getClip();
            paddleClip.open(paddleStream);

            AudioInputStream scoreStream = AudioSystem.getAudioInputStream(scoreFile);
            scoreClip = AudioSystem.getClip();
            scoreClip.open(scoreStream);
        } catch (Exception e) {
            System.err.println("Błąd ładowania dźwięków: " + e.getMessage());
        }
    }

    private void playSound(Clip clip) {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    private void resetBall() {
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2 - BALL_SIZE / 2;
        // Losowy kierunek i nieco większa prędkość bazowa
        ballVelX = (Math.random() > 0.5 ? 9 : -9);
        ballVelY = (int)(Math.random() * 10 - 5); // od -5 do 4
        if (ballVelY == 0) ballVelY = 6;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        ballX += ballVelX;
        ballY += ballVelY;

        // Płynniejsze AI – reaguje tylko gdy różnica jest większa niż 15 pikseli
        final int tolerance = 15;
        final int paddleSpeed = 9; // szybsze paletki

        // Lewa paletka (AI)
        if (ballVelX < 0) { // piłka leci w lewo
            int paddleCenter = leftPaddleY + PADDLE_HEIGHT / 2;
            if (ballY < paddleCenter - tolerance) {
                leftPaddleY -= paddleSpeed;
            } else if (ballY > paddleCenter + tolerance) {
                leftPaddleY += paddleSpeed;
            }
        }

        // Prawa paletka (AI)
        if (ballVelX > 0) { // piłka leci w prawo
            int paddleCenter = rightPaddleY + PADDLE_HEIGHT / 2;
            if (ballY < paddleCenter - tolerance) {
                rightPaddleY -= paddleSpeed;
            } else if (ballY > paddleCenter + tolerance) {
                rightPaddleY += paddleSpeed;
            }
        }

        // Odbicie od góry/dół
        if (ballY <= 0 || ballY >= HEIGHT - BALL_SIZE) {
            ballVelY = -ballVelY;
        }

        // Kolizja z lewą paletką
        if (ballX <= 40 + PADDLE_WIDTH && ballX + BALL_SIZE >= 40 &&
            ballY + BALL_SIZE >= leftPaddleY && ballY <= leftPaddleY + PADDLE_HEIGHT) {
            if (ballVelX < 0) { // upewniamy się, że odbija tylko od przodu
                ballVelX = Math.abs(ballVelX) + 1; // lekkie przyspieszenie
                ballVelY += (Math.random() * 4 - 2);
                playSound(paddleClip);
            }
        }

        // Kolizja z prawą paletką
        if (ballX + BALL_SIZE >= WIDTH - 60 && ballX <= WIDTH - 60 + PADDLE_WIDTH &&
            ballY + BALL_SIZE >= rightPaddleY && ballY <= rightPaddleY + PADDLE_HEIGHT) {
            if (ballVelX > 0) {
                ballVelX = -Math.abs(ballVelX) - 1; // lekkie przyspieszenie
                ballVelY += (Math.random() * 4 - 2);
                playSound(paddleClip);
            }
        }

        // Limit maksymalnej prędkości (żeby nie było za szybko)
        ballVelX = Math.max(-20, Math.min(20, ballVelX));
        ballVelY = Math.max(-15, Math.min(15, ballVelY));

        // Punktacja
        if (ballX <= 0) {
            rightScore++;
            playSound(scoreClip);
            resetBall();
        }
        if (ballX >= WIDTH - BALL_SIZE) {
            leftScore++;
            playSound(scoreClip);
            resetBall();
        }

        // Ograniczenie paletek
        leftPaddleY = Math.max(0, Math.min(HEIGHT - PADDLE_HEIGHT, leftPaddleY));
        rightPaddleY = Math.max(0, Math.min(HEIGHT - PADDLE_HEIGHT, rightPaddleY));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);

        // Paletki
        g.fillRect(40, leftPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect(WIDTH - 60, rightPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Piłka
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Linia środkowa
        g.setColor(Color.GRAY);
        for (int i = 0; i < HEIGHT; i += 20) {
            g.fillRect(WIDTH / 2 - 2, i, 4, 10);
        }

        // Wynik
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString(String.valueOf(leftScore), WIDTH / 4, 80);
        g.drawString(String.valueOf(rightScore), 3 * WIDTH / 4, 80);
    }
}