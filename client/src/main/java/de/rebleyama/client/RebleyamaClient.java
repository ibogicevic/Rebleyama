package de.rebleyama.client;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Image;


public class RebleyamaClient extends ApplicationAdapter implements InputProcessor {

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMapRenderer tiledMapRenderer;
    private TiledMap tiledMap;

    //Global Variable of window for Mini,ap, UI Skin, UI Stage and Pixmap for Minimap
    private Stage stage;
    private Skin skin;
    private Pixmap pixmap;
    private Window mapwindow;


    @Override
    public void create() {
        batch = new SpriteBatch();

        //get the window size for the camera
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        //initiate the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);
        camera.update();
        camera.position.x = 10240;
        camera.position.y = 10240;


        //load the map
        //also available: ../client/assets/custommaps/testMap.tmx
        tiledMap = new TmxMapLoader().load("../client/assets/custommaps/default.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        //Calls method that is responsible to create UI elements
        createUI();
        //Creation of a Multiplexer which allows multi layer event handling (UI Layer and TiledMap Layer) (UI layer needs to be first ORDER IS IMPORTANT)
        InputMultiplexer im = new InputMultiplexer(stage, this);
        Gdx.input.setInputProcessor(im);

    }

    /**
     * Create Method for UI Elements
     */
    private void createUI() {
        //TODO NOTES
        // MAP ALL INPUTS TO INPUT PROCESSOR - raise issue
        // where to find skins - raise issue
        // processing of events done -> return true else retun false
        // correcte farb werte von paul bekommen
        // render minimap every time new... (record changes)(make pixmal global etc.)
        // keep aspect ratio maybe later
        // enum

        //setup skin, stage, input
        skin = new Skin(Gdx.files.internal("assets/uiskin/skin/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        //Create UI Elements here

        // Calls method that creates the minimap
        createMinimap();


    }

    /**
     * Creates a windows with a minimap in it
     */
    private void createMinimap() {
        //create window
        mapwindow = new Window("Map", skin);
        //set postion of window (-size)
        mapwindow.setPosition(Gdx.graphics.getWidth()-200, Gdx.graphics.getHeight()-200);
        //set size of window
        mapwindow.setSize(200, 200);
        //allow the window to be resized
        mapwindow.setResizable(true);
        //call method that creates a pixmap of our tiledmap
        pixmap = createPixmap();
        //fill inside of window with minimap
        mapwindow.add(new Image(new TextureRegion(new Texture(pixmap))));
        //add minimap to ui stage
        stage.addActor(mapwindow);
    }

    /**
     * creates a pixmap of our tiledmap
     * A pixmap is similar to a bitmap or bufferedimage
     * @return Pixmap of our Tiledmap
     */
    private Pixmap createPixmap() {
        //create Pixmap
        pixmap = new Pixmap(512, 512, Pixmap.Format.RGBA8888);
        //get our tiledMap layer
        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        //loop through all map (for each tile)
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                //get current cell id
                int tmp = layer.getCell(x, y).getTile().getId();
                //set color of pixmap pixel similar to the color of the tile on our tiledmap
                if (tmp != -1) {
                    //TODO replace with dict/enum like access list
                    switch (tmp) {
                        case 1:
                            pixmap.drawPixel(x, y, Color.rgba8888(Color.DARK_GRAY));
                            break;
                        case 2:
                            pixmap.drawPixel(x, y, Color.rgba8888(Color.FOREST));
                            break;
                        case 3:
                            pixmap.drawPixel(x, y, Color.rgba8888(Color.LIGHT_GRAY));
                            break;
                        case 4:
                            pixmap.drawPixel(x, y, Color.rgba8888(Color.GRAY));
                            break;
                        case 5:
                            pixmap.drawPixel(x, y, Color.rgba8888(Color.BLUE));
                            break;
                        default:
                            pixmap.drawPixel(x, y, Color.rgba8888(Color.ORANGE));
                            break;
                    }

                }

            }

        }
        //return pixmap
        return pixmap;

    }

    @Override
    public void render() {
        //prevent camera from going out of bounds
        stayInBounds();
        //handle Input
        handleKeyMovementInput();
        handleKeyZoomInput();
        handleMouseMovementInput();

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

        // MiniMap Test 1
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    /**
     * Resize event which triggers on size change of client window
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        //calls method to resize for Minimap window (can be used for other too)
        resizeWindow(width,height);
        //update viewport of stage
        stage.getViewport().update(width, height, true);


    }

    /**
     * resize ui Stage and positions minimap window relative to last position on new window size
     * @param width after resize
     * @param height after resize
     */
    public void resizeWindow(int width, int height){
        //get old client window height (-size of window for "correct" coordinate system)
        float oldheight = stage.getViewport().getScreenHeight()-mapwindow.getHeight();
        float oldwidth = stage.getViewport().getScreenWidth()-mapwindow.getWidth();
        //get old position of minimap window
        float oldheightWindow = mapwindow.getY();
        float oldwidthWindow = mapwindow.getX();
        //calculate old relative position on screen
        float relativheight = oldheightWindow/oldheight;
        float relativwidth = oldwidthWindow/oldwidth;
        //calculate new position after resize
        float newheightWindow = (height-200) * relativheight;
        float newwidthWindow = (width-200) * relativwidth;
        //set new position
        mapwindow.setPosition(newwidthWindow,newheightWindow);
    }

    /**
     * dispose of all the native resources we allocated in create()
     */
    @Override
    public void dispose() {
        tiledMap.dispose();
        batch.dispose();
        skin.dispose();
        stage.dispose();
        pixmap.dispose();

    }

    /**
     * Stub method for recognizing keypress
     * This triggers when any key is pressed down
     *
     * @param keycode keycode of the key that was pressed
     */
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    /**
     * Stub method for recognizing keypress
     * This triggers when any key is released
     *
     * @param keycode keycode of the key that was released
     */
    @Override
    public boolean keyUp(int keycode) {
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
     * This triggers when any key is typed (pressed and released)
     *
     * @param character character that was typed
     */
    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     * Stub method for recognizing keypress
     * This triggers when screen is touched
     */
    int testn;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            testn += 1;
            Gdx.app.log("Click", "Clicked" + testn);
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
     * This triggers when the scrollwheel is scrolled
     *
     * @param amount amount that the scrollwheel was moved
     */
    @Override
    public boolean scrolled(int amount) {
        /*calculate the effective area of the map that is shown on screen (only calculating width
        because it will always be larger than height as soon as we enforce 16:9 aspect ration)*/
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;

        /*check if the area shown on screen is still within the minimum (480 px -> 12 fields) and the
        maximum (20480 px -> 512 fields) bounds. If it is not, camera is not zoomed unless the zoom amount
        is going to change the effective area shown away from the bound
        */
        if (((effectiveViewportWidth < 20480) || amount < 0) && ((effectiveViewportWidth > 480) || amount > 0)) {
            camera.zoom += (0.3 * amount);
            return true;
        }

        return false;
    }

    private void handleKeyMovementInput() {
        //if key is pressed, move camera accordingly
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-25, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(25, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0, -25);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0, 25);
        }
    }

    private void handleMouseMovementInput() {
        float mousePositionX = Gdx.input.getX();
        float mousePositionY = Gdx.input.getY();
        float windowWidth = Gdx.graphics.getWidth();
        float windowHeight = Gdx.graphics.getHeight();

        //if mouse is in a 5% range of any of the edges of the window, move the camera accordingly
        if (mousePositionY <= windowHeight / 20) {
            camera.translate(0, 25 * camera.zoom);
        }
        if (mousePositionY >= windowHeight - windowHeight / 20) {
            camera.translate(0, -25 * camera.zoom);
        }
        if (mousePositionX >= windowWidth - windowWidth / 20) {
            camera.translate(25 * camera.zoom, 0);
        }
        if (mousePositionX <= windowWidth / 20) {
            camera.translate(-25 * camera.zoom, 0);
        }
    }

    private void handleKeyZoomInput() {
        /*calculate the effective area of the map that is shown on screen (only calculating width
        because it will always be larger than height as soon as we enforce 16:9 aspect ration)*/
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;

        /*check if the area shown on screen is still within the minimum (480 px -> 12 fields) and the
        maximum (20480 px -> 512 fields) bounds. If it is not, camera is not zoomed unless the zoom amount
        is going to change the effective area shown away from the bound
        */
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS) && (effectiveViewportWidth > 480)) {
            camera.zoom -= 0.1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS) && (effectiveViewportWidth < 20480)) {
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
        if (camera.position.x < 0 + effectiveViewportWidth / 2) {
            camera.position.x = 0 + effectiveViewportWidth / 2;
        }
        if (camera.position.x > 20480 - effectiveViewportWidth / 2) {
            camera.position.x = 20480 - effectiveViewportWidth / 2;
        }
        if (camera.position.y < 0 + effectiveViewportHeight / 2) {
            camera.position.y = 0 + effectiveViewportHeight / 2;
        }
        if (camera.position.y > 20480 - effectiveViewportHeight / 2) {
            camera.position.y = 20480 - effectiveViewportHeight / 2;
        }
    }
}
