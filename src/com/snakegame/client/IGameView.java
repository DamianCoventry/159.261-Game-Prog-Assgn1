package com.snakegame.client;

public interface IGameView {
    Texture getGameOverTexture();
    Texture getGameWonTexture();
    Texture getGetReadyTexture();
    Texture getPlayer1DiedTexture();
    Texture getPlayer2DiedTexture();
    Texture getBothPlayersDiedTexture();
    Texture getLevelCompleteTexture();
    Texture getGamePausedTexture();
    void freeNativeResources();
    void draw3d(long nowMs);
    void draw2d(long nowMs);
}
