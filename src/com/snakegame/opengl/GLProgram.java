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

import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class GLProgram {
    private final int m_ProgramId;
    private final int m_VertexShaderId;
    private final int m_FragmentShaderId;
    private final int m_MvpMatrixLocation;

    public static void deactivateCurrent() {
        glUseProgram(0);
    }

    public GLProgram(String vertexShaderSourceCode, String fragmentShaderSourceCode) {
        m_ProgramId = glCreateProgram();
        if (m_ProgramId < 1) {
            throw new RuntimeException("Unable to create a new program object");
        }

        m_VertexShaderId = compile(GL_VERTEX_SHADER, vertexShaderSourceCode);
        m_FragmentShaderId = compile(GL_FRAGMENT_SHADER, fragmentShaderSourceCode);

        link();

        m_MvpMatrixLocation = getUniformLocation("mvpMatrix");
    }

    public void freeNativeResource() {
        deactivateCurrent();
        glDeleteProgram(m_ProgramId);
    }

    public void bind() {
        glUseProgram(m_ProgramId);
    }

    protected int getProgramId() {
        return m_ProgramId;
    }

    public void setMvpMatrix(Matrix4f mvpMatrix) {
        setUniform(m_MvpMatrixLocation, mvpMatrix);
    }

    protected int getUniformLocation(String uniformName) {
        int location = glGetUniformLocation(getProgramId(), uniformName);
        if (location < 0) {
            throw new RuntimeException("Uniform name does not exist");
        }
        return location;
    }

    protected void setUniform(int location, int value) {
        glUniform1i(location, value);
    }

    protected void setUniform(int location, Vector3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(3);
            value.get(fb);
            glUniform3fv(location, fb);
        }
    }

    protected void setUniform(int location, Vector4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(4);
            value.get(fb);
            glUniform4fv(location, fb);
        }
    }

    protected void setUniform(int location, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }

    private int compile(int type, String sourceCode) {
        int shaderId = glCreateShader(type);
        if (shaderId < 1) {
            throw new RuntimeException("Unable to create a new shader object");
        }

        glShaderSource(shaderId, sourceCode);

        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("The shader source code doesn't compile.\n" + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(m_ProgramId, shaderId);
        return shaderId;
    }

    private void link() {
        glLinkProgram(m_ProgramId);
        if (glGetProgrami(m_ProgramId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("The program doesn't link.\n" + glGetProgramInfoLog(m_ProgramId, 1024));
        }

        glDetachShader(m_ProgramId, m_VertexShaderId);
        glDetachShader(m_ProgramId, m_FragmentShaderId);

        glValidateProgram(m_ProgramId);
        if (glGetProgrami(m_ProgramId, GL_VALIDATE_STATUS) == 0) {
            throw new RuntimeException("The program doesn't validate.\n" + glGetProgramInfoLog(m_ProgramId, 1024));
        }
    }
}
