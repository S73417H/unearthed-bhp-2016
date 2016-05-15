package com.thales.motherload;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.thales.screens.LoadingScreen;
import com.thales.screens.PositioningScreen;

public class MotherloadGame extends Game {

    private SpriteBatch spriteBatch;


    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        this.setScreen(new PositioningScreen(spriteBatch, this));
    }

    public void switchScreens() {
        this.setScreen(new LoadingScreen(spriteBatch));
    }

}
