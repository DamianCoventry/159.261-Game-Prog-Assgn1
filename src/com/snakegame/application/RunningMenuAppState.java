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
import com.snakegame.opengl.*;
import com.snakegame.rules.IGameController;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.*;

import static org.lwjgl.glfw.GLFW.*;

public class RunningMenuAppState implements IAppState {
    private static final float s_CameraZPosition = 5.0f;
    private static final float s_AppleXPosition = 0.65f;
    private final IAppStateContext m_AppStateContext;
    private final IGameView m_View;
    private final Matrix4f m_MvpMatrix;
    private final Matrix4f m_ViewMatrix;
    private final Matrix4f m_ModelMatrix;
    private GLStaticPolyhedron[] m_Rectangles;
    private GLStaticPolyhedron m_AppleDisplayMesh;
    private enum Page {MAIN, HELP }
    private Page m_Page;
    private float m_Angle;

    public RunningMenuAppState(IAppStateContext context) {
        m_AppStateContext = context;
        m_View = m_AppStateContext.getView();
        m_MvpMatrix = new Matrix4f();
        m_ViewMatrix = new Matrix4f().translate(0.0f, 0.0f, -s_CameraZPosition);
        m_ModelMatrix = new Matrix4f();
        m_Angle = 0.0f;
    }

    @Override
    public void begin(long nowMs) throws Exception {
        m_AppleDisplayMesh = m_View.loadDisplayMesh("meshes\\AppleHiResDisplayMesh.obj");

        m_Rectangles = new GLStaticPolyhedron[2];

        GLTexture mainMenuTexture = new GLTexture(ImageIO.read(new File("images\\MainMenu.png")));
        m_Rectangles[0] = m_View.createCenteredRectangle(mainMenuTexture.getWidth(), mainMenuTexture.getHeight(), mainMenuTexture);

        GLTexture helpMenuTexture = new GLTexture(ImageIO.read(new File("images\\HelpMenu.png")));
        m_Rectangles[1] = m_View.createCenteredRectangle(helpMenuTexture.getWidth(), helpMenuTexture.getHeight(), helpMenuTexture);

        m_Page = Page.MAIN;
    }

    @Override
    public void end(long nowMs) {
        m_Rectangles[1].freeNativeResources();
        m_Rectangles[0].freeNativeResources();
        m_AppleDisplayMesh.freeNativeResources();
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
        animateApple();
        drawApple();
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        float oneThird = m_AppStateContext.getWindowWidth() * 0.3333f;
        float halfWidth = m_Rectangles[0].getPiece(0).getDiffuseTexture().getWidth() / 2.0f;
        m_ModelMatrix.identity();
        m_ModelMatrix.setTranslation(-(oneThird - halfWidth), 0.0f, 0.0f);
        switch (m_Page) {
            case MAIN:
                m_View.drawOrthographicPolyhedron(m_Rectangles[0], m_ModelMatrix);
                break;
            case HELP:
                //m_View.drawOrthographicPolyhedron(m_Rectangles[1]);
                break;
        }
    }

    private void startNewGame(IGameController.Mode mode) {
        m_AppStateContext.changeState(new GameLoadingAppState(m_AppStateContext, mode));
    }

    private void animateApple() {
        m_Angle += 0.5f;
        if (m_Angle >= 360.0f) {
            m_Angle -= 360.0f;
        }
    }

    private void drawApple() {
        m_ModelMatrix.identity();
        m_ModelMatrix.setTranslation(s_AppleXPosition, 0.0f, 0.0f)
                     .rotate((float)Math.toRadians(m_Angle), 0.0f, 1.0f, 0.0f);

        m_MvpMatrix.identity();
        m_MvpMatrix.set(m_AppStateContext.getPerspectiveMatrix())
                   .mul(m_ViewMatrix)
                   .mul(m_ModelMatrix);

        m_View.getTexturedShaderProgram().activate(m_MvpMatrix);
        m_AppleDisplayMesh.draw();
    }
}
