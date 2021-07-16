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

package com.snakegame.application;

import com.snakegame.client.Texture;
import com.snakegame.rules.GameField;
import com.snakegame.rules.IGameWorld;
import com.snakegame.rules.Snake;
import com.snakegame.rules.Vector2i;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PlayingGameAppState implements IAppState {
    private static final int s_RangeBegin = 1;
    private final IAppStateContext m_AppStateContext;
    private final IGameWorld m_GameWorld;
    private final Random m_Rng;
    private Texture m_WallTexture;
    private Texture m_AppleTexture;
    private Texture m_DotTexture;
    private Texture m_HeadTexture;
    private long m_LastAppleTime;
    private long m_LastMovementTime;

    public PlayingGameAppState(IAppStateContext context, IGameWorld gameWorld) {
        m_AppStateContext = context;
        m_GameWorld = gameWorld;
        m_Rng = new Random();
    }

    @Override
    public void onStateBegin() throws IOException {
        m_WallTexture = new Texture(ImageIO.read(new File("soil-seamless-texture.jpg")));
        m_AppleTexture = new Texture(ImageIO.read(new File("icons8-green-apple-48.png")));
        m_DotTexture = new Texture(ImageIO.read(new File("dot.png")));
        m_HeadTexture = new Texture(ImageIO.read(new File("head.png")));
        m_LastAppleTime = System.currentTimeMillis();
        m_LastMovementTime = m_LastAppleTime;
    }

    @Override
    public void onStateEnd() {
        m_WallTexture.close();
        m_AppleTexture.close();
        m_DotTexture.close();
        m_HeadTexture.close();
    }

    @Override
    public void processKeyEvent(long window, int key, int scanCode, int action, int mods) {
        if (action != GLFW_PRESS) {
            return;
        }
        if (key == GLFW_KEY_ESCAPE) {
            m_AppStateContext.changeState(new RunningMenuAppState(m_AppStateContext));
        }
        
        Snake player1 = m_GameWorld.getSnakes()[0];
        if (key == GLFW_KEY_W) {
            if (player1.getDirection() == Snake.Direction.Left || player1.getDirection() == Snake.Direction.Right) {
                player1.setDirection(Snake.Direction.Up);
            }
        }
        if (key == GLFW_KEY_S) {
            if (player1.getDirection() == Snake.Direction.Left || player1.getDirection() == Snake.Direction.Right) {
                player1.setDirection(Snake.Direction.Down);
            }
        }
        if (key == GLFW_KEY_A) {
            if (player1.getDirection() == Snake.Direction.Up || player1.getDirection() == Snake.Direction.Down) {
                player1.setDirection(Snake.Direction.Left);
            }
        }
        if (key == GLFW_KEY_D) {
            if (player1.getDirection() == Snake.Direction.Up || player1.getDirection() == Snake.Direction.Down) {
                player1.setDirection(Snake.Direction.Right);
            }
        }
       
        if (m_GameWorld.getMode() == IGameWorld.Mode.TWO_PLAYERS) {
            Snake player2 = m_GameWorld.getSnakes()[1];
            if (key == GLFW_KEY_UP) {
                if (player2.getDirection() == Snake.Direction.Left || player2.getDirection() == Snake.Direction.Right) {
                    player2.setDirection(Snake.Direction.Up);
                }
            }
            if (key == GLFW_KEY_DOWN) {
                if (player2.getDirection() == Snake.Direction.Left || player2.getDirection() == Snake.Direction.Right) {
                    player2.setDirection(Snake.Direction.Down);
                }
            }
            if (key == GLFW_KEY_LEFT) {
                if (player2.getDirection() == Snake.Direction.Up || player2.getDirection() == Snake.Direction.Down) {
                    player2.setDirection(Snake.Direction.Left);
                }
            }
            if (key == GLFW_KEY_RIGHT) {
                if (player2.getDirection() == Snake.Direction.Up || player2.getDirection() == Snake.Direction.Down) {
                    player2.setDirection(Snake.Direction.Right);
                }
            }
        }
    }

    @Override
    public void think(long nowMs) throws IOException {
        if (nowMs - m_LastAppleTime >= 5000) {
            Vector2i position = getEmptyGameFieldCell();
            m_GameWorld.getGameField().setCell(position, GameField.Cell.APPLE);
            m_LastAppleTime = nowMs;
        }
        if (nowMs - m_LastMovementTime >= 100) {
            boolean reset = false;
            for (var snake : m_GameWorld.getSnakes()) {
                snake.moveForwards();
                m_LastMovementTime = nowMs;
                if (snake.isCollidingWithItself()) {
                    reset = true;
                } else {
                    Vector2i position = snake.getBodyParts().getFirst();
                    GameField.Cell cell = m_GameWorld.getGameField().getCell(position);
                    if (cell == GameField.Cell.APPLE) {
                        snake.addOneBodyPart();
                        m_GameWorld.getGameField().setCell(position, GameField.Cell.EMPTY);
                    } else if (cell == GameField.Cell.WALL) {
                        reset = true;
                    }
                }
            }
            if (m_GameWorld.getSnakes().length > 1) {
                reset = m_GameWorld.getSnakes()[0].isCollidingWith(m_GameWorld.getSnakes()[1]);
            }
            if (reset) {
                m_GameWorld.reset();
                m_LastAppleTime = nowMs;
            }
        }
    }

    @Override
    public void perspectiveDrawing(long nowMs) {
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

                switch (m_GameWorld.getGameField().getCell(x, y))
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
        for (var snake : m_GameWorld.getSnakes()) {
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
    public void orthographicDrawing(long nowMs) {
        // TODO: draw game status
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
            while (m_GameWorld.getGameField().getCell(x, y) != GameField.Cell.EMPTY);

            for (var snake : m_GameWorld.getSnakes()) {
                for (int i = 0; i < snake.getBodyParts().size() && !found; ++i) {
                    found = snake.getBodyParts().get(i).equals(new Vector2i(x, y));
                }
            }
        }
        while (found);
        return new Vector2i(x, y);
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

    // https://stackoverflow.com/questions/5271598/java-generate-random-number-between-two-given-values
    private int getRandomNumber(int rangeEnd) {
        return m_Rng.nextInt(rangeEnd - s_RangeBegin) + s_RangeBegin;
    }
}
