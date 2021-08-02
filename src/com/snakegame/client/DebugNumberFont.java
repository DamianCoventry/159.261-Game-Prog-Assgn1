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
