package com.snakegame.opengl;

import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

public class TexturedShaderProgram extends GLProgram {
    private final int m_TextureLocation;

    public TexturedShaderProgram() throws IOException {
        super(Files.readString(Paths.get("shaders\\BasicVertexShader.vert"), StandardCharsets.US_ASCII),
              Files.readString(Paths.get("shaders\\BasicFragmentShader.frag"), StandardCharsets.US_ASCII));

        m_TextureLocation = glGetUniformLocation(getProgramId(), "diffuseTexture");
        if (m_TextureLocation < 0) {
            throw new RuntimeException("Uniform variable does not exist");
        }
    }

    public void activate(Matrix4f mvpMatrix, GLTexture texture) {
        setMvpMatrix(mvpMatrix);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        setUniform(m_TextureLocation, 0);
    }
}
