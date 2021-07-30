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
import com.snakegame.rules.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class PlayingGameAppState implements IAppState {
    private final IAppStateContext m_Context;
    private final IGameController m_Controller;
    private final IGameView m_View;

    public PlayingGameAppState(IAppStateContext context) {
        m_Context = context;
        m_Controller = m_Context.getController();
        m_View = m_Context.getView();
    }

    @Override
    public void begin(long nowMs) {
        m_View.resetSnakeGiblets();
        m_Controller.start(nowMs);
    }

    @Override
    public void end(long nowMs) {
        m_Controller.stop(nowMs);
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        if (action != GLFW_PRESS) {
            return;
        }
        if (key == GLFW_KEY_ESCAPE) {
            m_Context.changeState(new GamePausedAppState(m_Context));
            return;
        }
        if (key == GLFW_KEY_F5) { // temp cheat key
            m_Context.changeState(new LevelCompleteAppState(m_Context));
            return;
        }
        
        processKeyPress(m_Controller.getSnakes()[0], GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D, key);

        if (m_Controller.getMode() == IGameController.Mode.TWO_PLAYERS) {
            processKeyPress(m_Controller.getSnakes()[1], GLFW_KEY_UP, GLFW_KEY_DOWN, GLFW_KEY_LEFT, GLFW_KEY_RIGHT, key);
        }
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
        m_Controller.think(nowMs);
        m_View.think(nowMs);
    }

    @Override
    public void draw3d(long nowMs) {
        m_View.draw3d(nowMs);
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        m_View.draw2d(nowMs);
    }

    private void processKeyPress(Snake snake, int upKey, int downKey, int leftKey, int rightKey, int keyPressed) {
        if (snake.getDirection() == Snake.Direction.Left || snake.getDirection() == Snake.Direction.Right) {
            if (keyPressed == upKey) {
                snake.setDirection(Snake.Direction.Up);
            }
            if (keyPressed == downKey) {
                snake.setDirection(Snake.Direction.Down);
            }
        }
        if (snake.getDirection() == Snake.Direction.Up || snake.getDirection() == Snake.Direction.Down) {
            if (keyPressed == leftKey) {
                snake.setDirection(Snake.Direction.Left);
            }
            if (keyPressed == rightKey) {
                snake.setDirection(Snake.Direction.Right);
            }
        }
    }
}
