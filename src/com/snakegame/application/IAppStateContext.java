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

import com.snakegame.client.TimeoutManager;

import java.util.function.Function;

// https://en.wikipedia.org/wiki/State_pattern
public interface IAppStateContext {
    void changeState(IAppState newState);
    float getWindowWidth();
    float getWindowHeight();
    int addTimeout(long timeoutMs, Function<Integer, TimeoutManager.CallbackResult> callback);
    void removeTimeout(int timeoutId);
}
