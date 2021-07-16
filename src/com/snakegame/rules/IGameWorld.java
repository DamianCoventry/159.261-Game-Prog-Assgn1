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

public interface IGameWorld {
    enum Mode { SINGLE_PLAYER, TWO_PLAYERS}
    Mode getMode();
    GameField getGameField();
    Snake[] getSnakes();
    void reset() throws IOException;
}
