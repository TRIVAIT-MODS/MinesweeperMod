package org.trivait.minesweeper.logic;


import java.util.Random;

public class Board {
    public static final int SIZE = 8;
    public static final int BOMBS = 10;

    public boolean gameOver = false;
    public final Cell[][] cells = new Cell[SIZE][SIZE];

    public Board() {
        generate();
    }

    public void generate() {
        gameOver = false;
        for (int x = 0; x < SIZE; x++)
            for (int y = 0; y < SIZE; y++)
                cells[x][y] = new Cell();

        int placed = 0;
        Random random = new Random();
        while (placed < BOMBS) {
            int x = random.nextInt(SIZE);
            int y = random.nextInt(SIZE);
            if (!cells[x][y].isBomb) {
                cells[x][y].isBomb = true;
                placed++;
            }
        }

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (!cells[x][y].isBomb) {
                    int count = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE && cells[nx][ny].isBomb) {
                                count++;
                            }
                        }
                    }
                    cells[x][y].adjacentBombs = count;
                }
            }
        }
    }

    public void reveal(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return;
        Cell cell = cells[x][y];
        if (cell.isRevealed || cell.isFlagged) return;

        cell.isRevealed = true;

        if (cell.adjacentBombs == 0 && !cell.isBomb) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        reveal(x + dx, y + dy);
        }
    }

    public boolean isWin() {
        for (int x = 0; x < SIZE; x++)
            for (int y = 0; y < SIZE; y++) {
                Cell cell = cells[x][y];
                if (!cell.isBomb && !cell.isRevealed)
                    return false;
            }
        return true;
    }
}
