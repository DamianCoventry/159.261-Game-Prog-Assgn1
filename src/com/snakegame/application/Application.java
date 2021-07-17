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
// Designed and implemented for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.application;

import com.snakegame.client.TimeoutManager;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * This class keeps the main application objects resident for the lifetime of the application.
 * */

// https://en.wikipedia.org/wiki/State_pattern
public class Application implements IAppStateContext {
    private static final int s_DesiredWindowWidth = 1024;
    private static final int s_DesiredWindowHeight = 768;
    private static final double s_FovYDegrees = 60.0;
    private static final double s_NearClipPlane = 1.0;
    private static final double s_FarClipPlane = 1000.0;
    private static final String s_WindowTitle = "159.261 Game Programming (Assignment 1)";

    private final TimeoutManager m_TimeoutManager;
    private long m_Window;
    private float m_ActualWidth = (float)s_DesiredWindowWidth;
    private float m_ActualHeight = (float)s_DesiredWindowHeight;
    private IAppState m_PendingState = null;
    private IAppState m_CurrentState = null;

    public Application() throws IOException {
        m_TimeoutManager = new TimeoutManager();
        initialiseOpenGL();
        changeStateNow(new RunningMenuAppState(this), System.currentTimeMillis());
    }

    private void initialiseOpenGL() {
        // https://www.glfw.org/docs/latest/window_guide.html
        // https://github.com/glfw/glfw
        // https://en.wikipedia.org/wiki/GLFW

        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        m_Window = glfwCreateWindow(s_DesiredWindowWidth, s_DesiredWindowHeight, s_WindowTitle, NULL, NULL);
        if (m_Window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidMode == null) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetWindowPos(m_Window,
                (vidMode.width() - s_DesiredWindowWidth) / 2,
                (vidMode.height() - s_DesiredWindowHeight) / 2);
        glfwMakeContextCurrent(m_Window);
        GL.createCapabilities();
        glfwSwapInterval(1); // Sync to monitor's refresh rate

        glfwSetWindowSizeCallback(m_Window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if (glfwGetCurrentContext() > 0) {
                    glViewport(0, 0, width, height);
                }
                m_ActualWidth = (float)Math.max(width, 1);
                m_ActualHeight = (float)Math.max(height, 1);
            }
        });

        glfwSetKeyCallback(m_Window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (m_CurrentState != null) {
                    try {
                        m_CurrentState.processKey(window, key, scancode, action, mods);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        glViewport(0, 0, s_DesiredWindowWidth, s_DesiredWindowHeight);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
    }

    public void close() {
        glfwDestroyWindow(m_Window);
        glfwTerminate();
    }

    public void run() throws IOException {
        long nowMs;
        while (!glfwWindowShouldClose(m_Window)) {
            nowMs = System.currentTimeMillis();
            m_TimeoutManager.dispatchTimeouts(nowMs);
            m_CurrentState.think(nowMs);
            draw(nowMs);
            performPendingStateChange(nowMs);
        }
    }

    private void draw(long nowMs) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        perform3dDrawing(nowMs);
        perform2dDrawing(nowMs);
        glfwSwapBuffers(m_Window);
        glfwPollEvents();
    }

    private void perform3dDrawing(long nowMs) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        setPerspectiveProjection(m_ActualWidth / m_ActualHeight);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslated(0.0, 0.0, -110.0); // Place the camera
        m_CurrentState.draw3d(nowMs);
    }

    private void perform2dDrawing(long nowMs) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, m_ActualWidth, 0.0, m_ActualHeight, -1.0f, 1.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        // No camera in 2d mode
        m_CurrentState.draw2d(nowMs);
    }

    // https://www.khronos.org/opengl/wiki/GluPerspective_code
    private static void setPerspectiveProjection(double aspect) {
        double tangent = Math.tan(Math.toRadians(s_FovYDegrees / 2.0));
        double height = s_NearClipPlane * tangent;
        double width = height * aspect;
        glFrustum(-width, width, -height, height, s_NearClipPlane, s_FarClipPlane);
    }

    @Override
    public void changeState(IAppState newState) {
        m_PendingState = newState;
    }

    @Override
    public float getWindowWidth() {
        return m_ActualWidth;
    }

    @Override
    public float getWindowHeight() {
        return m_ActualHeight;
    }

    @Override
    public int addTimeout(long nowMs, long timeoutMs, Function<Integer, TimeoutManager.CallbackResult> callback) {
        return m_TimeoutManager.addTimeout(nowMs, timeoutMs, callback);
    }

    @Override
    public void removeTimeout(int timeoutId) {
        m_TimeoutManager.removeTimeout(timeoutId);
    }

    public void performPendingStateChange(long nowMs) throws IOException {
        if (m_PendingState != null) {
            changeStateNow(m_PendingState, nowMs);
            m_PendingState = null;
        }
    }

    private void changeStateNow(IAppState newState, long nowMs) throws IOException {
        if (m_CurrentState != null) {
            m_CurrentState.end(nowMs);
        }
        m_CurrentState = newState;
        if (m_CurrentState != null) {
            m_CurrentState.begin(nowMs);
        }
    }

    // This is here for convenience.
    public static void main(String[] args) {
        Application app = null;
        try {
            app = new Application();
            app.run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (app != null) {
                app.close(); // ensure release of OpenGL resources
            }
        }
    }
}
