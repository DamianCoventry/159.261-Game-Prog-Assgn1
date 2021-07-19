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
        INC_SPEED, DEC_SPEED,
        INC_LIVES, DEC_LIVES,
        INC_POINTS, DEC_POINTS,
        DEC_LENGTH, RANDOM
    }

    public Type getType() {
        return m_Type;
    }
    public Vector2i getLocation() {
        return m_Location;
    }
}
