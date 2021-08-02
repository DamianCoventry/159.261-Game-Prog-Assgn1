package com.snakegame.client;

import com.snakegame.application.IAppStateContext;
import com.snakegame.opengl.GLStaticPolyhedronVxTc;
import com.snakegame.opengl.GLTexture;
import com.snakegame.rules.IGameController;
import com.snakegame.rules.Vector2i;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.glDepthMask;

public class Toolbar {
    private static final float s_TextAnimationSpeed = 6.0f;
    private static final float s_HorizontalScrollSpeed = 50.0f; // pixels per second
    private static final float s_MsPerFrame = 0.01666666f;
    private static final float s_BackgroundAlpha = 0.075f;

    private static final int s_P1RemainingSnakesAnimation = 0;
    private static final int s_P1ScoreAnimation = 1;
    private static final int s_P2RemainingSnakesAnimation = 2;
    private static final int s_P2ScoreAnimation = 3;

    private static final Vector2i s_P1Snakes = new Vector2i(20, 905);
    private static final Vector2i s_P1Score = new Vector2i(110, 905);
    private static final Vector2i s_P2Snakes = new Vector2i(1046, 905);
    private static final Vector2i s_P2Score = new Vector2i(1134, 905);
    private static final Vector2i s_CurrentLevel = new Vector2i(594, 905);
    private static final Vector2i s_NumLevels = new Vector2i(650, 905);
    private static final Vector4f s_Yellow = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);

    private final IAppStateContext m_Context;
    private final IGameView m_View;
    private final IGameController m_Controller;
    private final Matrix4f m_ModelMatrix;
    private final NumberFont m_NumberFont;

    private GLStaticPolyhedronVxTc m_AnimatedText;
    private GLStaticPolyhedronVxTc m_Plaques;
    private GLStaticPolyhedronVxTc m_Gradient;
    private float m_ScrollOffsetX;

    private static class Animation {
        private float m_Value;
        private Vector4f m_Colour;
        public Animation() {
            m_Value = 1.0f;
            m_Colour = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
        public float getValue() {
            return m_Value;
        }
        public void setValue(float value) {
            m_Value = value;
        }
        public Vector4f getColour() {
            return m_Colour;
        }
        public void setColour(Vector4f value) {
            m_Colour = value;
        }
    }
    private final Animation[] m_TextAnimations;
    
    public Toolbar(IAppStateContext context) throws IOException {
        m_Context = context;
        m_View = m_Context.getView();
        m_Controller = m_Context.getController();

        m_ModelMatrix = new Matrix4f();
        m_NumberFont = new ToolbarNumberFont(m_View.getTexturedProgram());
        m_ScrollOffsetX = 0.0f;
        
        m_TextAnimations = new Animation[4];
        for (int i = 0; i < 4; ++i) {
            m_TextAnimations[i] = new Animation();
            m_TextAnimations[i].setColour(s_Yellow);
        }

        GLTexture toolbarTexture = new GLTexture(ImageIO.read(new File("images\\ToolbarBackgroundText.png")));
        float y = m_Context.getWindowHeight() - toolbarTexture.getHeight();
        m_AnimatedText = m_View.createPolyhedron(0.0f, y, toolbarTexture.getWidth(), toolbarTexture.getHeight(), toolbarTexture);

        toolbarTexture = new GLTexture(ImageIO.read(new File("images\\ToolbarPlaques.png")));
        y = m_Context.getWindowHeight() - toolbarTexture.getHeight();
        m_Plaques = m_View.createPolyhedron(0.0f, y, toolbarTexture.getWidth(), toolbarTexture.getHeight(), toolbarTexture);

        toolbarTexture = new GLTexture(ImageIO.read(new File("images\\ToolbarBackgroundGradient.png")));
        y = m_Context.getWindowHeight() - toolbarTexture.getHeight();
        m_Gradient = m_View.createPolyhedron(0.0f, y, toolbarTexture.getWidth(), toolbarTexture.getHeight(), toolbarTexture);
    }
    
    void freeNativeResources() {
        if (m_AnimatedText != null) {
            m_AnimatedText.freeNativeResources();
            m_AnimatedText = null;
        }
        if (m_Plaques != null) {
            m_Plaques.freeNativeResources();
            m_Plaques = null;
        }
        if (m_Gradient != null) {
            m_Gradient.freeNativeResources();
            m_Gradient = null;
        }
        m_NumberFont.freeNativeResource();
    }

    public void startRemainingSnakesAnimation(int playerId, Vector4f colour) {
        int i = playerId == 0 ? s_P1RemainingSnakesAnimation : s_P2RemainingSnakesAnimation;
        m_TextAnimations[i].setValue(2.0f);
        m_TextAnimations[i].setColour(colour);
    }

    public void startScoreAnimation(int playerId, Vector4f colour) {
        int i = playerId == 0 ? s_P1ScoreAnimation : s_P2ScoreAnimation;
        m_TextAnimations[i].setValue(2.0f);
        m_TextAnimations[i].setColour(colour);
    }

    public void think(long nowMs) {
        m_ScrollOffsetX += s_HorizontalScrollSpeed * s_MsPerFrame;
        if (m_ScrollOffsetX >= m_Context.getWindowWidth()) {
            m_ScrollOffsetX -= m_Context.getWindowWidth();
        }
    }

    public void draw2d(long nowMs) {
        updateTextAnimations();

        glDepthMask(false);
        m_ModelMatrix.identity();
        m_View.drawOrthographicPolyhedron(m_Gradient, m_ModelMatrix);

        m_ModelMatrix.identity().translate(m_ScrollOffsetX, 0.0f, 0.0f);
        m_View.drawOrthographicPolyhedron(m_AnimatedText, m_ModelMatrix, s_BackgroundAlpha);

        m_ModelMatrix.identity().translate(m_ScrollOffsetX - m_Context.getWindowWidth(), 0.0f, 0.0f);
        m_View.drawOrthographicPolyhedron(m_AnimatedText, m_ModelMatrix, s_BackgroundAlpha);

        m_ModelMatrix.identity();
        m_View.drawOrthographicPolyhedron(m_Plaques, m_ModelMatrix);
        glDepthMask(true);

        int level = m_Controller.getCurrentLevel() + 1;
        Matrix4f projectionMatrix = m_Context.getOrthographicMatrix();

        // Draw the level's state
        if (level < 10) {
            m_NumberFont.drawNumber(projectionMatrix, level, s_CurrentLevel.m_X, s_CurrentLevel.m_Z, 1.0f, s_Yellow);
        }
        else {
            m_NumberFont.drawNumber(projectionMatrix, level, s_CurrentLevel.m_X - 8.0f, s_CurrentLevel.m_Z, 1.0f, s_Yellow);
        }
        m_NumberFont.drawNumber(projectionMatrix, m_Controller.getLevelCount(), s_NumLevels.m_X, s_NumLevels.m_Z, 1.0f, s_Yellow);

        // Draw player 1's state
        Animation animation = m_TextAnimations[s_P1RemainingSnakesAnimation];
        m_NumberFont.drawNumber(projectionMatrix, m_Controller.getSnakes()[0].getNumLives(), s_P1Snakes.m_X, s_P1Snakes.m_Z,
                animation.getValue(), animation.getColour());
        animation = m_TextAnimations[s_P1ScoreAnimation];
        m_NumberFont.drawNumber(projectionMatrix, m_Controller.getSnakes()[0].getPoints(), s_P1Score.m_X, s_P1Score.m_Z,
                animation.getValue(), animation.getColour());

        if (m_Controller.getSnakes().length > 1) {
            // Draw player 2's state
            animation = m_TextAnimations[s_P2RemainingSnakesAnimation];
            m_NumberFont.drawNumber(projectionMatrix, m_Controller.getSnakes()[1].getNumLives(), s_P2Snakes.m_X, s_P2Snakes.m_Z,
                    animation.getValue(), animation.getColour());
            animation = m_TextAnimations[s_P2ScoreAnimation];
            m_NumberFont.drawNumber(projectionMatrix, m_Controller.getSnakes()[1].getPoints(), s_P2Score.m_X, s_P2Score.m_Z,
                    animation.getValue(), animation.getColour());
        }
    }

    private void updateTextAnimations() {
        for (Animation animation : m_TextAnimations) {
            if (animation.getValue() > 1.0f) {
                animation.setValue(animation.getValue() - s_TextAnimationSpeed * s_MsPerFrame);
                if (animation.getValue() < 1.0f) {
                    animation.setValue(1.0f);
                    animation.setColour(s_Yellow);
                }
            }
        }
    }
}
