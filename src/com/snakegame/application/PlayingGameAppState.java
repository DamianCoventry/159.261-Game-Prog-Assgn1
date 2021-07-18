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

import com.snakegame.rules.IGameWorld;
import com.snakegame.rules.Snake;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class PlayingGameAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameWorld m_GameWorld;

    public PlayingGameAppState(IAppStateContext context, IGameWorld gameWorld) {
        m_AppStateContext = context;
        m_GameWorld = gameWorld;
    }

    @Override
    public void begin(long nowMs) {
        m_GameWorld.start(nowMs);
    }

    @Override
    public void end(long nowMs) {
        m_GameWorld.stop(nowMs);
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        if (action != GLFW_PRESS) {
            return;
        }
        if (key == GLFW_KEY_ESCAPE) {
            // TODO: Need an "are you sure?" question here.
            m_GameWorld.freeNativeResources();
            m_AppStateContext.changeState(new RunningMenuAppState(m_AppStateContext));
        }
        
        processKeyPress(m_GameWorld.getSnakes()[0], GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D, key);

        if (m_GameWorld.getMode() == IGameWorld.Mode.TWO_PLAYERS) {
            processKeyPress(m_GameWorld.getSnakes()[1], GLFW_KEY_UP, GLFW_KEY_DOWN, GLFW_KEY_LEFT, GLFW_KEY_RIGHT, key);
        }
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

    @Override
    public void think(long nowMs) throws IOException {
        m_GameWorld.think(nowMs);
    }

    @Override
    public void draw3d(long nowMs) {
        m_GameWorld.draw3d(nowMs);
    }

    @Override
    public void draw2d(long nowMs) {
        m_GameWorld.draw2d(nowMs);
    }
}
