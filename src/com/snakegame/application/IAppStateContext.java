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

import com.jme3.bullet.PhysicsSpace;
import com.snakegame.client.*;
import com.snakegame.opengl.GLWindow;
import com.snakegame.rules.IGameController;
import org.joml.Matrix4f;

import java.util.function.Function;

// https://en.wikipedia.org/wiki/State_pattern
public interface IAppStateContext {
    void exitApplication();
    void changeState(IAppState newState);

    float getWindowWidth();
    float getWindowHeight();

    void activateArrowMouseCursor();
    void activateHandMouseCursor();
    void activateGrabMouseCursor();
    GLWindow.CursorPosition getMouseCursorPosition();

    int addTimeout(long timeoutMs, Function<Integer, TimeoutManager.CallbackResult> callback);
    void removeTimeout(int timeoutId);

    IGameController getController();
    IGameView getView();
    Matrix4f getPerspectiveMatrix();
    Matrix4f getOrthographicMatrix();
    PhysicsSpace getPhysicsSpace();
}
