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

import com.snakegame.client.IGameView;
import com.snakegame.client.Texture;
import com.snakegame.client.TimeoutManager;
import com.snakegame.rules.IGameController;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class SnakeDyingAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameController m_Controller;
    private final IGameView m_View;
    private final int m_Player;
    private final boolean m_BothSnakes;

    public SnakeDyingAppState(IAppStateContext context, int player) {
        m_AppStateContext = context;
        m_Controller = m_AppStateContext.getController();
        m_View = m_AppStateContext.getView();
        m_Player = player;
        m_BothSnakes = false;
    }

    public SnakeDyingAppState(IAppStateContext context) {
        m_AppStateContext = context;
        m_Controller = m_AppStateContext.getController();
        m_View = m_AppStateContext.getView();
        m_Player = -1;
        m_BothSnakes = true;
    }

    @Override
    public void begin(long nowMs) {
        m_AppStateContext.addTimeout(2000, (callCount) -> {
            if (m_Controller.getMode() == IGameController.Mode.TWO_PLAYERS) {
                subtractSnakeTwoPlayersGame();
            }
            else {
                subtractSnakeSinglePlayerGame();
            }
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    @Override
    public void end(long nowMs) {
        // No work to do
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        // No work to do
    }

    @Override
    public void think(long nowMs) throws IOException {
        // No work to do
    }

    @Override
    public void draw3d(long nowMs) {
        m_View.draw3d(nowMs);
    }

    @Override
    public void draw2d(long nowMs) {
        m_View.draw2d(nowMs);

        Texture texture;
        if (m_BothSnakes) {
            texture = m_View.getBothPlayersDiedTexture();
        }
        else if (m_Player == 0) {
            texture = m_View.getPlayer1DiedTexture();
        }
        else {
            texture = m_View.getPlayer2DiedTexture();
        }
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        var w = texture.getWidth();
        var h = texture.getHeight();
        var x = (m_AppStateContext.getWindowWidth() / 2.0f) - (w / 2.0f);
        var y = (m_AppStateContext.getWindowHeight() / 2.0f) - (h / 2.0f);
        glBegin(GL_QUADS);
        glTexCoord2d(0.0, 0.0); glVertex3d(x, y + h, 0.1f);
        glTexCoord2d(0.0, 1.0); glVertex3d(x , y, 0.1f);
        glTexCoord2d(1.0, 1.0); glVertex3d(x + w, y, 0.1f);
        glTexCoord2d(1.0, 0.0); glVertex3d(x + w, y + h, 0.1f);
        glEnd();
    }

    private void subtractSnakeSinglePlayerGame() {
        if (m_Controller.subtractSnake(0) == IGameController.SubtractSnakeResult.SNAKE_AVAILABLE) {
            m_AppStateContext.changeState(new GetReadyAppState(m_AppStateContext, true));
        }
        else {
            m_AppStateContext.changeState(new GameOverAppState(m_AppStateContext, 0));
        }
    }

    private void subtractSnakeTwoPlayersGame() {
        if (m_BothSnakes) {
            subtractSnakeFromBothPlayers();
        }
        else if (m_Controller.subtractSnake(m_Player) == IGameController.SubtractSnakeResult.NO_SNAKES_REMAIN) {
            // Then the other player won
            m_AppStateContext.changeState(new GameWonAppState(m_AppStateContext, m_Player == 0 ? 1 : 0));
        }
        else {
            m_AppStateContext.changeState(new GetReadyAppState(m_AppStateContext, true));
        }
    }

    private void subtractSnakeFromBothPlayers() {
        IGameController.SubtractSnakeResult player1Result = m_Controller.subtractSnake(0);
        IGameController.SubtractSnakeResult player2Result = m_Controller.subtractSnake(1);
        if (player1Result == IGameController.SubtractSnakeResult.NO_SNAKES_REMAIN) {
            if (player2Result == IGameController.SubtractSnakeResult.NO_SNAKES_REMAIN) {
                // Then it's a joint loss
                m_AppStateContext.changeState(new GameOverAppState(m_AppStateContext));
            }
            else {
                // Then player 2 won
                m_AppStateContext.changeState(new GameWonAppState(m_AppStateContext, 1));
            }
        }
        else if (player2Result == IGameController.SubtractSnakeResult.NO_SNAKES_REMAIN) {
            // Then player 1 won
            m_AppStateContext.changeState(new GameWonAppState(m_AppStateContext, 0));
        }
        else {
            m_AppStateContext.changeState(new GetReadyAppState(m_AppStateContext, true));
        }
    }
}
