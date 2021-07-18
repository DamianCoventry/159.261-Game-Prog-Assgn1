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

import com.snakegame.client.Texture;
import com.snakegame.rules.IGameWorld;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class RunningMenuAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private Texture m_MainMenuTexture;
    private Texture m_HelpMenuTexture;
    private enum Page {MAIN, HELP }
    private Page m_Page;

    public RunningMenuAppState(IAppStateContext context) {
        m_AppStateContext = context;
    }

    @Override
    public void begin(long nowMs) throws IOException {
        m_MainMenuTexture = new Texture(ImageIO.read(new File("images\\FrontMenu.png")));
        m_HelpMenuTexture = new Texture(ImageIO.read(new File("images\\HelpMenu.png")));
        m_Page = Page.MAIN;
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        setPage(Page.MAIN);
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
                setPage(Page.MAIN);
            }
            return;
        }

        switch (key)
        {
            case GLFW_KEY_1:
                startNewGame(IGameWorld.Mode.SINGLE_PLAYER);
                break;
            case GLFW_KEY_2:
                startNewGame(IGameWorld.Mode.TWO_PLAYERS);
                break;
            case GLFW_KEY_3:
                setPage(Page.HELP);
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
        glBegin(GL_QUADS);
        glTexCoord2d(0.0, 0.0); glVertex2f(0.0f, m_AppStateContext.getWindowHeight());
        glTexCoord2d(0.0, 1.0); glVertex2f(0.0f, 0.0f);
        glTexCoord2d(1.0, 1.0); glVertex2f(m_AppStateContext.getWindowWidth(), 0.0f);
        glTexCoord2d(1.0, 0.0); glVertex2f(m_AppStateContext.getWindowWidth(), m_AppStateContext.getWindowHeight());
        glEnd();
    }

    private void setPage(Page page) {
        m_Page = page;
        switch (m_Page) {
            case MAIN:
                glBindTexture(GL_TEXTURE_2D, m_MainMenuTexture.getId());
                break;
            case HELP:
                glBindTexture(GL_TEXTURE_2D, m_HelpMenuTexture.getId());
                break;
        }
    }

    private void startNewGame(IGameWorld.Mode mode) {
        m_AppStateContext.changeState(new GameLoadingAppState(m_AppStateContext, mode));
    }
}
