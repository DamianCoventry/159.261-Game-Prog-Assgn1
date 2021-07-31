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

import com.snakegame.client.*;
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
    private static final float s_ScrollWheelScale = 20.0f;
    private static final float s_MaxScrollOffsetY = 0.0f;
    private static final float s_HelpTextFadeRange = 100.0f;
    private static final float s_HelpTextPadding = 2.0f * s_HelpTextFadeRange;

    private final IAppStateContext m_Context;
    private final IGameView m_View;
    private final Matrix4f m_MvMatrix;
    private final Matrix4f m_ProjectionMatrix;
    private final Matrix4f m_ViewMatrix;
    private final Matrix4f m_ModelMatrix;
    private final Vector3f m_LightDirection;
    private final Vector3f m_AmbientLight;

    private GLStaticPolyhedronVxTc m_BackgroundTextPolyhedron;
    private GLStaticPolyhedronVxTc m_BackgroundPolyhedron;
    private GLStaticPolyhedronVxTc[] m_MenuPagePolyhedra;
    private GLStaticPolyhedronVxTcNm m_AppleDisplayMesh;

    private Button m_SinglePlayerGameButton;
    private Button m_TwoPlayersGameButton;
    private Button m_HelpGameButton;
    private Button m_ExitGameButton;
    private Button m_BackGameButton;

    private enum Page {MAIN, HELP }
    private Page m_Page;
    private float m_Angle;
    private float m_ScrollOffsetX;
    private float m_ScrollOffsetY;
    private float m_MinScrollOffsetY;
    private boolean m_DraggingHelpText;
    private double m_DragOffsetY;

    public RunningMenuAppState(IAppStateContext context) {
        m_Context = context;
        m_View = m_Context.getView();
        m_DraggingHelpText = false;

        m_MvMatrix = new Matrix4f();
        m_ProjectionMatrix = new Matrix4f();
        m_ViewMatrix = new Matrix4f().translate(0.0f, 0.0f, -s_CameraZPosition);
        m_ModelMatrix = new Matrix4f();

        m_Angle = m_ScrollOffsetX = m_ScrollOffsetY = 0.0f;

        m_LightDirection = new Vector3f(-10.0f, 7.50f, 8.75f).normalize();
        m_AmbientLight = new Vector3f(0.05f, 0.05f, 0.05f);
    }

    @Override
    public void begin(long nowMs) throws Exception {
        m_View.unloadResources();

        m_AppleDisplayMesh = m_View.loadDisplayMeshWithNormals("meshes\\AppleHiResDisplayMesh.obj");

        loadMenuBackground();
        loadButtons();

        m_View.activateArrowMouseCursor();
        m_Page = Page.MAIN;
    }

    @Override
    public void end(long nowMs) {
        for (var menuPage : m_MenuPagePolyhedra) {
            menuPage.freeNativeResources();
        }
        m_BackgroundTextPolyhedron.freeNativeResources();
        m_BackgroundPolyhedron.freeNativeResources();
        m_AppleDisplayMesh.freeNativeResources();
        m_SinglePlayerGameButton.freeNativeResources();
        m_TwoPlayersGameButton.freeNativeResources();
        m_HelpGameButton.freeNativeResources();
        m_ExitGameButton.freeNativeResources();
        m_BackGameButton.freeNativeResources();
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) throws IOException {
        switch (m_Page) {
            case MAIN:
                processMainPageKey(key, action);
                break;
            case HELP:
                processHelpPageKey(key, action);
                break;
        }
    }

    @Override
    public void processMouseButton(long window, int button, int action, int mods) {
        GLWindow.CursorPosition cursorPosition = m_Context.getMouseCursorPosition();
        double yPosInverted = m_Context.getWindowHeight() - cursorPosition.m_YPos;
        switch (m_Page) {
            case MAIN:
                if (action == GLFW_PRESS) {
                    processMainPageMouseButtonPressed(button, (float)cursorPosition.m_XPos, (float)yPosInverted);
                }
                else if (action == GLFW_RELEASE) {
                    processMainPageMouseButtonReleased(button, (float)cursorPosition.m_XPos, (float)yPosInverted);
                }
                break;
            case HELP:
                if (action == GLFW_PRESS) {
                    processHelpPageMouseButtonPressed(button, (float)cursorPosition.m_XPos, (float)yPosInverted);
                }
                else if (action == GLFW_RELEASE) {
                    processHelpPageMouseButtonRelease(button, (float)cursorPosition.m_XPos, (float)yPosInverted);
                }
                break;
        }
    }

    @Override
    public void processMouseWheel(long window, double xOffset, double yOffset) {
        if (m_Page == Page.HELP) {
            setScrollOffsetY(m_ScrollOffsetY - yOffset * s_ScrollWheelScale);
        }
    }

    @Override
    public void processMouseCursorMovement(long window, double xPos, double yPos) {
        float yPosInverted = m_Context.getWindowHeight() - (float)yPos;
        switch (m_Page) {
            case MAIN:
                processMainPageCursorMovement((float)xPos, yPosInverted);
                break;
            case HELP:
                processHelpPageCursorMovement((float)xPos, yPosInverted);
                break;
        }
    }

    @Override
    public void think(long nowMs) {
        m_ScrollOffsetX += s_HorizontalScrollSpeed * s_MsPerFrame;
        if (m_ScrollOffsetX >= m_Context.getWindowWidth()) {
            m_ScrollOffsetX -= m_Context.getWindowWidth();
        }

        animateApple();
    }

    @Override
    public void draw3d(long nowMs) {
        drawBackground();
        drawApple();
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        switch (m_Page) {
            case MAIN:
                drawMainMenuPage();
                break;
            case HELP:
                drawHelpMenuPage();
                break;
        }
    }

    private void loadMenuBackground() throws IOException {
        GLTexture backgroundTexture = new GLTexture(ImageIO.read(new File("images\\MainMenuBackground.png")));
        m_BackgroundPolyhedron = m_View.createPolyhedron(0, 0, backgroundTexture.getWidth(), backgroundTexture.getHeight(), backgroundTexture);

        GLTexture backgroundTextTexture = new GLTexture(ImageIO.read(new File("images\\MainMenuBackgroundText.png")));
        m_BackgroundTextPolyhedron = m_View.createPolyhedron(0, 0, backgroundTextTexture.getWidth(), backgroundTextTexture.getHeight(), backgroundTextTexture);

        m_MenuPagePolyhedra = new GLStaticPolyhedronVxTc[2];

        GLTexture mainMenuTexture = new GLTexture(ImageIO.read(new File("images\\MainMenu.png")));
        m_MenuPagePolyhedra[0] = m_View.createCenteredPolyhedron(mainMenuTexture.getWidth(), mainMenuTexture.getHeight(), mainMenuTexture);

        GLTexture helpMenuTexture = new GLTexture(ImageIO.read(new File("images\\HelpMenu.png")));
        m_MenuPagePolyhedra[1] = m_View.createPolyhedron(0, 0, helpMenuTexture.getWidth(), helpMenuTexture.getHeight(), helpMenuTexture);

        setInitialScrollOffsetY(m_MenuPagePolyhedra[1].getPiece(0).getDiffuseTexture().getHeight());
    }

    private void loadButtons() throws IOException {
        float oneThird = (float)Math.floor(m_Context.getWindowWidth() * 0.3333f);
        float y = m_Context.getWindowHeight() - 384.0f;

        m_SinglePlayerGameButton = new Button(m_View,
                "images\\SinglePlayerNP.png",
                "images\\SinglePlayerNPF.png",
                "images\\SinglePlayerP.png");
        float x = oneThird - (m_SinglePlayerGameButton.getWidth() / 2.0f) - 22.0f;
        m_SinglePlayerGameButton.setPosition(x, y);
        m_SinglePlayerGameButton.setOnClickFunction(
                (value) -> startNewGame(IGameController.Mode.SINGLE_PLAYER));
        y -= m_SinglePlayerGameButton.getHeight();

        m_TwoPlayersGameButton = new Button(m_View,
                "images\\TwoPlayersNP.png",
                "images\\TwoPlayersNPF.png",
                "images\\TwoPlayersP.png");
        m_TwoPlayersGameButton.setPosition(x, y);
        m_TwoPlayersGameButton.setOnClickFunction(
                (value) -> startNewGame(IGameController.Mode.TWO_PLAYERS));
        y -= m_TwoPlayersGameButton.getHeight();

        m_HelpGameButton = new Button(m_View,
                "images\\HelpNP.png",
                "images\\HelpNPF.png",
                "images\\HelpP.png");
        m_HelpGameButton.setPosition(x, y);
        m_HelpGameButton.setOnClickFunction((value) -> m_Page = Page.HELP);
        y -= m_HelpGameButton.getHeight();

        m_ExitGameButton = new Button(m_View,
                "images\\ExitNP.png",
                "images\\ExitNPF.png",
                "images\\ExitP.png");
        m_ExitGameButton.setPosition(x, y);
        m_ExitGameButton.setOnClickFunction((value) -> m_Context.exitApplication());

        m_BackGameButton = new Button(m_View,
                "images\\BackNP.png",
                "images\\BackNPF.png",
                "images\\BackP.png");
        float halfWindowWidth = m_Context.getWindowWidth() / 2.0f;
        float halfButtonWidth = m_BackGameButton.getWidth() / 2.0f;
        m_BackGameButton.setPosition(halfWindowWidth - halfButtonWidth, -10.0f);
        m_BackGameButton.setOnClickFunction((value) -> {
            m_View.activateArrowMouseCursor();
            m_Page = Page.MAIN;
        });
    }

    private void processMainPageKey(int key, int action) {
        if (action == GLFW_PRESS) {
            switch (key) {
                case GLFW_KEY_1:
                    m_SinglePlayerGameButton.click(0);
                    break;
                case GLFW_KEY_2:
                    m_TwoPlayersGameButton.click(1);
                    break;
                case GLFW_KEY_3:
                    m_HelpGameButton.click(2);
                    break;
                case GLFW_KEY_4:
                    m_ExitGameButton.click(3);
                    break;
            }
        }
    }

    private void processHelpPageKey(int key, int action) {
        switch (key) {
            case GLFW_KEY_B:
                if (action == GLFW_PRESS) {
                    m_View.activateArrowMouseCursor();
                    m_Page = Page.MAIN;
                }
                break;
            case GLFW_KEY_UP: {
                if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                    setScrollOffsetY(m_ScrollOffsetY - s_ScrollWheelScale);
                }
                break;
            }
            case GLFW_KEY_DOWN: {
                if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                    setScrollOffsetY(m_ScrollOffsetY + s_ScrollWheelScale);
                }
                break;
            }
        }
    }

    private void processMainPageMouseButtonPressed(int button, float xPos, float yPos) {
        m_SinglePlayerGameButton.processMouseButtonPressed(button, xPos, yPos);
        m_TwoPlayersGameButton.processMouseButtonPressed(button, xPos, yPos);
        m_HelpGameButton.processMouseButtonPressed(button, xPos, yPos);
        m_ExitGameButton.processMouseButtonPressed(button, xPos, yPos);
    }

    private void processMainPageMouseButtonReleased(int button, float xPos, float yPos) {
        m_SinglePlayerGameButton.processMouseButtonReleased(button, xPos, yPos);
        m_TwoPlayersGameButton.processMouseButtonReleased(button, xPos, yPos);
        m_HelpGameButton.processMouseButtonReleased(button, xPos, yPos);
        m_ExitGameButton.processMouseButtonReleased(button, xPos, yPos);
    }

    private void processHelpPageMouseButtonPressed(int button, float xPos, float yPos) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            GLWindow.CursorPosition cursorPosition = m_Context.getMouseCursorPosition();
            double yPosInverted = m_Context.getWindowHeight() - cursorPosition.m_YPos;
            if (helpTextContainsPoint(cursorPosition.m_XPos, yPosInverted)) {
                m_DragOffsetY = yPosInverted - m_ScrollOffsetY;
                m_DraggingHelpText = true;
            }
        }
        m_BackGameButton.processMouseButtonPressed(button, xPos, yPos);
    }

    private void processHelpPageMouseButtonRelease(int button, float xPos, float yPos) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            m_DraggingHelpText = false;
        }
        m_BackGameButton.processMouseButtonReleased(button, xPos, yPos);
    }

    private void processMainPageCursorMovement(float xPos, float yPos) {
        m_View.activateArrowMouseCursor();
        m_SinglePlayerGameButton.processMouseCursorMovement(xPos, yPos);
        m_TwoPlayersGameButton.processMouseCursorMovement(xPos, yPos);
        m_HelpGameButton.processMouseCursorMovement(xPos, yPos);
        m_ExitGameButton.processMouseCursorMovement(xPos, yPos);
    }

    private void processHelpPageCursorMovement(float xPos, float yPos) {
        if (helpTextContainsPoint(xPos, yPos)) {
            m_View.activateGrabMouseCursor();
            if (m_DraggingHelpText) {
                setScrollOffsetY(yPos - m_DragOffsetY);
            }
        }
        else {
            m_View.activateArrowMouseCursor();
        }
        m_BackGameButton.processMouseCursorMovement(xPos, yPos);
    }

    private void setInitialScrollOffsetY(float textureHeight) {
        float availableHeight = m_Context.getWindowHeight() - (s_HelpTextPadding * 2.0f);
        if (textureHeight > availableHeight) {
            m_MinScrollOffsetY = -(textureHeight - availableHeight);
        }
        else {
            m_MinScrollOffsetY = 0.0f;
        }
        m_ScrollOffsetY = m_MinScrollOffsetY;
    }

    private void setScrollOffsetY(double value) {
        m_ScrollOffsetY = (float)Math.max(m_MinScrollOffsetY, Math.min(s_MaxScrollOffsetY, value));
    }

    private boolean helpTextContainsPoint(double xPos, double yPos) {
        final float halfWidth = m_MenuPagePolyhedra[1].getPiece(0).getDiffuseTexture().getWidth() / 2.0f;
        final float xCenter = (float)Math.floor(m_Context.getWindowWidth() * 0.3333f);
        final float xMin = xCenter - halfWidth;
        final float xMax = xCenter + halfWidth;
        final float yMin = s_HelpTextPadding;
        final float yMax = m_Context.getWindowHeight() - yMin;
        return xPos >= xMin && xPos <= xMax && yPos >= yMin && yPos <= yMax;
    }

    private void drawMainMenuPage() {
        float oneThird = (float)Math.floor(m_Context.getWindowWidth() * 0.3333f);
        float halfWidth = m_MenuPagePolyhedra[0].getPiece(0).getDiffuseTexture().getWidth() / 2.0f;
        m_ModelMatrix.identity().translate(-(oneThird - halfWidth), 0.0f, 0.0f);
        m_View.drawOrthographicPolyhedron(m_MenuPagePolyhedra[0], m_ModelMatrix);

        m_SinglePlayerGameButton.draw2d();
        m_TwoPlayersGameButton.draw2d();
        m_HelpGameButton.draw2d();
        m_ExitGameButton.draw2d();
    }

    private void drawHelpMenuPage() {
        float oneThird = (float)Math.floor(m_Context.getWindowWidth() * 0.3333f);
        float halfWidth = m_MenuPagePolyhedra[1].getPiece(0).getDiffuseTexture().getWidth() / 2.0f;

        m_ModelMatrix.identity().translate(oneThird - halfWidth, m_ScrollOffsetY + s_HelpTextPadding, 0.0f);
        m_View.drawOrthographicPolyhedronWithFadeRange(m_MenuPagePolyhedra[1], m_ModelMatrix, s_HelpTextFadeRange);

        m_BackGameButton.draw2d();
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

    private void drawBackground() {
        glDepthMask(false);
        m_ModelMatrix.identity();
        m_View.drawOrthographicPolyhedron(m_BackgroundPolyhedron, m_ModelMatrix);

        m_ModelMatrix.identity().translate(m_ScrollOffsetX, 0.0f, 0.0f);
        m_View.drawOrthographicPolyhedron(m_BackgroundTextPolyhedron, m_ModelMatrix, s_BackgroundAlpha);

        m_ModelMatrix.identity().translate(m_ScrollOffsetX - m_Context.getWindowWidth(), 0.0f, 0.0f);
        m_View.drawOrthographicPolyhedron(m_BackgroundTextPolyhedron, m_ModelMatrix, s_BackgroundAlpha);
        glDepthMask(true);
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
