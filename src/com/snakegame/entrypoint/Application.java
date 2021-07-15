package com.snakegame.entrypoint;

import com.snakegame.client.GameView;
import com.snakegame.rules.GameField;
import com.snakegame.rules.SinglePlayerGame;
import com.snakegame.rules.Snake;
import com.snakegame.rules.Vector2i;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * This class keeps the main application objects resident for the lifetime of the application.
 * */

public class Application {
    private static final int s_DesiredWindowWidth = 1024;
    private static final int s_DesiredWindowHeight = 768;
    private static final double s_FovYDegrees = 60.0;
    private static final double s_NearClipPlane = 1.0;
    private static final double s_FarClipPlane = 1000.0;
    private static final String s_WindowTitle = "159.261 Game Programming (Assignment 1)";

    private final long m_Window;
    private final SinglePlayerGame m_Game;
    private final GameView m_GameView;
    private float m_ActualWidth = (float)s_DesiredWindowWidth;
    private float m_ActualHeight = (float)s_DesiredWindowHeight;

    public Application() throws IOException {
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
                if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (key == GLFW_KEY_UP && action == GLFW_PRESS) {
                    if (m_Game.getSnake().getDirection() == Snake.Direction.Left ||
                        m_Game.getSnake().getDirection() == Snake.Direction.Right) {
                        m_Game.getSnake().setDirection(Snake.Direction.Up);
                    }
                }
                if (key == GLFW_KEY_DOWN && action == GLFW_PRESS) {
                    if (m_Game.getSnake().getDirection() == Snake.Direction.Left ||
                        m_Game.getSnake().getDirection() == Snake.Direction.Right) {
                        m_Game.getSnake().setDirection(Snake.Direction.Down);
                    }
                }
                if (key == GLFW_KEY_LEFT && action == GLFW_PRESS) {
                    if (m_Game.getSnake().getDirection() == Snake.Direction.Up ||
                        m_Game.getSnake().getDirection() == Snake.Direction.Down) {
                        m_Game.getSnake().setDirection(Snake.Direction.Left);
                    }
                }
                if (key == GLFW_KEY_RIGHT && action == GLFW_PRESS) {
                    if (m_Game.getSnake().getDirection() == Snake.Direction.Up ||
                        m_Game.getSnake().getDirection() == Snake.Direction.Down) {
                        m_Game.getSnake().setDirection(Snake.Direction.Right);
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

        m_Game = new SinglePlayerGame();
        ArrayList<Snake> snakes = new ArrayList<>();
        snakes.add(m_Game.getSnake());
        m_GameView = new GameView(m_Game.getGameField(), snakes);
    }

    public void close() {
        m_GameView.close();
        glfwDestroyWindow(m_Window);
        glfwTerminate();
    }

    public void run() {
        long lastAppleTime = System.currentTimeMillis();
        long lastMovementTime = lastAppleTime;

        while (!glfwWindowShouldClose(m_Window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            setPerspectiveProjection(m_ActualWidth / m_ActualHeight);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glTranslated(0.0, 0.0, -110.0); // Place the camera

            m_GameView.draw();

            glfwSwapBuffers(m_Window);
            glfwPollEvents();

            long nowMs = System.currentTimeMillis();
            if (nowMs - lastAppleTime >= 5000) {
                Vector2i position = m_Game.getEmptyGameFieldCell();
                m_Game.getGameField().setCell(position, GameField.Cell.APPLE);
                lastAppleTime = nowMs;
            }
            if (nowMs - lastMovementTime >= 100) {
                m_Game.getSnake().moveForwards();
                lastMovementTime = nowMs;
                if (m_Game.getSnake().isCollidingWithItself()) {
                    m_Game.reset();
                    lastAppleTime = nowMs;
                }
                else {
                    Vector2i position = m_Game.getSnake().getBodyParts().getFirst();
                    GameField.Cell cell = m_Game.getGameField().getCell(position);
                    if (cell == GameField.Cell.APPLE) {
                        m_Game.getSnake().addOneBodyPart();
                        m_Game.getGameField().setCell(position, GameField.Cell.EMPTY);
                    } else if (cell == GameField.Cell.WALL) {
                        m_Game.reset();
                        lastAppleTime = nowMs;
                    }
                }
            }
        }
    }

    // https://www.khronos.org/opengl/wiki/GluPerspective_code
    private static void setPerspectiveProjection(double aspect) {
        double tangent = Math.tan(Math.toRadians(s_FovYDegrees / 2.0));
        double height = s_NearClipPlane * tangent;
        double width = height * aspect;
        glFrustum(-width, width, -height, height, s_NearClipPlane, s_FarClipPlane);
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
