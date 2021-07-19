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
import com.snakegame.rules.IGameController;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class SnakeDyingAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameController m_Controller;
    private final IGameView m_View;
    private final int m_Player;
    private final boolean m_BothSnakes;
    private Texture m_Player1DiedTexture;
    private Texture m_Player2DiedTexture;
    private Texture m_BothPlayersDiedTexture;

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
    public void begin(long nowMs) throws IOException {
        m_Player1DiedTexture = new Texture(ImageIO.read(new File("images\\Player1Died.png")));
        m_Player2DiedTexture = new Texture(ImageIO.read(new File("images\\Player2Died.png")));
        m_BothPlayersDiedTexture = new Texture(ImageIO.read(new File("images\\BothSnakesDied.png")));
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
        m_Player1DiedTexture.freeNativeResource();
        m_Player2DiedTexture.freeNativeResource();
        m_BothPlayersDiedTexture.freeNativeResource();
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
        if (m_BothSnakes) {
            m_View.drawCenteredImage(m_BothPlayersDiedTexture);
        }
        else if (m_Player == 0) {
            m_View.drawCenteredImage(m_Player1DiedTexture);
        }
        else {
            m_View.drawCenteredImage(m_Player2DiedTexture);
        }
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
