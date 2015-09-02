package com.omegaraven.chisel.screens;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.mappings.Ouya;
import com.omegaraven.chisel.ChiselGame;
import com.omegaraven.chisel.gameobjects.EnemyObject;
import com.omegaraven.chisel.gameobjects.PlayerTest;
import com.omegaraven.chisel.helpers.ScoreController;

// ***************************************************************************
// Additional Self Notes:
// At this time there is the controller library imported into the
// project. This was added for Ouya Testing.
// I am leaving it for now while I work on finishing the game.
// If I decide to port into Ouya I will need to implement the controllers.
// ***************************************************************************

public class GameScreen implements Screen, InputProcessor {

    final ChiselGame game;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    // Skipping some here and will try to implement in player.

    private PlayerTest player;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();
    private final float GRAVITY = -2.5f;
    private float mouseAngle;

    // Enemies Mod
    private ArrayList<EnemyObject> enemies = new ArrayList<EnemyObject>();


    // Attempting Score Work
    private ScoreController scoreController;

    public GameScreen(final ChiselGame game, String selectedMap){
        this.game = game;
        mouseAngle = 0f;
        System.out.println(selectedMap);
        // Score Mod
        scoreController = new ScoreController(selectedMap);

        Gdx.input.setInputProcessor(this);
        // Stopped here need to define width and height inside the class then instantiate from here.

        player = new PlayerTest(scoreController);
        map = new TmxMapLoader().load(selectedMap);
        renderer = new OrthogonalTiledMapRenderer(map, 1/16f);

        // Will need to pull the properties from the map here and then
        // grab what is needed to set the players position.

        int playerx = Integer.valueOf(map.getProperties().get("startx", String.class));
        int playery = Integer.valueOf(map.getProperties().get("starty", String.class));

        // Need ot build a tool to parse the properties of the map into the proper fields
        for (Iterator<String> iter = map.getProperties().getKeys(); iter.hasNext();)
        {
           String current = iter.next();
           if (current.startsWith("enemy_")){
        	   System.out.println("Enemy found");
        	   String enemy_type = map.getProperties().get(current, String.class);
        	   // need to fish out x_enemy_1 hopefully "x_"+current works
        	   float enemy_x = Float.valueOf(map.getProperties().get("x_" + current, String.class));
        	   float enemy_y = Float.valueOf(map.getProperties().get("y_" + current, String.class));
        	   enemies.add(new EnemyObject(enemy_type, enemy_x, enemy_y));
           }
        }

        for (EnemyObject e : enemies){
        	System.out.println(e.getType());
        }

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();
        player.setIsAlive(true);
        player.setPosition(playerx,playery);
    }

    @Override
    public void render(float delta){
//    	initialize();
        Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mouseMoved(Gdx.input.getX(), Gdx.input.getY());
        player.update(delta, rectPool, tiles, map, mouseAngle);

        camera.position.x = player.getPosition().x;
        camera.update();

        renderer.setView(camera);
        renderer.render();

        for (EnemyObject e : enemies){
        	e.update();
        	e.renderEnemy(delta, renderer);
        }

        player.renderPlayer(delta, renderer);
        if(player.getIsAlive() == false){
        	player.cleanUp();
        	game.setScreen(new GameOverScreen(game));
        }
        if(player.getHasWon() == true){
        	player.cleanUp();
        	game.setScreen(new WinScreen(game, scoreController));
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    /*
     * With the following show hide pause and resume()
     * these will need to be fully implemented before Android Phone release.
     * Ouya seems to still be stable without them.
     */
    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean keyDown(int keycode) {
        switch(keycode) {
            case Input.Keys.LEFT:
            case Input.Keys.A:
                player.moveLeft();
                break;
            case Input.Keys.RIGHT:
            case Input.Keys.D:
                player.moveRight();
                break;
        }
        //System.out.println("The Activate KeyCode: " + keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode){
            case Input.Keys.LEFT:
            case Input.Keys.A:
                player.stopLeft();
                break;
            case Input.Keys.RIGHT:
            case Input.Keys.D:
                player.stopRight();
                break;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // TODO implement works for touchscreen devices.
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // TODO implement works for touchscreen devices.
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO implement works for touchscreen devices.
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector2 centerPosition = new Vector2(player.getPosition().x + 0.5f, player.getPosition().y + 0.5f);
        Vector3 worldCoordinates = new Vector3(screenX, screenY, 0);
        camera.unproject(worldCoordinates);
        Vector2 mouseLoc = new Vector2(worldCoordinates.x, worldCoordinates.y);
        Vector2 direction = mouseLoc.sub(centerPosition);
        mouseAngle = direction.angle();
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

}
