package org.trivait.minesweeper.game;

public class SavedGame {
    public int w, h, mines;
    public boolean minesPlaced;
    public int remainingSafe;
    public boolean alive, won, firstClick;
    public boolean timerRunning;
    public int elapsedSeconds;
    public boolean[] mine;
    public boolean[] revealed;
    public boolean[] flagged;
    public int[] adjacent;
    public float[] revealProgress;
    public int[] delayTicks;
    public boolean[] scheduled;
}
