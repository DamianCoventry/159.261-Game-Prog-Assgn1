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

public class PowerUp {
    public static final int s_NumPowerUps = 8;
    private final Type m_Type;
    private final Vector2i m_Location;

    public PowerUp(Type type, Vector2i location) {
        m_Type = type;
        m_Location = location;
    }

    public enum Type {
        INC_SPEED, DEC_SPEED, INC_LIVES, DEC_LIVES, INC_POINTS, DEC_POINTS, DEC_LENGTH, RANDOM
    }

    public Type getType() {
        return m_Type;
    }
    public Vector2i getLocation() {
        return m_Location;
    }
}
