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

import java.io.IOException;

public class GameWorld implements IGameWorld {
    private static final int s_NumLevels = 10;

    private final Mode m_Mode;
    private final Snake[] m_Snakes;
    private GameField m_GameField;
    private int m_CurrentLevel;

    public GameWorld(Mode mode) throws IOException {
        m_Mode = mode;
        m_CurrentLevel = 0;

        GameFieldFile file = new GameFieldFile(makeLevelFileName(), m_Mode == Mode.TWO_PLAYERS);
        m_GameField = file.getGameField();

        Vector2i minBounds = new Vector2i(0, 0);
        Vector2i maxBounds = new Vector2i(GameField.WIDTH - 1, GameField.HEIGHT - 1);

        m_Snakes = new Snake[m_Mode == Mode.TWO_PLAYERS ? 2 : 1];
        m_Snakes[0] = new Snake(m_GameField.getPlayer1Start(), Snake.Direction.Right, minBounds, maxBounds);
        if (m_Mode == Mode.TWO_PLAYERS) {
            m_Snakes[1] = new Snake(m_GameField.getPlayer2Start(), Snake.Direction.Left, minBounds, maxBounds);
        }
    }

    @Override
    public Mode getMode() {
        return m_Mode;
    }

    @Override
    public GameField getGameField() {
        return m_GameField;
    }

    @Override
    public Snake[] getSnakes() {
        return m_Snakes;
    }

    @Override
    public void reset() throws IOException {
        GameFieldFile file = new GameFieldFile(makeLevelFileName(), m_Mode == Mode.TWO_PLAYERS);
        m_GameField = file.getGameField();
        for (var snake : m_Snakes) {
            snake.reset();
        }
    }

    private String makeLevelFileName() {
        return String.format("Level%02d.txt", m_CurrentLevel);
    }
}
