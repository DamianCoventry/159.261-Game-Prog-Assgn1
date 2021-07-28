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
import java.lang.Math;
import java.util.ArrayList;

// https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller
public class GameView implements IGameView {
    private static final int s_NumWallMeshes = 4;
    private static final float s_CellSize = 1.0f;
    private static final float s_HalfCellSize = s_CellSize / 2.0f;
    private static final float s_CameraXRotation = -65.0f;
    private static final float s_CameraYPosition = 50.0f;
    private static final float s_CameraZPosition = 22.5f;
    private static final float s_ObjectYPosition = 0.5f;
    private static final float s_LightIntensity = 0.8735f;
    private static final float s_ItemYRotationInc = 60.0f;
    private static final float s_ItemXRotationRadians = (float) Math.toRadians(5.0f);
    private static final float s_ItemBobOffsetMax = 0.30f;
    private static final float s_ItemBobRotationInc = 180.0f;
    private static final float s_MsPerFrame = 0.01666666f;
    private static final long s_MaxRandomPowerUpTypeTime = 250;

    private final Matrix4f m_MvMatrix;
    private final Matrix4f m_MvpMatrix;
    private final Matrix4f m_ProjectionMatrix;
    private final Matrix4f m_ModelMatrix;
    private final Matrix4f m_ViewMatrix;

    private final GLDiffuseTextureProgram m_DiffuseTexturedProgram;
    private final GLSpecularDirectionalLightProgram m_SpecularDirectionalLightProgram;
    private final GLDirectionalLightProgram m_DirectionalLightProgram;
    private final PowerUp.Type[] m_PowerUpTypes;

    private GLStaticPolyhedronVxTcNm m_WorldDisplayMesh;
    private GLStaticPolyhedronVxTcNm m_ApplePolyhedron;
    private GLStaticPolyhedronVxTcNm m_PowerUpIncreaseSpeedPolyhedron;
    private GLStaticPolyhedronVxTcNm m_PowerUpDecreaseSpeedPolyhedron;
    private GLStaticPolyhedronVxTcNm m_PowerUpIncreasePointsPolyhedron;
    private GLStaticPolyhedronVxTcNm m_PowerUpDecreasePointsPolyhedron;
    private GLStaticPolyhedronVxTcNm m_PowerUpIncreaseLivesPolyhedron;
    private GLStaticPolyhedronVxTcNm m_PowerUpDecreaseLivesPolyhedron;
    private GLStaticPolyhedronVxTcNm m_PowerUpDecreaseLengthPolyhedron;
    private GLStaticPolyhedronVxTcNm[] m_WallPolyhedra;
    private IAppStateContext m_Context;
    private Toolbar m_Toolbar;
    private GameField m_GameField;
    private Snake[] m_Snakes;
    private float m_ItemYRotation;
    private float m_ItemBobRotation;
    private float m_ItemBobOffset;
    private int m_RandomPowerUpType;
    private long m_LastRandomPowerUpTypeTime;

    public GameView() throws IOException {
        m_MvMatrix = new Matrix4f();
        m_MvpMatrix = new Matrix4f();
        m_ProjectionMatrix = new Matrix4f();
        m_ModelMatrix = new Matrix4f();
        m_ViewMatrix = new Matrix4f();

        m_ViewMatrix.rotate((float)Math.toRadians(-s_CameraXRotation), 1.0f, 0.0f, 0.0f)
                    .translate(0, -s_CameraYPosition, -s_CameraZPosition);

        m_DiffuseTexturedProgram = new GLDiffuseTextureProgram();
        m_SpecularDirectionalLightProgram = new GLSpecularDirectionalLightProgram();

        m_DirectionalLightProgram = new GLDirectionalLightProgram();
        m_DirectionalLightProgram.setLightDirection(new Vector3f(-0.5f, 0.0f, 1.0f).normalize());
        m_DirectionalLightProgram.setLightIntensity(s_LightIntensity);

        m_PowerUpTypes = new PowerUp.Type[] {
                PowerUp.Type.INC_SPEED, PowerUp.Type.DEC_SPEED,
                PowerUp.Type.INC_LIVES, PowerUp.Type.DEC_LIVES,
                PowerUp.Type.INC_POINTS, PowerUp.Type.DEC_POINTS,
                PowerUp.Type.DEC_LENGTH
        };
        m_RandomPowerUpType = 0;
        m_LastRandomPowerUpTypeTime = 0;

        m_ItemYRotation = 0.0f;
        m_ItemBobRotation = 0.0f;
        m_ItemBobOffset = 0.0f;
    }

    @Override
    public void setAppStateContext(IAppStateContext context) throws IOException {
        m_Context = context;
        m_GameField = m_Context.getController().getGameField();
        m_Snakes = m_Context.getController().getSnakes();
        m_Toolbar = new Toolbar(m_Context);
    }

    @Override
    public void loadResources() throws Exception {
        m_WallPolyhedra = new GLStaticPolyhedronVxTcNm[s_NumWallMeshes];
        for (int i = 0; i < s_NumWallMeshes; ++i) {
            m_WallPolyhedra[i] = loadDisplayMeshWithNormals(String.format("meshes\\WallDisplayMesh%d.obj", i));
        }

        m_WorldDisplayMesh = loadDisplayMeshWithNormals("meshes\\LevelDisplayMesh.obj");
        m_ApplePolyhedron = loadDisplayMeshWithNormals("meshes\\AppleLoResDisplayMesh.obj");
        m_PowerUpIncreaseSpeedPolyhedron = loadDisplayMeshWithNormals("meshes\\PowerUpIncreaseSpeed.obj");
        m_PowerUpDecreaseSpeedPolyhedron = loadDisplayMeshWithNormals("meshes\\PowerUpDecreaseSpeed.obj");
        m_PowerUpIncreasePointsPolyhedron = loadDisplayMeshWithNormals("meshes\\PowerUpIncreasePoints.obj");
        m_PowerUpDecreasePointsPolyhedron = loadDisplayMeshWithNormals("meshes\\PowerUpDecreasePoints.obj");
        m_PowerUpIncreaseLivesPolyhedron = loadDisplayMeshWithNormals("meshes\\PowerUpIncreaseLives.obj");
        m_PowerUpDecreaseLivesPolyhedron = loadDisplayMeshWithNormals("meshes\\PowerUpDecreaseLives.obj");
        m_PowerUpDecreaseLengthPolyhedron = loadDisplayMeshWithNormals("meshes\\PowerUpDecreaseLength.obj");
    }

    @Override
    public void unloadResources() {
        if (m_WallPolyhedra != null) {
            for (int i = 0; i < s_NumWallMeshes; ++i) {
                m_WallPolyhedra[i].freeNativeResources();
            }
            m_WallPolyhedra = null;
        }
        if (m_WorldDisplayMesh != null ) {
            m_WorldDisplayMesh.freeNativeResources();
            m_WorldDisplayMesh = null;
        }
        if (m_ApplePolyhedron != null ) {
            m_ApplePolyhedron.freeNativeResources();
            m_ApplePolyhedron = null;
        }
        if (m_PowerUpDecreaseSpeedPolyhedron != null) {
            m_PowerUpDecreaseSpeedPolyhedron.freeNativeResources();
            m_PowerUpDecreaseSpeedPolyhedron = null;
        }
        if (m_PowerUpIncreaseSpeedPolyhedron != null ) {
            m_PowerUpIncreaseSpeedPolyhedron.freeNativeResources();
            m_PowerUpIncreaseSpeedPolyhedron = null;
        }
        if (m_PowerUpDecreasePointsPolyhedron != null) {
            m_PowerUpDecreasePointsPolyhedron.freeNativeResources();
            m_PowerUpDecreasePointsPolyhedron = null;
        }
        if (m_PowerUpIncreasePointsPolyhedron != null ) {
            m_PowerUpIncreasePointsPolyhedron.freeNativeResources();
            m_PowerUpIncreasePointsPolyhedron = null;
        }
        if (m_PowerUpDecreaseLivesPolyhedron != null) {
            m_PowerUpDecreaseLivesPolyhedron.freeNativeResources();
            m_PowerUpDecreaseLivesPolyhedron = null;
        }
        if (m_PowerUpIncreaseLivesPolyhedron != null ) {
            m_PowerUpIncreaseLivesPolyhedron.freeNativeResources();
            m_PowerUpIncreaseLivesPolyhedron = null;
        }
        if (m_PowerUpDecreaseLengthPolyhedron != null) {
            m_PowerUpDecreaseLengthPolyhedron.freeNativeResources();
            m_PowerUpDecreaseLengthPolyhedron = null;
        }
        if (m_Toolbar != null) {
            m_Toolbar.freeNativeResources();
            m_Toolbar = null;
        }
    }

    @Override
    public void freeNativeResources() {
        m_DiffuseTexturedProgram.freeNativeResource();
        m_SpecularDirectionalLightProgram.freeNativeResource();
    }

    @Override
    public void think(long nowMs) {
        m_ItemYRotation += s_MsPerFrame * s_ItemYRotationInc;
        if (m_ItemYRotation >= 360.0f) {
            m_ItemYRotation -= 360.0f;
        }

        m_ItemBobRotation += s_MsPerFrame * s_ItemBobRotationInc;
        if (m_ItemBobRotation >= 360.0f) {
            m_ItemBobRotation -= 360.0f;
        }

        m_ItemBobOffset = s_ItemBobOffsetMax * (float)Math.sin(Math.toRadians(m_ItemBobRotation));

        if (nowMs - m_LastRandomPowerUpTypeTime >= s_MaxRandomPowerUpTypeTime) {
            m_LastRandomPowerUpTypeTime = nowMs;
            if (++m_RandomPowerUpType >= m_PowerUpTypes.length) {
                m_RandomPowerUpType = 0;
            }
        }

        if (m_Toolbar != null) {
            m_Toolbar.think(nowMs);
        }
    }

    @Override
    public void draw3d(long nowMs) {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        drawWorld();
        drawGameField();
        drawSnakes();
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        m_Toolbar.draw2d(nowMs);
    }

    @Override
    public void drawOrthographicPolyhedron(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix) {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        m_MvpMatrix.identity().set(m_Context.getOrthographicMatrix()).mul(modelMatrix);
        m_DiffuseTexturedProgram.setDefaultDiffuseColour();
        m_DiffuseTexturedProgram.activate(m_MvpMatrix);
        polyhedron.draw();
    }

    @Override
    public void drawOrthographicPolyhedron(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix, float alpha) {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        m_MvpMatrix.identity().set(m_Context.getOrthographicMatrix()).mul(modelMatrix);
        m_DiffuseTexturedProgram.setDiffuseColour(new Vector4f(1.0f, 1.0f, 1.0f, alpha));
        m_DiffuseTexturedProgram.activate(m_MvpMatrix);
        polyhedron.draw();
    }

    @Override
    public void startRemainingSnakesAnimation(int playerId, Vector4f colour) {
        m_Toolbar.startRemainingSnakesAnimation(playerId, colour);
    }

    @Override
    public void startScoreAnimation(int playerId, Vector4f colour) {
        m_Toolbar.startScoreAnimation(playerId, colour);
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
        var x = (m_Context.getWindowWidth() / 2.0f) - (width / 2.0f);
        var y = (m_Context.getWindowHeight() / 2.0f) - (height / 2.0f);
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

    private void drawWorld() {
        m_MvMatrix.identity().mul(m_ViewMatrix);
        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);
        m_WorldDisplayMesh.draw();
    }

    private void drawGameField() {
        float startX = GameField.WIDTH / 2.0f * -s_CellSize;
        float startZ = GameField.HEIGHT / 2.0f * -s_CellSize;
        for (int cellZIndex = 0; cellZIndex < GameField.HEIGHT; ++cellZIndex) {
            float cellDrawZ = (-startZ - cellZIndex * s_CellSize) - s_HalfCellSize;
            for (int cellXIndex = 0; cellXIndex < GameField.WIDTH; ++cellXIndex) {
                float cellDrawX = (startX + cellXIndex * s_CellSize) + s_HalfCellSize;
                switch (m_GameField.getCellType(cellXIndex, cellZIndex)) {
                    case WALL:
                        drawGameFieldWall(cellXIndex, cellZIndex, cellDrawX, cellDrawZ);
                        break;
                    case POWER_UP: {
                        drawGameFieldPowerUp(cellXIndex, cellZIndex, cellDrawX, cellDrawZ);
                        break;
                    }
                    case NUMBER: {
                        drawGameFieldNumber(cellDrawX, cellDrawZ);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    private void drawGameFieldWall(int cellXIndex, int cellZIndex, float cellDrawX, float cellDrawZ) {
        m_ModelMatrix
                .identity()
                .translate(cellDrawX, s_ObjectYPosition, cellDrawZ);

        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);

        m_WallPolyhedra[(cellXIndex + cellZIndex) % s_NumWallMeshes].draw();
    }

    private void drawGameFieldPowerUp(int cellXIndex, int cellZIndex, float cellDrawX, float cellDrawZ) {
        m_ModelMatrix
                .identity()
                .translate(cellDrawX, s_ObjectYPosition + (s_ItemBobOffsetMax - m_ItemBobOffset), cellDrawZ)
                .rotate((float)Math.toRadians(-m_ItemYRotation), 0.0f, 1.0f, 0.0f)
                .rotate(s_ItemXRotationRadians, 1.0f, 0.0f, 0.0f);

        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);

        submitPowerUpPolygons(m_GameField.getPowerUp(cellXIndex, cellZIndex).getType());
    }

    private void submitPowerUpPolygons(PowerUp.Type type) {
        switch (type) {
            case INC_SPEED:
                m_PowerUpIncreaseSpeedPolyhedron.draw();
                break;
            case DEC_SPEED:
                m_PowerUpDecreaseSpeedPolyhedron.draw();
                break;
            case INC_LIVES:
                m_PowerUpIncreaseLivesPolyhedron.draw();
                break;
            case DEC_LIVES:
                m_PowerUpDecreaseLivesPolyhedron.draw();
                break;
            case INC_POINTS:
                m_PowerUpIncreasePointsPolyhedron.draw();
                break;
            case DEC_POINTS:
                m_PowerUpDecreasePointsPolyhedron.draw();
                break;
            case DEC_LENGTH:
                m_PowerUpDecreaseLengthPolyhedron.draw();
                break;
            case RANDOM:
                submitPowerUpPolygons(m_PowerUpTypes[m_RandomPowerUpType]);
                break;
        }
    }

    private void drawGameFieldNumber(float cellDrawX, float cellDrawZ) {
        m_ModelMatrix
                .identity()
                .translate(cellDrawX, s_ObjectYPosition + m_ItemBobOffset, cellDrawZ)
                .rotate((float)Math.toRadians(m_ItemYRotation), 0.0f, 1.0f, 0.0f)
                .rotate(s_ItemXRotationRadians, 1.0f, 0.0f, 0.0f);

        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);

        m_ApplePolyhedron.draw();
//        Number number = m_GameField.getNumber(x, y);
//        switch (number.getType()) {
//            case NUM_1:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[0]);
//                break;
//            case NUM_2:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[1]);
//                break;
//            case NUM_3:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[2]);
//                break;
//            case NUM_4:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[3]);
//                break;
//            case NUM_5:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[4]);
//                break;
//            case NUM_6:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[5]);
//                break;
//            case NUM_7:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[6]);
//                break;
//            case NUM_8:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[7]);
//                break;
//            case NUM_9:
//                drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_NumberTextures[8]);
//                break;
//        }
    }

    private void drawSnakes() {
        float startX = GameField.WIDTH / 2.0f * -s_CellSize;
        float startZ = GameField.HEIGHT / 2.0f * -s_CellSize;

        for (var snake : m_Snakes) {
            boolean firstLoop = true;
            for (var bodyPart : snake.getBodyParts()) {
                float cellOffsetX = (startX + bodyPart.m_X * s_CellSize) + s_HalfCellSize;
                float cellOffsetZ = (-startZ - bodyPart.m_Z * s_CellSize) - s_HalfCellSize;

                m_ModelMatrix.identity().translate(cellOffsetX, s_ObjectYPosition, cellOffsetZ);
                m_MvMatrix.identity().mul(m_ViewMatrix).mul(m_ModelMatrix);
                m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
                m_DirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);

                if (firstLoop) {
                    m_WallPolyhedra[0].draw(); // temp
                }
                else {
                    m_ApplePolyhedron.draw(); // temp
                }
                firstLoop = false;
            }
        }
    }
}
