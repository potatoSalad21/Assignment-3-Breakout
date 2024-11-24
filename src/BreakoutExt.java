import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.graphics.GImage;
import acm.program.GraphicsProgram;
import acm.util.RandomGenerator;
import acm.util.MediaTools;

import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.MouseEvent;

public class BreakoutExt extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;


/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 1;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;

    // SET THE APPROPRIATE PATH FOR ASSETS
    private static final String ASSET_PATH = "/home/dato/assignments/assignment3/Assignment3/assets/";

    private static final int DELAY = 5;
    private static final int SCORE_MULTIPLIER = 30;
    private static final int INFO_FONT_SIZE = 40;

    private static final int HEART_WIDTH = 30;
    private static final int HEART_HEIGHT = 30;
	private static final int STATS_Y_OFFSET = 10;

    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 50;

    // load audio assets
    private AudioClip bounceSfx = MediaTools.loadAudioClip(ASSET_PATH + "bounce.au");
    private AudioClip winSfx = MediaTools.loadAudioClip(ASSET_PATH + "win.wav");
    private AudioClip lossSfx = MediaTools.loadAudioClip(ASSET_PATH + "game-over.wav");
    private AudioClip missSfx = MediaTools.loadAudioClip(ASSET_PATH + "miss.wav");

    private String playerName;
    private GLabel warningLabel;
    private GLabel statLabel = new GLabel("");
    private GRect startButton;
    private GLabel startButtonLabel;
    private Boolean gameRunning = false;
    private GLabel scoreLabel;
    private int lives = NTURNS;
    private int score = 0;
    private int brickNum = NBRICK_ROWS * NBRICKS_PER_ROW;
    private GRect paddle;
    private GOval ball;

    // ball speed vector
    private double vx, vy;

    private static RandomGenerator rgen = RandomGenerator.getInstance();

    public void init() {
        addMouseListeners();
    }

    /* Method: run() */
    /** Runs the Breakout program. */
	public void run() {
        handleGameMenu();

        // wait for the user to start the game
        while (!gameRunning) {
            pause(500);
        }

        initGame();
        setSpeed();
        startGameLoop();
	}

    // game loop
    private void startGameLoop() {
        while (lives > 0 && brickNum > 0) {
            moveBall();
            checkCollisions();
            pause(DELAY);
        }

        if (brickNum < 1) {
            winSfx.play();
            showScreenMessage("You Win! :3", Color.GREEN, INFO_FONT_SIZE);
        } else {
            lossSfx.play();
            showScreenMessage("YOU DIED.", Color.RED, INFO_FONT_SIZE);
            saveScore();
        }

        gameRunning = false;
        remove(ball);
    }

    // draws the first 5 player scores in the save file (not sorted by scores)
    private void drawLeaderBoard() {
        try {
            File saveFile = new File("saveFile.txt");
            Scanner reader = new Scanner(saveFile);

            int lineNum = 0;
            String stats = "";
            while (reader.hasNextLine() && lineNum < 5) {
                String line = reader.nextLine();
                stats += line + " ";
                lineNum++;
            }

            statLabel.setLabel(stats);
            statLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
            statLabel.setColor(Color.CYAN);
            double x = (WIDTH - statLabel.getWidth()) / 2.0;
            add(statLabel, x, STATS_Y_OFFSET + statLabel.getAscent());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sort out player stats and draw start button
    private void handleGameMenu() {
        drawWarningLabel();
        drawLeaderBoard();
        getUserName();
        drawStartButton();
    }

    private void getUserName() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter your username: ");
        playerName = input.nextLine();
    }

    private void drawWarningLabel() {
        warningLabel = new GLabel("Enter your username in console.");
        warningLabel.setColor(Color.RED);
        warningLabel.setFont(new Font("Serif", Font.BOLD, 20));

        double x = (WIDTH - warningLabel.getWidth()) / 2.0;
        double y = (WIDTH - warningLabel.getAscent());
        add(warningLabel, x, y);
    }

    private void drawStartButton() {
        double buttonX = (WIDTH - BUTTON_WIDTH) / 2.0;
        double buttonY = (HEIGHT - BUTTON_HEIGHT) / 2.0;

        startButton = new GRect(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        startButton.setFilled(true);
        startButton.setColor(Color.GREEN);

        startButtonLabel = new GLabel("Play");
        startButtonLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        double textX = buttonX + BUTTON_WIDTH / 2.0 - startButtonLabel.getWidth() / 2.0;
        double textY = buttonY + BUTTON_HEIGHT / 2.0 - startButtonLabel.getAscent() / 2.0;

        add(startButton);
        add(startButtonLabel, textX, textY);
    }

    private void initGame() {
        drawBricks();
        drawPaddle();
        drawBall();
        drawHearts();
        drawScore();
    }

    private void setSpeed() {
        vx = rgen.nextDouble(1.0, 3.0);
        vy = rgen.nextDouble(1.0, 3.0);

        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }
    }

    /*
     * ~~ Methods for drawing structures ~~
     */
    // draws rows of bricks according to colors
    private void drawBricks() {
        int y = BRICK_Y_OFFSET;
        for (int i = 0; i < NBRICK_ROWS; i++) {
            drawBrickRow(i, y, getBrickColor(i));
            y += BRICK_HEIGHT + BRICK_SEP;
        }
    }

    private void drawBrickRow(int i, int startingY, Color brickColor) {
        for (int j = 0; j < NBRICKS_PER_ROW; j++) {
            int x = j * (BRICK_WIDTH + BRICK_SEP);

            GRect brick = new GRect(x, startingY, BRICK_WIDTH, BRICK_HEIGHT);
            brick.setFilled(true);
            brick.setFillColor(brickColor);
            brick.setColor(brickColor);

            add(brick);
        }
    }

    // draws the paddle in the center
    private void drawPaddle() {
        paddle = new GRect((WIDTH - PADDLE_WIDTH) / 2, HEIGHT - PADDLE_Y_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        paddle.setColor(Color.BLACK);
        add(paddle);
    }

    private void drawBall() {
        ball = new GOval(WIDTH / 2 - BALL_RADIUS, HEIGHT / 2 - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
        ball.setFilled(true);
        add(ball);
    }

    private void drawScore() {
        scoreLabel = new GLabel("Score: " + score);
        scoreLabel.setColor(Color.BLUE);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        double x = WIDTH - scoreLabel.getWidth();
        add(scoreLabel, x, STATS_Y_OFFSET + scoreLabel.getAscent());
    }

    private void drawHearts() {
        for (int i = 0; i < lives; i++) {
            double x = i * (HEART_WIDTH + BRICK_SEP);
            GImage heartImg = new GImage(ASSET_PATH + "heart.png");
            heartImg.setSize(HEART_WIDTH, HEART_HEIGHT);
            heartImg.sendToFront();

            add(heartImg, x, STATS_Y_OFFSET);
        }
    }

    /*
     *  ~~EVENT LISTENERS~~
     */
    // detect if the user clicked the start button
    public void mouseClicked(MouseEvent e) {
        if (startButton == null) return;
        if (startButton.contains(e.getX(), e.getY())) {
            gameRunning = true;
            remove(startButton);
            remove(startButtonLabel);
            remove(warningLabel);
            remove(statLabel);
        }
    }

    // moves the paddle according to mouse X location
    public void mouseMoved(MouseEvent e) {
        if (!gameRunning) return;

        double paddleX = e.getX() - PADDLE_WIDTH / 2.0;

        if (paddleX >= 0 && paddleX + PADDLE_WIDTH <= WIDTH) {
            paddle.setLocation(paddleX, paddle.getY());
        }
    }

    private void moveBall() {
        ball.move(vx, vy);

        if (ball.getX() <= 0 || ball.getX() + 2 * BALL_RADIUS >= WIDTH) {
            vx *= -1;
        }

        if (ball.getY() <= 0) {
            vy *= -1;
        }

        if (ball.getY() + 2 * BALL_RADIUS >= HEIGHT) {
            handleMiss();
        }
    }

    // checks and handles the brick collision
    private void checkCollisions() {
        double leftX = ball.getX();
        double rightX = ball.getX() + 2 * BALL_RADIUS;
        double topY = ball.getY();
        double bottomY = ball.getY() + 2 * BALL_RADIUS;

        GObject obj = getBallCollider(leftX, rightX, topY, bottomY);
        if (obj == paddle) {
            handlePaddleCollision();
        } else if (obj instanceof GRect) {   // ball collided with a brick
            bounceSfx.play();

            score += 10;
            scoreLabel.setLabel("Score: " + score);
            remove(obj);
            brickNum--;
            vy *= -1;
        }
    }

    // handles the paddle collision and adjusts ball speed
    private void handlePaddleCollision() {
        vy = -Math.abs(vy);

        double paddleCenterX = paddle.getX() - PADDLE_WIDTH / 2.0;
        vx = (ball.getX() + BALL_RADIUS - paddleCenterX) / 32;
        score += vx * SCORE_MULTIPLIER;
        scoreLabel.setLabel("Score: " + score);
    }

    // checking all corners of the ball to see what it collided with
    private GObject getBallCollider(double leftX, double rightX, double topY, double bottomY) {
        GObject obj = null;

        // top left corner
        obj = getElementAt(leftX, topY);
        if (obj != null) {
            return obj;
        }
        // top right corner
        obj = getElementAt(rightX, topY);
        if (obj != null) {
            return obj;
        }
        // bottom left corner
        obj = getElementAt(leftX, bottomY);
        if (obj != null) {
            return obj;
        }
        // bottom right corner
        obj = getElementAt(rightX, bottomY);
        if (obj != null) {
            return obj;
        }

        return obj;
    }

    // player missed the ball
    private void handleMiss() {
        lives--;
        GObject heart = getLastHeart();
        remove(heart);
        missSfx.play();

        remove(ball);
        drawBall();
    }

    // general method for displaying messages in center
    private void showScreenMessage(String text, Color color, int fontSz) {
        GLabel label = new GLabel(text);
        label.setColor(color);
        label.setFont(new Font("SansSerif", Font.BOLD, fontSz));

        double x = (WIDTH - label.getWidth()) / 2;
        double y = (WIDTH - label.getAscent()) / 2;

        add(label, x, y);
        pause(5000);
    }

    // pick the brick color according to the row
    private Color getBrickColor(int row) {
        if (row >= 8) {
            return Color.CYAN;
        } else if (row >= 6) {
            return Color.GREEN;
        } else if (row >= 4) {
            return Color.YELLOW;
        } else if (row >= 2) {
            return Color.ORANGE;
        }

        return Color.RED;
    }

    // get the current heart of the player
    private GObject getLastHeart() {
        double x = lives * (HEART_WIDTH + BRICK_SEP);
        double y = STATS_Y_OFFSET;

        return getElementAt(x, y);
    }

    // save user score in a text file after the match
    private void saveScore() {
        try {
            File newFile = new File("saveFile.txt");
            FileWriter saveFile = new FileWriter("saveFile.txt", true);
            saveFile.append(playerName + ": " + score + "\n");

            saveFile.flush();
            saveFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
