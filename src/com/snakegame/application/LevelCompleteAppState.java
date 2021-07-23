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
import com.snakegame.opengl.*;
import com.snakegame.rules.IGameController;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.*;

public class LevelCompleteAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameController m_Controller;
    private final IGameView m_View;
    private final Matrix4f m_ModelMatrix;
    private GLStaticPolyhedron m_Rectangle;

    public LevelCompleteAppState(IAppStateContext context) {
        m_AppStateContext = context;
        m_Controller = m_AppStateContext.getController();
        m_View = m_AppStateContext.getView();
        m_ModelMatrix = new Matrix4f();
    }

    @Override
    public void begin(long nowMs) throws IOException {
        GLTexture levelCompleteTexture = new GLTexture(ImageIO.read(new File("images\\LevelComplete.png")));
        m_Rectangle = m_View.createCenteredRectangle(levelCompleteTexture.getWidth(), levelCompleteTexture.getHeight(), levelCompleteTexture);

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
        m_Rectangle.freeNativeResources();
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
    public void draw2d(long nowMs) throws IOException {
        m_View.draw2d(nowMs);
        m_View.drawOrthographicPolyhedron(m_Rectangle, m_ModelMatrix);
    }
}
