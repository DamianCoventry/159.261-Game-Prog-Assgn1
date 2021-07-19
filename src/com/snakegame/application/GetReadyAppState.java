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

import com.snakegame.client.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class GetReadyAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameView m_View;
    private final boolean m_ResetState;
    private Texture m_GetReadyTexture;

    public GetReadyAppState(IAppStateContext context, boolean resetState) {
        m_AppStateContext = context;
        m_View = m_AppStateContext.getView();
        m_ResetState = resetState;
    }

    @Override
    public void begin(long nowMs) throws IOException {
        m_GetReadyTexture = new Texture(ImageIO.read(new File("images\\GetReady.png")));
        if (m_ResetState) {
            m_AppStateContext.getController().resetAfterSnakeDeath(nowMs);
        }
        m_AppStateContext.addTimeout(2000, (callCount) -> {
            m_AppStateContext.changeState(new PlayingGameAppState(m_AppStateContext));
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    @Override
    public void end(long nowMs) {
        m_GetReadyTexture.freeNativeResource();
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
        m_View.draw3d(nowMs);
    }

    @Override
    public void draw2d(long nowMs) {
        m_View.draw2d(nowMs);
        m_View.drawCenteredImage(m_GetReadyTexture);
    }
}
