package com.snakegame.rules;

import java.util.Arrays;

public class GameField {
    public static final int WIDTH = 160;
    public static final int HEIGHT = 90;
    public static final int TOTAL_CELLS = 14400;

    private final Cell[] m_Cells;
    private Vector2i m_Player1Start;
    private Vector2i m_Player2Start;

    public GameField() {
        m_Cells = new Cell[TOTAL_CELLS];
        resetAllCells();
    }

    public Vector2i getPlayer1Start() {
        return m_Player1Start;
    }

    public void setPlayer1Start(Vector2i start) {
        m_Player1Start = start;
    }

    public Vector2i getPlayer2Start() {
        return m_Player2Start;
    }

    public void setPlayer2Start(Vector2i start) {
        m_Player2Start = start;
    }
    
    public enum Cell { EMPTY, WALL, APPLE, SNAKE }

    public void setCell(int x, int y, Cell cell) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            m_Cells[y * WIDTH + x] = cell;
        }
    }

    public void setAllCells(String gameField) {
        if (gameField.length() != GameField.TOTAL_CELLS) {
            throw new RuntimeException("Invalid game field string");
        }
        resetAllCells();
        String copy = gameField.toLowerCase();
        for (int i = 0; i < GameField.TOTAL_CELLS; ++i) {
            if (copy.charAt(i) == 'w') {
                m_Cells[i] = Cell.WALL;
            }
        }
    }

    public void resetAllCells() {
        Arrays.fill(m_Cells, Cell.EMPTY);
        setWallBorder();
    }

    public Cell getCell(int x, int y) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            return m_Cells[y * WIDTH + x];
        }
        throw new RuntimeException("Invalid coordinates");
    }

    public boolean isCellEmpty(int x, int y) {
        return getCell(x, y) == Cell.EMPTY;
    }

    public boolean isCellBlocked(int x, int y) {
        Cell type = getCell(x, y);
        return type == Cell.WALL || type == Cell.SNAKE;
    }

    public boolean isCellApple(int x, int y) {
        return getCell(x, y) == Cell.APPLE;
    }

    public void setWallBorder() {
        setHorizontalWall(0);
        setHorizontalWall(HEIGHT - 1);
        setVerticalWall(0);
        setVerticalWall(WIDTH - 1);
    }

    public void setVerticalWall(int x) {
        if (x >= 0 && x < WIDTH) {
            for (int y = 0; y < HEIGHT; ++y) {
                m_Cells[y * WIDTH + x] = Cell.WALL;
            }
        }
    }

    public void setHorizontalWall(int y) {
        if (y >= 0 && y < HEIGHT) {
            for (int x = 0; x < WIDTH; ++x) {
                m_Cells[y * WIDTH + x] = Cell.WALL;
            }
        }
    }
}
