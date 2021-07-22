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
import com.snakegame.opengl.*;

import javax.imageio.ImageIO;
import java.io.*;

import static org.lwjgl.glfw.GLFW.*;

public class GamePausedAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameView m_View;
    private GLStaticPolyhedron m_Rectangle;

    public GamePausedAppState(IAppStateContext context) {
        m_AppStateContext = context;
        m_View = m_AppStateContext.getView();
    }

    @Override
    public void begin(long nowMs) throws IOException {
        GLTexture gamePausedTexture = new GLTexture(ImageIO.read(new File("images\\GamePaused.png")));
        m_Rectangle = m_View.createRectangle(gamePausedTexture.getWidth(), gamePausedTexture.getHeight(), gamePausedTexture);
    }

    @Override
    public void end(long nowMs) {
        m_Rectangle.freeNativeResources();
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        if (action != GLFW_PRESS) {
            return;
        }
        if (key == GLFW_KEY_ESCAPE) {
            m_AppStateContext.changeState(new GetReadyAppState(m_AppStateContext, false));
        }
        else if (key == GLFW_KEY_Q) {
            m_AppStateContext.changeState(new RunningMenuAppState(m_AppStateContext));
        }
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
        m_View.drawOrthographicPolyhedron(m_Rectangle);
    }
}
