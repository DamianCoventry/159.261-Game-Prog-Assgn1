package com.snakegame.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    public int addTimeout(long nowMs, long timeoutMs, Function<Integer, CallbackResult> callback) {
        int timeoutId = ++s_UniqueId;
        m_PendingAdditions.put(timeoutId, new TimeoutInfo(nowMs, timeoutMs, callback));
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
        for (var timeout : m_PendingAdditions.entrySet()) {
            m_CurrentTimeouts.put(timeout.getKey(), timeout.getValue());
        }
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
            var info = timeout.getValue();
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
