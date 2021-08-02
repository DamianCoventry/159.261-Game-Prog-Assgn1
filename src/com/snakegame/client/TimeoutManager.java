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

package com.snakegame.client;

import java.util.*;
import java.util.function.Function;

public class TimeoutManager {
    private static int s_UniqueId = 0;
    private static class TimeoutInfo {
        public long m_TimeoutMs;
        public long m_LastCallTimeMs;
        public int m_CallCount;
        public Function<Integer, CallbackResult> m_Callback;
        public TimeoutInfo(long nowMs, long timeoutMs, Function<Integer, CallbackResult> callback) {
            m_TimeoutMs = timeoutMs;
            m_LastCallTimeMs = nowMs;
            m_CallCount = 0;
            m_Callback = callback;
        }
    }

    private final ArrayList<Integer> m_PendingRemovals;
    private final Map<Integer, TimeoutInfo> m_PendingAdditions;
    private final Map<Integer, TimeoutInfo> m_CurrentTimeouts;

    public TimeoutManager() {
        m_PendingRemovals = new ArrayList<>();
        m_PendingAdditions = new HashMap<>();
        m_CurrentTimeouts = new HashMap<>();
    }

    public enum CallbackResult { KEEP_CALLING, REMOVE_THIS_CALLBACK }

    public int addTimeout(long timeoutMs, Function<Integer, CallbackResult> callback) {
        int timeoutId = ++s_UniqueId;
        m_PendingAdditions.put(timeoutId, new TimeoutInfo(System.currentTimeMillis(), timeoutMs, callback));
        return timeoutId;
    }

    public void removeTimeout(int timeoutId) {
        m_PendingRemovals.add(timeoutId);
    }

    public void dispatchTimeouts(long nowMs) {
        performPendingRemovals();
        performPendingAdditions();
        dispatchCurrentTimeouts(nowMs);
    }

    private void performPendingAdditions() {
        m_CurrentTimeouts.putAll(m_PendingAdditions);
        m_PendingAdditions.clear();
    }

    private void performPendingRemovals() {
        for (var id : m_PendingRemovals) {
            m_CurrentTimeouts.remove(id);
        }
        m_PendingRemovals.clear();
    }

    private void dispatchCurrentTimeouts(long nowMs) {
        for (var timeout : m_CurrentTimeouts.entrySet()) {
            TimeoutInfo info = timeout.getValue();
            if (nowMs - info.m_LastCallTimeMs >= info.m_TimeoutMs) {
                info.m_LastCallTimeMs = nowMs;
                TimeoutManager.CallbackResult result = info.m_Callback.apply(++info.m_CallCount);
                if (result == CallbackResult.REMOVE_THIS_CALLBACK) {
                    m_PendingRemovals.add(timeout.getKey());
                }
            }
        }
    }
}
