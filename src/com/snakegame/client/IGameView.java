package com.snakegame.client;

public interface IGameView {
    void freeNativeResources();
    void draw3d(long nowMs);
    void draw2d(long nowMs);
    void drawCenteredImage(Texture texture);
}
