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
import com.snakegame.rules.Snake;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.io.IOException;

// https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller
public interface IGameView {
    void setAppStateContext(IAppStateContext context) throws IOException;

    void loadResources() throws Exception;
    void unloadResources();
    void freeNativeResources();

    void think(long nowMs);
    void draw3d(long nowMs);
    void draw2d(long nowMs) throws IOException;
    void drawOrthographicPolyhedron(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix);
    void drawOrthographicPolyhedron(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix, float alpha);
    void drawOrthographicPolyhedronWithFadeRange(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix, float fadeRange);

    void startRemainingSnakesAnimation(int playerId, Vector4f colour);
    void startScoreAnimation(int playerId, Vector4f colour);

    void resetSnakeGiblets();
    void spawnSnakeGiblets(Snake snake);

    GLStaticPolyhedronVxTc createPolyhedron(float x, float y, float width, float height, GLTexture texture);
    GLStaticPolyhedronVxTc createCenteredPolyhedron(float width, float height, GLTexture texture);
    GLStaticPolyhedronVxTc loadDisplayMeshWithoutNormals(String fileName) throws Exception;
    GLStaticPolyhedronVxTcNm loadDisplayMeshWithNormals(String fileName) throws Exception;
    GLDiffuseTextureProgram getTexturedProgram();
    GLSpecularDirectionalLightProgram getSpecularDirectionalLightProgram();
    GLDirectionalLightProgram getDirectionalLightProgram();
    GLDiffuseTextureAlphaFadeProgram getDiffuseTextureAlphaFadeProgram();
}
