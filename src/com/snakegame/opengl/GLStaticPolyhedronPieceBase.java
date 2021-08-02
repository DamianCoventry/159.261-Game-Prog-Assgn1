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

package com.snakegame.opengl;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.memFree;

public class GLStaticPolyhedronPieceBase {
    private final int m_VaoId;
    private final int m_NumVertices;
    private GLTexture m_DiffuseTexture;
    protected int[] m_VboIds;

    public GLStaticPolyhedronPieceBase(GLTexture diffuseTexture, int numVertices) {
        m_DiffuseTexture = diffuseTexture;
        m_NumVertices = numVertices;
        m_VaoId = createVao();
    }

    public GLTexture getDiffuseTexture() {
        return m_DiffuseTexture;
    }
    public void setDiffuseTexture(GLTexture texture) {
        m_DiffuseTexture = texture;
    }

    public void freeNativeResources() {
        m_DiffuseTexture.freeNativeResource();
        if (m_VboIds != null) {
            glDeleteBuffers(m_VboIds);
        }
        glDeleteVertexArrays(m_VaoId);
    }

    private int createVao() {
        int id = glGenVertexArrays();
        if (id == 0) {
            throw new RuntimeException("Unable to create a static polyhedron");
        }
        glBindVertexArray(id);
        return id;
    }

    protected int createVbo() {
        int id = glGenBuffers();
        if (id == 0) {
            if (m_VboIds != null) {
                glDeleteBuffers(m_VboIds);
            }
            if (m_VaoId != 0) {
                glDeleteVertexArrays(m_VaoId);
            }
            throw new RuntimeException("Unable to create a static polyhedron");
        }
        glBindBuffer(GL_ARRAY_BUFFER, id);
        return id;
    }

    protected void drawTriangles(int numAttributes) {
        glBindVertexArray(m_VaoId);
        for (int i = 0; i < numAttributes; ++ i) {
            glEnableVertexAttribArray(i);
        }
        glDrawArrays(GL_TRIANGLES, 0, m_NumVertices);
    }

    protected void copyFloatDataIntoVbo(int attribute, int size, float[] floatData) {
        FloatBuffer floatBuffer = null;
        try {
            floatBuffer = MemoryUtil.memAllocFloat(floatData.length);
            floatBuffer.put(floatData).flip();

            glBufferData(GL_ARRAY_BUFFER, floatBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(attribute);
            glVertexAttribPointer(attribute, size, GL_FLOAT, false, 0, 0);
        }
        finally {
            if (floatBuffer != null) {
                memFree(floatBuffer);
            }
        }
    }
}
