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

import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.*;
import com.snakegame.application.IAppStateContext;
import com.snakegame.opengl.*;
import com.snakegame.rules.*;
import com.snakegame.rules.Number;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.Math;
import java.util.*;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL11.glDepthMask;

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
    private static final float s_ItemXRotationRadians = (float)Math.toRadians(7.5f);
    private static final float s_ItemBobOffsetMax = 0.30f;
    private static final float s_ItemBobRotationInc = 180.0f;
    private static final float s_MsPerFrame = 0.01666666f;
    private static final float s_LightShininess = 32.0f;
    private static final float s_SnakeGibletHalfSize = 0.25f;
    private static final long s_MaxRandomPowerUpTypeTime = 250;
    private static final float s_AppleNumberScale = 0.05f;
    private static final float s_PowerUpScaleStart = 0.03f;
    private static final float s_PowerUpScaleInc = 0.00005f;
    private static final float s_PowerUpVerticalMovement = 0.03222f;
    private static final float s_PowerUpFadeOutInc = 0.004f;
    private static final Vector4f s_Yellow = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);

    private final Matrix4f m_MvMatrix;
    private final Matrix4f m_MvpMatrix;
    private final Matrix4f m_ProjectionMatrix;
    private final Matrix4f m_ModelMatrix;
    private final Matrix4f m_ViewMatrix;

    private final GLDiffuseTextureProgram m_DiffuseTexturedProgram;
    private final GLSpecularDirectionalLightProgram m_SpecularDirectionalLightProgram;
    private final GLDirectionalLightProgram m_DirectionalLightProgram;
    private final GLDiffuseTextureAlphaFadeProgram m_DiffuseTextureAlphaFadeProgram;
    private final GLSpecularDirectionalLightClipPlaneProgram m_SpecularDirectionalLightClipPlaneProgram;

    private final PowerUp.Type[] m_PowerUpTypes;
    private final Random m_Rng;

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
    private GLStaticPolyhedronVxTcNm[] m_SnakeBodyPolyhedra;
    private GLStaticPolyhedronVxTcNm[] m_SnakeHeadPolyhedra;
    private GLStaticPolyhedronVxTcNm[] m_SnakeTailPolyhedra;
    private GLStaticPolyhedronVxTcNm[] m_SnakeElbowPolyhedra;
    private GLStaticPolyhedronVxTcNm m_SnakeGibPolyhedron;

    private boolean m_PowerUpAnimationActive;
    private Vector3f m_PowerUpAnimationPosition;
    private float m_PowerUpFadeOut;
    private GLStaticPolyhedronVxTc m_PowerUpTextPolyhedron;
    private int m_PowerUpTextTimeoutId;

    private GLTexture m_BlueSnakeSkinTexture;
    private GLTexture m_RedSnakeSkinTexture;
    private GLTexture[] m_PowerUpTextTextures;

    private IAppStateContext m_Context;
    private Toolbar m_Toolbar;
    private GameField m_GameField;
    private Snake[] m_Snakes;
    private float m_ItemYRotation;
    private float m_ItemBobRotation;
    private float m_ItemBobOffset;
    private float m_PowerUpScale;
    private int m_RandomPowerUpType;
    private long m_LastRandomPowerUpTypeTime;

    private static class SnakeGiblet {
        PhysicsRigidBody m_RigidBody;
        GLTexture m_SnakeSkinTexture;
        public SnakeGiblet(PhysicsRigidBody rigidBody, GLTexture snakeSkinTexture) {
            m_RigidBody = rigidBody;
            m_SnakeSkinTexture = snakeSkinTexture;
        }
    }

    private final ArrayList<SnakeGiblet> m_SnakeGibRigidBodies;

    public GameView() throws Exception {
        m_MvMatrix = new Matrix4f();
        m_MvpMatrix = new Matrix4f();
        m_ProjectionMatrix = new Matrix4f();
        m_ModelMatrix = new Matrix4f();
        m_ViewMatrix = new Matrix4f();

        m_SnakeGibRigidBodies = new ArrayList<>();
        m_Rng = new Random();

        m_ViewMatrix.rotate((float)Math.toRadians(-s_CameraXRotation), 1.0f, 0.0f, 0.0f)
                    .translate(0, -s_CameraYPosition, -s_CameraZPosition);

        m_DiffuseTexturedProgram = new GLDiffuseTextureProgram();

        Vector3f lightDirection = new Vector3f(-0.5f, 0.0f, 1.0f).normalize();

        m_SpecularDirectionalLightProgram = new GLSpecularDirectionalLightProgram();
        m_SpecularDirectionalLightProgram.setAmbientLight(new Vector3f(0.15f, 0.15f, 0.15f));
        m_SpecularDirectionalLightProgram.setLightDirection(lightDirection);
        m_SpecularDirectionalLightProgram.setLightIntensity(s_LightIntensity);
        m_SpecularDirectionalLightProgram.setShininess(s_LightShininess);

        m_DirectionalLightProgram = new GLDirectionalLightProgram();
        m_DirectionalLightProgram.setLightDirection(lightDirection);
        m_DirectionalLightProgram.setLightIntensity(s_LightIntensity);

        m_DiffuseTextureAlphaFadeProgram = new GLDiffuseTextureAlphaFadeProgram();
        m_SpecularDirectionalLightClipPlaneProgram = new GLSpecularDirectionalLightClipPlaneProgram("meshes\\Noise64x64.png");

        m_PowerUpTypes = new PowerUp.Type[] {
                PowerUp.Type.INC_SPEED, PowerUp.Type.DEC_SPEED,
                PowerUp.Type.INC_LIVES, PowerUp.Type.DEC_LIVES,
                PowerUp.Type.INC_POINTS, PowerUp.Type.DEC_POINTS,
                PowerUp.Type.DEC_LENGTH
        };
        m_RandomPowerUpType = 0;
        m_LastRandomPowerUpTypeTime = 0;
        m_PowerUpTextTimeoutId = 0;

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
    public void loadResources(BiConsumer<Long, Long> progress) throws Exception {
        final long numberOfThingsToLoad = 26 + s_NumWallMeshes; // <-- there are 26 calls to loadXYZ() within this method
        long numLoaded = 0;

        progress.accept(numLoaded, numberOfThingsToLoad);

        m_SnakeBodyPolyhedra = new GLStaticPolyhedronVxTcNm[2];
        m_SnakeBodyPolyhedra[0] = loadDisplayMesh("meshes\\SnakeBodyPartHoriz.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeBodyPolyhedra[1] = loadDisplayMesh("meshes\\SnakeBodyPartVert.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);

        m_SnakeHeadPolyhedra = new GLStaticPolyhedronVxTcNm[4];
        m_SnakeHeadPolyhedra[0] = loadDisplayMesh("meshes\\SnakeBodyPartHeadLeft.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeHeadPolyhedra[1] = loadDisplayMesh("meshes\\SnakeBodyPartHeadTop.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeHeadPolyhedra[2] = loadDisplayMesh("meshes\\SnakeBodyPartHeadRight.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeHeadPolyhedra[3] = loadDisplayMesh("meshes\\SnakeBodyPartHeadBottom.obj");

        m_SnakeTailPolyhedra = new GLStaticPolyhedronVxTcNm[4];
        m_SnakeTailPolyhedra[0] = loadDisplayMesh("meshes\\SnakeBodyPartTailLeft.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeTailPolyhedra[1] = loadDisplayMesh("meshes\\SnakeBodyPartTailTop.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeTailPolyhedra[2] = loadDisplayMesh("meshes\\SnakeBodyPartTailRight.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeTailPolyhedra[3] = loadDisplayMesh("meshes\\SnakeBodyPartTailBottom.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);

        m_SnakeElbowPolyhedra = new GLStaticPolyhedronVxTcNm[4];
        m_SnakeElbowPolyhedra[0] = loadDisplayMesh("meshes\\SnakeBodyPartElbowTL.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeElbowPolyhedra[1] = loadDisplayMesh("meshes\\SnakeBodyPartElbowTR.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeElbowPolyhedra[2] = loadDisplayMesh("meshes\\SnakeBodyPartElbowBL.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_SnakeElbowPolyhedra[3] = loadDisplayMesh("meshes\\SnakeBodyPartElbowBR.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);

        m_SnakeGibPolyhedron = loadDisplayMesh("meshes\\SnakeGib.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);

        m_BlueSnakeSkinTexture = m_SnakeBodyPolyhedra[0].getPiece(0).getDiffuseTexture();
        m_RedSnakeSkinTexture = new GLTexture(ImageIO.read(new File("meshes\\SnakeSkinRed.png")));
        progress.accept(++numLoaded, numberOfThingsToLoad);

        m_WorldDisplayMesh = loadDisplayMesh("meshes\\LevelDisplayMesh.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_ApplePolyhedron = loadDisplayMesh("meshes\\AppleLoResDisplayMesh.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);

        m_WallPolyhedra = new GLStaticPolyhedronVxTcNm[s_NumWallMeshes];
        for (int i = 0; i < s_NumWallMeshes; ++i) {
            m_WallPolyhedra[i] = loadDisplayMesh(String.format("meshes\\WallDisplayMesh%d.obj", i));
            progress.accept(++numLoaded, numberOfThingsToLoad);
        }

        m_PowerUpIncreaseSpeedPolyhedron = loadDisplayMesh("meshes\\PowerUpIncreaseSpeed.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_PowerUpDecreaseSpeedPolyhedron = loadDisplayMesh("meshes\\PowerUpDecreaseSpeed.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_PowerUpIncreasePointsPolyhedron = loadDisplayMesh("meshes\\PowerUpIncreasePoints.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_PowerUpDecreasePointsPolyhedron = loadDisplayMesh("meshes\\PowerUpDecreasePoints.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_PowerUpIncreaseLivesPolyhedron = loadDisplayMesh("meshes\\PowerUpIncreaseLives.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_PowerUpDecreaseLivesPolyhedron = loadDisplayMesh("meshes\\PowerUpDecreaseLives.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);
        m_PowerUpDecreaseLengthPolyhedron = loadDisplayMesh("meshes\\PowerUpDecreaseLength.obj");
        progress.accept(++numLoaded, numberOfThingsToLoad);

        m_PowerUpTextTextures = new GLTexture[PowerUp.s_NumPowerUps];
        m_PowerUpTextTextures[0] = new GLTexture(ImageIO.read(new File("images\\PowerUpTextSpeedUp.png")));
        m_PowerUpTextTextures[1] = new GLTexture(ImageIO.read(new File("images\\PowerUpTextSpeedDown.png")));
        m_PowerUpTextTextures[2] = new GLTexture(ImageIO.read(new File("images\\PowerUpTextExtraSnake.png")));
        m_PowerUpTextTextures[3] = new GLTexture(ImageIO.read(new File("images\\PowerUpTextLoseASnake.png")));
        m_PowerUpTextTextures[4] = new GLTexture(ImageIO.read(new File("images\\PowerUpText1kPoints.png")));
        m_PowerUpTextTextures[5] = new GLTexture(ImageIO.read(new File("images\\PowerUpText-1kPoints.png")));
        m_PowerUpTextTextures[6] = new GLTexture(ImageIO.read(new File("images\\PowerUpTextDecreaseLength.png")));
        m_PowerUpTextPolyhedron = createPolyhedron(
                -m_PowerUpTextTextures[0].getWidth() / 2.0f, -m_PowerUpTextTextures[0].getHeight() / 2.0f,
                m_PowerUpTextTextures[0].getWidth(), m_PowerUpTextTextures[0].getHeight(), m_PowerUpTextTextures[0]);

        loadWorldCollisionMesh();
        progress.accept(numberOfThingsToLoad, numberOfThingsToLoad);
    }

    @Override
    public void unloadResources() {
        if (m_PowerUpTextTimeoutId != 0) {
            m_Context.removeTimeout(m_PowerUpTextTimeoutId);
            m_PowerUpTextTimeoutId = 0;
        }

        for (var rigidBody : m_SnakeGibRigidBodies) {
            m_Context.getPhysicsSpace().remove(rigidBody.m_RigidBody);
            rigidBody.m_SnakeSkinTexture.freeNativeResource();
        }
        m_SnakeGibRigidBodies.clear();

        if (m_PowerUpTextPolyhedron != null) {
            m_PowerUpTextPolyhedron.freeNativeResources();
            m_PowerUpTextPolyhedron = null;
        }

        if (m_PowerUpTextTextures != null) {
            for (var texture : m_PowerUpTextTextures) {
                texture.freeNativeResource();
            }
            m_PowerUpTextTextures = null;
        }

        if (m_WallPolyhedra != null) {
            for (var polyhedron : m_WallPolyhedra) {
                polyhedron.freeNativeResources();
            }
            m_WallPolyhedra = null;
        }
        if (m_SnakeBodyPolyhedra != null) {
            for (var polyhedron : m_SnakeBodyPolyhedra) {
                polyhedron.freeNativeResources();
            }
            m_SnakeBodyPolyhedra = null;
        }
        if (m_SnakeTailPolyhedra != null) {
            for (var polyhedron : m_SnakeTailPolyhedra) {
                polyhedron.freeNativeResources();
            }
            m_SnakeTailPolyhedra = null;
        }
        if (m_SnakeElbowPolyhedra != null) {
            for (var polyhedron : m_SnakeElbowPolyhedra) {
                polyhedron.freeNativeResources();
            }
            m_SnakeElbowPolyhedra = null;
        }
        if (m_SnakeHeadPolyhedra != null) {
            for (var polyhedron : m_SnakeHeadPolyhedra) {
                polyhedron.freeNativeResources();
            }
            m_SnakeHeadPolyhedra = null;
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
        if (m_SnakeGibPolyhedron != null) {
            m_SnakeGibPolyhedron.freeNativeResources();
            m_SnakeGibPolyhedron = null;
        }
        if (m_Toolbar != null) {
            m_Toolbar.freeNativeResources();
            m_Toolbar = null;
        }
    }

    @Override
    public void freeNativeResources() {
        m_DiffuseTexturedProgram.freeNativeResource();
        m_DirectionalLightProgram.freeNativeResource();
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
            m_Toolbar.think();
        }

        if (m_PowerUpAnimationActive) {
            m_PowerUpScale += s_PowerUpScaleInc;
            m_PowerUpAnimationPosition.y += s_PowerUpVerticalMovement;
            m_PowerUpFadeOut = Math.max(0.0f, m_PowerUpFadeOut - s_PowerUpFadeOutInc);
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
        drawGiblets();
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        drawGameFieldNumbers();
        drawPowerUpTextAnimation();
        m_Toolbar.draw2d();
    }

    @Override
    public void drawOrthographicPolyhedron(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix) {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        m_MvpMatrix.identity().set(m_Context.getOrthographicMatrix()).mul(modelMatrix);
        m_DiffuseTexturedProgram.setDefaultDiffuseColour();
        m_DiffuseTexturedProgram.activate(m_MvpMatrix);
        glDepthMask(false);
        polyhedron.draw();
        glDepthMask(true);
    }

    @Override
    public void drawOrthographicPolyhedron(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix, float alpha) {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        m_MvpMatrix.identity().set(m_Context.getOrthographicMatrix()).mul(modelMatrix);
        m_DiffuseTexturedProgram.setDiffuseColour(new Vector4f(1.0f, 1.0f, 1.0f, alpha));
        m_DiffuseTexturedProgram.activate(m_MvpMatrix);
        glDepthMask(false);
        polyhedron.draw();
        glDepthMask(true);
    }

    @Override
    public void drawOrthographicPolyhedronWithFadeRange(GLStaticPolyhedronVxTc polyhedron, Matrix4f modelMatrix, float fadeRange) {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }

        m_DiffuseTextureAlphaFadeProgram.setDefaultDiffuseColour();
        m_DiffuseTextureAlphaFadeProgram.setWindowHeight(m_Context.getWindowHeight());
        m_DiffuseTextureAlphaFadeProgram.setFadeRange(fadeRange);

        m_MvpMatrix.identity().set(m_Context.getOrthographicMatrix()).mul(modelMatrix);
        m_DiffuseTextureAlphaFadeProgram.activate(m_MvpMatrix);
        glDepthMask(false);
        polyhedron.draw();
        glDepthMask(true);
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
    public void startPowerUpAnimation(int playerId, PowerUp.Type powerUpType) {
        if (m_PowerUpTextTimeoutId != 0) {
            m_Context.removeTimeout(m_PowerUpTextTimeoutId);
            m_PowerUpTextTimeoutId = 0;
        }

        Vector2i location = m_Snakes[playerId].getBodyParts().getFirst().m_Location;

        float startX = GameField.WIDTH / 2.0f * -s_CellSize;
        float startZ = GameField.HEIGHT / 2.0f * -s_CellSize;
        float cellOffsetX = (startX + location.m_X * s_CellSize) + s_HalfCellSize;
        float cellOffsetZ = (-startZ - location.m_Z * s_CellSize) - s_HalfCellSize;
        m_PowerUpAnimationPosition = new Vector3f(cellOffsetX, s_ObjectYPosition * 10.0f, cellOffsetZ);

        switch (powerUpType) {
            case INC_SPEED:
                m_PowerUpTextPolyhedron.getPiece(0).setDiffuseTexture(m_PowerUpTextTextures[0]);
                break;
            case DEC_SPEED:
                m_PowerUpTextPolyhedron.getPiece(0).setDiffuseTexture(m_PowerUpTextTextures[1]);
                break;
            case INC_LIVES:
                m_PowerUpTextPolyhedron.getPiece(0).setDiffuseTexture(m_PowerUpTextTextures[2]);
                break;
            case DEC_LIVES:
                m_PowerUpTextPolyhedron.getPiece(0).setDiffuseTexture(m_PowerUpTextTextures[3]);
                break;
            case INC_POINTS:
                m_PowerUpTextPolyhedron.getPiece(0).setDiffuseTexture(m_PowerUpTextTextures[4]);
                break;
            case DEC_POINTS:
                m_PowerUpTextPolyhedron.getPiece(0).setDiffuseTexture(m_PowerUpTextTextures[5]);
                break;
            case DEC_LENGTH:
                m_PowerUpTextPolyhedron.getPiece(0).setDiffuseTexture(m_PowerUpTextTextures[6]);
                break;
        }

        m_PowerUpScale = s_PowerUpScaleStart;
        m_PowerUpAnimationActive = true;
        m_PowerUpFadeOut = 1.0f;

        m_PowerUpTextTimeoutId = m_Context.addTimeout(2000, (callCount) -> {
            m_PowerUpAnimationActive = false;
            m_PowerUpTextTimeoutId = 0;
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    @Override
    public void resetSnakeGiblets() {
        m_SnakeGibRigidBodies.clear();
    }

    @Override
    public void spawnSnakeGiblets(Snake snake) {
        float startX = GameField.WIDTH / 2.0f * -s_CellSize;
        float startZ = GameField.HEIGHT / 2.0f * -s_CellSize;

        BoxCollisionShape bcs = new BoxCollisionShape(s_SnakeGibletHalfSize, s_SnakeGibletHalfSize, s_SnakeGibletHalfSize);

        for (int bodyPart = 1; bodyPart < snake.getBodyParts().size(); ++bodyPart) { // <-- note we don't start from 0, because the head will be oob
            float x = (startX + snake.getBodyParts().get(bodyPart).m_Location.m_X * s_CellSize) + s_HalfCellSize;
            float z = (-startZ - snake.getBodyParts().get(bodyPart).m_Location.m_Z * s_CellSize) - s_HalfCellSize;

            int numGiblets = 2 + m_Rng.nextInt(3); // random int in the range (2, 4)

            for (int giblet = 0; giblet < numGiblets; ++giblet) {
                float xOffset = m_Rng.nextFloat() * 0.5f - 0.25f; // random float in the range (-0.25, 0.25)
                float zOffset = m_Rng.nextFloat() * 0.5f - 0.25f; // random float in the range (-0.25, 0.25)

                int numRows = 2 + m_Rng.nextInt(2); // random int in the range (2, 3)
                for (int row = 0; row < numRows; ++row) {
                    PhysicsRigidBody rigidBody = new PhysicsRigidBody(bcs, 1.0f);
                    rigidBody.setPhysicsLocation(new com.jme3.math.Vector3f(x + xOffset, (row + 1) * s_ObjectYPosition, z + zOffset));
                    rigidBody.setPhysicsRotation(new Quaternion().fromAngles(
                            0.0f, (float) Math.toRadians(m_Rng.nextFloat() * 360.0f),
                            (float) Math.toRadians(m_Rng.nextFloat() * 360.0f)));
                    GLTexture snakeSkin = snake.getId() == 0 ? m_BlueSnakeSkinTexture : m_RedSnakeSkinTexture;
                    m_SnakeGibRigidBodies.add(new SnakeGiblet(rigidBody, snakeSkin));

                    m_Context.getPhysicsSpace().addCollisionObject(rigidBody);
                }
            }
        }
    }

    @Override
    public void activateArrowMouseCursor() {
        m_Context.activateArrowMouseCursor();
    }

    @Override
    public void activateHandMouseCursor() {
        m_Context.activateHandMouseCursor();
    }

    @Override
    public void activateGrabMouseCursor() {
        m_Context.activateGrabMouseCursor();
    }

    @Override
    public GLStaticPolyhedronVxTc createPolyhedron(float x, float y, float width, float height, GLTexture texture) {
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
    public GLStaticPolyhedronVxTc createCenteredPolyhedron(float width, float height, GLTexture texture) {
        var x = (m_Context.getWindowWidth() / 2.0f) - (width / 2.0f);
        var y = (m_Context.getWindowHeight() / 2.0f) - (height / 2.0f);
        return createPolyhedron(x, y, width, height, texture);
    }

    @Override
    public GLStaticPolyhedronVxTcNm loadDisplayMesh(String fileName) throws Exception {
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
            GLTexture diffuseTexture = loadDiffuseTexture(materialFiles, piece);
            float[] vertices = loadVertices(objFile, piece);
            float[] texCoordinates = loadTexCoordinates(objFile, piece);
            float[] normals = loadNormals(objFile, piece);
            displayMesh.addPiece(new GLStaticPolyhedronPieceVxTcNm(diffuseTexture, vertices, texCoordinates, normals));
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

    @Override
    public GLDiffuseTextureAlphaFadeProgram getDiffuseTextureAlphaFadeProgram() {
        return m_DiffuseTextureAlphaFadeProgram;
    }

    @Override
    public GLSpecularDirectionalLightClipPlaneProgram getSpecularDirectionalLightClipPlaneProgram() {
        return m_SpecularDirectionalLightClipPlaneProgram;
    }


    private void drawPowerUpTextAnimation() {
        if (!m_PowerUpAnimationActive || m_PowerUpFadeOut <= 0.0f) {
            return;
        }


        Vector4f worldPosition = new Vector4f(m_PowerUpAnimationPosition.x, m_PowerUpAnimationPosition.y, m_PowerUpAnimationPosition.z, 1.0f);

        m_ModelMatrix.identity().translate(m_PowerUpAnimationPosition).scale(m_PowerUpScale);
        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        Vector4f screenPosition = m_ProjectionMatrix.mul(m_ViewMatrix).mul(m_ModelMatrix).transform(worldPosition);
        screenPosition = screenPosition.div(screenPosition.w);

        if (screenPosition.z >= 0.0f) {
            m_ModelMatrix.identity().translate(screenPosition.x, screenPosition.y, 0.5f);
            m_MvpMatrix.identity().mul(m_ProjectionMatrix).mul(m_ModelMatrix);
            m_DiffuseTexturedProgram.setDiffuseColour(new Vector4f(1.0f, 1.0f, 1.0f, m_PowerUpFadeOut));
            m_DiffuseTexturedProgram.activate(m_MvpMatrix);
            m_PowerUpTextPolyhedron.draw();
        }
    }

    private GLTexture loadDiffuseTexture(ArrayList<MtlFile> materialFiles, ObjFile.Piece piece) throws IOException {
        GLTexture diffuseTexture = null;
        for (var materialFile : materialFiles) {
            for (var material : materialFile.getMaterials()) {
                if (material.getName().equalsIgnoreCase(piece.getMaterialName())) {
                    if (diffuseTexture != null) {
                        diffuseTexture.freeNativeResource();
                    }
                    if (material.getDiffuseTexture() == null) {
                        throw new RuntimeException("Material [" + material.getName() + "] does not have a diffuse texture");
                    }
                    diffuseTexture = new GLTexture(ImageIO.read(new File("meshes\\" + material.getDiffuseTexture())));
                }
            }
        }
        if (diffuseTexture == null) {
            throw new RuntimeException("The level file does not have a valid diffuse texture within a piece");
        }
        return diffuseTexture;
    }

    private float[] loadVertices(ObjFile objFile, ObjFile.Piece piece) {
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

        return vertices;
    }

    private float[] loadTexCoordinates(ObjFile objFile, ObjFile.Piece piece) {
        int numFloats = piece.getFaces().size() * objFile.getTexCoordinates().size() * 2;
        int floatCount = 0;

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

        return texCoordinates;
    }

    private float[] loadNormals(ObjFile objFile, ObjFile.Piece piece) {
        int numFloats = piece.getFaces().size() * objFile.getNormals().size() * 3;
        int floatCount = 0;

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

        return normals;
    }

    private void loadWorldCollisionMesh() throws Exception {
        if (m_Context == null) {
            throw new RuntimeException("Application state context hasn't been set");
        }
        final ObjFile objFile = new ObjFile("meshes\\LevelCollisionMesh.obj");
        if (objFile.getObjects() == null || objFile.getObjects().isEmpty()) {
            throw new RuntimeException("Object file has no objects");
        }

        var positionArray = new com.jme3.math.Vector3f[objFile.getVertices().size()];
        for (int i = 0; i < objFile.getVertices().size(); ++i) {
            ObjFile.Vertex vertex = objFile.getVertices().get(i);
            positionArray[i] = new com.jme3.math.Vector3f(vertex.m_X, vertex.m_Y, vertex.m_Z);
        }

        int totalVertices = 0;
        for (var piece : objFile.getObjects().get(0).getPieces()) {
            totalVertices += piece.getFaces().size() * 3;
        }

        final int[] indexArray = new int[totalVertices];
        int count = 0;
        for (var piece : objFile.getObjects().get(0).getPieces()) {
            for (var face : piece.getFaces()) {
                indexArray[count++] = face.m_Vertices[0];
                indexArray[count++] = face.m_Vertices[1];
                indexArray[count++] = face.m_Vertices[2];
            }
        }

        IndexedMesh indexedMesh = new IndexedMesh(positionArray, indexArray);
        MeshCollisionShape mcs = new MeshCollisionShape(true, indexedMesh);
        m_Context.getPhysicsSpace().addCollisionObject(new PhysicsRigidBody(mcs, 0f));
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
                    case POWER_UP:
                        drawGameFieldPowerUp(cellXIndex, cellZIndex, cellDrawX, cellDrawZ);
                        break;
                    case NUMBER:
                        drawGameFieldApple(cellDrawX, cellDrawZ);
                        break;
                }
            }
        }
    }

    private void drawGameFieldNumbers() {
        float startX = GameField.WIDTH / 2.0f * -s_CellSize;
        float startZ = GameField.HEIGHT / 2.0f * -s_CellSize;

        for (int cellZIndex = 0; cellZIndex < GameField.HEIGHT; ++cellZIndex) {
            float cellDrawZ = (-startZ - cellZIndex * s_CellSize) - s_HalfCellSize;

            for (int cellXIndex = 0; cellXIndex < GameField.WIDTH; ++cellXIndex) {
                float cellDrawX = (startX + cellXIndex * s_CellSize) + s_HalfCellSize;

                if (m_GameField.getCellType(cellXIndex, cellZIndex) == GameField.CellType.NUMBER) {
                    drawGameFieldNumber(cellXIndex, cellZIndex, cellDrawX, cellDrawZ);
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
        m_SpecularDirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);

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

    private void drawGameFieldApple(float cellDrawX, float cellDrawZ) {
        m_ModelMatrix
                .identity()
                .translate(cellDrawX, s_ObjectYPosition + m_ItemBobOffset, cellDrawZ)
                .rotate((float)Math.toRadians(m_ItemYRotation), 0.0f, 1.0f, 0.0f)
                .rotate(s_ItemXRotationRadians, 1.0f, 0.0f, 0.0f);

        m_MvMatrix.set(m_ViewMatrix).mul(m_ModelMatrix);
        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_SpecularDirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);
        m_ApplePolyhedron.draw();
    }

    private void drawGameFieldNumber(int cellXIndex, int cellZIndex, float cellDrawX, float cellDrawZ) {
        m_ModelMatrix
                .identity()
                .translate(cellDrawX, s_ObjectYPosition + m_ItemBobOffset, cellDrawZ)
                .scale(s_AppleNumberScale);

        Vector4f worldPosition = new Vector4f(cellDrawX, s_ObjectYPosition + m_ItemBobOffset, cellDrawZ, 1.0f);
        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        Vector4f screenPosition = m_ProjectionMatrix.mul(m_ViewMatrix).mul(m_ModelMatrix).transform(worldPosition);
        screenPosition = screenPosition.div(screenPosition.w);

        if (screenPosition.z >= 0.0f) {
            int number = Number.toInteger(m_GameField.getNumber(cellXIndex, cellZIndex).getType());
            float width = m_Toolbar.getNumberFont().calculateWidth(number, false);
            screenPosition.x -= width / 2.0f;
            screenPosition.y += ToolbarNumberFont.s_FrameHeight * 0.5f;
            m_Toolbar.getNumberFont().drawNumber(m_ProjectionMatrix, number, screenPosition.x, screenPosition.y, 1.0f, s_Yellow);
        }
    }

    private void drawSnakes() {
        if (m_Snakes[0].isAlive()) {
            drawSnake(m_Snakes[0], m_BlueSnakeSkinTexture);
        }
        if (m_Snakes.length > 1 && m_Snakes[1].isAlive()) {
            drawSnake(m_Snakes[1], m_RedSnakeSkinTexture);
        }
    }

    private void drawGiblets() {
        Transform transform = new Transform();
        com.jme3.math.Matrix4f transformMatrix = new com.jme3.math.Matrix4f();

        for (var giblet : m_SnakeGibRigidBodies) {
            transform = giblet.m_RigidBody.getMotionState().physicsTransform(transform);
            transformMatrix = transform.toTransformMatrix(transformMatrix);

            m_ModelMatrix.m00(transformMatrix.m00);
            m_ModelMatrix.m01(transformMatrix.m10);
            m_ModelMatrix.m02(transformMatrix.m20);
            m_ModelMatrix.m03(transformMatrix.m30);

            m_ModelMatrix.m10(transformMatrix.m01);
            m_ModelMatrix.m11(transformMatrix.m11);
            m_ModelMatrix.m12(transformMatrix.m21);
            m_ModelMatrix.m13(transformMatrix.m31);

            m_ModelMatrix.m20(transformMatrix.m02);
            m_ModelMatrix.m21(transformMatrix.m12);
            m_ModelMatrix.m22(transformMatrix.m22);
            m_ModelMatrix.m23(transformMatrix.m32);

            m_ModelMatrix.m30(transformMatrix.m03);
            m_ModelMatrix.m31(transformMatrix.m13);
            m_ModelMatrix.m32(transformMatrix.m23);
            m_ModelMatrix.m33(transformMatrix.m33);

            m_MvMatrix.identity().mul(m_ViewMatrix).mul(m_ModelMatrix);
            m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
            m_SpecularDirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);
            m_SnakeGibPolyhedron.getPiece(0).setDiffuseTexture(giblet.m_SnakeSkinTexture);
            m_SnakeGibPolyhedron.draw();
        }
    }

    private void drawSnake(Snake snake, GLTexture snakeSkinTexture) {
        Snake.BodyPart bodyPart = snake.getBodyParts().getFirst();
        drawSnakeHeadOrTail(m_SnakeHeadPolyhedra, bodyPart.m_LeavingCellDirection, bodyPart.m_Location, snakeSkinTexture);

        for (int i = 1; i < snake.getBodyParts().size() - 1; ++i) { // <-- Note the indices
            drawSnakeMiddleBodyPart(snake.getBodyParts(), i, snakeSkinTexture);
        }

        bodyPart = snake.getBodyParts().getLast();
        drawSnakeHeadOrTail(m_SnakeTailPolyhedra, bodyPart.m_LeavingCellDirection, bodyPart.m_Location, snakeSkinTexture);
    }

    private void drawSnakeHeadOrTail(GLStaticPolyhedronVxTcNm[] polyhedra, Snake.Direction direction, Vector2i location, GLTexture snakeSkinTexture) {
        switch (direction) {
            case Left:
                drawSnakeBodyPart(polyhedra[0], location, snakeSkinTexture);
                break;
            case Up:
                drawSnakeBodyPart(polyhedra[1], location, snakeSkinTexture);
                break;
            case Right:
                drawSnakeBodyPart(polyhedra[2], location, snakeSkinTexture);
                break;
            case Down:
                drawSnakeBodyPart(polyhedra[3], location, snakeSkinTexture);
                break;
        }
    }

    private void drawSnakeMiddleBodyPart(LinkedList<Snake.BodyPart> bodyParts, int i, GLTexture snakeSkinTexture) {
        Snake.BodyPart bodyPart = bodyParts.get(i);
        switch (classifyBodyPart(bodyParts, i)) {
            case HORIZONTAL:
                drawSnakeBodyPart(m_SnakeBodyPolyhedra[0], bodyPart.m_Location, snakeSkinTexture);
                break;
            case VERTICAL:
                drawSnakeBodyPart(m_SnakeBodyPolyhedra[1], bodyPart.m_Location, snakeSkinTexture);
                break;
            case ELBOW_TL:
                drawSnakeBodyPart(m_SnakeElbowPolyhedra[0], bodyPart.m_Location, snakeSkinTexture);
                break;
            case ELBOW_TR:
                drawSnakeBodyPart(m_SnakeElbowPolyhedra[1], bodyPart.m_Location, snakeSkinTexture);
                break;
            case ELBOW_BL:
                drawSnakeBodyPart(m_SnakeElbowPolyhedra[2], bodyPart.m_Location, snakeSkinTexture);
                break;
            case ELBOW_BR:
                drawSnakeBodyPart(m_SnakeElbowPolyhedra[3], bodyPart.m_Location, snakeSkinTexture);
                break;
        }
    }

    private enum Classification { HORIZONTAL, VERTICAL, ELBOW_TL, ELBOW_TR, ELBOW_BL, ELBOW_BR }

    private Classification classifyBodyPart(LinkedList<Snake.BodyPart> bodyParts, int i) {
        Snake.BodyPart current = bodyParts.get(i);
        Snake.BodyPart previous = bodyParts.get(i - 1);
        Snake.BodyPart next = bodyParts.get(i + 1);
        Snake.Direction previousDirection = current.classifyNeighbour(previous.m_Location);
        Snake.Direction nextDirection = current.classifyNeighbour(next.m_Location);
        if ((previousDirection == Snake.Direction.Right && nextDirection == Snake.Direction.Left) ||
            (previousDirection == Snake.Direction.Left && nextDirection == Snake.Direction.Right)) {
            return Classification.HORIZONTAL;
        }
        if ((previousDirection == Snake.Direction.Down && nextDirection == Snake.Direction.Up) ||
            (previousDirection == Snake.Direction.Up && nextDirection == Snake.Direction.Down)) {
            return Classification.VERTICAL;
        }
        switch (previousDirection) {
            case Left:
                return nextDirection == Snake.Direction.Up ? Classification.ELBOW_TL : Classification.ELBOW_BL;
            case Up:
                return nextDirection == Snake.Direction.Left ? Classification.ELBOW_TL : Classification.ELBOW_TR;
            case Right:
                return nextDirection == Snake.Direction.Up ? Classification.ELBOW_TR : Classification.ELBOW_BR;
        }
        return nextDirection == Snake.Direction.Left ? Classification.ELBOW_BL : Classification.ELBOW_BR;
    }

    private void drawSnakeBodyPart(GLStaticPolyhedronVxTcNm polyhedron, Vector2i location, GLTexture snakeSkinTexture) {
        float startX = GameField.WIDTH / 2.0f * -s_CellSize;
        float startZ = GameField.HEIGHT / 2.0f * -s_CellSize;

        float cellOffsetX = (startX + location.m_X * s_CellSize) + s_HalfCellSize;
        float cellOffsetZ = (-startZ - location.m_Z * s_CellSize) - s_HalfCellSize;

        m_ModelMatrix.identity().translate(cellOffsetX, s_ObjectYPosition, cellOffsetZ);
        m_MvMatrix.identity().mul(m_ViewMatrix).mul(m_ModelMatrix);
        m_ProjectionMatrix.set(m_Context.getPerspectiveMatrix());
        m_SpecularDirectionalLightProgram.activate(m_MvMatrix, m_ProjectionMatrix);

        // Change the texture of the first piece
        polyhedron.getPiece(0).setDiffuseTexture(snakeSkinTexture);
        polyhedron.draw();
    }
}
