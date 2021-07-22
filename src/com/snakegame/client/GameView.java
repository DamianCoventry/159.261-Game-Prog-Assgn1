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

package com.snakegame.client;

import com.snakegame.application.IAppStateContext;
import com.snakegame.opengl.*;
import com.snakegame.rules.*;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;

public class GameView implements IGameView {
    private static final int s_NumNumbers = 9;
    private static final float s_CellSize = 3.0f;
    private static final float s_CameraXRotation = -90.0f;
    private static final float s_CameraYPosition = 95.0f;

    private final GLTexture[] m_NumberTextures;
    private final GLTexture[] m_PowerUpTextures;
    private final GLTexture m_DotTexture;
    private final GLTexture m_HeadTexture;
    private final NumberFont m_NumberFont;
    private final TexturedShaderProgram m_TexturedShaderProgram;
    private final Matrix4f m_ViewMatrix;

    private GLStaticPolyhedron m_WorldMesh;

    private IAppStateContext m_AppStateContext;
    private IGameController m_Controller;
    private GameField m_GameField;
    private Snake[] m_Snakes;

    public GameView() throws Exception {
        m_NumberTextures = new GLTexture[s_NumNumbers];
        for (int i = 0; i < s_NumNumbers; ++i) {
            m_NumberTextures[i] = new GLTexture(ImageIO.read(new File(String.format("images\\Apple%d.png", i + 1))));
        }

        loadWorldMesh();

        m_PowerUpTextures = new GLTexture[PowerUp.s_NumPowerUps];
        m_PowerUpTextures[0] = new GLTexture(ImageIO.read(new File("images\\DecreaseLength.png")));
        m_PowerUpTextures[1] = new GLTexture(ImageIO.read(new File("images\\IncreaseSpeed.png")));
        m_PowerUpTextures[2] = new GLTexture(ImageIO.read(new File("images\\DecreaseSpeed.png")));
        m_PowerUpTextures[3] = new GLTexture(ImageIO.read(new File("images\\IncreaseLives.png")));
        m_PowerUpTextures[4] = new GLTexture(ImageIO.read(new File("images\\DecreaseLives.png")));
        m_PowerUpTextures[5] = new GLTexture(ImageIO.read(new File("images\\IncreasePoints.png")));
        m_PowerUpTextures[6] = new GLTexture(ImageIO.read(new File("images\\DecreasePoints.png")));
        m_PowerUpTextures[7] = new GLTexture(ImageIO.read(new File("images\\Random.png")));

        m_DotTexture = new GLTexture(ImageIO.read(new File("images\\dot.png")));
        m_HeadTexture = new GLTexture(ImageIO.read(new File("images\\head.png")));
        m_TexturedShaderProgram = new TexturedShaderProgram();
        m_NumberFont = new NumberFont(m_TexturedShaderProgram);

        m_ViewMatrix = new Matrix4f();
        m_ViewMatrix.rotate((float)Math.toRadians(-s_CameraXRotation), 1.0f, 0.0f, 0.0f)
                    .translate(0, -s_CameraYPosition, 0.0f);
    }

    @Override
    public void setAppStateContext(IAppStateContext appStateContext) {
        m_AppStateContext = appStateContext;
        m_Controller = m_AppStateContext.getController();
        m_GameField = m_Controller.getGameField();
        m_Snakes = m_Controller.getSnakes();
    }

    @Override
    public void freeNativeResources() {
        for (int i = 0; i < s_NumNumbers; ++i) {
            m_NumberTextures[i].freeNativeResource();
        }
        for (int i = 0; i < PowerUp.s_NumPowerUps; ++i) {
            m_PowerUpTextures[i].freeNativeResource();
        }
        m_DotTexture.freeNativeResource();
        m_HeadTexture.freeNativeResource();
        m_NumberFont.freeNativeResource();
        m_TexturedShaderProgram.freeNativeResource();
        m_WorldMesh.freeNativeResources();
    }

    @Override
    public void draw3d(long nowMs) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }

        Matrix4f projectionMatrix = m_AppStateContext.getPerspectiveMatrix();
        Matrix4f mvpMatrix = new Matrix4f(projectionMatrix);
        m_TexturedShaderProgram.activate(mvpMatrix.mul(m_ViewMatrix));
        m_WorldMesh.draw();

//        float maxWidth = GameField.WIDTH * s_CellSize;
//        float maxHeight = GameField.HEIGHT * s_CellSize;
//        float startX = -maxWidth / 2.0f;
//        float startY = -maxHeight / 2.0f;
//        float u = 8*s_CellSize / m_WallTexture.getWidth();
//        float v = 8*s_CellSize / m_WallTexture.getHeight();
//
//        for (int y = 0; y < GameField.HEIGHT; ++y) {
//            float cellOffsetY = startY + y * s_CellSize;
//            for (int x = 0; x < GameField.WIDTH; ++x) {
//                float cellOffsetX = startX + x * s_CellSize;
//
//                switch (m_GameField.getCellType(x, y)) {
//                    case EMPTY:
//                        break;
//                    case WALL:
//                        drawTexturedQuad(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, x*u, y*v+v, x*u+u, y*v, m_WallTexture);
//                        break;
//                    case POWER_UP: {
//                        PowerUp powerUp = m_GameField.getPowerUp(x, y);
//                        switch (powerUp.getType()) {
//                            case DEC_LENGTH:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[0]);
//                                break;
//                            case INC_SPEED:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[1]);
//                                break;
//                            case DEC_SPEED:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[2]);
//                                break;
//                            case INC_LIVES:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[3]);
//                                break;
//                            case DEC_LIVES:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[4]);
//                                break;
//                            case INC_POINTS:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[5]);
//                                break;
//                            case DEC_POINTS:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[6]);
//                                break;
//                            case RANDOM:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_PowerUpTextures[7]);
//                                break;
//                        }
//                        break;
//                    }
//                    case NUMBER: {
//                        Number number = m_GameField.getNumber(x, y);
//                        switch (number.getType()) {
//                            case NUM_1:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[0]);
//                                break;
//                            case NUM_2:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[1]);
//                                break;
//                            case NUM_3:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[2]);
//                                break;
//                            case NUM_4:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[3]);
//                                break;
//                            case NUM_5:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[4]);
//                                break;
//                            case NUM_6:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[5]);
//                                break;
//                            case NUM_7:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[6]);
//                                break;
//                            case NUM_8:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[7]);
//                                break;
//                            case NUM_9:
//                                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[8]);
//                                break;
//                        }
//                        break;
//                    }
//                }
//            }
//        }
//
//        boolean firstLoop;
//        for (var snake : m_Snakes) {
//            firstLoop = true;
//            for (var bodyPart : snake.getBodyParts()) {
//                float cellOffsetX = startX + bodyPart.m_X * s_CellSize;
//                float cellOffsetY = startY + bodyPart.m_Y * s_CellSize;
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, firstLoop ? m_HeadTexture : m_DotTexture);
//                firstLoop = false;
//            }
//        }
    }

    @Override
    public void draw2d(long nowMs) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        float y = m_AppStateContext.getWindowHeight() - NumberFont.s_FrameHeight;
        float w = m_AppStateContext.getWindowWidth();
        float halfW = w / 2.0f;
        int level = m_Controller.getCurrentLevel() + 1;
        Matrix4f projectionMatrix = m_AppStateContext.getOrthographicMatrix();

        // Draw the level's state
        float width = m_NumberFont.calculateWidth(level);
        m_NumberFont.drawNumber(projectionMatrix, level, halfW - (2.0f * width), y);
        width = m_NumberFont.calculateWidth(m_Controller.getLevelCount());
        m_NumberFont.drawNumber(projectionMatrix, m_Controller.getLevelCount(), halfW + width, y);

        // Draw player 1's state
        m_NumberFont.drawNumber(projectionMatrix, m_Snakes[0].getNumLives(), 0.0f, y);
        m_NumberFont.drawNumber(projectionMatrix, m_Snakes[0].getPoints(), 100.0f, y);

        if (m_Snakes.length > 1) {
            // Draw player 2's state
            width = m_NumberFont.calculateWidth(m_Snakes[1].getNumLives());
            m_NumberFont.drawNumber(projectionMatrix, m_Snakes[1].getNumLives(), w - width, y);
            width = m_NumberFont.calculateWidth(m_Snakes[1].getPoints()) + (2.0f * width);
            m_NumberFont.drawNumber(projectionMatrix, m_Snakes[1].getPoints(), w - width, y);
        }
    }

    @Override
    public void drawOrthographicPolyhedron(GLStaticPolyhedron polyhedron) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        Matrix4f projectionMatrix = m_AppStateContext.getOrthographicMatrix();
        m_TexturedShaderProgram.activate(projectionMatrix);
        polyhedron.draw();
    }

    @Override
    public GLStaticPolyhedron createRectangle(float width, float height, GLTexture texture) {
        var x = (m_AppStateContext.getWindowWidth() / 2.0f) - (width / 2.0f);
        var y = (m_AppStateContext.getWindowHeight() / 2.0f) - (height / 2.0f);

        float[] vertices = new float[]{
                // triangle 0
                x, y + height, 0.1f,
                x, y, 0.1f,
                x + width, y, 0.1f,
                // triangle 1
                x, y + height, 0.1f,
                x + width, y, 0.1f,
                x + width, y + height, 0.1f
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

        GLStaticPolyhedron polyhedron = new GLStaticPolyhedron();
        polyhedron.addPiece(new GLStaticPolyhedronPiece(vertices, texCoordinates, texture));
        return polyhedron;
    }

    private void loadWorldMesh() throws Exception {
        final ObjFile objFile = new ObjFile("meshes\\LevelDisplayMesh.obj");
        if (objFile.getObjects() == null || objFile.getObjects().isEmpty()) {
            throw new RuntimeException("Object file has no objects");
        }

        final ObjFile.Object object = objFile.getObjects().get(0);
        if (object.getPieces() == null || object.getPieces().isEmpty()) {
            throw new RuntimeException("Object has no pieces");
        }

        final ArrayList<MtlFile> materialFiles = new ArrayList<>();
        for (var fileName : objFile.getMaterialFileNames()) {
            materialFiles.add(new MtlFile("meshes\\" + fileName));
        }

        m_WorldMesh = new GLStaticPolyhedron();

        for (var piece : object.getPieces()) {
            GLTexture pieceDiffuseTexture = null;
            for (var materialFile : materialFiles) {
                for (var material : materialFile.getMaterials()) {
                    if (material.getName().equalsIgnoreCase(piece.getMaterialName())) {
                        if (pieceDiffuseTexture != null) {
                            pieceDiffuseTexture.freeNativeResource();
                        }
                        if (material.getDiffuseTexture() == null) {
                            throw new RuntimeException("Material [" + material.getName() + "] does not have a diffuse texture");
                        }
                        pieceDiffuseTexture = new GLTexture(ImageIO.read(new File("meshes\\" + material.getDiffuseTexture())));
                    }
                }
            }
            if (pieceDiffuseTexture == null) {
                throw new RuntimeException("The level file does not have a valid diffuse texture within a piece");
            }

            int numFloats = piece.getFaces().size() * objFile.getVertices().size() * 3;
            int floatCount = 0;
            float[] vertices = new float[numFloats];
            for (int faceIndex = 0; faceIndex < piece.getFaces().size(); ++faceIndex) {
                ObjFile.Face face = piece.getFaces().get(faceIndex);

                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[0]).m_X;
                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[0]).m_Y;
                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[0]).m_Z;

                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[1]).m_X;
                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[1]).m_Y;
                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[1]).m_Z;

                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[2]).m_X;
                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[2]).m_Y;
                vertices[floatCount++] = objFile.getVertices().get(face.m_Vertices[2]).m_Z;
            }

            numFloats = piece.getFaces().size() * objFile.getVertices().size() * 2;
            floatCount = 0;
            float[] texCoordinates = new float[numFloats];
            for (int faceIndex = 0; faceIndex < piece.getFaces().size(); ++faceIndex) {
                ObjFile.Face face = piece.getFaces().get(faceIndex);

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[0]).m_S;
                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[0]).m_T;

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[1]).m_S;
                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[1]).m_T;

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[2]).m_S;
                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[2]).m_T;
            }

            m_WorldMesh.addPiece(new GLStaticPolyhedronPiece(vertices, texCoordinates, pieceDiffuseTexture));
        }
    }

    private void drawTexturedQuad(double x, double y, double w, double h, double u0, double v0, double u1, double v1, GLTexture GLTexture) {
//        glColor4d(1.0, 1.0, 1.0, 1.0);
//        glEnable(GL_TEXTURE_2D);
//        glBindTexture(GL_TEXTURE_2D, GLTexture.getId());
//
//        glBegin(GL_QUADS);
//        glTexCoord2d(u0, v0); glVertex2d(x, y + h);
//        glTexCoord2d(u0, v1); glVertex2d(x , y);
//        glTexCoord2d(u1, v1); glVertex2d(x + w, y);
//        glTexCoord2d(u1, v0); glVertex2d(x + w, y + h);
//        glEnd();
    }

    private void drawSingleImage(double x, double y, double w, double h, GLTexture GLTexture) {
//        glColor4d(1.0, 1.0, 1.0, 1.0);
//        glEnable(GL_TEXTURE_2D);
//        glBindTexture(GL_TEXTURE_2D, GLTexture.getId());
//
//        glBegin(GL_QUADS);
//        glTexCoord2d(0.0, 0.0); glVertex3d(x, y + h, 0.1f);
//        glTexCoord2d(0.0, 1.0); glVertex3d(x , y, 0.1f);
//        glTexCoord2d(1.0, 1.0); glVertex3d(x + w, y, 0.1f);
//        glTexCoord2d(1.0, 0.0); glVertex3d(x + w, y + h, 0.1f);
//        glEnd();
    }
}
