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

package com.snakegame.client;

import com.snakegame.application.IAppStateContext;
import com.snakegame.opengl.*;
import org.joml.Matrix4f;

import java.io.IOException;

public interface IGameView {
    void setAppStateContext(IAppStateContext appStateContext);
    void freeNativeResources();
    void draw3d(long nowMs);
    void draw2d(long nowMs) throws IOException;
    void drawOrthographicPolyhedron(GLStaticPolyhedron polyhedron, Matrix4f modelMatrix);
    GLStaticPolyhedron createRectangle(float x, float y, float width, float height, GLTexture texture);
    GLStaticPolyhedron createCenteredRectangle(float width, float height, GLTexture texture);
    GLStaticPolyhedron loadDisplayMesh(String fileName) throws Exception;
    TexturedShaderProgram getTexturedShaderProgram();
}
