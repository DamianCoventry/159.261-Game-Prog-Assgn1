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

package com.snakegame.application;

import java.io.IOException;

// https://en.wikipedia.org/wiki/State_pattern
public interface IAppState {
    void begin(long nowMs) throws IOException;
    void end(long nowMs);
    void processKey(long window, int key, int scanCode, int action, int mods) throws IOException;
    void think(long nowMs) throws IOException;
    void draw3d(long nowMs);
    void draw2d(long nowMs);
}
