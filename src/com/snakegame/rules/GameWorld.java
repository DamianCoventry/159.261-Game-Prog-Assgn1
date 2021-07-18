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
import java.util.Objects;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class GameWorld implements IGameWorld {
    private static final float s_CellSize = 3.0f;
    private static final int s_NumApples = 9;
    private static final int s_MaxPlayers = 2;
    private static final long s_MaxSnakeSpeedTimeoutMs = 150;
    private static final long s_MinSnakeSpeedTimeoutMs = 75;
    private static final long s_SnakeSpeedPowerUpAdjustment = 8;
    private static final long s_SnakeSpeedLevelAdjustment = 6;
    private static final long s_PowerUpInitialTimeoutMs = 3750;
    private static final long s_PowerUpSubsequentTimeoutMs = 15000;
    private static final long s_PowerUpExpireTimeoutMs = 6000;
    private static final long s_InsertWallsTimeoutMs = 12000;

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
    private final Texture m_GamePausedTexture;
    private final Texture[] m_PowerUpTextures;
    private final NumberFont m_NumberFont;
    private final ArrayList<String> m_LevelFileNames;

    private GameField m_GameField;
    private PowerUp m_PowerUp;
    private Number m_Number;
    private long m_SnakeTimeoutMs;
    private int m_SnakeTimeoutId;
    private int m_PowerUpTimeoutId;
    private int m_WallsTimeoutId;
    private int m_CurrentLevel;

    public GameWorld(IAppStateContext appStateContext, Mode mode) throws IOException {
        m_AppStateContext = appStateContext;
        m_Rng = new Random();
        m_Mode = mode;
        m_CurrentLevel = 0;
        m_SnakeTimeoutId = 0;
        m_PowerUpTimeoutId = 0;

        m_LevelFileNames = new ArrayList<>();
        File directory = new File("levels");
        for (var file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                m_LevelFileNames.add(file.getName());
            }
        }
        if (m_LevelFileNames.isEmpty()) {
            throw new RuntimeException("There are no level files");
        }
        m_LevelFileNames.sort(String::compareToIgnoreCase);

        m_AppleTextures = new Texture[s_NumApples];
        for (int i = 0; i < s_NumApples; ++i) {
            m_AppleTextures[i] = new Texture(ImageIO.read(new File(String.format("images\\Apple%d.png", i + 1))));
        }

        m_PowerUpTextures = new Texture[8];
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
        m_GamePausedTexture = new Texture(ImageIO.read(new File("images\\GamePaused.png")));
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
        for (int i = 0; i < 8; ++i) {
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
        m_GamePausedTexture.freeNativeResource();
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
        if (m_CurrentLevel < m_LevelFileNames.size() - 1) {
            ++m_CurrentLevel;
        }
        loadLevelFile(m_CurrentLevel);
        resetForNewLevel(nowMs);
    }

    @Override
    public void resetForNewLevel(long nowMs) {
        m_SnakeTimeoutMs = s_MaxSnakeSpeedTimeoutMs - (s_SnakeSpeedLevelAdjustment * m_CurrentLevel);
        insertNumber(Number.Type.NUM_1);
        for (var snake : m_Snakes) {
            snake.resetToInitialState();
        }
    }
    
    @Override
    public void resetAfterSnakeDeath(long nowMs) {
        m_GameField.clearPowerUpsAndNumbers();
        insertNumber(Number.Type.NUM_1);
        for (var snake : m_Snakes) {
            snake.resetToInitialState();
        }
    }

    @Override
    public boolean isLastLevel() {
        return m_CurrentLevel == m_LevelFileNames.size() - 1;
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
        scheduleSnakeMovement();
        scheduleInsertPowerUp(s_PowerUpInitialTimeoutMs);
        scheduleInsertWalls();
    }

    @Override
    public void stop(long nowMs) {
        removeSnakeMovementTimeout();
        removePowerUpTimeout();
        removeWallsTimeout();
    }

    @Override
    public void think(long nowMs) {
        // No work to do
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
        float y = m_AppStateContext.getWindowHeight() - NumberFont.s_FrameHeight;

        // Draw the level's state
        float width = m_NumberFont.calculateWidth(m_CurrentLevel + 1);
        m_NumberFont.drawNumber(m_CurrentLevel + 1, (m_AppStateContext.getWindowWidth() / 2.0f) - (2.0f * width), y);
        width = m_NumberFont.calculateWidth(m_LevelFileNames.size());
        m_NumberFont.drawNumber(m_LevelFileNames.size(), (m_AppStateContext.getWindowWidth() / 2.0f) + width, y);

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
    public Texture getGamePausedTexture() {
        return m_GamePausedTexture;
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

    private void insertPowerUp(PowerUp.Type type) {
        if (m_PowerUp != null) {
            m_GameField.removePowerUp(m_PowerUp);
        }
        m_PowerUp = new PowerUp(type, chooseRandomEmptyCell());
        m_GameField.insertPowerUp(m_PowerUp);
    }

    private void insertNumber(Number.Type type) {
        if (m_Number != null) {
            m_GameField.removeNumber(m_Number);
        }
        m_Number = new Number(type, chooseRandomEmptyCell());
        m_GameField.insertNumber(m_Number);
    }

    private void loadLevelFile(int level) throws IOException {
        GameFieldFile file = new GameFieldFile("levels\\" + m_LevelFileNames.get(level), m_Mode == Mode.TWO_PLAYERS);
        m_GameField = file.getGameField();
        m_GameField.setWallBorder();
        m_Snakes[0].setStartPosition(m_GameField.getPlayer1Start());
        if (m_Mode == Mode.TWO_PLAYERS) {
            m_Snakes[1].setStartPosition(m_GameField.getPlayer2Start());
        }
    }

    private void scheduleSnakeMovement() {
        m_SnakeTimeoutId = m_AppStateContext.addTimeout(m_SnakeTimeoutMs, (callCount) -> {
            moveSnakesForwards();
            CollisionResult r = performCollisionDetection();
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

    private void refreshSnakeMovementTimeout() {
        removeSnakeMovementTimeout();
        scheduleSnakeMovement();
    }

    private void scheduleInsertPowerUp(long timeoutMs) {
        removePowerUpTimeout();
        m_PowerUpTimeoutId = m_AppStateContext.addTimeout(timeoutMs, (callCount) -> {
            insertRandomPowerUp();
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    private PowerUp.Type chooseRandomPowerUpType() {
        final PowerUp.Type[] powerUps = {
                PowerUp.Type.INC_SPEED, PowerUp.Type.DEC_SPEED,
                PowerUp.Type.INC_LIVES, PowerUp.Type.DEC_LIVES,
                PowerUp.Type.INC_POINTS, PowerUp.Type.DEC_POINTS,
                PowerUp.Type.DEC_LENGTH, PowerUp.Type.RANDOM
        };
        return powerUps[m_Rng.nextInt(powerUps.length)];
    }

    private PowerUp.Type chooseRandomPowerUpTypeExceptForRandom() {
        final PowerUp.Type[] powerUps = {
                PowerUp.Type.INC_SPEED, PowerUp.Type.DEC_SPEED,
                PowerUp.Type.INC_LIVES, PowerUp.Type.DEC_LIVES,
                PowerUp.Type.INC_POINTS, PowerUp.Type.DEC_POINTS,
                PowerUp.Type.DEC_LENGTH
        };
        return powerUps[m_Rng.nextInt(powerUps.length)];
    }

    private void insertRandomPowerUp() {
        insertPowerUp(chooseRandomPowerUpType());
        scheduleExpirePowerUp();
    }

    private void scheduleExpirePowerUp() {
        removePowerUpTimeout();
        m_PowerUpTimeoutId = m_AppStateContext.addTimeout(s_PowerUpExpireTimeoutMs, (callCount1) -> {
            if (m_PowerUp != null) {
                m_GameField.removePowerUp(m_PowerUp);
                m_PowerUp = null;
            }
            scheduleInsertPowerUp(s_PowerUpInitialTimeoutMs);
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    private void scheduleInsertWalls() {
        removeWallsTimeout();
        m_WallsTimeoutId = m_AppStateContext.addTimeout(s_InsertWallsTimeoutMs, (callCount1) -> {
            insertWalls();
            return TimeoutManager.CallbackResult.KEEP_CALLING;
        });
    }

    public void insertWalls() {
        Snake.Direction[] directions = {
                Snake.Direction.Left, Snake.Direction.Up,
                Snake.Direction.Right, Snake.Direction.Down
        };

        Vector2i increment;
        switch (directions[m_Rng.nextInt(4)]) {
            case Left: increment = new Vector2i(-1, 0); break;
            case Right: increment = new Vector2i(1, 0); break;
            case Up: increment = new Vector2i(0, 1); break;
            default: case Down: increment = new Vector2i(0, -1); break;
        }

        Vector2i location = chooseRandomEmptyCell();
        int numWalls = m_Rng.nextInt(4) + 1;
        for (int i = 0; i < numWalls; ++i) {
            if (isCellEmpty(location)) {
                m_GameField.insertWall(location);
            }
            location = m_GameField.clampCoordinates(location.add(increment));
        }
    }

    private void removeWallsTimeout() {
        if (m_WallsTimeoutId != 0) {
            m_AppStateContext.removeTimeout(m_WallsTimeoutId);
            m_WallsTimeoutId = 0;
        }
    }

    private void removeSnakeMovementTimeout() {
        if (m_SnakeTimeoutId != 0) {
            m_AppStateContext.removeTimeout(m_SnakeTimeoutId);
            m_SnakeTimeoutId = 0;
        }
    }

    private void removePowerUpTimeout() {
        if (m_PowerUpTimeoutId != 0) {
            m_AppStateContext.removeTimeout(m_PowerUpTimeoutId);
            m_PowerUpTimeoutId = 0;
        }
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

    private CollisionResult performCollisionDetection() {
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
            checkSnakeForCollisionWithNumber(snake);
            checkSnakeForCollisionWithPowerUp(snake);
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
        return m_GameField.getCellType(snake.getBodyParts().getFirst()) == GameField.CellType.WALL;
    }

    private void checkSnakeForCollisionWithNumber(Snake snake) {
        if (isSnakeCollidingWithNumber(snake) && m_Number != null) {
            awardNumber(m_Number.getType(), snake);
        }
    }

    private void checkSnakeForCollisionWithPowerUp(Snake snake) {
        if (isSnakeCollidingWithPowerUp(snake) && m_PowerUp != null) {
            removePowerUpTimeout();
            awardPowerUp(m_PowerUp.getType(), snake, true);
        }
    }

    private boolean isSnakeCollidingWithNumber(Snake snake) {
        return m_GameField.getCellType(snake.getBodyParts().getFirst()) == GameField.CellType.NUMBER;
    }

    private boolean isSnakeCollidingWithPowerUp(Snake snake) {
        return m_GameField.getCellType(snake.getBodyParts().getFirst()) == GameField.CellType.POWER_UP;
    }

    private void awardNumber(Number.Type numberType, Snake snake) {
        snake.awardNumber(numberType);

        if (m_Number != null) {
            m_GameField.removeNumber(m_Number);
            m_Number = null;
        }

        Number.Result r = Number.getNextInSeries(numberType);
        if (r.m_LevelComplete) {
            m_AppStateContext.changeState(new LevelCompleteAppState(m_AppStateContext, this));
        }
        else {
            insertNumber(r.m_Type);
        }
    }

    private void awardPowerUp(PowerUp.Type powerUpType, Snake snake, boolean scheduleNext) {
        if (m_PowerUp != null) {
            m_GameField.removePowerUp(m_PowerUp);
            m_PowerUp = null;
        }

        switch (powerUpType) {
            case INC_SPEED:
                m_SnakeTimeoutMs = Math.max(s_MinSnakeSpeedTimeoutMs, m_SnakeTimeoutMs - s_SnakeSpeedPowerUpAdjustment);
                refreshSnakeMovementTimeout();
                break;
            case DEC_SPEED:
                m_SnakeTimeoutMs = Math.min(s_MaxSnakeSpeedTimeoutMs, m_SnakeTimeoutMs + s_SnakeSpeedPowerUpAdjustment);
                refreshSnakeMovementTimeout();
                break;
            case RANDOM: {
                awardPowerUp(chooseRandomPowerUpTypeExceptForRandom(), snake, false);
                break;
            }
            default:
                snake.awardPowerUp(powerUpType);
                break;
        }

        if (scheduleNext) {
            scheduleInsertPowerUp(s_PowerUpSubsequentTimeoutMs);
        }
    }

    private Vector2i chooseRandomEmptyCell() {
        ArrayList<Vector2i> emptyFieldCells = m_GameField.getEmptyCells();
        ArrayList<Vector2i> emptyCells = new ArrayList<>(emptyFieldCells.size());
        for (var emptyFieldCell : emptyFieldCells) {
            if (isNeitherSnakeUsingThisCell(emptyFieldCell)) {
                emptyCells.add(emptyFieldCell);
            }
        }
        return emptyCells.get(m_Rng.nextInt(emptyCells.size()));
    }

    private boolean isNeitherSnakeUsingThisCell(Vector2i cell) {
        for (var snake : m_Snakes) {
            for (var bodyPart : snake.getBodyParts()) {
                if (bodyPart.equals(cell)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCellEmpty(Vector2i location) {
        return m_GameField.getCellType(location) == GameField.CellType.EMPTY && isNeitherSnakeUsingThisCell(location);
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
}
