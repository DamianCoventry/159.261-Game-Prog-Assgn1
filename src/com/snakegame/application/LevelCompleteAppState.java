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
import com.snakegame.rules.IGameController;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class LevelCompleteAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameController m_Controller;
    private final IGameView m_View;

    public LevelCompleteAppState(IAppStateContext context) {
        m_AppStateContext = context;
        m_Controller = m_AppStateContext.getController();
        m_View = m_AppStateContext.getView();
    }

    @Override
    public void begin(long nowMs) {
        m_AppStateContext.addTimeout(2000, (callCount) -> {
            if (m_Controller.isLastLevel()) {
                m_AppStateContext.changeState(new GameWonAppState(m_AppStateContext));
            }
            else {
                try {
                    m_Controller.loadNextLevel(nowMs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                m_AppStateContext.changeState(new GetReadyAppState(m_AppStateContext, true));
            }
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
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
        // No work to do
    }

    @Override
    public void draw3d(long nowMs) {
        m_View.draw3d(nowMs);
    }

    @Override
    public void draw2d(long nowMs) {
        m_View.draw2d(nowMs);

        // TODO: Display who lost (check m_BothSnakes && m_Player)
        glBindTexture(GL_TEXTURE_2D, m_View.getLevelCompleteTexture().getId());
        var w = m_View.getLevelCompleteTexture().getWidth();
        var h = m_View.getLevelCompleteTexture().getHeight();
        var x = (m_AppStateContext.getWindowWidth() / 2.0f) - (w / 2.0f);
        var y = (m_AppStateContext.getWindowHeight() / 2.0f) - (h / 2.0f);
        glBegin(GL_QUADS);
        glTexCoord2d(0.0, 0.0); glVertex3d(x, y + h, 0.1f);
        glTexCoord2d(0.0, 1.0); glVertex3d(x , y, 0.1f);
        glTexCoord2d(1.0, 1.0); glVertex3d(x + w, y, 0.1f);
        glTexCoord2d(1.0, 0.0); glVertex3d(x + w, y + h, 0.1f);
        glEnd();
    }
}
