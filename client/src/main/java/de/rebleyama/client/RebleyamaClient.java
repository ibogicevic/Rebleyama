package de.rebleyama.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import de.rebleyama.lib.utils.TileStructureGenerator;
import de.rebleyama.lib.game.Tile;

import java.util.List;

import java.util.logging.*;


public class RebleyamaClient extends ApplicationAdapter implements InputProcessor {
    
    private static final int TILESIZE = 40;
    private static final int MAPSIZE_TILES = 512;
    private static final int MAPSIZE_PIXELS = TILESIZE * MAPSIZE_TILES;    
    private static final String COORDINATE_LOGGER = "Coordinate Logger";

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private TiledMapRenderer tiledMapRenderer;
    private TiledMap tiledMap;
    private List<List<Tile>> interactionMap;

	@Override
	public void create () {

        ClientLogger.setLevel(Level.INFO);
        ClientLogger.addHandler(console);

		batch = new SpriteBatch();

		//get the window size for the camera
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		//initiate the camera
		camera = new OrthographicCamera();
        camera.setToOrtho(false,w,h);
		camera.update();
        camera.position.x = 10240;
        camera.position.y = 10240;


        //load the map
        //also available: ../client/assets/custommaps/testMap.tmx
        tiledMap = new TmxMapLoader().load("../client/assets/custommaps/default.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        interactionMap = TileStructureGenerator.generateTiles(tiledMap);
        
        Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render() {
        //prevent camera from going out of bounds
        stayInBounds();
        //handle Input
        handleMouseMovementInput();
        handleKeyMovementInput();
        handleKeyZoomInput();
		//clear background
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined);

		//update the camera
        camera.update();

		//start up map renderer
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
    }
    
/**
 * Stub method for recognizing keypress
 * This triggers when any key is pressed down
 * @param keycode keycode of the key that was pressed
 */
	@Override
    public boolean keyDown(int keycode) {
        return false;
    }

/**
 * Stub method for recognizing keypress
 * This triggers when any key is released
 * @param keycode keycode of the key that was released
 */
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

/**
 * Stub method for recognizing keypress
 * This triggers when any key is typed (pressed and released)
 * @param character character that was typed
 */
    @Override
    public boolean keyTyped(char character) {
        return false;
    }

/**
 * Stub method for recognizing keypress
 * This triggers when screen is touched, also the method that handles mouse input
 * 
 */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        //If left mouse button is clicked
        if (button == Input.Buttons.LEFT) {
            onLeftMouseDown(screenX, screenY);
            return true;
        }
        return false;
    }

/**
 * Stub method for recognizing keypress
 * This triggers when screen is released
 */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

/**
 * Stub method for recognizing keypress
 * This triggers when the finger is dragged across the screen
 */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

/**
 * Stub method for recognizing keypress
 * This triggers when the mouse is moved
 */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
   
/**
 * Stub method for recognizing keypress
 * This triggers when the scrollwheel is scrolled
 * @param amount amount that the scrollwheel was moved
 */
    @Override
    public boolean scrolled(int amount) {
        /*check if the area shown on screen is still within the minimum (480 px -> 12 fields) and the
        maximum (20480 px -> 512 fields) bounds. If it is not, camera is not zoomed unless the zoom amount
        is going to change the effective area shown away from the bound 
        */
        if ((((camera.viewportWidth * (camera.zoom + 0.3*amount)) < MAPSIZE_PIXELS) || amount < 0) && (((camera.viewportWidth * (camera.zoom + 0.3*amount)) > 480) || amount > 0)) {
            camera.zoom += (0.3*amount);
            return true;
        }

        return false;
    }

    /**
     * dispose of all the native resources we allocated in create()
     */
	@Override
	public void dispose() {
        tiledMap.dispose();
		batch.dispose();
    }
    
    private void handleMouseMovementInput() {
        float mousePositionX = Gdx.input.getX(); 
        float mousePositionY = Gdx.input.getY();
        float windowWidth = Gdx.graphics.getWidth();
        float windowHeight = Gdx.graphics.getHeight();

        //if mouse is in a 5% range of any of the edges of the window, move the camera accordingly
        if (mousePositionY <= windowHeight/20) {
            camera.translate(0,25*camera.zoom);
        }
        if (mousePositionY >= windowHeight - windowHeight/20) {
            camera.translate(0,-25*camera.zoom);
        }
        if (mousePositionX >= windowWidth - windowWidth/20) {
            camera.translate(25*camera.zoom,0);
        }
        if (mousePositionX <= windowWidth/20) {
            camera.translate(-25*camera.zoom,0);
        }
    }

    private void handleKeyMovementInput() {
        //if key is pressed, move camera accordingly
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-25*camera.zoom,0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(25*camera.zoom,0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0,-25*camera.zoom);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0,25*camera.zoom);
        }
    }

    private void handleKeyZoomInput() {
       /*check if the area shown on screen is still within the minimum (480 px -> 12 fields) and the
        maximum (20480 px -> 512 fields) bounds. If it is not, camera is not zoomed unless the zoom amount
        is going to change the effective area shown away from the bound 
        */
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS) && ((camera.viewportWidth * (camera.zoom + 0.1)) > 480)) {
            camera.zoom -= 0.1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS) && ((camera.viewportWidth * (camera.zoom + 0.1)) < MAPSIZE_PIXELS)) {
            camera.zoom += 0.1;
        }
    }

    private void stayInBounds() {
        //calculate the effective area of the map that is shown on screen
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
		float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        /*Check if camera position is at any coordinate out of the boundaries of our 512x512 tile map.
        If it is, set it to the corresponding edge. Since this method is called in the render() method,
        this is checked for every frame.
        */
        if (camera.position.x < 0 + effectiveViewportWidth/2) {
            camera.position.x = 0 + effectiveViewportWidth/2;
        }
        if (camera.position.x > MAPSIZE_PIXELS - effectiveViewportWidth/2) {
            camera.position.x = MAPSIZE_PIXELS - effectiveViewportWidth/2;
        }
        if (camera.position.y < 0 + effectiveViewportHeight/2) {
            camera.position.y = 0 + effectiveViewportHeight/2;
        }
        if (camera.position.y > MAPSIZE_PIXELS - effectiveViewportHeight/2) {
            camera.position.y = MAPSIZE_PIXELS - effectiveViewportHeight/2;
        }
    }

    private void onLeftMouseDown(int mousePositionX, int mousePositionY) {
        Gdx.app.log(COORDINATE_LOGGER, "Camera Tile (X): " + camera.position.x/40);
        Gdx.app.log(COORDINATE_LOGGER, "Mouse (X): " + mousePositionX);
        Gdx.app.log(COORDINATE_LOGGER, "Camera Tile (Y): " + camera.position.y/40);
        Gdx.app.log(COORDINATE_LOGGER, "Mouse (Y): " + mousePositionY);
    }
}
