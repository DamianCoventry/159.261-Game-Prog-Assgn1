package com.snakegame.client;

import com.snakegame.application.IAppStateContext;
import com.snakegame.opengl.GLStaticPolyhedron;
import com.snakegame.opengl.GLTexture;
import com.snakegame.opengl.TexturedShaderProgram;
import com.snakegame.rules.*;
import com.snakegame.rules.Number;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.io.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnd;

public class GameView implements IGameView {
    private static final int s_NumNumbers = 9;
    private static final float s_CellSize = 3.0f;
    private static final float s_CameraZ = -120.0f;

    private final GLTexture[] m_NumberTextures;
    private final GLTexture[] m_PowerUpTextures;
    private final GLTexture m_WallTexture;
    private final GLTexture m_DotTexture;
    private final GLTexture m_HeadTexture;
    private final NumberFont m_NumberFont;
    private final TexturedShaderProgram m_TexturedShaderProgram;
    private final Matrix4f m_ModelMatrix;

    private IAppStateContext m_AppStateContext;
    private IGameController m_Controller;
    private GameField m_GameField;
    private Snake[] m_Snakes;
    private Matrix4f m_ViewMatrix;
    private GLStaticPolyhedron m_Rectangle;

    public GameView() throws IOException {
        m_NumberTextures = new GLTexture[s_NumNumbers];
        for (int i = 0; i < s_NumNumbers; ++i) {
            m_NumberTextures[i] = new GLTexture(ImageIO.read(new File(String.format("images\\Apple%d.png", i + 1))));
        }

        m_PowerUpTextures = new GLTexture[PowerUp.s_NumPowerUps];
        m_PowerUpTextures[0] = new GLTexture(ImageIO.read(new File("images\\DecreaseLength.png")));
        m_PowerUpTextures[1] = new GLTexture(ImageIO.read(new File("images\\IncreaseSpeed.png")));
        m_PowerUpTextures[2] = new GLTexture(ImageIO.read(new File("images\\DecreaseSpeed.png")));
        m_PowerUpTextures[3] = new GLTexture(ImageIO.read(new File("images\\IncreaseLives.png")));
        m_PowerUpTextures[4] = new GLTexture(ImageIO.read(new File("images\\DecreaseLives.png")));
        m_PowerUpTextures[5] = new GLTexture(ImageIO.read(new File("images\\IncreasePoints.png")));
        m_PowerUpTextures[6] = new GLTexture(ImageIO.read(new File("images\\DecreasePoints.png")));
        m_PowerUpTextures[7] = new GLTexture(ImageIO.read(new File("images\\Random.png")));

        m_WallTexture = new GLTexture(ImageIO.read(new File("images\\Wall.jpg")));
        m_DotTexture = new GLTexture(ImageIO.read(new File("images\\dot.png")));
        m_HeadTexture = new GLTexture(ImageIO.read(new File("images\\head.png")));
        m_NumberFont = new NumberFont();
        m_TexturedShaderProgram = new TexturedShaderProgram();

        m_ViewMatrix = new Matrix4f();
        m_ViewMatrix.transform(new Vector4f(0, 0, s_CameraZ, 1.0f));
        m_ModelMatrix = new Matrix4f();
    }

    @Override
    public void setAppStateContext(IAppStateContext appStateContext) {
        m_AppStateContext = appStateContext;
        m_Controller = m_AppStateContext.getController();
        m_GameField = m_Controller.getGameField();
        m_Snakes = m_Controller.getSnakes();
        createRectangle(400, 300); // TODO
    }

    @Override
    public TexturedShaderProgram getTexturedShaderProgram() {
        return m_TexturedShaderProgram;
    }

    @Override
    public void freeNativeResources() {
        for (int i = 0; i < s_NumNumbers; ++i) {
            m_NumberTextures[i].freeNativeResource();
        }
        for (int i = 0; i < PowerUp.s_NumPowerUps; ++i) {
            m_PowerUpTextures[i].freeNativeResource();
        }
        m_WallTexture.freeNativeResource();
        m_DotTexture.freeNativeResource();
        m_HeadTexture.freeNativeResource();
        m_NumberFont.freeNativeResource();
        m_TexturedShaderProgram.freeNativeResource();
        m_Rectangle.freeNativeResources();
    }

    @Override
    public void draw3d(long nowMs) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }

        float maxWidth = GameField.WIDTH * s_CellSize;
        float maxHeight = GameField.HEIGHT * s_CellSize;
        float startX = -maxWidth / 2.0f;
        float startY = -maxHeight / 2.0f;
        float u = 8*s_CellSize / m_WallTexture.getWidth();
        float v = 8*s_CellSize / m_WallTexture.getHeight();

        for (int y = 0; y < GameField.HEIGHT; ++y) {
            float cellOffsetY = startY + y * s_CellSize;
            for (int x = 0; x < GameField.WIDTH; ++x) {
                float cellOffsetX = startX + x * s_CellSize;

                switch (m_GameField.getCellType(x, y)) {
                    case EMPTY:
                        break;
                    case WALL:
                        drawTexturedQuad(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, x*u, y*v+v, x*u+u, y*v, m_WallTexture);
                        break;
                    case POWER_UP: {
                        PowerUp powerUp = m_GameField.getPowerUp(x, y);
                        switch (powerUp.getType()) {
                            case DEC_LENGTH:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[0]);
                                break;
                            case INC_SPEED:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[1]);
                                break;
                            case DEC_SPEED:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[2]);
                                break;
                            case INC_LIVES:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[3]);
                                break;
                            case DEC_LIVES:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[4]);
                                break;
                            case INC_POINTS:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[5]);
                                break;
                            case DEC_POINTS:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[6]);
                                break;
                            case RANDOM:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[7]);
                                break;
                        }
                        break;
                    }
                    case NUMBER: {
                        Number number = m_GameField.getNumber(x, y);
                        switch (number.getType()) {
                            case NUM_1:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[0]);
                                break;
                            case NUM_2:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[1]);
                                break;
                            case NUM_3:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[2]);
                                break;
                            case NUM_4:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[3]);
                                break;
                            case NUM_5:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[4]);
                                break;
                            case NUM_6:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[5]);
                                break;
                            case NUM_7:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[6]);
                                break;
                            case NUM_8:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[7]);
                                break;
                            case NUM_9:
                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[8]);
                                break;
                        }
                        break;
                    }
                }
            }
        }

        boolean firstLoop;
        for (var snake : m_Snakes) {
            firstLoop = true;
            for (var bodyPart : snake.getBodyParts()) {
                float cellOffsetX = startX + bodyPart.m_X * s_CellSize;
                float cellOffsetY = startY + bodyPart.m_Y * s_CellSize;
                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, firstLoop ? m_HeadTexture : m_DotTexture);
                firstLoop = false;
            }
        }
    }

    @Override
    public void draw2d(long nowMs) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        float y = m_AppStateContext.getWindowHeight() - NumberFont.s_FrameHeight;

        // Draw the level's state
        float width = m_NumberFont.calculateWidth(m_Controller.getCurrentLevel() + 1);
        m_NumberFont.drawNumber(m_Controller.getCurrentLevel() + 1, (m_AppStateContext.getWindowWidth() / 2.0f) - (2.0f * width), y);
        width = m_NumberFont.calculateWidth(m_Controller.getLevelCount());
        m_NumberFont.drawNumber(m_Controller.getLevelCount(), (m_AppStateContext.getWindowWidth() / 2.0f) + width, y);

        // Draw player 1's state
        m_NumberFont.drawNumber(m_Snakes[0].getNumLives(), 0.0f, y);
        m_NumberFont.drawNumber(m_Snakes[0].getPoints(), 100.0f, y);

        if (m_Snakes.length > 1) {
            // Draw player 2's state
            width = m_NumberFont.calculateWidth(m_Snakes[1].getNumLives());
            m_NumberFont.drawNumber(m_Snakes[1].getNumLives(), m_AppStateContext.getWindowWidth() - width, y);
            width = m_NumberFont.calculateWidth(m_Snakes[1].getPoints()) + (2.0f * width);
            m_NumberFont.drawNumber(m_Snakes[1].getPoints(), m_AppStateContext.getWindowWidth() - width, y);
        }
    }

    @Override
    public void drawCenteredImage(GLTexture texture) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }

        Matrix4f projection = m_AppStateContext.getOrthographicMatrix();
        Matrix4f mvpMatrix = m_ModelMatrix.mul(m_ViewMatrix.mul(projection));
        m_TexturedShaderProgram.activate(mvpMatrix, texture);
        m_Rectangle.draw();

//        glColor4d(1.0, 1.0, 1.0, 1.0);
//        glBindTexture(GL_TEXTURE_2D, texture.getId());
//        var w = texture.getWidth();
//        var h = texture.getHeight();
//        var x = (m_AppStateContext.getWindowWidth() / 2.0f) - (w / 2.0f);
//        var y = (m_AppStateContext.getWindowHeight() / 2.0f) - (h / 2.0f);
//        glBegin(GL_QUADS);
//        glTexCoord2d(0.0, 0.0); glVertex3d(x, y + h, 0.1f);
//        glTexCoord2d(0.0, 1.0); glVertex3d(x , y, 0.1f);
//        glTexCoord2d(1.0, 1.0); glVertex3d(x + w, y, 0.1f);
//        glTexCoord2d(1.0, 0.0); glVertex3d(x + w, y + h, 0.1f);
//        glEnd();
    }

    private void createRectangle(float w, float h) {
        var x = (m_AppStateContext.getWindowWidth() / 2.0f) - (w / 2.0f);
        var y = (m_AppStateContext.getWindowHeight() / 2.0f) - (h / 2.0f);

        float[] vertices = new float[]{
                // triangle 0
                x, y + h, 0.1f,
                x, y, 0.1f,
                x + w, y, 0.1f,
                // triangle 1
                x, y + h, 0.1f,
                x + w, y, 0.1f,
                x + w, y + h, 0.1f
        };
        float[] texCoordinates = new float[]{
                // triangle 0
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                // triangle 1
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        m_Rectangle = new GLStaticPolyhedron(vertices, texCoordinates);
    }

    private void drawTexturedQuad(double x, double y, double w, double h, double u0, double v0, double u1, double v1, GLTexture GLTexture) {
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, GLTexture.getId());

        glBegin(GL_QUADS);
        glTexCoord2d(u0, v0); glVertex2d(x, y + h);
        glTexCoord2d(u0, v1); glVertex2d(x , y);
        glTexCoord2d(u1, v1); glVertex2d(x + w, y);
        glTexCoord2d(u1, v0); glVertex2d(x + w, y + h);
        glEnd();
    }

    private void drawSingleImage(double x, double y, double w, double h, GLTexture GLTexture) {
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, GLTexture.getId());

        glBegin(GL_QUADS);
        glTexCoord2d(0.0, 0.0); glVertex3d(x, y + h, 0.1f);
        glTexCoord2d(0.0, 1.0); glVertex3d(x , y, 0.1f);
        glTexCoord2d(1.0, 1.0); glVertex3d(x + w, y, 0.1f);
        glTexCoord2d(1.0, 0.0); glVertex3d(x + w, y + h, 0.1f);
        glEnd();
    }
}
