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
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;

// https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller
public class GameView implements IGameView {
    private static final int s_NumNumbers = 9;
    private static final int s_NumWallMeshes = 4;
    private static final float s_CellSize = 1.0f;
    private static final float s_CameraXRotation = -65.0f;
    private static final float s_CameraYPosition = 50.0f;
    private static final float s_CameraZPosition = 22.5f;
    private static final float s_ObjectYPosition = 0.5f;
    private static final float s_LightIntensity = 0.8735f;
    private static final float s_ItemYRotationInc = 60.0f;
    private static final float s_ItemXRotationRadians = (float)Math.toRadians(5.0f);
    private static final float s_ItemBobOffsetMax = 0.30f;
    private static final float s_ItemBobRotationInc = 180.0f;
    private static final float s_MsPerFrame = 0.01666666f;

    private static final Vector2i s_P1Snakes = new Vector2i(20, 905);
    private static final Vector2i s_P1Score = new Vector2i(110, 905);
    private static final Vector2i s_P2Snakes = new Vector2i(1046, 905);
    private static final Vector2i s_P2Score = new Vector2i(1134, 905);
    private static final Vector2i s_CurrentLevel = new Vector2i(594, 905);
    private static final Vector2i s_NumLevels = new Vector2i(650, 905);

    private final Matrix4f m_MvMatrix;
    private final Matrix4f m_MvpMatrix;
    private final Matrix4f m_ProjectionMatrix;
    private final Matrix4f m_ModelMatrix;
    private final Matrix4f m_ViewMatrix;
    private final GLDiffuseTextureProgram m_DiffuseTexturedProgram;
    private final GLSpecularDirectionalLightProgram m_SpecularDirectionalLightProgram;
    private final GLDirectionalLightProgram m_DirectionalLightProgram;
    private final Vector3f m_LightDirection;

    private GLTexture[] m_NumberTextures;
    private GLTexture[] m_PowerUpTextures;
    private GLTexture m_DotTexture;
    private GLTexture m_HeadTexture;
    private NumberFont m_NumberFont;
    private GLStaticPolyhedronVxTcNm m_WorldDisplayMesh;
    private GLStaticPolyhedronVxTcNm m_AppleDisplayMesh;
    private GLStaticPolyhedronVxTcNm m_PowerUpDisplayMesh;
    private GLStaticPolyhedronVxTcNm[] m_WallDisplayMeshes;
    private GLStaticPolyhedronVxTc m_ToolbarDisplayMesh;
    private IAppStateContext m_AppStateContext;
    private IGameController m_Controller;
    private GameField m_GameField;
    private Snake[] m_Snakes;
    private float m_ItemYRotation;
    private float m_ItemBobRotation;
    private float m_ItemBobOffset;

    public GameView() throws IOException {
        m_MvMatrix = new Matrix4f();
        m_MvpMatrix = new Matrix4f();
        m_ProjectionMatrix = new Matrix4f();
        m_ModelMatrix = new Matrix4f();
        m_ViewMatrix = new Matrix4f();

        m_ViewMatrix.rotate((float)Math.toRadians(-s_CameraXRotation), 1.0f, 0.0f, 0.0f)
                    .translate(0, -s_CameraYPosition, -s_CameraZPosition);

        m_LightDirection = new Vector3f(-0.5f, 0.0f, 1.0f).normalize();

        m_DiffuseTexturedProgram = new GLDiffuseTextureProgram();
        m_SpecularDirectionalLightProgram = new GLSpecularDirectionalLightProgram();
        m_DirectionalLightProgram = new GLDirectionalLightProgram();

        m_ItemYRotation = 0.0f;
        m_ItemBobRotation = 0.0f;
        m_ItemBobOffset = 0.0f;
    }

    @Override
    public void setAppStateContext(IAppStateContext appStateContext) {
        m_AppStateContext = appStateContext;
        m_Controller = m_AppStateContext.getController();
        m_GameField = m_Controller.getGameField();
        m_Snakes = m_Controller.getSnakes();
    }

    @Override
    public void loadResources() throws Exception {
        m_NumberTextures = new GLTexture[s_NumNumbers];
        for (int i = 0; i < s_NumNumbers; ++i) {
            m_NumberTextures[i] = new GLTexture(ImageIO.read(new File(String.format("images\\Apple%d.png", i + 1))));
        }

        m_WallDisplayMeshes = new GLStaticPolyhedronVxTcNm[s_NumWallMeshes];
        for (int i = 0; i < s_NumWallMeshes; ++i) {
            m_WallDisplayMeshes[i] = loadDisplayMeshWithNormals(String.format("meshes\\WallDisplayMesh%d.obj", i));
        }

        m_WorldDisplayMesh = loadDisplayMeshWithNormals("meshes\\LevelDisplayMesh.obj");
        m_AppleDisplayMesh = loadDisplayMeshWithNormals("meshes\\AppleLoResDisplayMesh.obj");
        m_PowerUpDisplayMesh = loadDisplayMeshWithNormals("meshes\\PowerUpDisplayMesh.obj");

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
        m_NumberFont = new ToolbarNumberFont(m_DiffuseTexturedProgram);
    }

    @Override
    public void unloadResources() {
        if (m_NumberTextures != null) {
            for (int i = 0; i < s_NumNumbers; ++i) {
                m_NumberTextures[i].freeNativeResource();
            }
            m_NumberTextures = null;
        }

        if (m_WallDisplayMeshes != null) {
            for (int i = 0; i < s_NumWallMeshes; ++i) {
                m_WallDisplayMeshes[i].freeNativeResources();
            }
            m_WallDisplayMeshes = null;
        }

        if (m_PowerUpTextures != null) {
            for (int i = 0; i < PowerUp.s_NumPowerUps; ++i) {
                m_PowerUpTextures[i].freeNativeResource();
            }
            m_PowerUpTextures = null;
        }

        if (m_DotTexture != null ) {
            m_DotTexture.freeNativeResource();
            m_DotTexture = null;
        }
        if (m_HeadTexture != null ) {
            m_HeadTexture.freeNativeResource();
            m_HeadTexture = null;
        }
        if (m_NumberFont != null ) {
            m_NumberFont.freeNativeResource();
            m_NumberFont = null;
        }
        if (m_WorldDisplayMesh != null ) {
            m_WorldDisplayMesh.freeNativeResources();
            m_WorldDisplayMesh = null;
        }
        if (m_AppleDisplayMesh != null ) {
            m_AppleDisplayMesh.freeNativeResources();
            m_AppleDisplayMesh = null;
        }
        if (m_PowerUpDisplayMesh != null ) {
            m_PowerUpDisplayMesh.freeNativeResources();
            m_PowerUpDisplayMesh = null;
        }
        if (m_ToolbarDisplayMesh != null) {
            m_ToolbarDisplayMesh.freeNativeResources();
            m_ToolbarDisplayMesh = null;
        }
    }

    @Override
    public void freeNativeResources() {
        m_DiffuseTexturedProgram.freeNativeResource();
        m_SpecularDirectionalLightProgram.freeNativeResource();
    }

    @Override
    public void draw3d(long nowMs) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }

        m_ItemYRotation += s_MsPerFrame * s_ItemYRotationInc;
        if (m_ItemYRotation >= 360.0f) {
            m_ItemYRotation -= 360.0f;
        }
        m_ItemBobRotation += s_MsPerFrame * s_ItemBobRotationInc;
        if (m_ItemBobRotation >= 360.0f) {
            m_ItemBobRotation -= 360.0f;
        }
        m_ItemBobOffset = s_ItemBobOffsetMax * (float)Math.sin(Math.toRadians(m_ItemBobRotation));

        m_ModelMatrix.identity();
        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
        m_ProjectionMatrix.set(m_AppStateContext.getPerspectiveMatrix());

        m_DirectionalLightProgram.setLightDirection(m_LightDirection);
        m_DirectionalLightProgram.setLightIntensity(s_LightIntensity);
        m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);

        m_WorldDisplayMesh.draw();

        float startX = GameField.WIDTH / 2.0f * -s_CellSize;
        float startZ = GameField.HEIGHT / 2.0f * -s_CellSize;

        for (int z = 0; z < GameField.HEIGHT; ++z) {
            float cellOffsetZ = (-startZ - z * s_CellSize) - (s_CellSize / 2.0f);
            for (int x = 0; x < GameField.WIDTH; ++x) {
                float cellOffsetX = (startX + x * s_CellSize) + (s_CellSize / 2.0f);

                m_ModelMatrix.identity();

                switch (m_GameField.getCellType(x, z)) {
                    case EMPTY:
                        break;
                    case WALL:
                        m_ModelMatrix.setTranslation(cellOffsetX, s_ObjectYPosition, cellOffsetZ);
                        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
                        m_ProjectionMatrix.set(m_AppStateContext.getPerspectiveMatrix());
                        m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);
                        m_WallDisplayMeshes[(x + z) % s_NumWallMeshes].draw();
                        break;
                    case POWER_UP: {
                        m_ModelMatrix
                                .setTranslation(cellOffsetX, s_ObjectYPosition + (s_ItemBobOffsetMax - m_ItemBobOffset), cellOffsetZ)
                                .rotate((float)Math.toRadians(-m_ItemYRotation), 0.0f, 1.0f, 0.0f)
                                .rotate(s_ItemXRotationRadians, 1.0f, 0.0f, 0.0f);
                        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
                        m_ProjectionMatrix.set(m_AppStateContext.getPerspectiveMatrix());
                        m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);
                        m_PowerUpDisplayMesh.draw();
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
                        break;
                    }
                    case NUMBER: {
                        m_ModelMatrix
                                .setTranslation(cellOffsetX, s_ObjectYPosition + m_ItemBobOffset, cellOffsetZ)
                                .rotate((float)Math.toRadians(m_ItemYRotation), 0.0f, 1.0f, 0.0f)
                                .rotate(s_ItemXRotationRadians, 1.0f, 0.0f, 0.0f);
                        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
                        m_ProjectionMatrix.set(m_AppStateContext.getPerspectiveMatrix());
                        m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);
                        m_AppleDisplayMesh.draw();
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
                        break;
                    }
                }
            }
        }

        boolean firstLoop;
        for (var snake : m_Snakes) {
            firstLoop = true;
            for (var bodyPart : snake.getBodyParts()) {
                float cellOffsetX = (startX + bodyPart.m_X * s_CellSize) + (s_CellSize / 2.0f);
                float cellOffsetZ = (-startZ - bodyPart.m_Z * s_CellSize) - (s_CellSize / 2.0f);

                m_ModelMatrix.identity();
                m_ModelMatrix.setTranslation(cellOffsetX, s_ObjectYPosition, cellOffsetZ);

                m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
                m_ProjectionMatrix.set(m_AppStateContext.getPerspectiveMatrix());

                m_DirectionalLightProgram.setLightDirection(m_LightDirection);
                m_DirectionalLightProgram.setLightIntensity(s_LightIntensity);
                m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);

                if (firstLoop) {
                    m_PowerUpDisplayMesh.draw();
                }
                else {
                    m_AppleDisplayMesh.draw();
                }
                firstLoop = false;
            }
        }
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }

        if (m_ToolbarDisplayMesh == null) {
            GLTexture toolbarTexture = new GLTexture(ImageIO.read(new File("images\\Toolbar.png")));
            float y = m_AppStateContext.getWindowHeight() - toolbarTexture.getHeight();
            m_ToolbarDisplayMesh = createRectangle(0.0f, y, toolbarTexture.getWidth(), toolbarTexture.getHeight(), toolbarTexture);
        }

        m_ModelMatrix.identity();
        drawOrthographicPolyhedron(m_ToolbarDisplayMesh, m_ModelMatrix);

        int level = m_Controller.getCurrentLevel() + 1;
        Matrix4f projectionMatrix = m_AppStateContext.getOrthographicMatrix();

        // Draw the level's state
        if (level < 10) {
            m_NumberFont.drawNumber(projectionMatrix, level, s_CurrentLevel.m_X, s_CurrentLevel.m_Z);
        }
        else {
            m_NumberFont.drawNumber(projectionMatrix, level, s_CurrentLevel.m_X - 8.0f, s_CurrentLevel.m_Z);
        }
        m_NumberFont.drawNumber(projectionMatrix, m_Controller.getLevelCount(), s_NumLevels.m_X, s_NumLevels.m_Z);

        // Draw player 1's state
        m_NumberFont.drawNumber(projectionMatrix, m_Snakes[0].getNumLives(), s_P1Snakes.m_X, s_P1Snakes.m_Z);
        m_NumberFont.drawNumber(projectionMatrix, m_Snakes[0].getPoints(), s_P1Score.m_X, s_P1Score.m_Z);

        if (m_Snakes.length > 1) {
            // Draw player 2's state
            m_NumberFont.drawNumber(projectionMatrix, m_Snakes[1].getNumLives(), s_P2Snakes.m_X, s_P2Snakes.m_Z);
            m_NumberFont.drawNumber(projectionMatrix, m_Snakes[1].getPoints(), s_P2Score.m_X, s_P2Score.m_Z);
        }
    }

    @Override
    public void drawOrthographicPolyhedron(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        m_MvpMatrix.identity();
        m_MvpMatrix.set(m_AppStateContext.getOrthographicMatrix()).mul(modelMatrix);
        m_DiffuseTexturedProgram.setDefaultDiffuseColour();
        m_DiffuseTexturedProgram.activate(m_MvpMatrix);
        polyhedron.draw();
    }

    @Override
    public void drawOrthographicPolyhedron(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix, float alpha) {
        if (m_AppStateContext == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        m_MvpMatrix.identity();
        m_MvpMatrix.set(m_AppStateContext.getOrthographicMatrix()).mul(modelMatrix);
        m_DiffuseTexturedProgram.setDiffuseColour(new Vector4f(1.0f, 1.0f, 1.0f, alpha));
        m_DiffuseTexturedProgram.activate(m_MvpMatrix);
        polyhedron.draw();
    }

    @Override
    public GLStaticPolyhedronVxTc createRectangle(float x, float y, float width, float height, GLTexture texture) {
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

        GLStaticPolyhedronVxTc polyhedron = new GLStaticPolyhedronVxTc();
        polyhedron.addPiece(new GLStaticPolyhedronPieceVxTc(texture, vertices, texCoordinates));
        return polyhedron;
    }

    @Override
    public GLStaticPolyhedronVxTc createCenteredRectangle(float width, float height, GLTexture texture) {
        var x = (m_AppStateContext.getWindowWidth() / 2.0f) - (width / 2.0f);
        var y = (m_AppStateContext.getWindowHeight() / 2.0f) - (height / 2.0f);
        return createRectangle(x, y, width, height, texture);
    }

    @Override
    public GLStaticPolyhedronVxTc loadDisplayMeshWithoutNormals(String fileName) throws Exception {
        final ObjFile objFile = new ObjFile(fileName);
        if (objFile.getObjects() == null || objFile.getObjects().isEmpty()) {
            throw new RuntimeException("Object file has no objects");
        }

        final ObjFile.Object object = objFile.getObjects().get(0);
        if (object.getPieces() == null || object.getPieces().isEmpty()) {
            throw new RuntimeException("Object has no pieces");
        }

        final ArrayList<MtlFile> materialFiles = new ArrayList<>();
        for (var materialFileName : objFile.getMaterialFileNames()) {
            materialFiles.add(new MtlFile("meshes\\" + materialFileName));
        }

        GLStaticPolyhedronVxTc displayMesh = new GLStaticPolyhedronVxTc();

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

            numFloats = piece.getFaces().size() * objFile.getTexCoordinates().size() * 2;
            floatCount = 0;
            float[] texCoordinates = new float[numFloats];
            for (int faceIndex = 0; faceIndex < piece.getFaces().size(); ++faceIndex) {
                ObjFile.Face face = piece.getFaces().get(faceIndex);

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[0]).m_U;
                texCoordinates[floatCount++] = 1-objFile.getTexCoordinates().get(face.m_TexCoordinates[0]).m_V;

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[1]).m_U;
                texCoordinates[floatCount++] = 1-objFile.getTexCoordinates().get(face.m_TexCoordinates[1]).m_V;

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[2]).m_U;
                texCoordinates[floatCount++] = 1-objFile.getTexCoordinates().get(face.m_TexCoordinates[2]).m_V;
            }

            displayMesh.addPiece(new GLStaticPolyhedronPieceVxTc(pieceDiffuseTexture, vertices, texCoordinates));
        }

        return displayMesh;
    }

    @Override
    public GLStaticPolyhedronVxTcNm loadDisplayMeshWithNormals(String fileName) throws Exception {
        final ObjFile objFile = new ObjFile(fileName);
        if (objFile.getObjects() == null || objFile.getObjects().isEmpty()) {
            throw new RuntimeException("Object file has no objects");
        }

        final ObjFile.Object object = objFile.getObjects().get(0);
        if (object.getPieces() == null || object.getPieces().isEmpty()) {
            throw new RuntimeException("Object has no pieces");
        }

        final ArrayList<MtlFile> materialFiles = new ArrayList<>();
        for (var materialFileName : objFile.getMaterialFileNames()) {
            materialFiles.add(new MtlFile("meshes\\" + materialFileName));
        }

        GLStaticPolyhedronVxTcNm displayMesh = new GLStaticPolyhedronVxTcNm();

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

            numFloats = piece.getFaces().size() * objFile.getTexCoordinates().size() * 2;
            floatCount = 0;
            float[] texCoordinates = new float[numFloats];
            for (int faceIndex = 0; faceIndex < piece.getFaces().size(); ++faceIndex) {
                ObjFile.Face face = piece.getFaces().get(faceIndex);

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[0]).m_U;
                texCoordinates[floatCount++] = 1-objFile.getTexCoordinates().get(face.m_TexCoordinates[0]).m_V;

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[1]).m_U;
                texCoordinates[floatCount++] = 1-objFile.getTexCoordinates().get(face.m_TexCoordinates[1]).m_V;

                texCoordinates[floatCount++] = objFile.getTexCoordinates().get(face.m_TexCoordinates[2]).m_U;
                texCoordinates[floatCount++] = 1-objFile.getTexCoordinates().get(face.m_TexCoordinates[2]).m_V;
            }

            numFloats = piece.getFaces().size() * objFile.getNormals().size() * 3;
            floatCount = 0;
            float[] normals = new float[numFloats];
            for (int faceIndex = 0; faceIndex < piece.getFaces().size(); ++faceIndex) {
                ObjFile.Face face = piece.getFaces().get(faceIndex);

                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[0]).m_X;
                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[0]).m_Y;
                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[0]).m_Z;

                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[1]).m_X;
                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[1]).m_Y;
                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[1]).m_Z;

                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[2]).m_X;
                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[2]).m_Y;
                normals[floatCount++] = objFile.getNormals().get(face.m_Normals[2]).m_Z;
            }

            displayMesh.addPiece(new GLStaticPolyhedronPieceVxTcNm(pieceDiffuseTexture, vertices, texCoordinates, normals));
        }

        return displayMesh;
    }

    @Override
    public GLDiffuseTextureProgram getTexturedProgram() {
        return m_DiffuseTexturedProgram;
    }

    @Override
    public GLSpecularDirectionalLightProgram getSpecularDirectionalLightProgram() {
        return m_SpecularDirectionalLightProgram;
    }

    @Override
    public GLDirectionalLightProgram getDirectionalLightProgram() {
        return m_DirectionalLightProgram;
    }
}
