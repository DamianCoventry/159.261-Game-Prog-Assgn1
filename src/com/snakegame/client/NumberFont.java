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

import com.snakegame.opengl.*;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.*;

public class NumberFont {
    private static final int s_NumDigits = 10;
    private final Character[] m_Characters;
    private final GLStaticPolyhedronVxTc[] m_Polyhedra;
    private final GLDiffuseTextureProgram m_DiffuseTexturedProgram;
    private final float m_FrameWidth;
    private final float m_FrameHeight;

    private static class Character {
        float m_U0, m_V0;
        float m_U1, m_V1;
        public Character(float u0, float v0, float u1, float v1) {
            m_U0 = u0;  m_V0 = v0;
            m_U1 = u1;  m_V1 = v1;
        }
    }
    
    protected NumberFont(GLDiffuseTextureProgram program, float frameWidth, float frameHeight, String diffuseTextureFileName) throws IOException {
        m_DiffuseTexturedProgram = program;
        m_FrameWidth = frameWidth;
        m_FrameHeight = frameHeight;
        m_Characters = new Character[s_NumDigits];
        m_Polyhedra = new GLStaticPolyhedronVxTc[s_NumDigits];
        GLTexture numberGLTexture = new GLTexture(ImageIO.read(new File(diffuseTextureFileName)));
        extractCharacterInfo(numberGLTexture);
    }

    private void extractCharacterInfo(GLTexture numberGLTexture) {
        final float deltaU = m_FrameWidth / numberGLTexture.getWidth();
        final float deltaV = m_FrameHeight / numberGLTexture.getHeight();
        for (int i = 0; i < s_NumDigits; ++i) {
            m_Characters[i] = new Character(i * deltaU,0.0f, (i * deltaU) + deltaU, deltaV);
            final float[] vertices = new float[] {
                    // Triangle 0
                    0.0f, m_FrameHeight, 0.0f,
                    0.0f, 0.0f, 0.0f,
                    m_FrameWidth, 0.0f, 0.0f,
                    // Triangle 1
                    0.0f, m_FrameHeight, 0.0f,
                    m_FrameWidth, 0.0f, 0.0f,
                    m_FrameWidth, m_FrameHeight, 0.0f,
            };
            final float[] texCoordinates = new float[] {
                    // Triangle 0
                    m_Characters[i].m_U0, m_Characters[i].m_V0,
                    m_Characters[i].m_U0, m_Characters[i].m_V1,
                    m_Characters[i].m_U1, m_Characters[i].m_V1,
                    // Triangle 1
                    m_Characters[i].m_U0, m_Characters[i].m_V0,
                    m_Characters[i].m_U1, m_Characters[i].m_V1,
                    m_Characters[i].m_U1, m_Characters[i].m_V0,
            };
            GLStaticPolyhedronVxTc polyhedron = new GLStaticPolyhedronVxTc();
            polyhedron.addPiece(new GLStaticPolyhedronPieceVxTc(numberGLTexture, vertices, texCoordinates));
            m_Polyhedra[i] = polyhedron;
        }
    }

    public void freeNativeResource() {
        for (var polyhedron : m_Polyhedra) {
            polyhedron.freeNativeResources();
        }
    }
    
    public void drawNumber(Matrix4f projectionMatrix, long number, float x, float y) {
        Matrix4f copy = new Matrix4f();
        Matrix4f modelMatrix = new Matrix4f();
        String text = String.valueOf(number);
        for (int i = 0; i < text.length(); ++i) {
            int j = text.charAt(i) - '0'; // Convert the character to an index into the m_Characters array
            modelMatrix.setTranslation(x, y, 0.5f);
            Matrix4f mvpMatrix = copy.set(projectionMatrix).mul(modelMatrix);
            m_DiffuseTexturedProgram.setDefaultDiffuseColour();
            m_DiffuseTexturedProgram.activate(mvpMatrix);
            m_Polyhedra[j].draw();
            x += m_FrameWidth;
        }
    }

    public float calculateWidth(long number) {
        String text = String.valueOf(number);
        return text.length() * m_FrameWidth;
    }
}
