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

package com.snakegame.application;

import java.io.IOException;

// https://en.wikipedia.org/wiki/State_pattern
public interface IAppState {
    void onStateBegin() throws IOException;
    void onStateEnd();
    void processKeyEvent(long window, int key, int scanCode, int action, int mods) throws IOException;
    void think(long nowMs) throws IOException;
    void perspectiveDrawing(long nowMs);
    void orthographicDrawing(long nowMs);
}
