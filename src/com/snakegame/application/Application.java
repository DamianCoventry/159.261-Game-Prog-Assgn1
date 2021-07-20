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
import com.snakegame.rules.*;
import org.joml.Matrix4f;
import org.lwjgl.glfw.*;

import java.io.IOException;
import java.util.function.Function;

/**
 * This class keeps the main application objects resident for the lifetime of the application.
 * */

// https://en.wikipedia.org/wiki/State_pattern
public class Application implements IAppStateContext {
    private static final int s_DesiredWindowWidth = 1024;
    private static final int s_DesiredWindowHeight = 768;
    private static final String s_WindowTitle = "159.261 Game Programming (Assignment 1)";

    private final TimeoutManager m_TimeoutManager;
    private final GLWindow m_GLWindow;
    private final IGameController m_Controller;
    private final IGameView m_View;

    private IAppState m_PendingState = null;
    private IAppState m_CurrentState = null;

    public Application() throws Exception {
        m_GLWindow = new GLWindow(s_DesiredWindowWidth, s_DesiredWindowHeight, s_WindowTitle);
        m_GLWindow.setKeyCallback(new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (m_CurrentState != null) {
                    try {
                        m_CurrentState.processKey(window, key, scancode, action, mods);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        m_Controller = new GameController(this);
        m_TimeoutManager = new TimeoutManager();
        m_View = new GameView();
        m_View.setAppStateContext(this);

        changeStateNow(new RunningMenuAppState(this), System.currentTimeMillis());
    }

    public void freeNativeResources() {
        m_View.freeNativeResources();
        m_GLWindow.freeNativeResources();
    }

    public void run() throws IOException {
        long nowMs;
        while (!m_GLWindow.quitRequested()) {
            nowMs = System.currentTimeMillis();
            m_TimeoutManager.dispatchTimeouts(nowMs);
            m_CurrentState.think(nowMs);

            m_GLWindow.beginDrawing();
            m_CurrentState.draw3d(nowMs);
            m_CurrentState.draw2d(nowMs);
            m_GLWindow.endDrawing();

            performPendingStateChange(nowMs);
        }
    }

    @Override
    public void changeState(IAppState newState) {
        m_PendingState = newState;
    }

    @Override
    public float getWindowWidth() {
        return m_GLWindow.getActualWidth();
    }

    @Override
    public float getWindowHeight() {
        return m_GLWindow.getActualHeight();
    }

    @Override
    public int addTimeout(long timeoutMs, Function<Integer, TimeoutManager.CallbackResult> callback) {
        return m_TimeoutManager.addTimeout(timeoutMs, callback);
    }

    @Override
    public void removeTimeout(int timeoutId) {
        m_TimeoutManager.removeTimeout(timeoutId);
    }

    @Override
    public IGameController getController() {
        return m_Controller;
    }

    @Override
    public IGameView getView() {
        return m_View;
    }

    @Override
    public Matrix4f getPerspectiveMatrix() {
        return m_GLWindow.getPerspectiveMatrix();
    }

    @Override
    public Matrix4f getOrthographicMatrix() {
        return m_GLWindow.getOrthographicMatrix();
    }

    public void performPendingStateChange(long nowMs) throws IOException {
        if (m_PendingState != null) {
            changeStateNow(m_PendingState, nowMs);
            m_PendingState = null;
        }
    }

    private void changeStateNow(IAppState newState, long nowMs) throws IOException {
        if (m_CurrentState != null) {
            m_CurrentState.end(nowMs);
        }
        m_CurrentState = newState;
        if (m_CurrentState != null) {
            m_CurrentState.begin(nowMs);
        }
    }

    // This is here for convenience.
    public static void main(String[] args) {
        Application app = null;
        try {
            app = new Application();
            app.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (app != null) {
                app.freeNativeResources(); // ensure release of OpenGL resources
            }
        }
    }
}
