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
// Designed and implemented for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.rules;

import com.snakegame.application.IAppStateContext;
import com.snakegame.application.SnakeDyingAppState;
import com.snakegame.client.Texture;
import com.snakegame.client.TimeoutManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class GameWorld implements IGameWorld {
    private static final int s_NumLevels = 10;
    private static final int s_RangeBegin = 1;
    private static final int s_MaxPlayers = 2;
    private static final int s_NumStartingPlayerSnakes = 3;
    private static final int s_MaxNumPlayerSnakes = 5;

    private final IAppStateContext m_AppStateContext;
    private final Random m_Rng;
    private final Mode m_Mode;
    private final Snake[] m_Snakes;
    private final Texture m_WallTexture;
    private final Texture m_AppleTexture;
    private final Texture m_DotTexture;
    private final Texture m_HeadTexture;
    private final Texture m_GameOverTexture;
    private final Texture m_GameWonTexture;
    private final Texture m_GetReadyTexture;
    private final Texture m_Player1DiedTexture;
    private final Texture m_Player2DiedTexture;
    private final Texture m_BothPlayersDiedTexture;
    private final int[] m_NumPlayerSnakes;

    private GameField m_GameField;
    private int m_AppleTimeoutId = 0;
    private int m_SnakeTimeoutId = 0;
    private int m_CurrentLevel;

    public GameWorld(IAppStateContext appStateContext, Mode mode) throws IOException {
        m_AppStateContext = appStateContext;
        m_Rng = new Random();
        m_Mode = mode;
        m_CurrentLevel = 0;
        m_NumPlayerSnakes = new int[s_MaxPlayers];
        m_NumPlayerSnakes[0] = s_NumStartingPlayerSnakes - 1; // Allocate a snake right now
        m_NumPlayerSnakes[1] = s_NumStartingPlayerSnakes - 1; // Allocate a snake right now

        m_WallTexture = new Texture(ImageIO.read(new File("soil-seamless-texture.jpg")));
        m_AppleTexture = new Texture(ImageIO.read(new File("icons8-green-apple-48.png")));
        m_DotTexture = new Texture(ImageIO.read(new File("dot.png")));
        m_HeadTexture = new Texture(ImageIO.read(new File("head.png")));
        m_GameOverTexture = new Texture(ImageIO.read(new File("GameOver.png")));
        m_GameWonTexture = new Texture(ImageIO.read(new File("GameWon.png")));
        m_GetReadyTexture = new Texture(ImageIO.read(new File("GetReady.png")));
        m_Player1DiedTexture = new Texture(ImageIO.read(new File("Player1Died.png")));
        m_Player2DiedTexture = new Texture(ImageIO.read(new File("Player2Died.png")));
        m_BothPlayersDiedTexture = new Texture(ImageIO.read(new File("BothSnakesDied.png")));

        GameFieldFile file = new GameFieldFile(makeLevelFileName(), m_Mode == Mode.TWO_PLAYERS);
        m_GameField = file.getGameField();

        Vector2i minBounds = new Vector2i(0, 0);
        Vector2i maxBounds = new Vector2i(GameField.WIDTH - 1, GameField.HEIGHT - 1);

        m_Snakes = new Snake[m_Mode == Mode.TWO_PLAYERS ? 2 : 1];
        m_Snakes[0] = new Snake(m_GameField.getPlayer1Start(), Snake.Direction.Right, minBounds, maxBounds);
        if (m_Mode == Mode.TWO_PLAYERS) {
            m_Snakes[1] = new Snake(m_GameField.getPlayer2Start(), Snake.Direction.Left, minBounds, maxBounds);
        }
    }

    @Override
    public void close() {
        m_WallTexture.delete();
        m_AppleTexture.delete();
        m_DotTexture.delete();
        m_HeadTexture.delete();
        m_GameOverTexture.delete();
        m_GameWonTexture.delete();
        m_GetReadyTexture.delete();
        m_Player1DiedTexture.delete();
        m_Player2DiedTexture.delete();
    }

    @Override
    public void reset(long nowMs) throws IOException {
        GameFieldFile file = new GameFieldFile(makeLevelFileName(), m_Mode == Mode.TWO_PLAYERS);
        m_GameField = file.getGameField();
        for (var snake : m_Snakes) {
            snake.reset();
        }
    }

    @Override
    public Mode getMode() {
        return m_Mode;
    }

    @Override
    public GameField getGameField() {
        return m_GameField;
    }

    @Override
    public Snake[] getSnakes() {
        return m_Snakes;
    }

    @Override
    public void start(long nowMs) {
        startAppleRecurringCallback(nowMs);
        startSnakeRecurringCallback(nowMs);
    }

    @Override
    public void stop(long nowMs) {
        stopAppleRecurringCallback();
        stopSnakeRecurringCallback();
    }

    @Override
    public void think(long nowMs) {
        // TODO
    }

    @Override
    public void draw3d(long nowMs) {
        float cellSize = 2.0f;
        float maxWidth = GameField.WIDTH * cellSize;
        float maxHeight = GameField.HEIGHT * cellSize;
        float startX = -maxWidth / 2.0f;
        float startY = -maxHeight / 2.0f;
        float u = m_WallTexture.getWidth() / cellSize;
        float v = m_WallTexture.getHeight() / cellSize;

        for (int y = 0; y < GameField.HEIGHT; ++y) {
            float cellOffsetY = startY + y * cellSize;
            for (int x = 0; x < GameField.WIDTH; ++x) {
                float cellOffsetX = startX + x * cellSize;

                switch (m_GameField.getCell(x, y))
                {
                    case EMPTY:
                        drawColouredQuad(cellOffsetX, cellOffsetY, cellSize, cellSize);
                        break;
                    case WALL:
                        drawTexturedQuad(cellOffsetX, cellOffsetY, cellSize, cellSize, u, v, m_WallTexture);
                        break;
                    case APPLE:
                        drawSingleImage(cellOffsetX, cellOffsetY, cellSize, cellSize, m_AppleTexture);
                        break;
                }
            }
        }

        boolean firstLoop;
        for (var snake : m_Snakes) {
            firstLoop = true;
            for (var bodyPart : snake.getBodyParts()) {
                float cellOffsetX = startX + bodyPart.m_X * cellSize;
                float cellOffsetY = startY + bodyPart.m_Y * cellSize;
                drawSingleImage(cellOffsetX, cellOffsetY, cellSize, cellSize, firstLoop ? m_HeadTexture : m_DotTexture);
                firstLoop = false;
            }
        }
    }

    @Override
    public void draw2d(long nowMs) {
        // TODO
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
    public SubtractSnakeResult subtractSnake(int player) {
        if (player < 0 || player > s_MaxPlayers - 1) {
            throw new RuntimeException("Invalid player index");
        }
        if (m_NumPlayerSnakes[player] == 0) {
            return SubtractSnakeResult.NO_SNAKES_REMAIN;
        }
        --m_NumPlayerSnakes[player];
        return SubtractSnakeResult.SNAKE_AVAILABLE;
    }

    private void startAppleRecurringCallback(long nowMs) {
        stopAppleRecurringCallback();

        m_AppleTimeoutId = m_AppStateContext.addTimeout(nowMs, 5000, (callCount) -> {
            Vector2i position = getEmptyGameFieldCell();
            m_GameField.setCell(position, GameField.Cell.APPLE);
            return TimeoutManager.CallbackResult.KEEP_CALLING;
        });
    }

    private void stopAppleRecurringCallback() {
        if (m_AppleTimeoutId != 0) {
            m_AppStateContext.removeTimeout(m_AppleTimeoutId);
            m_AppleTimeoutId = 0;
        }
    }

    private void startSnakeRecurringCallback(long nowMs) {
        stopSnakeRecurringCallback();

        m_SnakeTimeoutId = m_AppStateContext.addTimeout(nowMs, 200, (callCount) -> {
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

    private void stopSnakeRecurringCallback() {
        if (m_SnakeTimeoutId != 0) {
            m_AppStateContext.removeTimeout(m_SnakeTimeoutId);
            m_SnakeTimeoutId = 0;
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
            checkSnakeForCollisionWithAnApple(snake);
        }
        return r;
    }

    private CollisionResult collideSnakesWithWalls() {
        boolean player1Colliding = isSnakeCollidingWithAWall(m_Snakes[0]);
        boolean player2Colliding = m_Snakes.length > 1 && isSnakeCollidingWithAWall(m_Snakes[1]);
        if (player1Colliding) {
            if (player2Colliding) {
                return new CollisionResult(true, -1);
            }
            return new CollisionResult(false, 0);
        }
        else if (player2Colliding) {
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
        else if (player2Colliding) {
            return new CollisionResult(false, 1);
        }
        return new CollisionResult();
    }

    private CollisionResult collideSnakesWithEachOther() {
        if (m_Snakes.length > 1 && m_Snakes[0].isCollidingWith(m_Snakes[1])) {
            return new CollisionResult(true, -1);
        }
        return new CollisionResult();
    }

    private boolean isSnakeCollidingWithAWall(Snake snake) {
        return m_GameField.getCell(snake.getBodyParts().getFirst()) == GameField.Cell.WALL;
    }

    private void checkSnakeForCollisionWithAnApple(Snake snake) {
        if (isSnakeCollidingWithAnApple(snake)) {
            snake.addOneBodyPart();
            m_GameField.setCell(snake.getBodyParts().getFirst(), GameField.Cell.EMPTY);
        }
    }

    private boolean isSnakeCollidingWithAnApple(Snake snake) {
        return m_GameField.getCell(snake.getBodyParts().getFirst()) == GameField.Cell.APPLE;
    }

    private String makeLevelFileName() {
        return String.format("Level%02d.txt", m_CurrentLevel);
    }

    private void drawColouredQuad(double x, double y, double w, double h) {
        glColor4d(201.0/255.0, 203.0/255.0, 204.0/255.0, 1.0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glVertex2d(x, y + h);
        glVertex2d(x , y);
        glVertex2d(x + w, y );
        glVertex2d(x + w, y + h);
        glEnd();
    }

    private void drawTexturedQuad(double x, double y, double w, double h, double u, double v, Texture texture) {
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBegin(GL_QUADS);
        glTexCoord2d(x, y + v); glVertex2d(x, y + h);
        glTexCoord2d(x, y); glVertex2d(x , y);
        glTexCoord2d(x + u, y); glVertex2d(x + w, y);
        glTexCoord2d(x + u, y + v); glVertex2d(x + w, y + h);
        glEnd();
    }

    private void drawSingleImage(double x, double y, double w, double h, Texture texture) {
        glColor4d(1.0, 1.0, 1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBegin(GL_QUADS);
        glTexCoord2d(0.0, 1.0); glVertex3d(x, y + h, 0.1f);
        glTexCoord2d(0.0, 0.0); glVertex3d(x , y, 0.1f);
        glTexCoord2d(1.0, 0.0); glVertex3d(x + w, y, 0.1f);
        glTexCoord2d(1.0, 1.0); glVertex3d(x + w, y + h, 0.1f);
        glEnd();
    }

    public Vector2i getEmptyGameFieldCell() {
        boolean found = false;
        int x, y;
        do {
            do {
                // There's always a border wall, therefore don't bother generating coordinates for the border.
                x = getRandomNumber(GameField.WIDTH - 2);
                y = getRandomNumber(GameField.HEIGHT - 2);
            }
            while (m_GameField.getCell(x, y) != GameField.Cell.EMPTY);

            for (var snake : m_Snakes) {
                for (int i = 0; i < snake.getBodyParts().size() && !found; ++i) {
                    found = snake.getBodyParts().get(i).equals(new Vector2i(x, y));
                }
            }
        }
        while (found);
        return new Vector2i(x, y);
    }

    // https://stackoverflow.com/questions/5271598/java-generate-random-number-between-two-given-values
    private int getRandomNumber(int rangeEnd) {
        return m_Rng.nextInt(rangeEnd - s_RangeBegin) + s_RangeBegin;
    }
}
