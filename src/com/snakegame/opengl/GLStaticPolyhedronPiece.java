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

public class GLStaticPolyhedronPiece {
    private final int m_VaoId;
    private final int[] m_VboIds;
    private final int m_NumVertices;
    private final GLTexture m_DiffuseTexture;

    public GLStaticPolyhedronPiece(float[] vertices, float[] texCoordinates, GLTexture diffuseTexture) {
        m_DiffuseTexture = diffuseTexture;

        FloatBuffer verticesBuffer = null;
        FloatBuffer texCoordinatesBuffer = null;
        try {
            m_NumVertices = vertices.length / 3;

            m_VaoId = glGenVertexArrays();
            if (m_VaoId == 0) {
                throw new RuntimeException("Unable to create a static polyhedron");
            }
            glBindVertexArray(m_VaoId);

            m_VboIds = new int[2];
            m_VboIds[0] = glGenBuffers();
            if (m_VboIds[0] == 0) {
                glDeleteVertexArrays(m_VaoId);
                throw new RuntimeException("Unable to create a static polyhedron");
            }

            verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();

            glBindBuffer(GL_ARRAY_BUFFER, m_VboIds[0]);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            m_VboIds[1] = glGenBuffers();
            if (m_VboIds[1] == 0) {
                glDeleteBuffers(m_VboIds[0]);
                glDeleteVertexArrays(m_VaoId);
                throw new RuntimeException("Unable to create a static polyhedron");
            }

            texCoordinatesBuffer = MemoryUtil.memAllocFloat(texCoordinates.length);
            texCoordinatesBuffer.put(texCoordinates).flip();

            glBindBuffer(GL_ARRAY_BUFFER, m_VboIds[1]);
            glBufferData(GL_ARRAY_BUFFER, texCoordinatesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        }
        finally {
            if (texCoordinatesBuffer != null) {
                memFree(texCoordinatesBuffer);
            }
            if (verticesBuffer != null) {
                memFree(verticesBuffer);
            }
        }
    }

    public GLTexture getDiffuseTexture() {
        return m_DiffuseTexture;
    }

    public void freeNativeResources() {
        m_DiffuseTexture.freeNativeResource();
        glDeleteBuffers(m_VboIds);
        glDeleteVertexArrays(m_VaoId);
    }

    public void draw() {
        glBindVertexArray(m_VaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawArrays(GL_TRIANGLES, 0, m_NumVertices);
    }
}
