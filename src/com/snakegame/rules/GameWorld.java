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

package com.snakegame.rules;

import com.snakegame.application.IAppStateContext;
import com.snakegame.application.LevelCompleteAppState;
import com.snakegame.application.SnakeDyingAppState;
import com.snakegame.client.NumberFont;
import com.snakegame.client.Texture;
import com.snakegame.client.TimeoutManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class GameWorld implements IGameWorld {
    private static final float s_CellSize = 3.0f;
    private static final int s_NumApples = 9;
    private static final int s_NumLevels = 10; // TODO: discover levels dynamically
    private static final int s_RangeBegin = 1;
    private static final int s_MaxPlayers = 2;
    private static final long s_MaxSnakeSpeedTimeoutMs = 150;
    private static final long s_MinSnakeSpeedTimeoutMs = 75;
    private static final long s_SnakeSpeedPowerUpAdjustment = 8;
    private static final long s_SnakeSpeedLevelAdjustment = 6;
    private static final long s_PowerUpInitialTimeoutMs = 3750;
    private static final long s_PowerUpSubsequentTimeoutMs = 15000;
    private static final long s_PowerUpExpireTimeoutMs = 6000;

    private final IAppStateContext m_AppStateContext;
    private final Random m_Rng;
    private final Mode m_Mode;
    private final Snake[] m_Snakes;
    private final Texture[] m_AppleTextures;
    private final Texture m_WallTexture;
    private final Texture m_DotTexture;
    private final Texture m_HeadTexture;
    private final Texture m_GameOverTexture;
    private final Texture m_GameWonTexture;
    private final Texture m_GetReadyTexture;
    private final Texture m_Player1DiedTexture;
    private final Texture m_Player2DiedTexture;
    private final Texture m_BothPlayersDiedTexture;
    private final Texture m_LevelCompleteTexture;
    private final Texture[] m_PowerUpTextures;
    private final NumberFont m_NumberFont;

    private GameField m_GameField;
    private long m_SnakeMovementTimeoutMs;
    private int m_SnakeMovementTimeoutId;
    private int m_SpawnPowerUpTimeoutId;
    private int m_ExpirePowerUpTimeoutId;
    private int m_CurrentLevel;
    private Vector2i m_PowerUpCell;

    public GameWorld(IAppStateContext appStateContext, Mode mode) throws IOException {
        m_AppStateContext = appStateContext;
        m_Rng = new Random();
        m_Mode = mode;
        m_CurrentLevel = 0;
        m_SnakeMovementTimeoutId = 0;
        m_SpawnPowerUpTimeoutId = 0;
        m_ExpirePowerUpTimeoutId = 0;

        m_AppleTextures = new Texture[s_NumApples];
        for (int i = 0; i < s_NumApples; ++i) {
            m_AppleTextures[i] = new Texture(ImageIO.read(new File(String.format("images\\Apple%d.png", i + 1))));
        }

        m_PowerUpTextures = new Texture[GameField.s_NumPowerUps];
        m_PowerUpTextures[0] = new Texture(ImageIO.read(new File("images\\DecreaseLength.png")));
        m_PowerUpTextures[1] = new Texture(ImageIO.read(new File("images\\IncreaseSpeed.png")));
        m_PowerUpTextures[2] = new Texture(ImageIO.read(new File("images\\DecreaseSpeed.png")));
        m_PowerUpTextures[3] = new Texture(ImageIO.read(new File("images\\IncreaseLives.png")));
        m_PowerUpTextures[4] = new Texture(ImageIO.read(new File("images\\DecreaseLives.png")));
        m_PowerUpTextures[5] = new Texture(ImageIO.read(new File("images\\IncreasePoints.png")));
        m_PowerUpTextures[6] = new Texture(ImageIO.read(new File("images\\DecreasePoints.png")));
        m_PowerUpTextures[7] = new Texture(ImageIO.read(new File("images\\Random.png")));

        m_WallTexture = new Texture(ImageIO.read(new File("images\\soil-seamless-texture.jpg")));
        m_DotTexture = new Texture(ImageIO.read(new File("images\\dot.png")));
        m_HeadTexture = new Texture(ImageIO.read(new File("images\\head.png")));
        m_GameOverTexture = new Texture(ImageIO.read(new File("images\\GameOver.png")));
        m_GameWonTexture = new Texture(ImageIO.read(new File("images\\GameWon.png")));
        m_GetReadyTexture = new Texture(ImageIO.read(new File("images\\GetReady.png")));
        m_Player1DiedTexture = new Texture(ImageIO.read(new File("images\\Player1Died.png")));
        m_Player2DiedTexture = new Texture(ImageIO.read(new File("images\\Player2Died.png")));
        m_BothPlayersDiedTexture = new Texture(ImageIO.read(new File("images\\BothSnakesDied.png")));
        m_LevelCompleteTexture = new Texture(ImageIO.read(new File("images\\LevelComplete.png")));
        m_NumberFont = new NumberFont();

        Vector2i minBounds = new Vector2i(0, 0);
        Vector2i maxBounds = new Vector2i(GameField.WIDTH - 1, GameField.HEIGHT - 1);

        m_Snakes = new Snake[m_Mode == Mode.TWO_PLAYERS ? 2 : 1];
        m_Snakes[0] = new Snake(Snake.Direction.Right, minBounds, maxBounds);
        if (m_Mode == Mode.TWO_PLAYERS) {
            m_Snakes[1] = new Snake(Snake.Direction.Left, minBounds, maxBounds);
        }
    }

    @Override
    public void freeNativeResources() {
        for (int i = 0; i < s_NumApples; ++i) {
            m_AppleTextures[i].freeNativeResource();
        }
        for (int i = 0; i < GameField.s_NumPowerUps; ++i) {
            m_PowerUpTextures[i].freeNativeResource();
        }
        m_WallTexture.freeNativeResource();
        m_DotTexture.freeNativeResource();
        m_HeadTexture.freeNativeResource();
        m_GameOverTexture.freeNativeResource();
        m_GameWonTexture.freeNativeResource();
        m_GetReadyTexture.freeNativeResource();
        m_Player1DiedTexture.freeNativeResource();
        m_Player2DiedTexture.freeNativeResource();
        m_BothPlayersDiedTexture.freeNativeResource();
        m_LevelCompleteTexture.freeNativeResource();
        m_NumberFont.freeNativeResource();
    }

    @Override
    public void loadFirstLevel(long nowMs) throws IOException {
        m_CurrentLevel = 0;
        loadLevelFile(m_CurrentLevel);
        resetForNewLevel(nowMs);
    }

    @Override
    public void loadNextLevel(long nowMs) throws IOException {
        if (m_CurrentLevel < s_NumLevels - 1) {
            ++m_CurrentLevel;
        }
        loadLevelFile(m_CurrentLevel);
        resetForNewLevel(nowMs);
    }

    @Override
    public void resetForNewLevel(long nowMs) {
        m_SnakeMovementTimeoutMs = s_MaxSnakeSpeedTimeoutMs - (s_SnakeSpeedLevelAdjustment * m_CurrentLevel);
        m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_1);
        for (var snake : m_Snakes) {
            snake.resetToInitialState();
        }
    }

    @Override
    public void resetAfterSnakeDeath(long nowMs) {
        m_GameField.resetToInitialState();
        m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_1);
        for (var snake : m_Snakes) {
            snake.resetToInitialState();
        }
    }

    @Override
    public boolean isLastLevel() {
        return m_CurrentLevel == s_NumLevels - 1;
    }

    @Override
    public Mode getMode() {
        return m_Mode;
    }

    @Override
    public Snake[] getSnakes() {
        return m_Snakes;
    }

    @Override
    public void start(long nowMs) {
        stop(nowMs);
        scheduleSnakeMovement(nowMs);
        scheduleSpawnPowerUp(nowMs, s_PowerUpInitialTimeoutMs);
    }

    private void loadLevelFile(int level) throws IOException {
        GameFieldFile file = new GameFieldFile(makeLevelFileName(level), m_Mode == Mode.TWO_PLAYERS);
        m_GameField = file.getGameField();
        m_Snakes[0].setStartPosition(m_GameField.getPlayer1Start());
        if (m_Mode == Mode.TWO_PLAYERS) {
            m_Snakes[1].setStartPosition(m_GameField.getPlayer2Start());
        }
    }

    private void scheduleSnakeMovement(long nowMs) {
        m_SnakeMovementTimeoutId = m_AppStateContext.addTimeout(m_SnakeMovementTimeoutMs, (callCount) -> {
            moveSnakesForwards();
            CollisionResult r = performCollisionDetection(nowMs);
            if (r.collisionOccurred()) {
                if (r.getResult() == CollisionResult.Result.BOTH_SNAKES) {
                    m_AppStateContext.changeState(new SnakeDyingAppState(m_AppStateContext, this));
                }
                else {
                    m_AppStateContext.changeState(new SnakeDyingAppState(m_AppStateContext, this, r.getPlayer()));
                }
            }
            return TimeoutManager.CallbackResult.KEEP_CALLING;
        });
    }

    private void scheduleSpawnPowerUp(long nowMs, long timeoutMs) {
        removeSpawnPowerUpTimeout();
        m_SpawnPowerUpTimeoutId = m_AppStateContext.addTimeout(timeoutMs, (callCount) -> {
            spawnRandomPowerUp();
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    private void scheduleExpirePowerUp(long nowMs) {
        removeExpirePowerUpTimeout();
        m_ExpirePowerUpTimeoutId = m_AppStateContext.addTimeout(s_PowerUpExpireTimeoutMs, (callCount1) -> {
            m_GameField.setCell(m_PowerUpCell, GameField.Cell.EMPTY);
            scheduleSpawnPowerUp(System.currentTimeMillis(), s_PowerUpInitialTimeoutMs);
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    private void spawnRandomPowerUp() {
        final GameField.Cell[] powerUps = {
                GameField.Cell.DEC_LENGTH, GameField.Cell.INC_SPEED, GameField.Cell.DEC_SPEED,
                GameField.Cell.INC_LIVES, GameField.Cell.DEC_LIVES, GameField.Cell.INC_POINTS,
                GameField.Cell.DEC_POINTS, GameField.Cell.RANDOM
        };

        m_PowerUpCell = chooseRandomEmptyCell();
        m_GameField.setCell(m_PowerUpCell, powerUps[m_Rng.nextInt(powerUps.length)]);

        scheduleExpirePowerUp(System.currentTimeMillis());
    }

    @Override
    public void stop(long nowMs) {
        if (m_SnakeMovementTimeoutId != 0) {
            m_AppStateContext.removeTimeout(m_SnakeMovementTimeoutId);
            m_SnakeMovementTimeoutId = 0;
        }
        removeSpawnPowerUpTimeout();
        removeExpirePowerUpTimeout();
    }

    private void removeSpawnPowerUpTimeout() {
        if (m_SpawnPowerUpTimeoutId != 0) {
            m_AppStateContext.removeTimeout(m_SpawnPowerUpTimeoutId);
            m_SpawnPowerUpTimeoutId = 0;
        }
    }

    private void removeExpirePowerUpTimeout() {
        if (m_ExpirePowerUpTimeoutId != 0) {
            m_GameField.setCell(m_PowerUpCell, GameField.Cell.EMPTY);
            m_AppStateContext.removeTimeout(m_ExpirePowerUpTimeoutId);
            m_ExpirePowerUpTimeoutId = 0;
        }
    }

    @Override
    public void think(long nowMs) {
        // TODO
    }

    @Override
    public void draw3d(long nowMs) {
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

                switch (m_GameField.getCell(x, y)) {
                    case EMPTY:
                        break;
                    case WALL:
                        drawTexturedQuad(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, x*u, y*v+v, x*u+u, y*v, m_WallTexture);
                        break;
                    case NUM_1:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[0]);
                        break;
                    case NUM_2:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[1]);
                        break;
                    case NUM_3:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[2]);
                        break;
                    case NUM_4:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[3]);
                        break;
                    case NUM_5:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[4]);
                        break;
                    case NUM_6:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[5]);
                        break;
                    case NUM_7:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[6]);
                        break;
                    case NUM_8:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[7]);
                        break;
                    case NUM_9:
                        drawSingleImage(cellOffsetX, cellOffsetY, s_CellSize, s_CellSize, m_AppleTextures[8]);
                        break;
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
        float y = m_AppStateContext.getWindowHeight() - NumberFont.s_FrameHeight;
        m_NumberFont.drawNumber(m_Snakes[0].getNumLives(), 0.0f, y);
        m_NumberFont.drawNumber(m_Snakes[0].getPoints(), 100.0f, y);
        if (m_Snakes.length > 1) {
            float width = m_NumberFont.calculateWidth(m_Snakes[1].getNumLives());
            m_NumberFont.drawNumber(m_Snakes[1].getNumLives(), m_AppStateContext.getWindowWidth() - width, y);
            width = m_NumberFont.calculateWidth(m_Snakes[1].getPoints()) + (2.0f * width);
            m_NumberFont.drawNumber(m_Snakes[1].getPoints(), m_AppStateContext.getWindowWidth() - width, y);
        }
    }

    @Override
    public Texture getGameOverTexture() {
        return m_GameOverTexture;
    }

    @Override
    public Texture getGameWonTexture() {
        return m_GameWonTexture;
    }

    @Override
    public Texture getGetReadyTexture() {
        return m_GetReadyTexture;
    }

    @Override
    public Texture getPlayer1DiedTexture() {
        return m_Player1DiedTexture;
    }

    @Override
    public Texture getPlayer2DiedTexture() {
        return m_Player2DiedTexture;
    }

    @Override
    public Texture getBothPlayersDiedTexture() {
        return m_BothPlayersDiedTexture;
    }

    @Override
    public Texture getLevelCompleteTexture() {
        return m_LevelCompleteTexture;
    }

    @Override
    public SubtractSnakeResult subtractSnake(int player) {
        if (player < 0 || player > s_MaxPlayers - 1) {
            throw new RuntimeException("Invalid player index");
        }
        if (m_Snakes[player].getNumLives() == 0) {
            return SubtractSnakeResult.NO_SNAKES_REMAIN;
        }
        m_Snakes[player].decrementLives();
        return SubtractSnakeResult.SNAKE_AVAILABLE;
    }

    private void moveSnakesForwards() {
        for (var snake : m_Snakes) {
            snake.moveForwards();
        }
    }

    private static class CollisionResult {
        private final Result m_Result;
        private final int m_Player;
        public enum Result { SINGLE_SNAKE, BOTH_SNAKES, NO_COLLISION }
        public CollisionResult() {
            m_Result = Result.NO_COLLISION;
            m_Player = -1;
        }
        public CollisionResult(boolean bothSnakes, int player) {
            m_Result = bothSnakes ? Result.BOTH_SNAKES :  Result.SINGLE_SNAKE;
            m_Player = bothSnakes ? -1 : Math.min(1, Math.max(0, player));
        }
        public boolean collisionOccurred() {
            return m_Result != Result.NO_COLLISION;
        }
        public Result getResult() {
            return m_Result;
        }
        public int getPlayer() {
            return m_Player;
        }
    }

    private CollisionResult performCollisionDetection(long nowMs) {
        CollisionResult r = collideSnakesWithWalls();
        if (r.collisionOccurred()) {
            return r;
        }
        r = collideSnakesWithThemselves();
        if (r.collisionOccurred()) {
            return r;
        }
        r = collideSnakesWithEachOther();
        if (r.collisionOccurred()) {
            return r;
        }

        for (var snake : m_Snakes) {
            checkSnakeForCollisionWithPowerUp(nowMs, snake);
        }
        return r;
    }

    private CollisionResult collideSnakesWithWalls() {
        boolean player1Colliding = isSnakeCollidingWithWall(m_Snakes[0]);
        boolean player2Colliding = m_Snakes.length > 1 && isSnakeCollidingWithWall(m_Snakes[1]);
        if (player1Colliding) {
            if (player2Colliding) {
                return new CollisionResult(true, -1);
            }
            return new CollisionResult(false, 0);
        }
        if (player2Colliding) {
            return new CollisionResult(false, 1);
        }
        return new CollisionResult();
    }

    private CollisionResult collideSnakesWithThemselves() {
        boolean player1Colliding = m_Snakes[0].isCollidingWithItself();
        boolean player2Colliding = m_Snakes.length > 1 && m_Snakes[1].isCollidingWithItself();
        if (player1Colliding) {
            if (player2Colliding) {
                return new CollisionResult(true, -1);
            }
            return new CollisionResult(false, 0);
        }
        if (player2Colliding) {
            return new CollisionResult(false, 1);
        }
        return new CollisionResult();
    }

    private CollisionResult collideSnakesWithEachOther() {
        if (m_Snakes.length > 1) {
            boolean player1Colliding = m_Snakes[0].isCollidingWith(m_Snakes[1]);
            boolean player2Colliding = m_Snakes[1].isCollidingWith(m_Snakes[0]);
            if (player1Colliding) {
                if (player2Colliding) {
                    return new CollisionResult(true, -1);
                }
                return new CollisionResult(false, 0);
            }
            if (player2Colliding) {
                return new CollisionResult(false, 1);
            }
        }
        return new CollisionResult();
    }

    private boolean isSnakeCollidingWithWall(Snake snake) {
        return m_GameField.getCell(snake.getBodyParts().getFirst()) == GameField.Cell.WALL;
    }

    private void checkSnakeForCollisionWithPowerUp(long nowMs, Snake snake) {
        if (isSnakeCollidingWithPowerUp(snake)) {
            var cell = m_GameField.getCell(snake.getBodyParts().getFirst());
            m_GameField.setCell(snake.getBodyParts().getFirst(), GameField.Cell.EMPTY);
            collectPowerUp(nowMs, cell, snake);
        }
    }

    private boolean isSnakeCollidingWithPowerUp(Snake snake) {
        var cell = m_GameField.getCell(snake.getBodyParts().getFirst());
        return cell != GameField.Cell.EMPTY && cell != GameField.Cell.WALL;
    }

    private void collectPowerUp(long nowMs, GameField.Cell powerUp, Snake snake) {
        switch (powerUp) {
            case INC_SPEED:
                m_SnakeMovementTimeoutMs = Math.max(s_MinSnakeSpeedTimeoutMs, m_SnakeMovementTimeoutMs - s_SnakeSpeedPowerUpAdjustment);
                start(nowMs);
                break;
            case DEC_SPEED:
                m_SnakeMovementTimeoutMs = Math.min(s_MaxSnakeSpeedTimeoutMs, m_SnakeMovementTimeoutMs + s_SnakeSpeedPowerUpAdjustment);
                start(nowMs);
                break;
            case RANDOM: {
                final GameField.Cell[] powerUps = {
                        GameField.Cell.DEC_LENGTH, GameField.Cell.INC_SPEED, GameField.Cell.DEC_SPEED,
                        GameField.Cell.INC_LIVES, GameField.Cell.DEC_LIVES, GameField.Cell.INC_POINTS,
                        GameField.Cell.DEC_POINTS
                };
                collectPowerUp(nowMs, powerUps[m_Rng.nextInt(powerUps.length)], snake);
                break;
            }
            default:
                switch (powerUp) {
                    case NUM_1: m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_2); break;
                    case NUM_2: m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_3); break;
                    case NUM_3: m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_4); break;
                    case NUM_4: m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_5); break;
                    case NUM_5: m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_6); break;
                    case NUM_6: m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_7); break;
                    case NUM_7: m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_8); break;
                    case NUM_8: m_GameField.setCell(chooseRandomEmptyCell(), GameField.Cell.NUM_9); break;
                    case NUM_9:
                        m_AppStateContext.changeState(new LevelCompleteAppState(m_AppStateContext, this));
                        break;
                }
                snake.collectPowerUp(powerUp);
                break;
        }

        scheduleNextPowerUpSpawn(powerUp);
    }

    private Vector2i chooseRandomEmptyCell() {
        ArrayList<Vector2i> emptyFieldCells = m_GameField.getEmptyCells();
        ArrayList<Vector2i> emptyCells = new ArrayList<>(emptyFieldCells.size());
        for (var emptyFieldCell : emptyFieldCells) {
            if (!isEitherSnakeUsingThisCell(emptyFieldCell)) {
                emptyCells.add(emptyFieldCell);
            }
        }
        return emptyCells.get(m_Rng.nextInt(emptyCells.size()));
    }

    private boolean isEitherSnakeUsingThisCell(Vector2i cell) {
        for (var snake : m_Snakes) {
            for (var bodyPart : snake.getBodyParts()) {
                if (bodyPart.equals(cell)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void scheduleNextPowerUpSpawn(GameField.Cell lastPowerUp) {
        switch (lastPowerUp) {
            case DEC_LENGTH:
            case INC_SPEED:
            case DEC_SPEED:
            case INC_LIVES:
            case DEC_LIVES:
            case INC_POINTS:
            case DEC_POINTS:
            case RANDOM:
                removeExpirePowerUpTimeout();
                scheduleSpawnPowerUp(System.currentTimeMillis(), s_PowerUpSubsequentTimeoutMs);
                break;
            default:
                break;
        }
    }

    private String makeLevelFileName(int level) {
        return String.format("levels\\Level%02d.txt", level);
    }

    private void drawTexturedQuad(double x, double y, double w, double h, double u0, double v0, double u1, double v1, Texture texture) {
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBegin(GL_QUADS);
        glTexCoord2d(u0, v0); glVertex2d(x, y + h);
        glTexCoord2d(u0, v1); glVertex2d(x , y);
        glTexCoord2d(u1, v1); glVertex2d(x + w, y);
        glTexCoord2d(u1, v0); glVertex2d(x + w, y + h);
        glEnd();
    }

    private void drawSingleImage(double x, double y, double w, double h, Texture texture) {
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBegin(GL_QUADS);
        glTexCoord2d(0.0, 0.0); glVertex3d(x, y + h, 0.1f);
        glTexCoord2d(0.0, 1.0); glVertex3d(x , y, 0.1f);
        glTexCoord2d(1.0, 1.0); glVertex3d(x + w, y, 0.1f);
        glTexCoord2d(1.0, 0.0); glVertex3d(x + w, y + h, 0.1f);
        glEnd();
    }

    // https://stackoverflow.com/questions/5271598/java-generate-random-number-between-two-given-values
    private int getRandomNumber(int rangeEnd) {
        return m_Rng.nextInt(rangeEnd - s_RangeBegin) + s_RangeBegin;
    }
}
