import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


abstract class Cell {
    abstract boolean isAlive();
    abstract Cell nextState(int neighbors);
}

class LiveCell extends Cell {
    @Override
    boolean isAlive() { return true; }

    @Override
    Cell nextState(int neighbors) {
        if (neighbors == 2 || neighbors == 3) return new LiveCell();
        else return new DeadCell();
    }
}

class DeadCell extends Cell {
    @Override
    boolean isAlive() { return false; }

    @Override
    Cell nextState(int neighbors) {
        if (neighbors == 3) return new LiveCell();
        else return new DeadCell();
    }
}

public class GameOfLife extends JPanel {
    private final int rows;
    private final int cols;
    private final int cellSize = 20;
    private Cell[][] grid;
    private int hoverRow = -1;
    private int hoverCol = -1;
    private final ExecutorService executor;

    public GameOfLife(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = new DeadCell();

        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = e.getY() / cellSize;
                int col = e.getX() / cellSize;
                setCellAlive(row, col);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                hoverCol = -1;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverRow = e.getY() / cellSize;
                hoverCol = e.getX() / cellSize;
                repaint();
            }
        });
    }

    public void setCellAlive(int row, int col) {
        if(row >= 0 && row < rows && col >= 0 && col < cols)
            grid[row][col] = new LiveCell();
    }

    private int countNeighbors(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                if (!(i == 0 && j == 0) && row + i >= 0 && row + i < rows && col + j >= 0 && col + j < cols)
                    if (grid[row + i][col + j].isAlive()) count++;
        return count;
    }

    public void nextGeneration() throws InterruptedException {
        Cell[][] newGrid = new Cell[rows][cols];
        CountDownLatch latch = new CountDownLatch(rows);

        for (int r = 0; r < rows; r++) {
            final int row = r;
            executor.submit(() -> {
                for (int c = 0; c < cols; c++)
                    newGrid[row][c] = grid[row][c].nextState(countNeighbors(row, c));
                latch.countDown();
            });
        }

        latch.await();
        grid = newGrid;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g.setColor(grid[r][c].isAlive() ? Color.BLACK : Color.WHITE);
                g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);

                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(c * cellSize, r * cellSize, cellSize, cellSize);
            }
        }

        if (hoverRow >= 0 && hoverCol >= 0 && hoverRow < rows && hoverCol < cols) {
            g.setColor(Color.RED);
            g.drawRect(hoverCol * cellSize, hoverRow * cellSize, cellSize, cellSize);
        }
    }


    // Predefined patterns
    public void addGlider(int x, int y) {
        setCellAlive(x, y + 1);
        setCellAlive(x + 1, y + 2);
        setCellAlive(x + 2, y);
        setCellAlive(x + 2, y + 1);
        setCellAlive(x + 2, y + 2);
    }

    public void addSmallExploder(int x, int y) {
        setCellAlive(x, y + 1);
        setCellAlive(x + 1, y);
        setCellAlive(x + 1, y + 1);
        setCellAlive(x + 1, y + 2);
        setCellAlive(x + 2, y);
        setCellAlive(x + 2, y + 2);
        setCellAlive(x + 3, y + 1);
    }

    public void addBlinker(int x, int y) {
        setCellAlive(x, y);
        setCellAlive(x, y + 1);
        setCellAlive(x, y + 2);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Conway's Game of Life");
        GameOfLife game = new GameOfLife(30, 30);

        game.addGlider(1, 1);
        game.addSmallExploder(10, 10);
        game.addBlinker(20, 15);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setVisible(true);

        new Timer(200, e -> {
            try {
                game.nextGeneration();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
