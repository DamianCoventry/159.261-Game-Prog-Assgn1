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
// Written for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.rules;

import java.util.ArrayList;

public class GameField {
    public static final int WIDTH = 60;
    public static final int HEIGHT = 40;
    public static final int TOTAL_CELLS = WIDTH * HEIGHT;

    private static class CellInfo {
        private CellType m_CellType;
        private PowerUp m_PowerUp;
        private Number m_Number;
        public CellInfo(CellType cellType) {
            m_CellType = cellType;
            m_PowerUp = null;
            m_Number = null;
        }
        public boolean isEmpty() {
            return m_CellType == CellType.EMPTY;
        }

        public PowerUp getPowerUp() {
            return m_PowerUp;
        }
        public void setPowerUp(PowerUp powerUp) {
            if (m_CellType == CellType.EMPTY) {
                m_CellType = CellType.POWER_UP;
                m_PowerUp = powerUp;
            }
        }
        public void clearPowerUp() {
            if (m_CellType == CellType.POWER_UP) {
                m_CellType = CellType.EMPTY;
                m_PowerUp = null;
            }
        }

        public Number getNumber() {
            return m_Number;
        }
        public void setNumber(Number Number) {
            if (m_CellType == CellType.EMPTY) {
                m_CellType = CellType.NUMBER;
                m_Number = Number;
            }
        }
        public void clearNumber() {
            if (m_CellType == CellType.NUMBER) {
                m_CellType = CellType.EMPTY;
                m_Number = null;
            }
        }
    }
    
    private final CellInfo[] m_CellInfo;
    private Vector2i m_Player1Start;
    private Vector2i m_Player2Start;

    public GameField() {
        m_CellInfo = new CellInfo[TOTAL_CELLS];
        makeEmptyFieldWithBorderWall();
    }

    public enum CellType {
        EMPTY, WALL, POWER_UP, NUMBER
    }

    public Vector2i getPlayer1Start() {
        if (m_Player1Start == null) {
            throw new RuntimeException("Player 1 start position has not been set");
        }
        return m_Player1Start;
    }

    public Vector2i getPlayer2Start() {
        if (m_Player2Start == null) {
            throw new RuntimeException("Player 2 start position has not been set");
        }
        return m_Player2Start;
    }

    public void clearPowerUpsAndNumbers() {
        for (int i = 0; i < GameField.TOTAL_CELLS; ++i) {
            m_CellInfo[i].clearPowerUp();
            m_CellInfo[i].clearNumber();
        }
    }

    public void insertPowerUp(PowerUp powerUp) {
        if (isValidLocation(powerUp.getLocation())) {
            m_CellInfo[powerUp.getLocation().m_Y * WIDTH + powerUp.getLocation().m_X].setPowerUp(powerUp);
        }
    }

    public void removePowerUp(PowerUp powerUp) {
        if (isValidLocation(powerUp.getLocation())) {
            m_CellInfo[powerUp.getLocation().m_Y * WIDTH + powerUp.getLocation().m_X].clearPowerUp();
        }
    }

    public void insertNumber(Number number) {
        if (isValidLocation(number.getLocation())) {
            m_CellInfo[number.getLocation().m_Y * WIDTH + number.getLocation().m_X].setNumber(number);
        }
    }

    public void removeNumber(Number number) {
        if (isValidLocation(number.getLocation())) {
            m_CellInfo[number.getLocation().m_Y * WIDTH + number.getLocation().m_X].clearNumber();
        }
    }

    public void insertWall(Vector2i location) {
        m_CellInfo[location.m_Y * WIDTH + location.m_X] = new CellInfo(CellType.WALL);
    }

    public void setAllCells(String gameField, boolean requirePlayer2) {
        if (gameField.length() != GameField.TOTAL_CELLS) {
            throw new RuntimeException(String.format("Invalid game field supplied (%d characters present, %d required)", gameField.length(), GameField.TOTAL_CELLS));
        }
        makeEmptyFieldWithBorderWall();
        String copy = gameField.toLowerCase();
        for (int i = 0; i < GameField.TOTAL_CELLS; ++i) {
            switch (copy.charAt(i)) {
                case 'w':
                    m_CellInfo[i] = new CellInfo(CellType.WALL);
                    break;
                case '1':
                    m_Player1Start = new Vector2i(i % WIDTH, i / WIDTH);
                    m_CellInfo[i] = new CellInfo(CellType.EMPTY);
                    break;
                case '2':
                    m_Player2Start = new Vector2i(i % WIDTH, i / WIDTH);
                    m_CellInfo[i] = new CellInfo(CellType.EMPTY);
                    break;
                default:
                    m_CellInfo[i] = new CellInfo(CellType.EMPTY);
                    break;
            }
        }
        if (m_Player1Start == null) {
            throw new RuntimeException("The game field data do not include a player 1 start position");
        }
        if (requirePlayer2 && m_Player2Start == null) {
            throw new RuntimeException("The game field data do not include a player 2 start position");
        }
    }

    public void makeEmptyFieldWithBorderWall() {
        m_Player1Start = null;
        m_Player2Start = null;
        for (int i = 0; i < GameField.TOTAL_CELLS; ++i) {
            m_CellInfo[i] = new CellInfo(CellType.EMPTY);
        }
        setWallBorder();
    }

    public PowerUp getPowerUp(int x, int y) {
        if (isValidLocation(x, y)) {
            return m_CellInfo[y * WIDTH + x].getPowerUp();
        }
        throw new RuntimeException("Invalid coordinates");
    }

    public Number getNumber(int x, int y) {
        if (isValidLocation(x, y)) {
            return m_CellInfo[y * WIDTH + x].getNumber();
        }
        throw new RuntimeException("Invalid coordinates");
    }

    public CellType getCellType(int x, int y) {
        if (isValidLocation(x, y)) {
            return m_CellInfo[y * WIDTH + x].m_CellType;
        }
        throw new RuntimeException("Invalid coordinates");
    }

    public CellType getCellType(Vector2i position) {
        return getCellType(position.m_X, position.m_Y);
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
                m_CellInfo[y * WIDTH + x] = new CellInfo(CellType.WALL);
            }
        }
    }

    public void setHorizontalWall(int y) {
        if (y >= 0 && y < HEIGHT) {
            for (int x = 0; x < WIDTH; ++x) {
                m_CellInfo[y * WIDTH + x] = new CellInfo(CellType.WALL);
            }
        }
    }

    public ArrayList<Vector2i> getEmptyCells() {
        ArrayList<Vector2i> emptyCells = new ArrayList<>(TOTAL_CELLS);
        for (int x = 0; x < WIDTH; ++x) {
            for (int y = 0; y < HEIGHT; ++y) {
                if (m_CellInfo[y * WIDTH + x].isEmpty()) {
                    emptyCells.add(new Vector2i(x, y));
                }
            }
        }
        return emptyCells;
    }

    public Vector2i clampCoordinates(Vector2i coordinates) {
        return new Vector2i(
                Math.max(0, Math.min(WIDTH - 1, coordinates.m_X)),
                Math.max(0, Math.min(HEIGHT - 1, coordinates.m_Y))
        );
    }

    private boolean isValidLocation(Vector2i location) {
        return isValidLocation(location.m_X, location.m_Y);
    }

    private boolean isValidLocation(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }
}
