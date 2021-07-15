package com.snakegame.rules;

import java.util.LinkedList;

public class Snake {
    private static final int s_MinBodyParts = 3;
    private static final int s_MaxBodyParts = 20;

    private final LinkedList<Vector2i> m_BodyParts;
    private final Vector2i m_MinBounds;
    private final Vector2i m_MaxBounds;
    private final Vector2i m_StartPosition;
    private final Direction m_StartDirection;
    private Direction m_CurrentDirection;
    private boolean m_AddOneBodyPart;

    public Snake(Vector2i startPosition, Direction startDirection, Vector2i minBounds, Vector2i maxBounds) {
        m_StartPosition = startPosition;
        m_StartDirection = startDirection;
        m_CurrentDirection = startDirection;
        m_BodyParts = new LinkedList<>();
        m_MinBounds = minBounds;
        m_MaxBounds = maxBounds;
        reset();
    }

    public void reset() {
        m_AddOneBodyPart = false;
        m_CurrentDirection = m_StartDirection;

        Vector2i movementDelta = getMovementDelta(getOppositeDirection(m_StartDirection));
        Vector2i currentPosition = m_StartPosition.createCopy();
        m_BodyParts.clear();
        for (int i = 0; i < s_MinBodyParts; ++i) {
            m_BodyParts.add(currentPosition);
            currentPosition = currentPosition.add(movementDelta);
            checkBounds(currentPosition);
        }
    }

    public void addOneBodyPart() {
        if (m_BodyParts.size() < s_MaxBodyParts) {
            m_AddOneBodyPart = true;
        }
    }

    public void moveForwards() {
        Vector2i movementDelta = getMovementDelta(m_CurrentDirection);
        m_BodyParts.addFirst(clipToBounds(m_BodyParts.getFirst().add(movementDelta)));
        if (m_AddOneBodyPart) {
            m_AddOneBodyPart = false;
        }
        else {
            m_BodyParts.removeLast();
        }
    }

    public boolean isCollidingWithItself() {
        for (int outer = 0; outer < m_BodyParts.size(); ++outer) {
            Vector2i outerBodyPart = m_BodyParts.get(outer);
            for (int inner = 0; inner < m_BodyParts.size(); ++inner) {
                if (outer != inner && outerBodyPart.equals(m_BodyParts.get(inner))) {
                    return true;
                }
            }
        }
        return false;
    }

    public enum Direction { Left, Right, Up, Down }
    public Direction getDirection() {
        return m_CurrentDirection;
    }
    public void setDirection(Direction direction) {
        m_CurrentDirection = direction;
    }

    public LinkedList<Vector2i> getBodyParts() {
        return m_BodyParts;
    }

    private void checkBounds(Vector2i position) {
        if (position.m_X <= m_MinBounds.m_X) {
            throw new RuntimeException("Snake start position is out of bounds to the left");
        }
        if (position.m_Y <= m_MinBounds.m_Y) {
            throw new RuntimeException("Snake start position is out of bounds to the bottom");
        }
        if (position.m_X >= m_MaxBounds.m_X) {
            throw new RuntimeException("Snake start position is out of bounds to the right");
        }
        if (position.m_Y >= m_MaxBounds.m_Y) {
            throw new RuntimeException("Snake start position is out of bounds to the top");
        }
    }

    private Vector2i clipToBounds(Vector2i position) {
        Vector2i newVector = position.createCopy();
        if (position.m_X < m_MinBounds.m_X) {
            newVector.m_X = m_MinBounds.m_X;
        }
        if (position.m_Y < m_MinBounds.m_Y) {
            newVector.m_Y = m_MinBounds.m_Y;
        }
        if (position.m_X > m_MaxBounds.m_X) {
            newVector.m_X = m_MaxBounds.m_X;
        }
        if (position.m_Y > m_MaxBounds.m_Y) {
            newVector.m_Y = m_MaxBounds.m_Y;
        }
        return newVector;
    }

    private Vector2i getMovementDelta(Direction direction) {
        switch (direction)
        {
            case Left: return new Vector2i(-1, 0);
            case Right: return new Vector2i(1, 0);
            case Up: return new Vector2i(0, 1);
        }
        return new Vector2i(0, -1);
    }

    private Direction getOppositeDirection(Direction direction) {
        switch (direction)
        {
            case Left: return Direction.Right;
            case Right: return Direction.Left;
            case Up: return Direction.Down;
        }
        return Direction.Up;
    }
}
