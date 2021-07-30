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

package com.snakegame.application;

import com.snakegame.client.*;
import com.snakegame.opengl.GLStaticPolyhedronVxTc;
import com.snakegame.opengl.GLTexture;
import com.snakegame.rules.IGameController;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.*;

public class SnakeDyingAppState implements IAppState {
    private final IAppStateContext m_Context;
    private final IGameController m_Controller;
    private final IGameView m_View;
    private final int m_Player;
    private final boolean m_BothSnakes;
    private final Matrix4f m_ModelMatrix;
    private GLStaticPolyhedronVxTc[] m_Polyhedra;
    private boolean m_firstPhaseCompleted;

    public SnakeDyingAppState(IAppStateContext context, int player) {
        m_Context = context;
        m_Controller = m_Context.getController();
        m_View = m_Context.getView();
        m_ModelMatrix = new Matrix4f();
        m_Player = player;
        m_BothSnakes = false;
    }

    public SnakeDyingAppState(IAppStateContext context) {
        m_Context = context;
        m_Controller = m_Context.getController();
        m_View = m_Context.getView();
        m_ModelMatrix = new Matrix4f();
        m_Player = -1;
        m_BothSnakes = true;
    }

    @Override
    public void begin(long nowMs) throws IOException {
        m_Polyhedra = new GLStaticPolyhedronVxTc[3];

        GLTexture player1DiedTexture = new GLTexture(ImageIO.read(new File("images\\Player1Died.png")));
        m_Polyhedra[0] = m_View.createCenteredPolyhedron(player1DiedTexture.getWidth(), player1DiedTexture.getHeight(), player1DiedTexture);

        GLTexture layer2DiedTexture = new GLTexture(ImageIO.read(new File("images\\Player2Died.png")));
        m_Polyhedra[1] = m_View.createCenteredPolyhedron(layer2DiedTexture.getWidth(), layer2DiedTexture.getHeight(), layer2DiedTexture);

        GLTexture bothPlayersDiedTexture = new GLTexture(ImageIO.read(new File("images\\BothSnakesDied.png")));
        m_Polyhedra[2] = m_View.createCenteredPolyhedron(bothPlayersDiedTexture.getWidth(), bothPlayersDiedTexture.getHeight(), bothPlayersDiedTexture);

        if (m_BothSnakes) {
            m_Controller.getSnakes()[0].setDead(); // This stops this snake being displayed
            m_Controller.getSnakes()[1].setDead(); // This stops this snake being displayed
            m_View.spawnSnakeGiblets(m_Controller.getSnakes()[0]);
            m_View.spawnSnakeGiblets(m_Controller.getSnakes()[1]);
        }
        else {
            m_Controller.getSnakes()[m_Player].setDead(); // This stops this snake being displayed
            m_View.spawnSnakeGiblets(m_Controller.getSnakes()[m_Player]);
        }

        m_firstPhaseCompleted = false;
        m_Context.addTimeout(2500, (callCount) -> {
            m_firstPhaseCompleted = true;
            scheduleStateTransition();
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    @Override
    public void end(long nowMs) {
        for (var r : m_Polyhedra) {
            r.freeNativeResources();
        }
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        // No work to do
    }

    @Override
    public void processMouseButton(long window, int button, int action, int mods) {
        // No work to do
    }

    @Override
    public void processMouseWheel(long window, double xOffset, double yOffset) {
        // No work to do
    }

    @Override
    public void think(long nowMs) throws IOException {
        m_View.think(nowMs);
    }

    @Override
    public void draw3d(long nowMs) {
        m_View.draw3d(nowMs);
    }

    @Override
    public void draw2d(long nowMs) throws IOException {
        m_View.draw2d(nowMs);
        if (m_firstPhaseCompleted) {
            if (m_BothSnakes) {
                m_View.drawOrthographicPolyhedron(m_Polyhedra[2], m_ModelMatrix);
            } else if (m_Player == 0) {
                m_View.drawOrthographicPolyhedron(m_Polyhedra[0], m_ModelMatrix);
            } else {
                m_View.drawOrthographicPolyhedron(m_Polyhedra[1], m_ModelMatrix);
            }
        }
    }

    private void scheduleStateTransition() {
        m_Context.addTimeout(1500, (callCount) -> {
            if (m_Controller.getMode() == IGameController.Mode.TWO_PLAYERS) {
                subtractSnakeTwoPlayersGame();
            }
            else {
                subtractSnakeSinglePlayerGame();
            }
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    private void subtractSnakeSinglePlayerGame() {
        if (m_Controller.subtractSnake(0) == IGameController.SubtractSnakeResult.SNAKE_AVAILABLE) {
            m_Context.changeState(new GetReadyAppState(m_Context, true));
        }
        else {
            m_Context.changeState(new GameOverAppState(m_Context, 0));
        }
    }

    private void subtractSnakeTwoPlayersGame() {
        if (m_BothSnakes) {
            subtractSnakeFromBothPlayers();
        }
        else if (m_Controller.subtractSnake(m_Player) == IGameController.SubtractSnakeResult.NO_SNAKES_REMAIN) {
            // Then the other player won
            m_Context.changeState(new GameWonAppState(m_Context, m_Player == 0 ? 1 : 0));
        }
        else {
            m_Context.changeState(new GetReadyAppState(m_Context, true));
        }
    }

    private void subtractSnakeFromBothPlayers() {
        IGameController.SubtractSnakeResult player1Result = m_Controller.subtractSnake(0);
        IGameController.SubtractSnakeResult player2Result = m_Controller.subtractSnake(1);
        if (player1Result == IGameController.SubtractSnakeResult.NO_SNAKES_REMAIN) {
            if (player2Result == IGameController.SubtractSnakeResult.NO_SNAKES_REMAIN) {
                // Then it's a joint loss
                m_Context.changeState(new GameOverAppState(m_Context));
            }
            else {
                // Then player 2 won
                m_Context.changeState(new GameWonAppState(m_Context, 1));
            }
        }
        else if (player2Result == IGameController.SubtractSnakeResult.NO_SNAKES_REMAIN) {
            // Then player 1 won
            m_Context.changeState(new GameWonAppState(m_Context, 0));
        }
        else {
            m_Context.changeState(new GetReadyAppState(m_Context, true));
        }
    }
}
