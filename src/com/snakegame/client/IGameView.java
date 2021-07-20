package com.snakegame.client;

import com.snakegame.application.IAppStateContext;
import com.snakegame.opengl.*;
import org.joml.Matrix4f;

public interface IGameView {
    void setAppStateContext(IAppStateContext appStateContext);
    void freeNativeResources();
    void draw3d(long nowMs);
    void draw2d(long nowMs);
    void drawCenteredImage(GLTexture GLTexture);
    TexturedShaderProgram getTexturedShaderProgram();
}
