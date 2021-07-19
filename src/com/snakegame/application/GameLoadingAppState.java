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

import com.snakegame.client.IGameView;
import com.snakegame.client.Texture;
import com.snakegame.client.TimeoutManager;
import com.snakegame.rules.IGameController;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GameLoadingAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameView m_View;
    private final IGameController.Mode m_Mode;
    private Texture m_LoadingTexture;

    public GameLoadingAppState(IAppStateContext context, IGameController.Mode mode) {
        m_AppStateContext = context;
        m_View = m_AppStateContext.getView();
        m_Mode = mode;
    }

    @Override
    public void begin(long nowMs) throws IOException {
        m_LoadingTexture = new Texture(ImageIO.read(new File("images\\Loading.png")));
        m_AppStateContext.getController().startNewGame(nowMs, m_Mode);
        m_AppStateContext.addTimeout(500, (callCount) ->{
            m_AppStateContext.changeState(new GetReadyAppState(m_AppStateContext, true));
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    @Override
    public void end(long nowMs) {
        m_LoadingTexture.freeNativeResource();
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        // No work to do
    }

    @Override
    public void think(long nowMs) throws IOException {
        // No work to do
    }

    @Override
    public void draw3d(long nowMs) {
        // No work to do
    }

    @Override
    public void draw2d(long nowMs) {
        m_View.drawCenteredImage(m_LoadingTexture);
    }
}
