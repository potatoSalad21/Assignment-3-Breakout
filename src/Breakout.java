/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 *
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

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
	private static final int NBRICK_ROWS = 10;

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

    private static final int DELAY = 5;
    private int lives = NTURNS;
    private int brickNum = NBRICK_ROWS * NBRICKS_PER_ROW;

    private GRect paddle;
    private GOval ball;

    // ball speed vector
    private double vx, vy;

    private static RandomGenerator rgen = RandomGenerator.getInstance();

/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
        // game loop
        while (true) {
            moveBall();
            pause(DELAY);
            checkCollisions();
        }
	}

    public void init() {
        vx = rgen.nextDouble(1.0, 3.0);
        vy = rgen.nextDouble(1.0, 3.0);

        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }

        drawBricks();
        drawPaddle();
        drawBall();

        addMouseListeners();
    }

    // draws several rows of bricks according to colors
    private void drawBricks() {
        // draw 10 rows and 10 columns
        for (int i = 0; i < NBRICK_ROWS; i++) {
            drawBrickRow(i, getBrickColor(i));
        }
    }

    private void drawBrickRow(int i, Color brickColor) {
        for (int j = 0; j < NBRICKS_PER_ROW; j++) {
            int x = BRICK_Y_OFFSET + j * (BRICK_WIDTH + BRICK_SEP);
            int y = i * (BRICK_HEIGHT + BRICK_SEP);

            GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
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

    // moves the paddle according to mouse X location
    public void mouseMoved(MouseEvent e) {
        double paddleX = e.getX() - PADDLE_WIDTH / 2.0;

        if (paddleX >= 0 && paddleX + PADDLE_WIDTH <= WIDTH) {
            paddle.setLocation(e.getX(), paddle.getY());
        }
    }

    private void drawBall() {
        ball = new GOval(WIDTH / 2 - BALL_RADIUS, HEIGHT / 2 - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
        ball.setFilled(true);
        add(ball);
    }

    private void moveBall() {
        ball.move(vx, vy);

        if (ball.getX() <= 0 || ball.getX() >= WIDTH) {
            vx *= -1;
        }

        if (ball.getY() <= 0) {
            vy *= -1;
        }

        if (ball.getY() >= HEIGHT) {
            handleLoss(); // | |i || |-
        }
    }

    // checks and handles the brick collision
    private void checkCollisions() {
        GObject obj = getCollidedObject(ball.getX(), ball.getY());
        if (obj == null) {
            return;
        }

        remove(obj);
        brickNum--;
        vy *= -1;
    }

    private GObject getCollidedObject(double x, double y) {
        GObject obj = getElementAt(x, y);

        return obj;
    }

    // players missed the ball
    private void handleLoss() {
        lives--;
        System.out.println(lives);
        if (lives < 1) {
            handleGameLoss(); // reset ball, life count, paddle, bricks
            return;
        }

        remove(ball);
        drawBall();
    }

    private void handleGameLoss() {
        lives = NTURNS;
        remove(ball);
        showDeathScreen();
        drawBall();
    }

    private void showDeathScreen() {
        GLabel text = new GLabel("YOU DIED");
        text.setColor(Color.RED);

        double x = (WIDTH - text.getWidth()) / 2;
        double y = (WIDTH - text.getAscent()) / 2;

        add(text, x, y);
        pause(5000);
        remove(text);
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
}
