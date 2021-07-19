package com.snakegame.rules;

public class Vector2i {
    public int m_X, m_Y;
    public Vector2i(int x, int y) {
        m_X = x; m_Y = y;
    }

    public Vector2i createCopy() {
        return new Vector2i(m_X, m_Y);
    }

    public Vector2i add(Vector2i other) {
        return new Vector2i(m_X + other.m_X, m_Y + other.m_Y);
    }

    public boolean equals(Vector2i other) {
        return m_X == other.m_X && m_Y == other.m_Y;
    }

    public boolean notEquals(Vector2i other) {
        return !equals(other);
    }

    public int magntiude(Vector2i other) {
        double deltaX = Math.abs(m_X - other.m_X);
        double deltaY = Math.abs(m_Y - other.m_Y);
        return (int)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}
