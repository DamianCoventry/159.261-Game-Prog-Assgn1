package com.snakegame.opengl;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryUtil.*;

public class GLStaticPolyhedron {
    private final int m_VaoId;
    private final int[] m_VboIds;
    private final int m_NumVertices;

    public static void deactivateCurrent() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public GLStaticPolyhedron(float[] vertices, float[] texCoordinates) {
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

            deactivateCurrent();
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

    public void freeNativeResources() {
        deactivateCurrent();
        glDeleteBuffers(m_VboIds);
        glDeleteVertexArrays(m_VaoId);
    }

    public void draw() {
        glBindVertexArray(m_VaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawArrays(GL_TRIANGLES, 0, m_NumVertices);

        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }
}
