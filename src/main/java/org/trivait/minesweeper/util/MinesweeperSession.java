package org.trivait.minesweeper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.MinecraftClient;
import org.trivait.minesweeper.logic.Board;
import org.trivait.minesweeper.logic.Cell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MinesweeperSession {
    private static final File FILE = new File(MinecraftClient.getInstance().runDirectory, "config/minesweeper_session.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private record SavedCell(int x, int y, boolean isBomb, boolean isRevealed, boolean isFlagged, int adjacentBombs) {}

    public static void save(Board board) {
        List<SavedCell> list = new ArrayList<>();

        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Cell c = board.cells[x][y];
                list.add(new SavedCell(x, y, c.isBomb, c.isRevealed, c.isFlagged, c.adjacentBombs));
            }
        }

        try (Writer writer = new FileWriter(FILE)) {
            GSON.toJson(list, writer);
        } catch (IOException ignored) {}
    }

    public static Cell[][] load() {
        if (!FILE.exists()) return null;
        try (Reader reader = new FileReader(FILE)) {
            SavedCell[] saved = GSON.fromJson(reader, SavedCell[].class);
            Cell[][] result = new Cell[Board.SIZE][Board.SIZE];
            for (SavedCell s : saved) {
                Cell c = new Cell();
                c.isBomb = s.isBomb();
                c.isRevealed = s.isRevealed();
                c.isFlagged = s.isFlagged();
                c.adjacentBombs = s.adjacentBombs();
                result[s.x()][s.y()] = c;
            }
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    public static void clear() {
        if (FILE.exists()) FILE.delete();
    }
}
