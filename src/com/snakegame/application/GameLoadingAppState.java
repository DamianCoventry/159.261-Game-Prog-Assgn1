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
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.io.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glDepthMask;
import static org.lwjgl.opengl.GL11C.glPolygonMode;

public class GameLoadingAppState implements IAppState {
    private static final float s_CameraZPosition = 5.0f;
    private static final float s_LightIntensity = 2.0f;
    private static final float s_LightShininess = 16.0f;
    private static final float s_AppleXPosition = 0.65f;
    private static final float s_AppleZPosition = 1.0f;
    private static final float s_MsPerFrame = 0.01666666f;
    private static final float s_AppleMinOnPlaneY = -0.5f;
    private static final float s_AppleMaxOnPlaneY = 0.75f;
    private static final Vector4f s_Yellow = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);

    private final IAppStateContext m_Context;
    private final IGameView m_View;
    private final IGameController.Mode m_Mode;

    private final Matrix4f m_ModelMatrix;
    private final Matrix4f m_ViewMatrix;
    private final Matrix4f m_MvMatrix;
    private final Matrix4f m_ProjectionMatrix;

    private final Vector3f m_AmbientLight;
    private final Vector3f m_LightDirection;
    private final Vector3f m_PlaneNormal;
    private final Vector3f m_PointOnPlane;

    private GLStaticPolyhedronVxTc m_BackgroundPolyhedron;
    private GLStaticPolyhedronVxTcNm m_AppleDisplayMesh;
    private GLStaticPolyhedronVxTc m_LoadingPolyhedron;
    private NumberFont m_NumberFont;

    private float m_FadeUpAlpha;
    private boolean m_FadingUp;
    private long m_LoadPercentage;

    public GameLoadingAppState(IAppStateContext context, IGameController.Mode mode) {
        m_Context = context;
        m_View = m_Context.getView();
        m_MvMatrix = new Matrix4f();
        m_ProjectionMatrix = new Matrix4f();
        m_ViewMatrix = new Matrix4f().translate(0.0f, 0.0f, -s_CameraZPosition);
        m_ModelMatrix = new Matrix4f();
        m_LightDirection = new Vector3f(-10.0f, 7.50f, 8.75f).normalize();
        m_AmbientLight = new Vector3f(0.05f, 0.05f, 0.05f);
        m_PlaneNormal = new Vector3f(0.0f, -1.0f, 0.0f);
        m_PointOnPlane = new Vector3f(0.0f, -0.5f, 0.0f);
        m_Mode = mode;
    }

    @Override
    public void begin(long nowMs) throws Exception {
        m_AppleDisplayMesh = m_View.loadDisplayMesh("meshes\\AppleHiResDisplayMesh.obj");

        GLTexture backgroundTexture = new GLTexture(ImageIO.read(new File("images\\MainMenuBackground.png")));
        m_BackgroundPolyhedron = m_View.createPolyhedron(0, 0, backgroundTexture.getWidth(), backgroundTexture.getHeight(), backgroundTexture);

        GLTexture loadingTexture = new GLTexture(ImageIO.read(new File("images\\Loading.png")));
        m_LoadingPolyhedron = m_View.createPolyhedron(
                m_Context.getWindowWidth() / 2.0f, m_Context.getWindowHeight() / 2.0f,
                loadingTexture.getWidth(), loadingTexture.getHeight(), loadingTexture);

        m_NumberFont = new ToolbarNumberFont(m_View.getTexturedProgram());

        m_View.resetSnakeGiblets();
        m_View.activateArrowMouseCursor();
        m_View.getTexturedProgram().setDiffuseColour(new Vector4f(1.0f));
        m_View.getDirectionalLightProgram().setDiffuseColour(new Vector4f(1.0f));
        m_View.getSpecularDirectionalLightProgram().setDiffuseColour(new Vector4f(1.0f));
        m_View.getDiffuseTextureAlphaFadeProgram().setDiffuseColour(new Vector4f(1.0f));
        m_View.getSpecularDirectionalLightClipPlaneProgram().setDiffuseColour(new Vector4f(1.0f));

        m_FadingUp = true;
        m_FadeUpAlpha = 0.0f;
        m_LoadPercentage = 0;
    }

    @Override
    public void end(long nowMs) {
        m_AppleDisplayMesh.freeNativeResources();
        m_BackgroundPolyhedron.freeNativeResources();
        m_LoadingPolyhedron.freeNativeResources();
        m_NumberFont.freeNativeResource();
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        // No work to do
    }

    @Override
    public void processMouseButton(long window, int button, int action, int mods) {
        // No work to do
    }

    @Override
    public void processMouseWheel(long window, double xOffset, double yOffset) {
        // No work to do
    }

    @Override
    public void processMouseCursorMovement(long window, double xPos, double yPos) {
        // No work to do
    }

    @Override
    public void think(long nowMs) throws IOException {
        if (m_FadingUp) {
            m_FadeUpAlpha += s_MsPerFrame;
            if (m_FadeUpAlpha >= 1.0f) {
                m_FadeUpAlpha = 1.0f;
                m_FadingUp = false;
                startResourceLoad();
            }
        }
    }

    @Override
    public void draw3d(long nowMs) {
        glDepthMask(false);
        drawBackground();
        drawWireframeApple();
        if (!m_FadingUp) {
            drawSolidAppleWithClipPlane();
        }
        glDepthMask(true);
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        if (!m_FadingUp) {
            m_ModelMatrix.identity();
            m_View.drawOrthographicPolyhedron(m_LoadingPolyhedron, m_ModelMatrix);

            float x = m_Context.getWindowWidth() / 2.0f;
            float y = m_Context.getWindowHeight() / 2.0f;

            x += m_LoadingPolyhedron.getPiece(0).getDiffuseTexture().getWidth();
            y += (ToolbarNumberFont.s_FrameHeight / 2.0f) - 10.0f;

            Matrix4f projectionMatrix = m_Context.getOrthographicMatrix();
            m_NumberFont.drawPercentage(projectionMatrix, m_LoadPercentage, x, y, 1.0f, s_Yellow);
        }
    }

    private void startResourceLoad() {
        m_LoadPercentage = 0;
        m_Context.addTimeout(50, (callCount) -> {
            try {
                m_View.loadResources((current, max) -> {
                    try {
                        m_LoadPercentage = current * 100 / max;
                        m_PointOnPlane.y = s_AppleMinOnPlaneY + ((s_AppleMaxOnPlaneY - s_AppleMinOnPlaneY) * (m_LoadPercentage / 100.0f));
                        m_Context.forceThinkAndDraw();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                m_Context.getController().startNewGame(System.currentTimeMillis(), m_Mode);
                m_Context.changeState(new GetReadyAppState(m_Context, true));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    private void drawBackground() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        m_ModelMatrix.identity();
        m_View.drawOrthographicPolyhedron(m_BackgroundPolyhedron, m_ModelMatrix);
    }

    private void drawWireframeApple() {
        m_ModelMatrix.identity().translate(-s_AppleXPosition, 0.0f, s_AppleZPosition);

        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);

        GLSpecularDirectionalLightProgram program = m_View.getSpecularDirectionalLightProgram();
        program.setAmbientLight(m_AmbientLight);
        program.setLightDirection(m_LightDirection);
        program.setLightIntensity(s_LightIntensity);
        program.setShininess(s_LightShininess);
        program.setDiffuseColour(new Vector4f(1.0f, 1.0f, 1.0f, m_FadeUpAlpha));
        program.activate(m_MvMatrix, m_ProjectionMatrix);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        m_AppleDisplayMesh.draw();
    }

    private void drawSolidAppleWithClipPlane() {
        m_ModelMatrix.identity().translate(-s_AppleXPosition, 0.0f, s_AppleZPosition);

        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);

        GLSpecularDirectionalLightClipPlaneProgram program = m_View.getSpecularDirectionalLightClipPlaneProgram();
        program.setAmbientLight(m_AmbientLight);
        program.setLightDirection(m_LightDirection);
        program.setLightIntensity(s_LightIntensity);
        program.setShininess(s_LightShininess);
        program.setPlaneNormal(m_PlaneNormal);
        program.setPointOnPlane(m_PointOnPlane);
        program.activate(m_MvMatrix, m_ProjectionMatrix);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        m_AppleDisplayMesh.draw();
    }
}
