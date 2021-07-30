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

package com.snakegame.rules;

public class Vector2i {
    public int m_X, m_Z;
    public Vector2i(int x, int z) {
        m_X = x; m_Z = z;
    }

    public Vector2i createCopy() {
        return new Vector2i(m_X, m_Z);
    }

    public Vector2i add(Vector2i other) {
        return new Vector2i(m_X + other.m_X, m_Z + other.m_Z);
    }

    public boolean equals(Vector2i other) {
        return m_X == other.m_X && m_Z == other.m_Z;
    }

    public boolean notEquals(Vector2i other) {
        return !equals(other);
    }

    public int magnitude(Vector2i other) {
        double deltaX = Math.abs(m_X - other.m_X);
        double deltaY = Math.abs(m_Z - other.m_Z);
        return (int)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public boolean isBelow(Vector2i other) {
        return m_X == other.m_X && m_Z - 1 == other.m_Z;
    }

    public boolean isAbove(Vector2i other) {
        return m_X == other.m_X && m_Z + 1 == other.m_Z;
    }

    public boolean isLeftOf(Vector2i other) {
        return m_X - 1 == other.m_X && m_Z == other.m_Z;
    }

    public boolean isRightOf(Vector2i other) {
        return m_X + 1 == other.m_X && m_Z == other.m_Z;
    }
}
