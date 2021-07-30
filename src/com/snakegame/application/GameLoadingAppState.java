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
import com.snakegame.opengl.GLStaticPolyhedronVxTc;
import com.snakegame.opengl.GLTexture;
import com.snakegame.rules.IGameController;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.*;

public class GameLoadingAppState implements IAppState {
    private final IAppStateContext m_Context;
    private final IGameView m_View;
    private final IGameController.Mode m_Mode;
    private final Matrix4f m_ModelMatrix;
    private GLStaticPolyhedronVxTc m_Polyhedron;

    public GameLoadingAppState(IAppStateContext context, IGameController.Mode mode) {
        m_Context = context;
        m_View = m_Context.getView();
        m_ModelMatrix = new Matrix4f();
        m_Mode = mode;
    }

    @Override
    public void begin(long nowMs) throws IOException {
        GLTexture loadingTexture = new GLTexture(ImageIO.read(new File("images\\Loading.png")));
        m_Polyhedron = m_View.createCenteredPolyhedron(loadingTexture.getWidth(), loadingTexture.getHeight(), loadingTexture);

        m_View.resetSnakeGiblets();

        m_Context.addTimeout(50, (callCount) ->{
            try {
                m_View.loadResources();
                m_Context.getController().startNewGame(nowMs, m_Mode);
            } catch (Exception e) {
                e.printStackTrace();
            }
            m_Context.changeState(new GetReadyAppState(m_Context, true));
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    @Override
    public void end(long nowMs) {
        m_Polyhedron.freeNativeResources();
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        // No work to do
    }

    @Override
    public void processMouseButton(long window, int button, int action, int mods) {
        // No work to do
    }

    @Override
    public void processMouseWheel(long window, double xOffset, double yOffset) {
        // No work to do
    }

    @Override
    public void think(long nowMs) throws IOException {
        m_View.think(nowMs);
    }

    @Override
    public void draw3d(long nowMs) {
        // No work to do
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        m_View.drawOrthographicPolyhedron(m_Polyhedron, m_ModelMatrix);
    }
}
