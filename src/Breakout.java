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

    public void init() {
        addMouseListeners();
    }

    /* Method: run() */
    /** Runs the Breakout program. */
	public void run() {
        initGame();
        setSpeed();

        startGameLoop();
	}

    private void startGameLoop() {
        // game loop
        while (lives > 0 && brickNum > 0) {
            moveBall();
            checkCollisions();
            pause(DELAY);
        }

        if (brickNum < 1) {
            showScreenMessage("You Win! :>", Color.GREEN);
        } else {
            showScreenMessage("YOU DIED.", Color.RED);
        }

        remove(ball);
    }

    private void initGame() {
        drawBricks();
        drawPaddle();
        drawBall();
    }

    private void setSpeed() {
        vx = rgen.nextDouble(1.0, 3.0);
        vy = rgen.nextDouble(1.0, 3.0);

        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }
    }

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

    // moves the paddle according to mouse X location
    public void mouseMoved(MouseEvent e) {
        double paddleX = e.getX() - PADDLE_WIDTH / 2.0;

        if (paddleX >= 0 && paddleX + PADDLE_WIDTH <= WIDTH) {
            paddle.setLocation(paddleX, paddle.getY());
        }
    }

    private void drawBall() {
        ball = new GOval(WIDTH / 2 - BALL_RADIUS, HEIGHT / 2 - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
        ball.setFilled(true);
        add(ball);
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
            vy = -Math.abs(vy);
        } else if (obj != null) {
            remove(obj);
            brickNum--;
            vy *= -1;
        }
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
        remove(ball);
        drawBall();
    }

    // general method for displaying messages in center
    private void showScreenMessage(String text, Color color) {
        GLabel label = new GLabel(text);
        label.setColor(color);

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
}
