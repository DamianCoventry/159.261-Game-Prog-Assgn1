package com.snakegame.rules;

public class Number {
    private final Type m_Type;
    private final Vector2i m_Location;

    public Number(Type type, Vector2i location) {
        m_Type = type;
        m_Location = location;
    }

    public enum Type {
        NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9
    }

    public static class Result {
        public boolean m_LevelComplete;
        public Type m_Type;
        public Result(Type type) {
            m_LevelComplete = false;
            m_Type = type;
        }
        public Result() {
            m_LevelComplete = true;
            m_Type = Type.NUM_1;
        }
    }

    public static Result getNextInSeries(Type type) {
        switch (type) {
            case NUM_1:
                return new Result(Type.NUM_2);
            case NUM_2:
                return new Result(Type.NUM_3);
            case NUM_3:
                return new Result(Type.NUM_4);
            case NUM_4:
                return new Result(Type.NUM_5);
            case NUM_5:
                return new Result(Type.NUM_6);
            case NUM_6:
                return new Result(Type.NUM_7);
            case NUM_7:
                return new Result(Type.NUM_8);
            case NUM_8:
                return new Result(Type.NUM_9);
        }
        return new Result();
    }

    public Type getType() {
        return m_Type;
    }
    public Vector2i getLocation() {
        return m_Location;
    }
}
