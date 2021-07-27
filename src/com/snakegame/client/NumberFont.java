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
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.io.*;

import static org.lwjgl.opengl.GL11.glDepthMask;

public class NumberFont {
    private static final int s_NumDigits = 10;
    private final Character[] m_Characters;
    private final GLStaticPolyhedronVxTc[] m_Polyhedra;
    private final GLDiffuseTextureProgram m_DiffuseTexturedProgram;
    private final float m_FrameWidth;
    private final float m_HalfFrameWidth;
    private final float m_FrameHeight;
    private final float m_HalfFrameHeight;
    private final Matrix4f m_Copy;
    private final Matrix4f m_ModelMatrix;

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
        m_HalfFrameWidth = m_FrameWidth / 2.0f;
        m_FrameHeight = frameHeight;
        m_HalfFrameHeight = m_FrameHeight / 2.0f;

        m_Copy = new Matrix4f();
        m_ModelMatrix = new Matrix4f();
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
                    -m_HalfFrameWidth, m_HalfFrameHeight, 0.0f,
                    -m_HalfFrameWidth, -m_HalfFrameHeight, 0.0f,
                    m_HalfFrameWidth, -m_HalfFrameHeight, 0.0f,
                    // Triangle 1
                    -m_HalfFrameWidth, m_HalfFrameHeight, 0.0f,
                    m_HalfFrameWidth, -m_HalfFrameHeight, 0.0f,
                    m_HalfFrameWidth, m_HalfFrameHeight, 0.0f,
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
    
    public void drawNumber(Matrix4f projectionMatrix, long number, float x, float y, float scale, Vector4f colour) {
        glDepthMask(false);
        String text = String.valueOf(number);
        for (int i = 0; i < text.length(); ++i) {
            m_ModelMatrix.identity().translate(x + m_HalfFrameWidth, y + m_HalfFrameHeight, 0.5f).scale(scale);
            Matrix4f mvpMatrix = m_Copy.set(projectionMatrix).mul(m_ModelMatrix);

            m_DiffuseTexturedProgram.setDiffuseColour(colour);
            m_DiffuseTexturedProgram.activate(mvpMatrix);
            int j = text.charAt(i) - '0'; // Convert the character to an index into the m_Characters array
            m_Polyhedra[j].draw();

            x += m_FrameWidth;
        }
        glDepthMask(true);
    }

    public float calculateWidth(long number) {
        String text = String.valueOf(number);
        return text.length() * m_FrameWidth;
    }
}
