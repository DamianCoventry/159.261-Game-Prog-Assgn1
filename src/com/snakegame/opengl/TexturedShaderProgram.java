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

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.*;

public class TexturedShaderProgram extends GLProgram {
    private final int m_TextureLocation;
    private final int m_ColourLocation;
    private Vector4f m_DiffuseColour;

    public TexturedShaderProgram() throws IOException {
        super(Files.readString(Paths.get("shaders\\BasicVertexShader.vert"), StandardCharsets.US_ASCII),
              Files.readString(Paths.get("shaders\\BasicFragmentShader.frag"), StandardCharsets.US_ASCII));

        m_TextureLocation = getUniformLocation("diffuseTexture");
        m_ColourLocation = getUniformLocation("diffuseColour");
        m_DiffuseColour = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void setDiffuseColour(Vector4f diffuseColour) {
        m_DiffuseColour = diffuseColour;
    }

    public void activate(Matrix4f mvpMatrix) {
        super.bind();
        setMvpMatrix(mvpMatrix);
        setUniform(m_TextureLocation, 0);
        setUniform(m_ColourLocation, m_DiffuseColour);
    }
}
