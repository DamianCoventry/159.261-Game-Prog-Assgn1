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

import java.io.IOException;

// https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller
public interface IGameController {
    enum Mode { SINGLE_PLAYER, TWO_PLAYERS }
    enum SubtractSnakeResult { SNAKE_AVAILABLE, NO_SNAKES_REMAIN }

    Mode getMode();
    GameField getGameField();
    Snake[] getSnakes();
    SubtractSnakeResult subtractSnake(int player);

    boolean isLastLevel();
    int getCurrentLevel();
    int getLevelCount();
    void startNewGame(long nowMs, Mode mode) throws IOException;
    void loadNextLevel(long nowMs) throws IOException;
    void resetAfterSnakeDeath(long nowMs);
    void start(long nowMs);
    void stop(long nowMs);
    void think(long nowMs) throws IOException;
}
