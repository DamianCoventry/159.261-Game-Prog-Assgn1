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

public class GLStaticPolyhedronPieceVxTc extends GLStaticPolyhedronPieceBase {
    public GLStaticPolyhedronPieceVxTc(GLTexture diffuseTexture, float[] vertices, float[] texCoordinates) {
        super(diffuseTexture, vertices.length / 3);

        m_VboIds = new int[2];

        m_VboIds[0] = createVbo();
        copyFloatDataIntoVbo(0, 3, vertices);

        m_VboIds[1] = createVbo();
        copyFloatDataIntoVbo(1, 2, texCoordinates);
    }

    public void draw() {
        drawTriangles(2);
    }
}
