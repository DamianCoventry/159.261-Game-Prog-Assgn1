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

public class GLDirectionalLightProgram extends GLProgram {
    private final int m_MvMatrixLocation;
    private final int m_ProjectionMatrixLocation;
    private final int m_DiffuseTextureLocation;
    private final int m_DiffuseColourLocation;
    private final int m_AmbientLightLocation;
    private final int m_LightDirectionLocation;
    private final int m_LightColourLocation;
    private final int m_LightIntensityLocation;

    private Vector4f m_DiffuseColour;
    private Vector3f m_AmbientLight;
    private Vector3f m_LightDirection;
    private Vector3f m_LightColour;
    private float m_LightIntensity;

    public GLDirectionalLightProgram() throws IOException {
        super(Files.readString(Paths.get("shaders\\DiffuseDirectionalLight.vert"), StandardCharsets.US_ASCII),
              Files.readString(Paths.get("shaders\\DiffuseDirectionalLight.frag"), StandardCharsets.US_ASCII));

        m_MvMatrixLocation = getUniformLocation("mvMatrix");
        m_ProjectionMatrixLocation = getUniformLocation("projectionMatrix");
        m_DiffuseTextureLocation = getUniformLocation("diffuseTexture");
        m_DiffuseColourLocation = getUniformLocation("diffuseColour");
        m_AmbientLightLocation = getUniformLocation("ambientLight");
        m_LightDirectionLocation = getUniformLocation("lightDirection");
        m_LightColourLocation = getUniformLocation("lightColour");
        m_LightIntensityLocation = getUniformLocation("lightIntensity");

        m_DiffuseColour = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        m_AmbientLight = new Vector3f(0.15f, 0.15f, 0.15f);
        m_LightDirection = new Vector3f(0.0f, 0.0f, 1.0f);
        m_LightColour = new Vector3f(1.0f, 1.0f, 1.0f);
        m_LightIntensity = 1.0f;
    }

    public void setDiffuseColour(Vector4f diffuseColour) {
        m_DiffuseColour = diffuseColour;
    }

    public void setAmbientLight(Vector3f ambientLight) {
        m_AmbientLight = ambientLight;
    }

    public void setLightDirection(Vector3f lightDirection) {
        m_LightDirection = lightDirection;
    }

    public void setLightColour(Vector3f lightColour) {
        m_LightColour = lightColour;
    }

    public void setLightIntensity(float lightIntensity) {
        m_LightIntensity = lightIntensity;
    }

    public void activate(Matrix4f mvMatrix, Matrix4f projectionMatrix) {
        super.bind();
        setUniform(m_MvMatrixLocation, mvMatrix);
        setUniform(m_ProjectionMatrixLocation, projectionMatrix);
        setUniform(m_DiffuseTextureLocation, 0);
        setUniform(m_DiffuseColourLocation, m_DiffuseColour);
        setUniform(m_AmbientLightLocation, m_AmbientLight);
        setUniform(m_LightDirectionLocation, m_LightDirection);
        setUniform(m_LightColourLocation, m_LightColour);
        setUniform(m_LightIntensityLocation, m_LightIntensity);
    }
}
