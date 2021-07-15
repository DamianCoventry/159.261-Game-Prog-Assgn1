package com.snakegame.client;

import com.snakegame.rules.GameField;
import com.snakegame.rules.Snake;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class GameView {
    private final GameField m_GameField;
    private final ArrayList<Snake> m_Snakes;
    private final Texture m_WallTexture;
    private final Texture m_AppleTexture;
    private final Texture m_DotTexture;
    private final Texture m_HeadTexture;

    public GameView(GameField gameField, ArrayList<Snake> snakes) throws IOException {
        m_GameField = gameField;
        m_Snakes = snakes;
        m_WallTexture = new Texture(ImageIO.read(new File("soil-seamless-texture.jpg")));
        m_AppleTexture = new Texture(ImageIO.read(new File("icons8-green-apple-48.png")));
        m_DotTexture = new Texture(ImageIO.read(new File("dot.png")));
        m_HeadTexture = new Texture(ImageIO.read(new File("head.png")));
    }

    public void close() {
        m_WallTexture.close();
        m_AppleTexture.close();
        m_DotTexture.close();
        m_HeadTexture.close();
    }

    public void draw() {
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
            glTexCoord2d(0.0, 0.0); glVertex3d(x, y + h, 0.1f);
            glTexCoord2d(0.0, 1.0); glVertex3d(x , y, 0.1f);
            glTexCoord2d(1.0, 1.0); glVertex3d(x + w, y, 0.1f);
            glTexCoord2d(1.0, 0.0); glVertex3d(x + w, y + h, 0.1f);
        glEnd();
    }
}
