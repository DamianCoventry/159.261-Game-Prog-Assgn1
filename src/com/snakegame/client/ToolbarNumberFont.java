package com.snakegame.client;

import com.snakegame.opengl.GLDiffuseTextureProgram;

import java.io.IOException;

public class ToolbarNumberFont extends NumberFont {
    public static final float s_FrameWidth = 26.0f;
    public static final float s_FrameHeight = 37.0f;

    public ToolbarNumberFont(GLDiffuseTextureProgram program) throws IOException {
        super(program, s_FrameWidth, s_FrameHeight, "images\\Numbers.png");
    }
}
