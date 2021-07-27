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
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F5;

public class GetReadyAppState implements IAppState {
    private final IAppStateContext m_Context;
    private final IGameView m_View;
    private final boolean m_ResetState;
    private final Matrix4f m_ModelMatrix;
    private GLStaticPolyhedronVxTc[] m_GetReadyRectangles;
    private int m_TimeoutId;
    private int m_CurrentRectangle;

    public GetReadyAppState(IAppStateContext context, boolean resetState) {
        m_Context = context;
        m_View = m_Context.getView();
        m_ResetState = resetState;
        m_ModelMatrix = new Matrix4f();
    }

    @Override
    public void begin(long nowMs) throws IOException {
        m_GetReadyRectangles = new GLStaticPolyhedronVxTc[3];

        GLTexture getReadyTexture = new GLTexture(ImageIO.read(new File("images\\GetReady3.png")));
        m_GetReadyRectangles[0] = m_View.createCenteredRectangle(getReadyTexture.getWidth(), getReadyTexture.getHeight(), getReadyTexture);
        getReadyTexture = new GLTexture(ImageIO.read(new File("images\\GetReady2.png")));
        m_GetReadyRectangles[1] = m_View.createCenteredRectangle(getReadyTexture.getWidth(), getReadyTexture.getHeight(), getReadyTexture);
        getReadyTexture = new GLTexture(ImageIO.read(new File("images\\GetReady1.png")));
        m_GetReadyRectangles[2] = m_View.createCenteredRectangle(getReadyTexture.getWidth(), getReadyTexture.getHeight(), getReadyTexture);

        if (m_ResetState) {
            m_Context.getController().resetAfterSnakeDeath(nowMs);
        }

        m_CurrentRectangle = 0;
        m_TimeoutId = m_Context.addTimeout(1000, (callCount) -> {
            if (m_CurrentRectangle == 2) {
                m_Context.changeState(new PlayingGameAppState(m_Context));
                return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
            }
            ++m_CurrentRectangle;
            return TimeoutManager.CallbackResult.KEEP_CALLING;
        });
    }

    @Override
    public void end(long nowMs) {
        for (var rectangle : m_GetReadyRectangles) {
            rectangle.freeNativeResources();
        }
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        if (key == GLFW_KEY_F5) { // temp cheat key
            if (m_TimeoutId != 0) {
                m_Context.removeTimeout(m_TimeoutId);
                m_TimeoutId = 0;
            }
            m_Context.changeState(new LevelCompleteAppState(m_Context));
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
    public void draw2d(long nowMs) throws IOException {
        m_View.draw2d(nowMs);
        m_View.drawOrthographicPolyhedron(m_GetReadyRectangles[m_CurrentRectangle], m_ModelMatrix);
    }
}
