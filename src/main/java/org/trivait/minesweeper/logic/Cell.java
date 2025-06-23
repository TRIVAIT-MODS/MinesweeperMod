package org.trivait.minesweeper.logic;

public class Cell {
    public boolean isBomb = false;
    public boolean isRevealed = false;
    public boolean isFlagged = false;
    public int adjacentBombs = 0;
}
