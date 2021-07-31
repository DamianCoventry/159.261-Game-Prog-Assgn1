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

package com.snakegame.opengl;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

// https://www.glfw.org/docs/latest/window_guide.html
// https://github.com/glfw/glfw
// https://en.wikipedia.org/wiki/GLFW
public class GLWindow {
    private static final float s_FovYDegrees = 60.0f;
    private static final float s_NearClipPlane = 1.0f;
    private static final float s_FarClipPlane = 1000.0f;

    private final long m_Window;
    private long m_ArrowMouseCursor;
    private long m_HandMouseCursor;
    private long m_GrabMouseCursor;
    private long m_CurrentMouseCursor;
    private Matrix4f m_PerspectiveMatrix;
    private Matrix4f m_OrthographicMatrix;

    private float m_ActualWidth;
    private float m_ActualHeight;

    public GLWindow(int desiredWindowWidth, int desiredWindowHeight, String windowTitle) throws IOException {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // the window will not be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5); // Version 4.5 was released in 2014
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        m_Window = glfwCreateWindow(desiredWindowWidth, desiredWindowHeight, windowTitle, NULL, NULL);
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
                (vidMode.width() - desiredWindowWidth) / 2,
                (vidMode.height() - desiredWindowHeight) / 2);
        glfwMakeContextCurrent(m_Window);

        glfwSwapInterval(1); // Sync to monitor's refresh rate

        glfwSetWindowSizeCallback(m_Window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if (glfwGetCurrentContext() > 0) {
                    glViewport(0, 0, width, height);
                }
                m_ActualWidth = (float)Math.max(width, 1);
                m_ActualHeight = (float)Math.max(height, 1); // because we divide to get the aspect ratio
            }
        });

        // This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is
        // managed externally. LWJGL detects the context that is current in the current thread, creates the
        // ContextCapabilities instance and makes the OpenGL bindings available for use.
        GL.createCapabilities();

        glViewport(0, 0, desiredWindowWidth, desiredWindowHeight);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        loadMouseCursors();
        loadIcons();

        glfwSetCursor(m_Window, m_ArrowMouseCursor);
        m_CurrentMouseCursor = m_ArrowMouseCursor;
        glfwShowWindow(m_Window);

        m_PerspectiveMatrix = createPerspectiveMatrix();
        m_OrthographicMatrix = createOrthographicMatrix();

        m_ActualWidth = (float)desiredWindowWidth;
        m_ActualHeight = (float)desiredWindowHeight;
    }

    public void exitApplication() {
        glfwSetWindowShouldClose(m_Window, true);
    }

    public Matrix4f getPerspectiveMatrix() {
        m_PerspectiveMatrix = createPerspectiveMatrix();
        return m_PerspectiveMatrix;
    }

    public Matrix4f getOrthographicMatrix() {
        m_OrthographicMatrix = createOrthographicMatrix();
        return m_OrthographicMatrix;
    }

    public float getActualWidth() {
        return m_ActualWidth;
    }

    public float getActualHeight() {
        return m_ActualHeight;
    }

    public void activateArrowMouseCursor() {
        if (m_CurrentMouseCursor != m_ArrowMouseCursor) {
            m_CurrentMouseCursor = m_ArrowMouseCursor;
            glfwSetCursor(m_Window, m_ArrowMouseCursor);
        }
    }

    public void activateHandMouseCursor() {
        if (m_CurrentMouseCursor != m_HandMouseCursor) {
            m_CurrentMouseCursor = m_HandMouseCursor;
            glfwSetCursor(m_Window, m_HandMouseCursor);
        }
    }

    public void activateGrabMouseCursor() {
        if (m_CurrentMouseCursor != m_GrabMouseCursor) {
            m_CurrentMouseCursor = m_GrabMouseCursor;
            glfwSetCursor(m_Window, m_GrabMouseCursor);
        }
    }

    public void freeNativeResources() {
        glfwDestroyCursor(m_ArrowMouseCursor);
        glfwDestroyCursor(m_HandMouseCursor);
        glfwDestroyCursor(m_GrabMouseCursor);
        glfwDestroyWindow(m_Window);
        glfwTerminate();
    }

    public boolean quitRequested() {
        return glfwWindowShouldClose(m_Window);
    }

    public void setKeyCallback(GLFWKeyCallbackI callback) {
        glfwSetKeyCallback(m_Window, callback);
    }

    public void setMouseButtonCallback(GLFWMouseButtonCallbackI callback) {
        glfwSetMouseButtonCallback(m_Window, callback);
    }

    public void setMouseWheelCallback(GLFWScrollCallbackI callback) {
        glfwSetScrollCallback(m_Window, callback);
    }

    public void setMouseCursorMovementCallback(GLFWCursorPosCallback callback) {
        glfwSetCursorPosCallback(m_Window, callback);
    }

    public static class CursorPosition {
        public double m_XPos;
        public double m_YPos;
        public CursorPosition(double xPos, double yPos) {
            m_XPos = xPos;
            m_YPos = yPos;
        }
    }

    public CursorPosition getMouseCursorPosition() {
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        glfwGetCursorPos(m_Window, xPos, yPos);
        return new CursorPosition(xPos[0], yPos[0]);
    }

    public void beginDrawing() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endDrawing() {
        glfwSwapBuffers(m_Window);
        glfwPollEvents();
    }

    private void loadMouseCursors() throws IOException {
        var image = loadImageFileAsGlfwImage("images\\ArrowMouseCursor.png");
        m_ArrowMouseCursor = glfwCreateCursor(image, 0, 0);
        image.free();
        if (m_ArrowMouseCursor == NULL) {
            throw new RuntimeException("Unable to create a mouse cursor");
        }

        image = loadImageFileAsGlfwImage("images\\HandMouseCursor.png");
        m_HandMouseCursor = glfwCreateCursor(image, 19, 3);
        image.free();
        if (m_HandMouseCursor == NULL) {
            throw new RuntimeException("Unable to create a mouse cursor");
        }

        image = loadImageFileAsGlfwImage("images\\GrabMouseCursor.png");
        m_GrabMouseCursor = glfwCreateCursor(image, 23, 10);
        if (m_GrabMouseCursor == NULL) {
            throw new RuntimeException("Unable to create a mouse cursor");
        }
        image.free();
    }

    private void loadIcons() throws IOException {
        final String[] fileNames = {
                "images\\Snake64x64.png",
                "images\\Snake32x32.png",
                "images\\Snake24x24.png",
                "images\\Snake16x16.png"
        };

        GLFWImage.Buffer images = GLFWImage.malloc(4);
        int i = 0;
        for (var fileName : fileNames) {
            GLFWImage image = loadImageFileAsGlfwImage(fileName);
            images.put(i++, image);
            image.free();
        }

        glfwSetWindowIcon(m_Window, images);
        images.free();
    }

    private GLFWImage loadImageFileAsGlfwImage(String fileName) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(fileName));
        if (bufferedImage == null) {
            throw new RuntimeException("Unable to load the file [" + fileName + "]");
        }

        int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(bufferedImage.getWidth() * bufferedImage.getHeight() * 4);
        for(int y = 0; y < bufferedImage.getHeight(); y++){
            for(int x = 0; x < bufferedImage.getWidth(); x++){
                int pixel = pixels[y * bufferedImage.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));             // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));     // Alpha component. Only for RGBA
            }
        }

        // After a sequence of channel-read or put operations, invoke this method to prepare for a
        // sequence of channel-write or relative get operations.
        buffer.flip();

        GLFWImage image = GLFWImage.malloc();
        image.set(bufferedImage.getWidth(), bufferedImage.getHeight(), buffer);
        return image;
    }

    private Matrix4f createPerspectiveMatrix() {
        return new Matrix4f().perspective((float)Math.toRadians(s_FovYDegrees / 2.0), m_ActualWidth / m_ActualHeight,
                s_NearClipPlane, s_FarClipPlane);
    }

    private Matrix4f createOrthographicMatrix() {
        return new Matrix4f().ortho2D(0.0f, m_ActualWidth, 0.0f, m_ActualHeight);
    }
}
