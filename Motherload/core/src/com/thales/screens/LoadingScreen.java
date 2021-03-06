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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.thales.controllers.XboxOnePad;

/**
 * Created by choco on 14/05/2016.
 */
public class LoadingScreen implements Screen, ControllerListener{

    private int screenWidth;
    private int screenHeight;
    private int excavatorCenterX;
    private int halfScreenHeight;
    private int topRightX;
    private int topRightY;

    private double gamepadAxisMovementThreshold = 0.2;

    private Vector2 centerPointVector;

    private float bucketTranslateRate = 1f;
    private float bucketRotateRate = 1f;
    private float bucketPositionY;
    private float lineThickness = 3;
    private float lineHalfLength = 10;
    private float rotationSpeed = 0.5f;
    private int bucketMinDistanceFromCab = 50;
    private int bucketMaxDistanceFromCab = 200;

    private float bucketNearIdealThreshold = 10;

    private Vector2 optimalPositionVector;

    private final SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera orthographicCamera;

    private Controller controller = null;

    private int truckId;
    private String truckIdString;
    BitmapFont timerFont;
    BitmapFont textFont;

    double angle = 0;

    float bucketTranslateAmount = 0;
    float bucketRotateAmount = 0;
    private String timeString;
    private float countDown= 20f;

    public LoadingScreen(SpriteBatch spriteBatch) {
        this.spriteBatch = spriteBatch;
    }

    @Override
    public void show() {
        Controllers.addListener(this);
        createFonts();
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        excavatorCenterX = screenWidth / 3;
        halfScreenHeight = screenHeight / 2;

        int leftSpacer = 10;
        topRightX = excavatorCenterX * 2;
        topRightY = halfScreenHeight / 2;

        centerPointVector = new Vector2(excavatorCenterX, halfScreenHeight);
        optimalPositionVector = new Vector2(excavatorCenterX - 130, halfScreenHeight + 30);

        bucketPositionY = excavatorCenterX;

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.setAutoShapeType(true);

        orthographicCamera = new OrthographicCamera(screenWidth, screenHeight);
        spriteBatch.setProjectionMatrix(orthographicCamera.combined);

        truckId = 42;
        truckIdString = "Truck ID: " + truckId;
    }

    private void createFonts() {
        FileHandle fontFile = Gdx.files.internal("arial.ttf");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        textFont = generator.generateFont(parameter);
        parameter.size = 72;
        timerFont = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void render(float delta) {
        handleInput();
        handleMovement();
        limitPositions();
        updateOptimalBucketPosition();

        orthographicCamera.update();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Timer
        spriteBatch.begin();
        timeString = calcTime();

        if (countDown <= 10)
        {
            timerFont.setColor(Color.RED);
        }

        timerFont.draw (spriteBatch, timeString, 200, 180);
        spriteBatch.end();
        // Dividing line
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(topRightX, screenHeight-topRightY, screenWidth, screenHeight -topRightY);
        shapeRenderer.end();

        // Truck ID
        spriteBatch.begin();
        textFont.draw (spriteBatch, truckIdString, 185, 90);
        spriteBatch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(0, 0, excavatorCenterX * 2, screenHeight);

        shapeRenderer.set(ShapeRenderer.ShapeType.Line);

        // Draw the cab
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(excavatorCenterX-15, halfScreenHeight-15, 30, 30);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(optimalPositionVector.x, optimalPositionVector.y, 20);


        // Draw the current arc of the arm
        shapeRenderer.circle(excavatorCenterX, halfScreenHeight, bucketPositionY - halfScreenHeight);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);

        if(isBucketNearOptimal())
        {
            shapeRenderer.setColor(Color.GREEN);
        }

        // Bucket
        int bucketWidth = 20;
        shapeRenderer.rect
                (excavatorCenterX - lineHalfLength, bucketPositionY - lineHalfLength,bucketWidth,bucketWidth);

//        if(controller != null)
//        {
//            shapeRenderer.setColor(Color.GREEN);
//            shapeRenderer.circle(20, 20, 5);
//        }

        shapeRenderer.end();
    }

    private String calcTime() {
        float delta = Gdx.graphics.getDeltaTime();
        countDown -= delta;

        if (countDown <= 0)
        {
            return "0:00";
        }

        int minutes = (int)countDown / 60;
        int seconds = (int)countDown % 60;
        return (minutes + ":" + String.format("%02d",(seconds)));
    }

    private void updateOptimalBucketPosition()
    {
        angle = (angle) * (Math.PI/180); // Convert to radians
        double rotatedX = Math.cos(angle) * (optimalPositionVector.x - centerPointVector.x) - Math.sin(angle) * (optimalPositionVector.y-centerPointVector.y) + centerPointVector.x;
        double rotatedY = Math.sin(angle) * (optimalPositionVector.x - centerPointVector.x) + Math.cos(angle) * (optimalPositionVector.y - centerPointVector.y) + centerPointVector.y;
        optimalPositionVector.set((float)rotatedX, (float)rotatedY);
    }

    private boolean isBucketNearOptimal()
    {
        return Math.abs(bucketPositionY - optimalPositionVector.y) < bucketNearIdealThreshold &&
                Math.abs(optimalPositionVector.x - excavatorCenterX) < bucketNearIdealThreshold;
    }

    public void handleInput()
    {
        if(this.controller == null) {
            for (Controller controller : Controllers.getControllers()) {
                if (this.controller == null) {
                    this.controller = controller;
                    this.controller.addListener(this);
                }
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            angle--;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            angle++;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            bucketPositionY -= bucketTranslateRate;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            bucketPositionY += bucketTranslateRate;
        }
    }

    private void handleMovement()
    {
        bucketPositionY += bucketTranslateRate * bucketTranslateAmount;
        angle += bucketRotateRate * bucketRotateAmount;
    }

    private void limitPositions()
    {
        bucketPositionY = bucketPositionY < halfScreenHeight + bucketMinDistanceFromCab ? halfScreenHeight + bucketMinDistanceFromCab : bucketPositionY;
        bucketPositionY = bucketPositionY > halfScreenHeight + bucketMaxDistanceFromCab ? halfScreenHeight + bucketMaxDistanceFromCab : bucketPositionY;
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
        Controllers.clearListeners();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void connected(Controller controller) {
        if(this.controller == null)
        {
            this.controller = controller;
        }
    }

    @Override
    public void disconnected(Controller controller) {
            this.controller = null;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
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
            if (axisCode == XboxOnePad.AXIS_LEFT_Y) {
                bucketTranslateAmount = -value;
            }
            if (axisCode == XboxOnePad.AXIS_LEFT_X) {
                bucketRotateAmount = value;
            }

        }
        else
        {
            if (axisCode == 0) {
                bucketTranslateAmount = 0;
            }
            if(axisCode == 1)
            {
                bucketRotateAmount = 0;
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
