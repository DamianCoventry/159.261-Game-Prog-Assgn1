package com.snakegame.client;

import com.snakegame.opengl.GLDiffuseTextureProgram;

import java.io.IOException;

public class DebugNumberFont extends NumberFont {
    public static final float s_FrameWidth = 9.0f;
    public static final float s_FrameHeight = 12.0f;

    public DebugNumberFont(GLDiffuseTextureProgram program) throws IOException {
        super(program, s_FrameWidth, s_FrameHeight, "images\\DebugFontNumbers.png");
    }
}
