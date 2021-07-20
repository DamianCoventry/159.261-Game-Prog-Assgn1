package com.snakegame.client;

import com.snakegame.opengl.*;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.*;

public class NumberFont {
    public static final float s_FrameWidth = 26.0f;
    public static final float s_FrameHeight = 37.0f;
    private static final int s_NumDigits = 10;
    private final Character[] m_Characters;
    private final GLStaticPolyhedron[] m_Polyhedra;
    private final GLTexture m_NumberGLTexture;
    private final TexturedShaderProgram m_TexturedShaderProgram;

    private static class Character {
        float m_U0, m_V0;
        float m_U1, m_V1;
        public Character(float u0, float v0, float u1, float v1) {
            m_U0 = u0;  m_V0 = v0;
            m_U1 = u1;  m_V1 = v1;
        }
    }
    
    public NumberFont(TexturedShaderProgram texturedShaderProgram) throws IOException {
        m_TexturedShaderProgram = texturedShaderProgram;
        m_NumberGLTexture = new GLTexture(ImageIO.read(new File("images\\Numbers.png")));
        m_Characters = new Character[s_NumDigits];
        m_Polyhedra = new GLStaticPolyhedron[s_NumDigits];
        extractCharacterInfo();
    }

    private void extractCharacterInfo() {
        final float deltaU = s_FrameWidth / m_NumberGLTexture.getWidth();
        final float deltaV = s_FrameHeight / m_NumberGLTexture.getHeight();
        for (int i = 0; i < s_NumDigits; ++i) {
            m_Characters[i] = new Character(i * deltaU,0.0f, (i * deltaU) + deltaU, deltaV);
            final float[] vertices = new float[] {
                    // Triangle 0
                    0.0f, s_FrameHeight, 0.0f,
                    0.0f, 0.0f, 0.0f,
                    s_FrameWidth, 0.0f, 0.0f,
                    // Triangle 1
                    0.0f, s_FrameHeight, 0.0f,
                    s_FrameWidth, 0.0f, 0.0f,
                    s_FrameWidth, s_FrameHeight, 0.0f,
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
            m_Polyhedra[i] = new GLStaticPolyhedron(vertices, texCoordinates);
        }
    }

    public void freeNativeResource() {
        m_NumberGLTexture.freeNativeResource();
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
            modelMatrix.setTranslation(x, y, 0.0f);
            Matrix4f mvpMatrix = copy.set(projectionMatrix).mul(modelMatrix);
            m_TexturedShaderProgram.activate(mvpMatrix, m_NumberGLTexture);
            m_Polyhedra[j].draw();
            x += s_FrameWidth;
        }
    }

    public float calculateWidth(long number) {
        String text = String.valueOf(number);
        return text.length() * s_FrameWidth;
    }
}
