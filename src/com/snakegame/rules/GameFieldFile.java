package com.snakegame.rules;

import java.io.*;

public class GameFieldFile {
    private final GameField m_GameField;

    public GameFieldFile(String fileName, boolean checkForPlayer2StartPosition) throws IOException {
        m_GameField = new GameField();
        File file = new File(fileName);
        readGameField(new BufferedReader(new FileReader(file)), checkForPlayer2StartPosition);
    }

    public GameField getGameField() {
        return m_GameField;
    }

    public static void write(String fileName, String gameField) throws IOException {
        if (gameField.length() != GameField.TOTAL_CELLS) {
            throw new RuntimeException("Invalid game field string");
        }
        File file = new File(fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        int offset = gameField.length() - GameField.WIDTH;
        for (int y = 0; y < GameField.HEIGHT; ++y) {
            writer.write(gameField.substring(offset, GameField.WIDTH) + '\n');
            offset -= GameField.WIDTH;
        }
    }

    private void readGameField(BufferedReader reader, boolean checkForPlayer2StartPosition) throws IOException {
        int count = 0;
        StringBuilder stringBuilder = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            if (line.length() != GameField.WIDTH) {
                throw new RuntimeException("Invalid game field file");
            }
            stringBuilder.insert(0, line);
            ++count;
            line = reader.readLine();
        }
        if (count != GameField.HEIGHT) {
            throw new RuntimeException("Invalid game field file");
        }
        m_GameField.setAllCells(stringBuilder.toString(), checkForPlayer2StartPosition);
    }
}
