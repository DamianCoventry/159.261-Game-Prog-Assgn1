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

import com.snakegame.client.IGameView;
import org.joml.Vector4f;

import java.util.LinkedList;

public class Snake {
    private static final int s_NumStartingLives = 3;
    public static final int s_MaxNumLives = 5;
    public static final int s_MinBodyParts = 3;
    private static final int s_NumBodyPartsToAdd = 1;
    private static final int s_NumBodyPartsToRemove = 3;
    public static final long s_PowerUpPointsBonus = 1000;
    private static final long s_PowerUpPoints = 100;

    public static class BodyPart {
        public Direction m_LeavingCellDirection;
        public Vector2i m_Location;

        public BodyPart(Direction leavingCellDirection, Vector2i location) {
            m_LeavingCellDirection = leavingCellDirection;
            m_Location = location;
        }

        public Direction classifyNeighbour(Vector2i neighbour) {
            if (m_Location.isLeftOf(neighbour)) {
                return Direction.Left;
            }
            if (m_Location.isRightOf(neighbour)) {
                return Direction.Right;
            }
            if (m_Location.isBelow(neighbour)) {
                return Direction.Down;
            }
            if (m_Location.isAbove(neighbour)) {
                return Direction.Up;
            }
            throw new RuntimeException("Invalid neighbour");
        }
    }

    private final LinkedList<BodyPart> m_BodyParts;
    private final Vector2i m_MinBounds;
    private final Vector2i m_MaxBounds;
    private final Direction m_StartDirection;
    private final int m_Id;
    private final IGameView m_GameView;

    private final Vector4f s_Red = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
    private final Vector4f s_Green = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);

    private Vector2i m_StartPosition = null;
    private Direction m_CurrentDirection;
    private Vector2i m_LastDirectionChangeCell;
    private int m_NumLives;
    private int m_AddBodyParts;
    private int m_RemoveBodyParts;
    private long m_Points;

    public Snake(int id, IGameView gameView, Direction startDirection, Vector2i minBounds, Vector2i maxBounds) {
        m_Id = id;
        m_GameView = gameView;
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

    public Direction getStartDirection() {
        return m_StartDirection;
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
            m_BodyParts.add(new BodyPart(m_StartDirection, currentPosition));
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
            m_GameView.startRemainingSnakesAnimation(m_Id, s_Green);
        }
    }

    public void decrementLives() {
        if (m_NumLives > 0) {
            --m_NumLives;
            m_GameView.startRemainingSnakesAnimation(m_Id, s_Red);
        }
    }

    public long getPoints() {
        return m_Points;
    }

    public void incrementPoints(long points) {
        if (points > 0) {
            m_Points += points;
            m_GameView.startScoreAnimation(m_Id, s_Green);
        }
    }

    public void decrementPoints(long points) {
        if (points > 0) {
            m_Points = Math.max(0, m_Points - points);
            m_GameView.startScoreAnimation(m_Id, s_Red);
        }
    }

    public void awardNumber(Number.Type numberType) {
        int value = Number.toInteger(numberType);
        incrementPoints(value * s_PowerUpPoints);
        if (value > 7) {
            m_AddBodyParts = s_NumBodyPartsToAdd * 3;
        }
        else if (value > 5) {
            m_AddBodyParts = s_NumBodyPartsToAdd * 2;
        }
        else {
            m_AddBodyParts = s_NumBodyPartsToAdd;
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
        m_BodyParts.getFirst().m_LeavingCellDirection = m_CurrentDirection;
        m_BodyParts.push(new BodyPart(m_CurrentDirection, m_BodyParts.getFirst().m_Location.add(movementDelta)));
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
            var outerBodyPart = m_BodyParts.get(outer);
            for (int inner = 0; inner < m_BodyParts.size(); ++inner) {
                if (outer != inner && outerBodyPart.m_Location.equals(m_BodyParts.get(inner).m_Location)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCollidingWith(Snake otherSnake) {
        for (var other : otherSnake.m_BodyParts) {
            if (m_BodyParts.getFirst().m_Location.equals(other.m_Location)) {
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
        if (m_LastDirectionChangeCell.notEquals(m_BodyParts.getFirst().m_Location)) {
            m_CurrentDirection = direction;
            m_LastDirectionChangeCell = m_BodyParts.getFirst().m_Location.createCopy();
        }
    }

    public LinkedList<BodyPart> getBodyParts() {
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
