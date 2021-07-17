//
// Snake Game
// https://en.wikipedia.org/wiki/Snake_(video_game_genre)
//
// Based on the 1976 arcade game Blockade, and the 1991 game Nibbles
// https://en.wikipedia.org/wiki/Blockade_(video_game)
// https://en.wikipedia.org/wiki/Nibbles_(video_game)
//
// This implementation is Copyright (c) 2021, Damian Coventry
// All rights reserved
// Designed and implemented for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.rules;

import java.util.ArrayList;
import java.util.Arrays;

public class GameField {
    public static final int WIDTH = 60;
    public static final int HEIGHT = 40;
    public static final int TOTAL_CELLS = 2400;

    private final Cell[] m_Cells;
    private Vector2i m_Player1Start;
    private Vector2i m_Player2Start;

    public GameField() {
        m_Cells = new Cell[TOTAL_CELLS];
        reset();
    }

    public Vector2i getPlayer1Start() {
        if (m_Player1Start == null) {
            throw new RuntimeException("No player 1 start position has been set");
        }
        return m_Player1Start;
    }

    public void setPlayer1Start(Vector2i start) {
        m_Player1Start = start;
    }

    public Vector2i getPlayer2Start() {
        if (m_Player2Start == null) {
            throw new RuntimeException("No player 2 start position has been set");
        }
        return m_Player2Start;
    }

    public void setPlayer2Start(Vector2i start) {
        m_Player2Start = start;
    }

    public enum Cell { EMPTY, WALL, NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9, DEC_LENGTH,
        INC_SPEED, DEC_SPEED, INC_LIVES, DEC_LIVES, INC_POINTS, DEC_POINTS, BERSERK, RANDOM
    }

    public void setCell(Vector2i position, Cell cell) {
        if (position.m_X >= 0 && position.m_X < WIDTH && position.m_Y >= 0 && position.m_Y < HEIGHT) {
            m_Cells[position.m_Y * WIDTH + position.m_X] = cell;
        }
    }

    public void setAllCells(String gameField, boolean checkForPlayer2StartPosition) {
        if (gameField.length() != GameField.TOTAL_CELLS) {
            throw new RuntimeException("Invalid game field string");
        }
        reset();
        String copy = gameField.toLowerCase();
        for (int i = 0; i < GameField.TOTAL_CELLS; ++i) {
            switch (copy.charAt(i)) {
                case 'w':
                    m_Cells[i] = Cell.WALL;
                    break;
                case '1':
                    m_Player1Start = new Vector2i(i % WIDTH, i / WIDTH);
                    break;
                case '2':
                    m_Player2Start = new Vector2i(i % WIDTH, i / WIDTH);
                    break;
            }
        }
        if (m_Player1Start == null) {
            throw new RuntimeException("The game field data do not include a player 1 start position");
        }
        if (checkForPlayer2StartPosition && m_Player2Start == null) {
            throw new RuntimeException("The game field data do not include a player 2 start position");
        }
    }

    public void reset() {
        m_Player1Start = null;
        m_Player2Start = null;
        Arrays.fill(m_Cells, Cell.EMPTY);
        setWallBorder();
    }

    public Cell getCell(int x, int y) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            return m_Cells[y * WIDTH + x];
        }
        throw new RuntimeException("Invalid coordinates");
    }

    public Cell getCell(Vector2i position) {
        return getCell(position.m_X, position.m_Y);
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

    public ArrayList<Vector2i> getEmptyCells() {
        ArrayList<Vector2i> emptyCells = new ArrayList<>(GameField.WIDTH * GameField.HEIGHT);
        for (int x = 0; x < WIDTH; ++x) {
            for (int y = 0; y < HEIGHT; ++y) {
                if (m_Cells[y * WIDTH + x] == Cell.EMPTY) {
                    emptyCells.add(new Vector2i(x, y));
                }
            }
        }
        return emptyCells;
    }
}
