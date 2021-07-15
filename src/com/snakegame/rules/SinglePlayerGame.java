package com.snakegame.rules;

import java.io.IOException;
import java.util.Random;

public class SinglePlayerGame {
    private final GameField m_GameField;
    private final Snake m_Snake;
    private final Random m_Rng;

    public SinglePlayerGame() throws IOException {
        GameFieldFile file = new GameFieldFile("Level00.txt", false);
        m_GameField = file.getGameField();
        m_Snake = new Snake(m_GameField.getPlayer1Start(), Snake.Direction.Right, new Vector2i(0, 0),
                new Vector2i(GameField.WIDTH - 1, GameField.HEIGHT - 1));
        m_Rng = new Random();
    }

    public void reset() {
        m_GameField.removeAllApples();
        m_Snake.reset();
    }

    public GameField getGameField() {
        return m_GameField;
    }

    public Snake getSnake() {
        return m_Snake;
    }

    public Vector2i getEmptyGameFieldCell() {
        boolean found = false;
        int x, y;
        do {
            do {
                // There's always a border wall, therefore don't bother generating coordinates for the border.
                x = getRandomNumber(1, GameField.WIDTH - 2);
                y = getRandomNumber(1, GameField.HEIGHT - 2);
            }
            while (m_GameField.getCell(x, y) != GameField.Cell.EMPTY);

            for (int i = 0; i < m_Snake.getBodyParts().size() && !found; ++i) {
                found = m_Snake.getBodyParts().get(i).equals(new Vector2i(x, y));
            }
        }
        while (found);
        return new Vector2i(x, y);
    }

    // https://stackoverflow.com/questions/5271598/java-generate-random-number-between-two-given-values
    private int getRandomNumber(int rangeBegin, int rangeEnd) {
        if (rangeBegin >= rangeEnd) {
            throw new RuntimeException("Invalid number range");
        }
        return m_Rng.nextInt(rangeEnd - rangeBegin) + rangeBegin;
    }
}
