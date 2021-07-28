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
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.io.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glDepthMask;

public class RunningMenuAppState implements IAppState {
    private static final float s_CameraZPosition = 5.0f;
    private static final float s_AppleXPosition = 0.65f;
    private static final float s_AppleZPosition = 1.0f;
    private static final float s_HorizontalScrollSpeed = 50.0f; // pixels per second
    private static final float s_MsPerFrame = 0.01666666f;
    private static final float s_BackgroundAlpha = 0.075f;
    private static final float s_LightIntensity = 2.0f;
    private static final float s_LightShininess = 16.0f;

    private final IAppStateContext m_Context;
    private final IGameView m_View;
    private final Matrix4f m_MvMatrix;
    private final Matrix4f m_ProjectionMatrix;
    private final Matrix4f m_ViewMatrix;
    private final Matrix4f m_ModelMatrix;
    private final Vector3f m_LightDirection;
    private final Vector3f m_AmbientLight;

    private GLStaticPolyhedronVxTc m_BackgroundTextRectangle;
    private GLStaticPolyhedronVxTc m_BackgroundRectangle;
    private GLStaticPolyhedronVxTc[] m_MenuPageRectangles;
    private GLStaticPolyhedronVxTcNm m_AppleDisplayMesh;
    private enum Page {MAIN, HELP }
    private Page m_Page;
    private float m_Angle;
    private float m_ScrollOffsetX;

    public RunningMenuAppState(IAppStateContext context) {
        m_Context = context;
        m_View = m_Context.getView();

        m_MvMatrix = new Matrix4f();
        m_ProjectionMatrix = new Matrix4f();
        m_ViewMatrix = new Matrix4f().translate(0.0f, 0.0f, -s_CameraZPosition);
        m_ModelMatrix = new Matrix4f();

        m_Angle = m_ScrollOffsetX = 0.0f;

        m_LightDirection = new Vector3f(-10.0f, 7.50f, 8.75f).normalize();
        m_AmbientLight = new Vector3f(0.05f, 0.05f, 0.05f);
    }

    @Override
    public void begin(long nowMs) throws Exception {
        m_View.unloadResources();

        m_AppleDisplayMesh = m_View.loadDisplayMeshWithNormals("meshes\\AppleHiResDisplayMesh.obj");

        GLTexture backgroundTexture = new GLTexture(ImageIO.read(new File("images\\MainMenuBackground.png")));
        m_BackgroundRectangle = m_View.createRectangle(0, 0, backgroundTexture.getWidth(), backgroundTexture.getHeight(), backgroundTexture);

        GLTexture backgroundTextTexture = new GLTexture(ImageIO.read(new File("images\\MainMenuBackgroundText.png")));
        m_BackgroundTextRectangle = m_View.createRectangle(0, 0, backgroundTextTexture.getWidth(), backgroundTextTexture.getHeight(), backgroundTextTexture);

        m_MenuPageRectangles = new GLStaticPolyhedronVxTc[2];
        GLTexture mainMenuTexture = new GLTexture(ImageIO.read(new File("images\\MainMenu.png")));
        m_MenuPageRectangles[0] = m_View.createCenteredRectangle(mainMenuTexture.getWidth(), mainMenuTexture.getHeight(), mainMenuTexture);
        GLTexture helpMenuTexture = new GLTexture(ImageIO.read(new File("images\\HelpMenu.png")));
        m_MenuPageRectangles[1] = m_View.createCenteredRectangle(helpMenuTexture.getWidth(), helpMenuTexture.getHeight(), helpMenuTexture);

        m_Page = Page.MAIN;
    }

    @Override
    public void end(long nowMs) {
        m_MenuPageRectangles[1].freeNativeResources();
        m_MenuPageRectangles[0].freeNativeResources();
        m_BackgroundTextRectangle.freeNativeResources();
        m_BackgroundRectangle.freeNativeResources();
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
        m_ScrollOffsetX += s_HorizontalScrollSpeed * s_MsPerFrame;
        if (m_ScrollOffsetX >= m_Context.getWindowWidth()) {
            m_ScrollOffsetX -= m_Context.getWindowWidth();
        }
    }

    @Override
    public void draw3d(long nowMs) {
        glDepthMask(false);
        m_ModelMatrix.identity();
        m_View.drawOrthographicPolyhedron(m_BackgroundRectangle, m_ModelMatrix);

        m_ModelMatrix.identity().translate(m_ScrollOffsetX, 0.0f, 0.0f);
        m_View.drawOrthographicPolyhedron(m_BackgroundTextRectangle, m_ModelMatrix, s_BackgroundAlpha);

        m_ModelMatrix.identity().translate(m_ScrollOffsetX - m_Context.getWindowWidth(), 0.0f, 0.0f);
        m_View.drawOrthographicPolyhedron(m_BackgroundTextRectangle, m_ModelMatrix, s_BackgroundAlpha);

        glDepthMask(true);
        animateApple();
        drawApple();
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        float oneThird = m_Context.getWindowWidth() * 0.3333f;
        float halfWidth = m_MenuPageRectangles[0].getPiece(0).getDiffuseTexture().getWidth() / 2.0f;
        m_ModelMatrix.identity().translate(-(oneThird - halfWidth), 0.0f, 0.0f);
        switch (m_Page) {
            case MAIN:
                m_View.drawOrthographicPolyhedron(m_MenuPageRectangles[0], m_ModelMatrix);
                break;
            case HELP:
                //m_View.drawOrthographicPolyhedron(m_Rectangles[1]);
                break;
        }
    }

    private void startNewGame(IGameController.Mode mode) {
        m_Context.changeState(new GameLoadingAppState(m_Context, mode));
    }

    private void animateApple() {
        m_Angle += 0.5f;
        if (m_Angle >= 360.0f) {
            m_Angle -= 360.0f;
        }
    }

    private void drawApple() {
        m_ModelMatrix.identity().translate(s_AppleXPosition, 0.0f, s_AppleZPosition)
                     .rotate((float)Math.toRadians(m_Angle), 0.0f, 1.0f, 0.0f);

        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);

        GLSpecularDirectionalLightProgram program = m_View.getSpecularDirectionalLightProgram();
        program.setAmbientLight(m_AmbientLight);
        program.setLightDirection(m_LightDirection);
        program.setLightIntensity(s_LightIntensity);
        program.setShininess(s_LightShininess);
        program.activate(m_MvMatrix, m_ProjectionMatrix);
        m_AppleDisplayMesh.draw();
    }
}
