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

import java.util.LinkedList;

public class Snake {
    private static final int s_NumStartingLives = 3;
    private static final int s_MaxNumLives = 5;
    public static final int s_MinBodyParts = 3;
    private static final float s_BodyPartScale = 1.5f;
    private static final int s_NumBodyPartsToRemove = 3;
    private static final long s_PowerUpPointsBonus = 1000;
    private static final long s_PowerUpPoints = 100;

    private final LinkedList<Vector2i> m_BodyParts;
    private final Vector2i m_MinBounds;
    private final Vector2i m_MaxBounds;
    private final Direction m_StartDirection;

    private Vector2i m_StartPosition = null;
    private Direction m_CurrentDirection;
    private Vector2i m_LastDirectionChangeCell;
    private int m_NumLives;
    private int m_AddBodyParts;
    private int m_RemoveBodyParts;
    private long m_Points;

    public Snake(Direction startDirection, Vector2i minBounds, Vector2i maxBounds) {
        m_StartDirection = startDirection;
        m_BodyParts = new LinkedList<>();
        m_MinBounds = minBounds;
        m_MaxBounds = maxBounds;
        m_NumLives = s_NumStartingLives - 1; // Allocate a life immediately
    }

    public void setStartPosition(Vector2i startPosition) {
        m_StartPosition = startPosition;
        m_LastDirectionChangeCell = startPosition.createCopy();
    }

    public void moveToStartPosition() {
        if (m_StartPosition == null) {
            throw new RuntimeException("Start position hasn't been set");
        }
        m_AddBodyParts = m_RemoveBodyParts = 0;
        m_CurrentDirection = m_StartDirection;

        Vector2i movementDelta = getMovementDelta(getOppositeDirection(m_StartDirection));
        Vector2i currentPosition = m_StartPosition.createCopy();
        m_LastDirectionChangeCell = m_StartPosition.createCopy();

        m_BodyParts.clear();
        for (int i = 0; i < s_MinBodyParts; ++i) {
            m_BodyParts.add(currentPosition);
            currentPosition = currentPosition.add(movementDelta);
            checkBounds(currentPosition);
        }
    }

    public int getNumLives() {
        return m_NumLives;
    }

    public void incrementLives() {
        if (m_NumLives < s_MaxNumLives) {
            ++m_NumLives;
        }
    }

    public void decrementLives() {
        if (m_NumLives > 0) {
            --m_NumLives;
        }
    }

    public long getPoints() {
        return m_Points;
    }

    public void incrementPoints(long points) {
        if (points > 0) {
            m_Points += points;
        }
    }

    public void decrementPoints(long points) {
        if (points > 0) {
            m_Points = Math.max(0, m_Points - points);
        }
    }

    public void awardNumber(Number.Type numberType) {
        switch (numberType) {
            case NUM_1:
                m_AddBodyParts = (int)s_BodyPartScale;
                incrementPoints(s_PowerUpPoints);
                break;
            case NUM_2:
                m_AddBodyParts = (int)(2.0f * s_BodyPartScale);
                incrementPoints(2 * s_PowerUpPoints);
                break;
            case NUM_3:
                m_AddBodyParts = (int)(3.0f * s_BodyPartScale);
                incrementPoints(3 * s_PowerUpPoints);
                break;
            case NUM_4:
                m_AddBodyParts = (int)(4.0f * s_BodyPartScale);
                incrementPoints(4 * s_PowerUpPoints);
                break;
            case NUM_5:
                m_AddBodyParts = (int)(5.0f * s_BodyPartScale);
                incrementPoints(5 * s_PowerUpPoints);
                break;
            case NUM_6:
                m_AddBodyParts = (int)(6.0f * s_BodyPartScale);
                incrementPoints(6 * s_PowerUpPoints);
                break;
            case NUM_7:
                m_AddBodyParts = (int)(7.0f * s_BodyPartScale);
                incrementPoints(7 * s_PowerUpPoints);
                break;
            case NUM_8:
                m_AddBodyParts = (int)(8.0f * s_BodyPartScale);
                incrementPoints(8 * s_PowerUpPoints);
                break;
            case NUM_9:
                m_AddBodyParts = (int)(9.0f * s_BodyPartScale);
                incrementPoints(9 * s_PowerUpPoints);
                break;
        }
    }

    public void awardPowerUp(PowerUp.Type powerUp) {
        switch (powerUp) {
            case DEC_LENGTH:
                if (m_BodyParts.size() > s_MinBodyParts) {
                    m_RemoveBodyParts = Math.min(m_BodyParts.size() - s_MinBodyParts, s_NumBodyPartsToRemove);
                }
                break;
            case INC_LIVES:
                incrementLives();
                break;
            case DEC_LIVES:
                decrementLives();
                break;
            case INC_POINTS:
                incrementPoints(s_PowerUpPointsBonus);
                break;
            case DEC_POINTS:
                decrementPoints(s_PowerUpPointsBonus);
                break;
        }
    }

    public void moveForwards() {
        Vector2i movementDelta = getMovementDelta(m_CurrentDirection);
        m_BodyParts.addFirst(clampCoordinates(m_BodyParts.getFirst().add(movementDelta)));
        if (m_AddBodyParts > 0) {
            --m_AddBodyParts;
        }
        else {
            if (m_RemoveBodyParts > 0) {
                --m_RemoveBodyParts;
                m_BodyParts.removeLast();
            }
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

    public boolean isCollidingWith(Snake otherSnake) {
        for (var other : otherSnake.m_BodyParts) {
            if (m_BodyParts.getFirst().equals(other)) {
                return true;
            }
        }
        return false;
    }

    public enum Direction { Left, Right, Up, Down }
    public Direction getDirection() {
        return m_CurrentDirection;
    }
    public void setDirection(Direction direction) {
        // Prevent more than one direction change per cell
        if (m_LastDirectionChangeCell.notEquals(m_BodyParts.getFirst())) {
            m_CurrentDirection = direction;
            m_LastDirectionChangeCell = m_BodyParts.getFirst().createCopy();
        }
    }

    public LinkedList<Vector2i> getBodyParts() {
        return m_BodyParts;
    }

    private void checkBounds(Vector2i position) {
        if (position.m_X <= m_MinBounds.m_X) {
            throw new RuntimeException("Snake start position is out of bounds to the left");
        }
        if (position.m_Z <= m_MinBounds.m_Z) {
            throw new RuntimeException("Snake start position is out of bounds to the bottom");
        }
        if (position.m_X >= m_MaxBounds.m_X) {
            throw new RuntimeException("Snake start position is out of bounds to the right");
        }
        if (position.m_Z >= m_MaxBounds.m_Z) {
            throw new RuntimeException("Snake start position is out of bounds to the top");
        }
    }

    private Vector2i clampCoordinates(Vector2i position) {
        Vector2i newVector = position.createCopy();
        if (position.m_X < m_MinBounds.m_X) {
            newVector.m_X = m_MinBounds.m_X;
        }
        if (position.m_Z < m_MinBounds.m_Z) {
            newVector.m_Z = m_MinBounds.m_Z;
        }
        if (position.m_X > m_MaxBounds.m_X) {
            newVector.m_X = m_MaxBounds.m_X;
        }
        if (position.m_Z > m_MaxBounds.m_Z) {
            newVector.m_Z = m_MaxBounds.m_Z;
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
