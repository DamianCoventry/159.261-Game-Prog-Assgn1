package com.snakegame.rules;

import java.io.*;

public class GameFieldFile {
    private final GameField m_GameField;

    public GameFieldFile(String fileName) throws IOException {
        m_GameField = new GameField();
        File file = new File(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        readPlayer1Start(reader);
        readPlayer2Start(reader);
        readGameField(reader);
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
        for (int y = 0, offset = 0; y < GameField.HEIGHT; ++y, offset += GameField.WIDTH) {
            writer.write(gameField.substring(offset, GameField.WIDTH) + '\n');
        }
    }

    private void readPlayer1Start(BufferedReader reader) throws IOException {
        m_GameField.setPlayer1Start(readInteger2dCoordinates(reader));
    }

    private void readPlayer2Start(BufferedReader reader) throws IOException {
        m_GameField.setPlayer2Start(readInteger2dCoordinates(reader));
    }

    private void readGameField(BufferedReader reader) throws IOException {
        int count = 0;
        StringBuilder sb = new StringBuilder(GameField.TOTAL_CELLS);
        String line = reader.readLine();
        while (line != null) {
            if (line.length() != GameField.WIDTH) {
                throw new RuntimeException("Invalid game field file");
            }
            sb.append(line);
            ++count;
            line = reader.readLine();
        }
        if (count != GameField.HEIGHT) {
            throw new RuntimeException("Invalid game field file");
        }
        m_GameField.setAllCells(sb.toString());
    }

    private Vector2i readInteger2dCoordinates(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new RuntimeException("Invalid game field file");
        }
        String[] words = line.split("\\s");
        if (words.length != 2) {
            throw new RuntimeException("Invalid game field file");
        }
        return new Vector2i(
                Integer.parseInt(words[0]),
                Integer.parseInt(words[1])
        );
    }
}
