package com.snakegame.opengl;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryUtil.*;

public class GLStaticPolyhedron {
    private final int m_VaoId;
    private final int m_VboId;
    private final int m_NumTriangles;

    public static void deactivateCurrent() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public GLStaticPolyhedron(float[] vertices, float[] texCoordinates) {
        FloatBuffer verticesBuffer = null;
        FloatBuffer texCoordinatesBuffer = null;
        try {
            m_VaoId = glGenVertexArrays();
            if (m_VaoId == 0) {
                throw new RuntimeException("Unable to create a static polyhedron");
            }
            glBindVertexArray(m_VaoId);

            m_VboId = glGenBuffers();
            if (m_VboId == 0) {
                glDeleteVertexArrays(m_VaoId);
                throw new RuntimeException("Unable to create a static polyhedron");
            }
            glBindBuffer(GL_ARRAY_BUFFER, m_VboId);

            m_NumTriangles = vertices.length / 3;

            verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            texCoordinatesBuffer = MemoryUtil.memAllocFloat(texCoordinates.length);
            texCoordinatesBuffer.put(texCoordinates).flip();
            glBufferData(GL_ARRAY_BUFFER, texCoordinatesBuffer, GL_STATIC_DRAW);
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

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void freeNativeResources() {
        deactivateCurrent();
        glDeleteBuffers(m_VboId);
        glDeleteVertexArrays(m_VaoId);
    }

    public void draw() {
        glBindVertexArray(m_VaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawArrays(GL_TRIANGLES, 0, m_NumTriangles);

        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }
}
