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

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class GLSpecularDirectionalLightClipPlaneProgram extends GLProgram {
    private final int m_MvMatrixLocation;
    private final int m_ProjectionMatrixLocation;
    private final int m_DiffuseTextureLocation;
    private final int m_NoiseTextureLocation;
    private final int m_DiffuseColourLocation;
    private final int m_AmbientLightLocation;
    private final int m_LightDirectionLocation;
    private final int m_LightColourLocation;
    private final int m_LightIntensityLocation;
    private final int m_ShininessLocation;
    private final int m_PlaneNormalLocation;
    private final int m_PointOnPlaneLocation;
    private final GLTexture m_NoiseTexture;

    private Vector4f m_DiffuseColour;
    private Vector3f m_AmbientLight;
    private Vector3f m_LightDirection;
    private Vector3f m_LightColour;
    private Vector3f m_PlaneNormal;
    private Vector3f m_PointOnPlane;
    private float m_LightIntensity;
    private float m_Shininess;

    public GLSpecularDirectionalLightClipPlaneProgram(String noiseTextureFileName) throws IOException {
        super(Files.readString(Paths.get("shaders\\SpecularDirectionalLightClipPlane.vert"), StandardCharsets.US_ASCII),
              Files.readString(Paths.get("shaders\\SpecularDirectionalLightClipPlane.frag"), StandardCharsets.US_ASCII));

        m_NoiseTexture = new GLTexture(ImageIO.read(new File(noiseTextureFileName)));

        m_MvMatrixLocation = getUniformLocation("mvMatrix");
        m_ProjectionMatrixLocation = getUniformLocation("projectionMatrix");
        m_DiffuseTextureLocation = getUniformLocation("diffuseTexture");
        m_NoiseTextureLocation = getUniformLocation("noiseTexture");
        m_DiffuseColourLocation = getUniformLocation("diffuseColour");
        m_AmbientLightLocation = getUniformLocation("ambientLight");
        m_LightDirectionLocation = getUniformLocation("lightDirection");
        m_LightColourLocation = getUniformLocation("lightColour");
        m_LightIntensityLocation = getUniformLocation("lightIntensity");
        m_ShininessLocation = getUniformLocation("shininess");
        m_PlaneNormalLocation = getUniformLocation("planeNormal");
        m_PointOnPlaneLocation = getUniformLocation("pointOnPlane");

        m_DiffuseColour = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        m_AmbientLight = new Vector3f(0.15f, 0.15f, 0.15f);
        m_LightDirection = new Vector3f(0.0f, 0.0f, 1.0f);
        m_LightColour = new Vector3f(1.0f, 1.0f, 1.0f);
        m_PlaneNormal = new Vector3f(0.0f, 1.0f, 0.0f);
        m_PointOnPlane = new Vector3f(0.0f, 0.0f, 0.0f);
        m_LightIntensity = 1.0f;
        m_Shininess = 32.0f;
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

    public void setShininess(float shininess) {
        m_Shininess = shininess;
    }

    public void setPlaneNormal(Vector3f planeNormal) {
        m_PlaneNormal = planeNormal;
    }

    public void setPointOnPlane(Vector3f pointOnPlane) {
        m_PointOnPlane = pointOnPlane;
    }

    public void activate(Matrix4f mvMatrix, Matrix4f projectionMatrix) {
        super.bind();

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, m_NoiseTexture.getId());

        setUniform(m_MvMatrixLocation, mvMatrix);
        setUniform(m_ProjectionMatrixLocation, projectionMatrix);
        setUniform(m_DiffuseTextureLocation, 0);
        setUniform(m_NoiseTextureLocation, 1);
        setUniform(m_DiffuseColourLocation, m_DiffuseColour);
        setUniform(m_AmbientLightLocation, m_AmbientLight);
        setUniform(m_LightDirectionLocation, m_LightDirection);
        setUniform(m_LightColourLocation, m_LightColour);
        setUniform(m_LightIntensityLocation, m_LightIntensity);
        setUniform(m_ShininessLocation, m_Shininess);
        setUniform(m_PlaneNormalLocation, m_PlaneNormal);
        setUniform(m_PointOnPlaneLocation, m_PointOnPlane);
    }
}
