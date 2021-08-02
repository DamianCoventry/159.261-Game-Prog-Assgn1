package com.snakegame.client;

import com.snakegame.opengl.*;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Button {
    private final IGameView m_View;
    private final GLStaticPolyhedronVxTc m_Polyhedron;
    private final GLTexture m_NotPressedTexture;
    private final GLTexture m_NotPressedFocusedTexture;
    private final GLTexture m_PressedTexture;
    private final Matrix4f m_ModelMatrix;

    private Consumer<Integer> m_OnClickFunction;
    private float m_X, m_Y;
    private boolean m_Focused;
    private boolean m_Pressed;

    public Button(IGameView view, String notPressedFileName, String notPressedFocusedFileName, String pressedFileName) throws IOException {
        m_View = view;
        m_NotPressedTexture = new GLTexture(ImageIO.read(new File(notPressedFileName)));
        m_NotPressedFocusedTexture = new GLTexture(ImageIO.read(new File(notPressedFocusedFileName)));
        m_PressedTexture = new GLTexture(ImageIO.read(new File(pressedFileName)));
        m_Polyhedron = m_View.createPolyhedron(0, 0, m_NotPressedTexture.getWidth(), m_NotPressedTexture.getHeight(), m_NotPressedTexture);
        m_ModelMatrix = new Matrix4f();
        m_X = m_Y = 0;
    }

    public void setOnClickFunction(Consumer<Integer> function) {
        m_OnClickFunction = function;
    }

    public void click(int value) {
        if (m_OnClickFunction != null) {
            m_OnClickFunction.accept(value);
        }
    }

    public void freeNativeResources() {
        m_NotPressedTexture.freeNativeResource();
        m_NotPressedFocusedTexture.freeNativeResource();
        m_PressedTexture.freeNativeResource();
        m_Polyhedron.freeNativeResources();
    }

    public void setPosition(float x, float y) {
        m_X = x;
        m_Y = y;
    }

    public float getWidth() {
        return m_NotPressedTexture.getWidth();
    }

    public float getHeight() {
        return m_NotPressedTexture.getHeight();
    }

    public boolean containsPoint(float x, float y) {
        return x >= m_X && x < m_X + m_NotPressedTexture.getWidth() &&
               y >= m_Y && y < m_Y + m_NotPressedTexture.getHeight();
    }

    public void processMouseButtonPressed(int button, float xPos, float yPos) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            if (containsPoint(xPos, yPos)) {
                setPressed();
            }
            else {
                clearPressed();
            }
        }
    }

    public void processMouseButtonReleased(int button, float xPos, float yPos) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            if (containsPoint(xPos, yPos) && isPressed()) {
                clearPressed();
                setFocused();
                click(0);
            }
        }
    }

    public void processMouseCursorMovement(float xPos, float yPos) {
        if (containsPoint(xPos, yPos)) {
            m_View.activateHandMouseCursor();
            if (!isPressed()) {
                setFocused();
            }
        }
        else {
            clearFocused();
            clearPressed();
        }
    }

    public boolean isFocused() {
        return m_Focused;
    }
    public void setFocused() {
        m_Focused = true;
    }
    public void clearFocused() {
        m_Focused = false;
    }

    public boolean isPressed() {
        return m_Pressed;
    }
    public void setPressed() {
        m_Pressed = true;
    }
    public void clearPressed() {
        m_Pressed = false;
    }
    
    public void draw2d(float alpha) {
        m_ModelMatrix.identity().translate(m_X, m_Y, 0.0f);
        if (m_Pressed) {
            m_Polyhedron.getPiece(0).setDiffuseTexture(m_PressedTexture);
        }
        else if (m_Focused) {
            m_Polyhedron.getPiece(0).setDiffuseTexture(m_NotPressedFocusedTexture);
        }
        else {
            m_Polyhedron.getPiece(0).setDiffuseTexture(m_NotPressedTexture);
        }
        m_View.drawOrthographicPolyhedron(m_Polyhedron, m_ModelMatrix, alpha);
    }
}
