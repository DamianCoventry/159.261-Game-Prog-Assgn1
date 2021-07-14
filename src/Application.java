import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * This class keeps the main application objects resident for the lifetime of the application.
 * */

public class Application {
    private static final int s_DesiredWindowWidth = 1024;
    private static final int s_DesiredWindowHeight = 768;
    private static final String s_WindowTitle = "159.261 Game Programming (Assignment 1)";

    private final long m_Window;
    private float m_ActualWidth = (float)s_DesiredWindowWidth;
    private float m_ActualHeight = (float)s_DesiredWindowHeight;

    public Application() {
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

        glfwSetWindowPos(m_Window, (vidMode.width() - s_DesiredWindowWidth) / 2, (vidMode.height() - s_DesiredWindowHeight) / 2);
        glfwMakeContextCurrent(m_Window);
        GL.createCapabilities();
        glfwSwapInterval(1);

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
            }
        });

        glViewport(0, 0, s_DesiredWindowWidth, s_DesiredWindowHeight);
        glEnable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
    }

    public void close() {
        glfwDestroyWindow(m_Window);
        glfwTerminate();
    }

    public void run() {
        while (!glfwWindowShouldClose(m_Window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            gluPerspective(60.0, m_ActualWidth / m_ActualHeight, 1.0, 1000.0);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glfwSwapBuffers(m_Window);
            glfwPollEvents();
        }
    }

    private static void gluPerspective(double fovYDegrees, double aspect, double zNear, double zFar) {
        double tangent = Math.tan(Math.toRadians(fovYDegrees / 2.0));
        double height = zNear * tangent;
        double width = height * aspect;
        glFrustum(-width, width, -height, height, zNear, zFar);
    }

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
