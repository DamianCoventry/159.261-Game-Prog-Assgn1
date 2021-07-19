package com.snakegame.client;

import com.snakegame.application.IAppStateContext;

public interface IGameView {
    void setAppStateContext(IAppStateContext appStateContext);
    void freeNativeResources();
    void draw3d(long nowMs);
    void draw2d(long nowMs);
    void drawCenteredImage(Texture texture);
}
