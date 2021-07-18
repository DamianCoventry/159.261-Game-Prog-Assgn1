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

import com.snakegame.rules.GameWorld;
import com.snakegame.rules.IGameWorld;

import java.io.IOException;

public class GameLoadingAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameWorld.Mode m_Mode;
    private IGameWorld m_GameWorld;

    public GameLoadingAppState(IAppStateContext context, IGameWorld.Mode mode) {
        m_AppStateContext = context;
        m_Mode = mode;
    }

    @Override
    public void begin(long nowMs) throws IOException {
        m_GameWorld = new GameWorld(m_AppStateContext, m_Mode);
        m_GameWorld.loadFirstLevel(nowMs);
    }

    @Override
    public void end(long nowMs) {
        // No work to do
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        // No work to do
    }

    @Override
    public void think(long nowMs) throws IOException {
        m_AppStateContext.changeState(new GetReadyAppState(m_AppStateContext, m_GameWorld, true));
    }

    @Override
    public void draw3d(long nowMs) {
        // No work to do
    }

    @Override
    public void draw2d(long nowMs) {
        // TODO: Show a loading screen
    }
}
