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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class GLDiffuseTextureProgram extends GLProgram {
    private final int m_MvpMatrixLocation;
    private final int m_DiffuseTextureLocation;
    private final int m_DiffuseColourLocation;
    private Vector4f m_DiffuseColour;

    public GLDiffuseTextureProgram() throws IOException {
        super(Files.readString(Paths.get("shaders\\DiffuseTexture.vert"), StandardCharsets.US_ASCII),
              Files.readString(Paths.get("shaders\\DiffuseTexture.frag"), StandardCharsets.US_ASCII));

        m_MvpMatrixLocation = getUniformLocation("mvpMatrix");
        m_DiffuseTextureLocation = getUniformLocation("diffuseTexture");
        m_DiffuseColourLocation = getUniformLocation("diffuseColour");
        setDefaultDiffuseColour();
    }

    public void setDefaultDiffuseColour() {
        m_DiffuseColour = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void setDiffuseColour(Vector4f diffuseColour) {
        m_DiffuseColour = diffuseColour;
    }

    public void activate(Matrix4f mvpMatrix) {
        super.bind();
        setUniform(m_MvpMatrixLocation, mvpMatrix);
        setUniform(m_DiffuseTextureLocation, 0);
        setUniform(m_DiffuseColourLocation, m_DiffuseColour);
    }
}
