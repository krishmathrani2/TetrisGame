import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class TetrisGameGUI extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int BLOCK_SIZE = 30;

    private final char EMPTY_CELL = ' ';
    private final char BLOCK_CELL = '#';

    private char[][] board;
    private int currentX;
    private int currentY;
    private int currentPiece;
    private int rotation;

    private final char[][][][] tetrominos = {
            // "I" Tetromino
            {
                    {
                            {'#', '#', '#', '#'}
                    },
                    {
                            {'#'},
                            {'#'},
                            {'#'},
                            {'#'}
                    }
            },
            // "O" Tetromino
            {
                    {
                            {'#', '#'},
                            {'#', '#'}
                    }
            },
            // "T" Tetromino
            {
                    {
                            {'#', '#', '#'},
                            {' ', '#'},
                    },
                    {
                            {' ', '#'},
                            {'#', '#'},
                            {' ', '#'}
                    },
                    {
                            {' ', '#'},
                            {'#', '#'},
                            {' ', '#'}
                    },
                    {
                            {'#', ' ', ' '},
                            {'#', '#', '#'}
                    }
            },
            // "L" Tetromino
            {
                    {
                            {'#', '#', '#'},
                            {'#', ' ', ' '},
                    },
                    {
                            {'#', '#'},
                            {' ', '#'},
                            {' ', '#'}
                    },
                    {
                            {' ', ' ', '#'},
                            {'#', '#', '#'}
                    },
                    {
                            {'#', ' '},
                            {'#', ' '},
                            {'#', '#'}
                    }
            },
            // "J" Tetromino
            {
                    {
                            {'#', '#', '#'},
                            {' ', ' ', '#'},
                    },
                    {
                            {' ', '#'},
                            {' ', '#'},
                            {'#', '#'}
                    },
                    {
                            {'#', ' ', ' '},
                            {'#', '#', '#'}
                    },
                    {
                            {'#', '#'},
                            {'#', ' '},
                            {'#', ' '}
                    }
            },
            // "S" Tetromino
            {
                    {
                            {' ', '#', '#'},
                            {'#', '#', ' '},
                    },
                    {
                            {'#', ' '},
                            {'#', '#'},
                            {' ', '#'}
                    }
            },
            // "Z" Tetromino
            {
                    {
                            {'#', '#', ' '},
                            {' ', '#', '#'},
                    },
                    {
                            {' ', '#'},
                            {'#', '#'},
                            {'#', ' '}
                    }
            }
    };

    private final Color[] blockColors = {
            Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.GREEN, Color.RED, Color.ORANGE, Color.BLUE
    };

    private Timer timer;
    private int score;
    private boolean isGameOver;
    private ArrayList<Integer> nextTetrominos = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris Game");
            TetrisGameGUI game = new TetrisGameGUI();
            frame.add(game);

            int borderWidth = 10; // Choose the desired border size
            int gameWidth = game.BOARD_WIDTH * game.BLOCK_SIZE;
            int gameHeight = game.BOARD_HEIGHT * game.BLOCK_SIZE;

            // Calculate the total frame width and height, including the border
            int frameWidth = gameWidth + 2 * borderWidth;
            int frameHeight = gameHeight + 2 * borderWidth;

            frame.setPreferredSize(new Dimension(frameWidth, frameHeight));
            frame.setResizable(false); // Prevent resizing to maintain the game's aspect ratio
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // Center the game inside the frame
            int xOffset = (frame.getWidth() - frame.getContentPane().getWidth()) / 2;
            int yOffset = (frame.getHeight() - frame.getContentPane().getHeight()) / 2;
            game.setLocation(xOffset, yOffset);
        });
    }

    public TetrisGameGUI() {
        setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
        setBackground(Color.BLACK);

        board = new char[BOARD_HEIGHT][BOARD_WIDTH];
        initializeBoard();

        timer = new Timer(500, this);
        timer.start();

        score = 0;
        isGameOver = false;
        generateNewPiece(); // Generate the first piece

        addKeyListener(this);
        setFocusable(true);
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[i][j] = EMPTY_CELL;
            }
        }
    }

    private void generateNewPiece() {
        Random rand = new Random();
        if (nextTetrominos.isEmpty()) {
            // Fill the nextTetrominos list with all tetrominos and shuffle it
            for (int i = 0; i < tetrominos.length; i++) {
                nextTetrominos.add(i);
            }
            // Shuffle the list to randomize the order of tetrominos
            java.util.Collections.shuffle(nextTetrominos, rand);
        }
        currentPiece = nextTetrominos.get(0);
        nextTetrominos.remove(0);
        if (nextTetrominos.isEmpty()) {
            // Refill the list when all tetrominos have been used
            for (int i = 0; i < tetrominos.length; i++) {
                nextTetrominos.add(i);
            }
            java.util.Collections.shuffle(nextTetrominos, rand);
        }
        rotation = 0;
        currentX = BOARD_WIDTH / 2;
        currentY = 0;

        if (isColliding()) {
            // Game over if the new piece collides with other blocks immediately
            isGameOver = true;
            timer.stop();
        }
    }

    private boolean isValidMove(int piece, int rot, int newX, int newY) {
        char[][] currentTetromino = tetrominos[piece][rot];
        for (int i = 0; i < currentTetromino.length; i++) {
            for (int j = 0; j < currentTetromino[i].length; j++) {
                if (currentTetromino[i][j] == BLOCK_CELL) {
                    int x = newX + j;
                    int y = newY + i;
                    if (y >= 0 && (y >= BOARD_HEIGHT || x < 0 || x >= BOARD_WIDTH || board[y][x] == BLOCK_CELL)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isColliding() {
        char[][] currentTetromino = tetrominos[currentPiece][rotation];
        for (int i = 0; i < currentTetromino.length; i++) {
            for (int j = 0; j < currentTetromino[i].length; j++) {
                if (currentTetromino[i][j] == BLOCK_CELL) {
                    int x = currentX + j;
                    int y = currentY + i + 1;
                    if (y >= 0 && (y >= BOARD_HEIGHT || x < 0 || x >= BOARD_WIDTH || board[y][x] == BLOCK_CELL)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void mergePieceWithBoard() {
        char[][] currentTetromino = tetrominos[currentPiece][rotation];
        for (int i = 0; i < currentTetromino.length; i++) {
            for (int j = 0; j < currentTetromino[i].length; j++) {
                if (currentTetromino[i][j] == BLOCK_CELL && currentY + i >= 0) {
                    board[currentY + i][currentX + j] = BLOCK_CELL;
                }
            }
        }
    }

    private void checkLines() {
        int linesCleared = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineCompleted = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == EMPTY_CELL) {
                    lineCompleted = false;
                    break;
                }
            }

            if (lineCompleted) {
                linesCleared++;
                for (int k = i; k > 0; k--) {
                    System.arraycopy(board[k - 1], 0, board[k], 0, BOARD_WIDTH);
                }
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[0][j] = EMPTY_CELL;
                }
            }
        }

        score += calculateScore(linesCleared);
    }

    private int calculateScore(int linesCleared) {
        int[] scores = {0, 40, 100, 300, 1200};
        if (linesCleared >= 1 && linesCleared <= 4) {
            return scores[linesCleared];
        }
        return 0;
    }

    private void rotatePiece() {
        int newRotation = (rotation + 1) % tetrominos[currentPiece].length;
        if (isValidMove(currentPiece, newRotation, currentX, currentY)) {
            rotation = newRotation;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) {
            // Game over, stop the timer
            timer.stop();
            return;
        }

        if (!isColliding()) {
            // Move the piece down
            currentY++;
        } else {
            // Place the piece and generate a new one
            mergePieceWithBoard();
            checkLines();
            generateNewPiece();
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the border around the game
        int borderWidth = 10; // Choose the desired border size
        int gameWidth = BOARD_WIDTH * BLOCK_SIZE;
        int gameHeight = BOARD_HEIGHT * BLOCK_SIZE;
        int xOffset = (getWidth() - gameWidth) / 2;
        int yOffset = (getHeight() - gameHeight) / 2;

        g.setColor(Color.WHITE);
        g.fillRect(xOffset - borderWidth, yOffset - borderWidth, gameWidth + 2 * borderWidth, borderWidth); // Top border
        g.fillRect(xOffset - borderWidth, yOffset + gameHeight, gameWidth + 2 * borderWidth, borderWidth); // Bottom border
        g.fillRect(xOffset - borderWidth, yOffset, borderWidth, gameHeight); // Left border
        g.fillRect(xOffset + gameWidth, yOffset, borderWidth, gameHeight); // Right border

        // Center the game inside the panel
        g.translate(xOffset, yOffset);

        // Draw the board
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == BLOCK_CELL) {
                    int blockX = j * BLOCK_SIZE;
                    int blockY = i * BLOCK_SIZE;
                    Color blockColor;
                    if (i >= BOARD_HEIGHT - 2) {
                        // Set electric lime color for blocks at the bottom
                        blockColor = Color.GREEN;
                    } else {
                        blockColor = blockColors[currentPiece];
                    }
                    g.setColor(blockColor);
                    g.fillRect(blockX, blockY, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(blockX, blockY, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }

        // Draw the current piece
        char[][] currentTetromino = tetrominos[currentPiece][rotation % tetrominos[currentPiece].length];
        for (int i = 0; i < currentTetromino.length; i++) {
            for (int j = 0; j < currentTetromino[i].length; j++) {
                if (currentTetromino[i][j] == BLOCK_CELL) {
                    int x = (currentX + j) * BLOCK_SIZE;
                    int y = (currentY + i) * BLOCK_SIZE;
                    Color blockColor = blockColors[currentPiece];
                    g.setColor(blockColor);
                    g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }

        // Display the score on the screen
        String scoreText = "Score: " + score;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(scoreText, 10, 30);

        // Display "T3TRIS" in the top right corner
        String t3trisText = "T3TRIS";
        int t3trisTextWidth = g.getFontMetrics().stringWidth(t3trisText);
        g.drawString(t3trisText, getWidth() - t3trisTextWidth - 10, 30);

        // If the game is over, display "GAME OVER" in the center of the screen
        if (isGameOver) {
            String gameOverText = "GAME OVER";
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            int gameOverTextWidth = g.getFontMetrics().stringWidth(gameOverText);
            int gameOverTextHeight = g.getFontMetrics().getHeight();
            int xGameOver = (gameWidth - gameOverTextWidth) / 2;
            int yGameOver = (gameHeight - gameOverTextHeight) / 2;
            g.drawString(gameOverText, xGameOver, yGameOver);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            int newX = currentX - 1;
            if (isValidMove(currentPiece, rotation, newX, currentY)) {
                currentX = newX;
            }
        } else if (key == KeyEvent.VK_RIGHT) {
            int newX = currentX + 1;
            if (isValidMove(currentPiece, rotation, newX, currentY)) {
                currentX = newX;
            }
        } else if (key == KeyEvent.VK_DOWN) {
            int newY = currentY + 1;
            if (isValidMove(currentPiece, rotation, currentX, newY)) {
                currentY = newY;
            }
        } else if (key == KeyEvent.VK_UP) {
            rotatePiece();
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
