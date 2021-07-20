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
import com.snakegame.opengl.GLTexture;
import com.snakegame.rules.IGameController;

import javax.imageio.ImageIO;
import java.io.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class RunningMenuAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameView m_View;
    private GLTexture m_MainMenuTexture;
    private GLTexture m_HelpMenuTexture;
    private enum Page {MAIN, HELP }
    private Page m_Page;

    public RunningMenuAppState(IAppStateContext context) {
        m_AppStateContext = context;
        m_View = m_AppStateContext.getView();
    }

    @Override
    public void begin(long nowMs) throws IOException {
        m_MainMenuTexture = new GLTexture(ImageIO.read(new File("images\\MainMenu.png")));
        m_HelpMenuTexture = new GLTexture(ImageIO.read(new File("images\\HelpMenu.png")));

        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);

        m_Page = Page.MAIN;
    }

    @Override
    public void end(long nowMs) {
        m_MainMenuTexture.freeNativeResource();
        m_HelpMenuTexture.freeNativeResource();
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) throws IOException {
        if (action != GLFW_PRESS) {
            return;
        }

        if (m_Page == Page.HELP) {
            if (key == GLFW_KEY_ESCAPE) {
                m_Page = Page.MAIN;
            }
            return;
        }

        switch (key)
        {
            case GLFW_KEY_1:
                startNewGame(IGameController.Mode.SINGLE_PLAYER);
                break;
            case GLFW_KEY_2:
                startNewGame(IGameController.Mode.TWO_PLAYERS);
                break;
            case GLFW_KEY_3:
                m_Page = Page.HELP;
                break;
            case GLFW_KEY_ESCAPE:
                glfwSetWindowShouldClose(window, true);
                break;
        }
    }

    @Override
    public void think(long nowMs) {
        // Nothing to do
    }

    @Override
    public void draw3d(long nowMs) {
        // Nothing to do
    }

    @Override
    public void draw2d(long nowMs) {
        switch (m_Page) {
            case MAIN:
                m_View.drawCenteredImage(m_MainMenuTexture);
                break;
            case HELP:
                m_View.drawCenteredImage(m_HelpMenuTexture);
                break;
        }
    }

    private void startNewGame(IGameController.Mode mode) {
        m_AppStateContext.changeState(new GameLoadingAppState(m_AppStateContext, mode));
    }
}
