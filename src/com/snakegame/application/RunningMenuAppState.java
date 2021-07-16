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
// Designed and implemented for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.application;

import com.snakegame.client.Texture;
import com.snakegame.rules.GameWorld;
import com.snakegame.rules.IGameWorld;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class RunningMenuAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private Texture m_FrontMenu;
    private Texture m_HelpMenu;
    private enum Page { FRONT, HELP }
    private Page m_Page;

    public RunningMenuAppState(IAppStateContext context) {
        m_AppStateContext = context;
    }

    @Override
    public void onStateBegin() throws IOException {
        m_FrontMenu = new Texture(ImageIO.read(new File("FrontMenu.png")));
        m_HelpMenu = new Texture(ImageIO.read(new File("HelpMenu.png")));
        m_Page = Page.FRONT;
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        setPage(Page.FRONT);
    }

    @Override
    public void onStateEnd() {
        m_FrontMenu.close();
        m_HelpMenu.close();
    }

    @Override
    public void processKeyEvent(long window, int key, int scanCode, int action, int mods) throws IOException {
        if (action != GLFW_PRESS) {
            return;
        }
        if (key == GLFW_KEY_ESCAPE) {
            if (m_Page == Page.FRONT) {
                glfwSetWindowShouldClose(window, true);
            }
            else {
                setPage(Page.FRONT);
            }
        }
        else if (key == GLFW_KEY_1) {
            m_AppStateContext.changeState(new PlayingGameAppState(m_AppStateContext, new GameWorld(IGameWorld.Mode.SINGLE_PLAYER)));
        }
        else if (key == GLFW_KEY_2) {
            m_AppStateContext.changeState(new PlayingGameAppState(m_AppStateContext, new GameWorld(IGameWorld.Mode.TWO_PLAYERS)));
        }
        else if (key == GLFW_KEY_3) {
            setPage(Page.HELP);
        }
    }

    @Override
    public void think(long nowMs) {
        // Nothing to do
    }

    @Override
    public void perspectiveDrawing(long nowMs) {
        // Nothing to do
    }

    @Override
    public void orthographicDrawing(long nowMs) {
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
            case FRONT:
                glBindTexture(GL_TEXTURE_2D, m_FrontMenu.getId());
                break;
            case HELP:
                glBindTexture(GL_TEXTURE_2D, m_HelpMenu.getId());
                break;
        }
    }
}
