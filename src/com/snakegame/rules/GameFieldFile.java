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

import java.io.*;

public class GameFieldFile {
    private final GameField m_GameField;

    public GameFieldFile(String fileName, boolean requirePlayer2) throws IOException {
        m_GameField = new GameField();
        File file = new File(fileName);
        readGameField(new BufferedReader(new FileReader(file)), requirePlayer2);
    }

    public GameField getGameField() {
        return m_GameField;
    }

    private void readGameField(BufferedReader reader, boolean requirePlayer2) throws IOException {
        int count = 0;
        StringBuilder stringBuilder = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            if (line.length() != GameField.WIDTH) {
                throw new RuntimeException(String.format("Invalid game field file (line %d has %d characters, but %d required)", count, line.length(), GameField.WIDTH));
            }
            stringBuilder.insert(0, line);
            ++count;
            line = reader.readLine();
        }
        if (count != GameField.HEIGHT) {
            throw new RuntimeException(String.format("Invalid game field file (%d lines found, but %d required)", count, GameField.HEIGHT));
        }
        m_GameField.setAllCells(stringBuilder.toString(), requirePlayer2);
    }
}
