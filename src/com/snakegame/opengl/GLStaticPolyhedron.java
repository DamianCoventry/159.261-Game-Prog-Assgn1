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

import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public class GLStaticPolyhedron {
    private final ArrayList<GLStaticPolyhedronPiece> m_Pieces;

    public static void deactivateCurrent() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public GLStaticPolyhedron() {
        m_Pieces = new ArrayList<>();
    }

    public void addPiece(GLStaticPolyhedronPiece piece) {
        m_Pieces.add(piece);
    }

    public void freeNativeResources() {
        deactivateCurrent();
        for (var piece : m_Pieces) {
            piece.freeNativeResources();
        }
    }

    public int getNumPieces() {
        return m_Pieces.size();
    }

    public GLStaticPolyhedronPiece getPiece(int i) {
        return m_Pieces.get(i);
    }

    public void draw() {
        for (var piece : m_Pieces) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, piece.getDiffuseTexture().getId());
            piece.draw();
        }
    }
}
