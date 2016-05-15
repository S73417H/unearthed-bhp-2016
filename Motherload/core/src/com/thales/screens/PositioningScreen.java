package com.thales.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.thales.controllers.XboxOnePad;
import com.thales.motherload.MotherloadGame;

import javax.swing.SpringLayout;

/**
 * Created by choco on 15/05/2016.
 */
public class PositioningScreen implements Screen, ControllerListener {

    private static final float gridTranslateRate = 0.2f;
    private static final int gridSize = 10;
    private static final int gridDimensions = 150;
    private static final int gridBorderSize = 2;

    private double gamepadAxisMovementThreshold = 0.2;

    private int[][] colours = new int[gridSize][gridSize];

    private ShapeRenderer shapeRenderer;
    private Controller controller = null;
    private final SpriteBatch spriteBatch;
    private OrthographicCamera orthographicCamera;

    private Vector2 currentTilePosition;
    private float screenWidth;
    private float screenHeight;
    private float halfScreenWidth;
    private float halfScreenHeight;

    private float translateGridX = 0;
    private float translateGridY = 0;

    private float lastTranslateX = 0;
    private float lastTranslateY = 0;

    private Color highGradeColor = Color.LIME;
    private Color lowGradeColor = Color.GREEN;
    private Color dirt = Color.BROWN;

    private BitmapFont textFont;

    private final MotherloadGame game;

    public PositioningScreen(SpriteBatch spriteBatch, MotherloadGame game) {
        this.spriteBatch = spriteBatch;
        this.game = game;
        for(int i = 0; i < gridSize; i++)
        {
            for(int j = 0; j < gridSize; j++)
            {
             colours[i][j] = MathUtils.random(2);
            }
        }
    }

    @Override
    public void show() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        halfScreenWidth = screenWidth / 2;
        halfScreenHeight = screenHeight / 2;

        createFonts();

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.setAutoShapeType(true);

        orthographicCamera = new OrthographicCamera(screenWidth, screenHeight);
        spriteBatch.setProjectionMatrix(orthographicCamera.combined);
    }

    private void createFonts() {
        FileHandle fontFile = Gdx.files.internal("arial.ttf");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48;
        textFont = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void render(float delta) {
        handleInput();
        translateGrid();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderTiles();
        renderIdealPosition();
        renderTruck();
        shapeRenderer.end();

//        spriteBatch.begin();
//        int totalGridSpace = gridDimensions * gridSize;
//        float initialX = halfScreenWidth - totalGridSpace / 2;
//        float initialY = halfScreenHeight - totalGridSpace / 2;
//        for(int i = 0; i < gridSize; i++)
//        {
//            for(int j = 0; j < gridSize; j++)
//            {
//                textFont.draw (spriteBatch, i + "," + j, initialX + gridDimensions * i + translateGridX, initialY + gridDimensions * j + translateGridY);
//            }
//        }
//
//        spriteBatch.end();

    }

    private void translateGrid()
    {
        // Translations should be backwards as we're emulating the ground moving underneath us.
        translateGridX += lastTranslateX * gridTranslateRate;
        translateGridY += lastTranslateY * gridTranslateRate;
    }

    private void renderTruck()
    {
        // Draw the cab
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(halfScreenWidth-15, halfScreenHeight-15, 30, 30);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(halfScreenWidth, halfScreenHeight, 60);
    }

    private void renderIdealPosition(){
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(halfScreenWidth + translateGridX, halfScreenHeight + translateGridY - 85, 34, 34);
    }

    private void renderTiles()
    {
        int totalGridSpace = gridDimensions * gridSize;
        float initialX = halfScreenWidth - totalGridSpace / 2 + 40;
        float initialY = halfScreenHeight - totalGridSpace / 2 + 50;
        for(int i = 0; i < gridSize; i++)
        {
            for(int j = 0; j < gridSize; j++)
            {
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.rect(initialX + (gridDimensions * i) + translateGridX, initialY + (gridDimensions * j) + translateGridY, gridDimensions, gridDimensions);

                Color gridColor = Color.DARK_GRAY;
                if(i != 0 && i != gridSize - 1 && j != 0 && j != gridSize - 1) {
                    gridColor = getGridColor(colours[i][j]);
                }

                // Set some colours to already mined.
                if(i < 4 || (i == 4 && j > 3) || (i == 5 && j > 4))
                {
                    gridColor = Color.DARK_GRAY;
                }

                if((i==5 || i == 4) && j == 3)
                {
                    gridColor = dirt;
                }
                if(i == 5 && j == 4)
                {
                    gridColor = highGradeColor;
                }

                shapeRenderer.setColor(gridColor);
                shapeRenderer.rect(initialX + (gridDimensions * i) + gridBorderSize + translateGridX, initialY + (gridDimensions * j) + gridBorderSize + translateGridY, gridDimensions - (2 * gridBorderSize), gridDimensions - (2 * gridBorderSize));
            }
        }
    }

    private Color getGridColor(int colorVal)
    {
        switch(colorVal)
        {
            case 0:
                return highGradeColor;
            case 1:
                return lowGradeColor;
            default:
                return dirt;
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    private void handleInput()
    {
        if(this.controller == null) {
            for (Controller controller : Controllers.getControllers()) {
                if (this.controller == null) {
                    this.controller = controller;
                    this.controller.addListener(this);
                }
            }
        }
//        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//            angle--;
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//            angle++;
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//            bucketPositionY -= bucketTranslateRate;
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
//            bucketPositionY += bucketTranslateRate;
//        }
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if(buttonCode == XboxOnePad.BUTTON_A)
        {
            game.switchScreens();
        }
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        // Up and down
        if(Math.abs(value) > gamepadAxisMovementThreshold) {
            if (axisCode == XboxOnePad.AXIS_LEFT_X) {
                lastTranslateX = -value;
            }
            if (axisCode == XboxOnePad.AXIS_LEFT_Y) {
                lastTranslateY = value;
            }
        }
        else
        {
            if (axisCode == XboxOnePad.AXIS_LEFT_X) {
                lastTranslateX = 0;
            }
            if (axisCode == XboxOnePad.AXIS_LEFT_Y) {
                lastTranslateY = 0;
            }
        }
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }
}
