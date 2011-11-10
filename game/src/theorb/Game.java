package theorb;

import java.util.logging.Level;
import java.util.logging.Logger;
import theorb.GUI.GUI;
import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.app.StatsView;
import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.light.*;
import com.jme3.bullet.BulletAppState;
import com.jme3.scene.Node;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.ChaseCamera;
import com.jme3.math.FastMath;
import com.jme3.util.SkyFactory;
import de.lessvoid.nifty.EndNotify;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import theorb.GUI.Screens.InGame;
import theorb.Managers.LoadingScreenManager;
import theorb.Managers.MapManager;
import theorb.Map.MapList;

/**
 *
 * @author m4tx1
 */
public class Game extends Application {

    //<editor-fold defaultstate="collapsed" desc="Variables">
    // Config
    public static XML config;
    protected boolean showSettings = true;
    // Graphics
    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");
    //private PssmShadowRenderer pssmRenderer;
    private BulletAppState bulletAppState;
    private ChaseCamera chaseCam;
    boolean rotate = true;
    // Map
    static public Spatial[] block;
    static public Spatial[] collisionShape;
    static public Material[] blockMaterial;
    static public Spatial[] skies;
    static public AudioNode[] musics;
    // GUI
    private GUI gui;
    private String previousScreenId = "loading";
    // Light
    DirectionalLight sun;
    AmbientLight al;
    // Debug
    protected BitmapText fpsText;
    protected BitmapFont guiFont;
    protected StatsView statsView;
    // Orb
    private byte ballDirection; // -1: left; 0: none; 1: right
    public static Spatial orb;
    public static RigidBodyControlZLock orbPhys;
    // Others
    protected float secondCounter = 0.0f;
    static public AudioNode outroMusic;
    // Input
    java.util.Map pressedKeys = new HashMap();
    public ActionListener actionListener = new ActionListener() {
        
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("toggleMenu") && !keyPressed) {
                switch (gui.getCurrentScreenId()) {
                    case "in-game":
                        gui.gotoScreen("in-game-menu");
                        break;
                    case "in-game-menu":
                        gui.gotoScreen("in-game");
                        break;
                }
            }
            
            pressedKeys.put(name, keyPressed);
            
            if (pressedKeys.get("moveLeft") == (Object) true) {
                ballDirection = -1;
            }
            if (pressedKeys.get("moveRight") == (Object) true) {
                ballDirection = 1;
            }
            if ((pressedKeys.get("moveRight") == (Object) true
                    && pressedKeys.get("moveLeft") == (Object) true)
                    || (pressedKeys.get("moveRight") == (Object) false
                    && pressedKeys.get("moveLeft") == (Object) false)) {
                ballDirection = 0;
            }
            
            if (orbPhys != null) {
                if (ballDirection == -1) {
                    //bulletAppState.getPhysicsSpace().setGravity(new Vector3f(-10, -10, 0));
                    orbPhys.setGravity(new Vector3f(-10, -10, 0));
                }
                if (ballDirection == 0) {
                    //bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -10, 0));
                    orbPhys.setGravity(new Vector3f(0, -10, 0));
                }
                if (ballDirection == 1) {
                    //bulletAppState.getPhysicsSpace().setGravity(new Vector3f(10, -10, 0));
                    orbPhys.setGravity(new Vector3f(10, -10, 0));
                }
            }
        }
    };
    public AnalogListener analogListener = new AnalogListener() {
        
        public void onAnalog(String name, float value, float tpf) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    //</editor-fold>

    public Game() {
        super();
    }
    
    @Override
    public void start() {
        // set some default settings in-case
        // settings dialog is not shown
        boolean loadSettings = false;
        if (settings == null) {
            setSettings(new AppSettings(true));
            loadSettings = true;
        }

        // Try to load game icons
        try {
            settings.setIcons(new BufferedImage[]{
                        ImageIO.read(getClass().getResourceAsStream("/icon/icon-256.png")),
                        ImageIO.read(getClass().getResourceAsStream("/icon/icon-128.png")),
                        ImageIO.read(getClass().getResourceAsStream("/icon/icon-64.png")),
                        ImageIO.read(getClass().getResourceAsStream("/icon/icon-32.png")),
                        ImageIO.read(getClass().getResourceAsStream("/icon/icon-16.png"))});
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.WARNING, "Unable to load game icons", ex);
        }
        
        settings.setTitle("The Orb - display settings");

        // show settings dialog
        if (showSettings) {
            if (!JmeSystem.showSettingsDialog(settings, loadSettings)) {
                return;
            }
        }
        //re-setting settings they can have been merged from the registry.
        setSettings(settings);
        super.start();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        preInit();
        
        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        loadFPSText();
        loadStatsView();
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);
    }
    
    public void loadFPSText() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        fpsText = new BitmapText(guiFont, false);
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("FPS:");
        guiNode.attachChild(fpsText);
    }

    /**
     * Attaches Statistics View to guiNode and displays it on the screen
     * above FPS statistics line.
     */
    public void loadStatsView() {
        if (config.getContentOne("/config/debug/graphicEngine").equals("true")) {
            statsView = new StatsView("Statistics View", assetManager, renderer.getStatistics());
            // Move it up so it appears above fps text
            statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
            guiNode.attachChild(statsView);
        }
    }
    
    @Override
    public void update() {
        super.update(); // makes sure to execute AppTasks
        if (speed == 0 || paused) {
            return;
        }
        
        float tpf = timer.getTimePerFrame() * speed;
        
        secondCounter += timer.getTimePerFrame();
        int fps = (int) timer.getFrameRate();
        if (secondCounter >= 1.0f) {
            fpsText.setText("FPS: " + fps);
            secondCounter = 0.0f;
        }

        // update states
        stateManager.update(tpf);

        // simple update and root node
        updateGameState(tpf);
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        // render states
        stateManager.render(renderManager);
        renderManager.render(tpf, true);
        simpleRender(renderManager);
        stateManager.postRender();
    }

    //<editor-fold defaultstate="collapsed" desc="Initialization">
    public void preInit() {
        // Load config
        config = new XML();
        config.load("./Data/config.xml");

        // Init GUI
        gui = new GUI(assetManager, inputManager, audioRenderer, guiViewPort, null);
        gui.gotoScreen("loading");

        // Loading screen
        LoadingScreenManager.init(this, gui.nifty);
        
        LoadingScreenManager.setLoadCallable(loadingCallable);
        LoadingScreenManager.setPostLoadCallable(postLoadCallable);
        LoadingScreenManager.enable();
    }
    Callable<Void> loadingCallable = new Callable<Void>() {
        
        public Void call() {
            LoadingScreenManager.setProgress(0, "Loading models...");
            // Load models
            block = new Spatial[7];
            block[0] = assetManager.loadModel("Models/cube.j3o");
            block[1] = assetManager.loadModel("Models/end-of-level.j3o");
            block[2] = block[1].clone();
            block[2].rotate(0, 0, -FastMath.PI / 2);
            block[3] = assetManager.loadModel("Models/slope.j3o");
            block[4] = block[3].clone();
            block[4].rotate(0, 0, -FastMath.PI / 2);
            block[5] = block[3].clone();
            block[5].rotate(0, 0, FastMath.PI);
            block[6] = block[3].clone();
            block[6].rotate(0, 0, FastMath.PI / 2);
            
            collisionShape = new Spatial[2];
            collisionShape[0] = assetManager.loadModel("Models/end-of-level-collisionshape.j3o");
            collisionShape[1] = collisionShape[0].clone();
            collisionShape[1].rotate(0, 0, -FastMath.PI / 2);
            
            LoadingScreenManager.setProgress(0.125f, "Loading materials...");
            // Load materials
            blockMaterial = new Material[11];
            for (int i = 0; i < 11; i++) {
                blockMaterial[i] = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                blockMaterial[i].setFloat("Shininess", 5f);
            }
            LoadingScreenManager.setProgress(0.25f, "Loading textures...");
            blockMaterial[0].setTexture("DiffuseMap", assetManager.loadTexture("Textures/black.png"));
            blockMaterial[1].setTexture("DiffuseMap", assetManager.loadTexture("Textures/white.png"));
            blockMaterial[2].setTexture("DiffuseMap", assetManager.loadTexture("Textures/red.png"));
            blockMaterial[3].setTexture("DiffuseMap", assetManager.loadTexture("Textures/green.png"));
            blockMaterial[4].setTexture("DiffuseMap", assetManager.loadTexture("Textures/blue.png"));
            blockMaterial[5].setTexture("DiffuseMap", assetManager.loadTexture("Textures/pink.png"));
            blockMaterial[6].setTexture("DiffuseMap", assetManager.loadTexture("Textures/lightgreen.png"));
            blockMaterial[7].setTexture("DiffuseMap", assetManager.loadTexture("Textures/lightblue.png"));
            blockMaterial[8].setTexture("DiffuseMap", assetManager.loadTexture("Textures/orange.png"));
            blockMaterial[9].setTexture("DiffuseMap", assetManager.loadTexture("Textures/yellow.png"));
            blockMaterial[10].setTexture("DiffuseMap", assetManager.loadTexture("Textures/purple.png"));
            
            skies = new Spatial[8];
            skies[0] = SkyFactory.createSky(assetManager,
                    assetManager.loadTexture("Textures/skies/1/west.jpg"),
                    assetManager.loadTexture("Textures/skies/1/east.jpg"),
                    assetManager.loadTexture("Textures/skies/1/north.jpg"),
                    assetManager.loadTexture("Textures/skies/1/south.jpg"),
                    assetManager.loadTexture("Textures/skies/1/up.jpg"),
                    assetManager.loadTexture("Textures/skies/1/down.jpg"));
            skies[1] = SkyFactory.createSky(assetManager,
                    assetManager.loadTexture("Textures/skies/2/west.jpg"),
                    assetManager.loadTexture("Textures/skies/2/east.jpg"),
                    assetManager.loadTexture("Textures/skies/2/north.jpg"),
                    assetManager.loadTexture("Textures/skies/2/south.jpg"),
                    assetManager.loadTexture("Textures/skies/2/up.jpg"),
                    assetManager.loadTexture("Textures/skies/2/down.jpg"));
            skies[2] = SkyFactory.createSky(assetManager,
                    assetManager.loadTexture("Textures/skies/2/west.jpg"),
                    assetManager.loadTexture("Textures/skies/2/east.jpg"),
                    assetManager.loadTexture("Textures/skies/2/north.jpg"),
                    assetManager.loadTexture("Textures/skies/2/south.jpg"),
                    assetManager.loadTexture("Textures/skies/2/up.jpg"),
                    assetManager.loadTexture("Textures/skies/2/down.jpg"));
            skies[3] = SkyFactory.createSky(assetManager,
                    assetManager.loadTexture("Textures/skies/1/west.jpg"),
                    assetManager.loadTexture("Textures/skies/1/east.jpg"),
                    assetManager.loadTexture("Textures/skies/1/north.jpg"),
                    assetManager.loadTexture("Textures/skies/1/south.jpg"),
                    assetManager.loadTexture("Textures/skies/1/up.jpg"),
                    assetManager.loadTexture("Textures/skies/1/down.jpg"));
            skies[4] = SkyFactory.createSky(assetManager,
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"));
            skies[5] = SkyFactory.createSky(assetManager,
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"));
            skies[6] = SkyFactory.createSky(assetManager,
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"));
            skies[7] = SkyFactory.createSky(assetManager,
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"),
                    assetManager.loadTexture("Textures/skies/3/sky.png"));
            
            LoadingScreenManager.setProgress(0.375f, "Loading sounds...");
            musics = new AudioNode[8];
            musics[0] = new AudioNode(assetManager, "Sounds/music/01.ogg", true);
            musics[1] = new AudioNode(assetManager, "Sounds/music/02.ogg", true);
            musics[2] = new AudioNode(assetManager, "Sounds/music/03.ogg", true);
            musics[3] = new AudioNode(assetManager, "Sounds/music/04.ogg", true);
            musics[4] = new AudioNode(assetManager, "Sounds/music/05.ogg", true);
            musics[5] = new AudioNode(assetManager, "Sounds/music/06.ogg", true);
            musics[6] = new AudioNode(assetManager, "Sounds/music/07.ogg", true);
            musics[7] = new AudioNode(assetManager, "Sounds/music/08.ogg", true);
            
            outroMusic = new AudioNode(assetManager, "Sounds/credits_song.ogg", true);
            
            for (int i = 0; i < musics.length; i++) {
                musics[i].setVolume(0.25f);
                //musics[i].setLooping(true);
            }
            
            LoadingScreenManager.setProgress(0.5f, "Initializing physics...");
            // Init bullet
            bulletAppState = new BulletAppState();
            stateManager.attach(bulletAppState);
            if (config.getContentOne("/config/debug/physicEngine").equals("true")) {
                bulletAppState.getPhysicsSpace().enableDebug(assetManager);
            }
            
            LoadingScreenManager.setProgress(0.625f, "Creating light...");
            // Init light
            sun = new DirectionalLight();
            sun.setColor(ColorRGBA.White);
            sun.setDirection(new Vector3f(-1.13f, -5.13f, -5.13f).normalizeLocal());
            al = new AmbientLight();
            al.setColor(ColorRGBA.White.mult(0.5f));
            
            LoadingScreenManager.setProgress(0.75f, "Creating orb...");
            // Create an orb
            orb = assetManager.loadModel("Models/orb.j3o");
            Material mat_orb = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat_orb.setTexture("DiffuseMap", assetManager.loadTexture("Textures/orb.png"));
            mat_orb.setFloat("Shininess", 7.5f);
            orb.setMaterial(mat_orb);
            //orb.setShadowMode(ShadowMode.Cast);

            LoadingScreenManager.setProgress(0.875f, "Initializing...");
            MapManager.init(rootNode, bulletAppState);
            
            LoadingScreenManager.setProgress(1, "Loading complete.");
            
            return null;
        }
    };
    Callable<Void> postLoadCallable = new Callable<Void>() {
        
        public Void call() {
            gui.gotoScreen("main-menu");
            //rootNode.attachChild(terrain);
            init();
            return null;
        }
    };
    
    public void init() {
        // Init shadows
        //        pssmRenderer = new PssmShadowRenderer(assetManager, 2048, 3);
        //        pssmRenderer.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        //        pssmRenderer.setLambda(0.2f);
        //        pssmRenderer.setShadowIntensity(0.4f);
        //        pssmRenderer.setCompareMode(CompareMode.Software);
        //        pssmRenderer.setFilterMode(FilterMode.PCF8);
        //        pssmRenderer.displayDebug();
        //        viewPort.addProcessor(pssmRenderer);

        // Init light
        rootNode.addLight(sun);
        rootNode.addLight(al);

        // Add orb
        rootNode.attachChild(orb);

        // Add music
        for (int i = 0; i < musics.length; i++) {
            rootNode.attachChild(musics[i]);
        }

        // Enable a chase cam
        chaseCam = new ChaseCamera(cam, orb, inputManager);
        chaseCam.setInvertHorizontalAxis(true);
        chaseCam.setSmoothMotion(true);
        chaseCam.setTrailingEnabled(false);
        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        chaseCam.setMinDistance(5f);
        chaseCam.setMaxDistance(100f);
        cam.setFrustumPerspective(Integer.parseInt(config.getContentOne("/config/video/FOV")), (float) settings.getWidth() / (float) settings.getHeight(), .1f, 1000);
        
        registerInput();
    }
    //</editor-fold>

    public void registerInput() {
        inputManager.addMapping("moveRight", new KeyTrigger(keyInput.KEY_RIGHT), new KeyTrigger(keyInput.KEY_D));
        inputManager.addMapping("moveLeft", new KeyTrigger(keyInput.KEY_LEFT), new KeyTrigger(keyInput.KEY_A));
        inputManager.addMapping("toggleMenu", new KeyTrigger(keyInput.KEY_ESCAPE));
        inputManager.addListener(actionListener, "moveRight", "moveLeft", "toggleMenu");
    }
    
    public void updateGameState(float tpf) {
        LoadingScreenManager.updateLoadState();
        if (LoadingScreenManager.getIsDone()) {
            LoadingScreenManager.disable();
        }
        if (!previousScreenId.equals(gui.getCurrentScreenId())) {
            previousScreenId = gui.getCurrentScreenId();
            
            if (gui.getCurrentScreenId().equals("in-game")) {
                startSimulation();
            } else {
                stopSimulation();
            }
        }
        if (orbPhys != null) {
            if (MapManager.isOrbInEndOfMapBlock(orbPhys.getPhysicsLocation().x, orbPhys.getPhysicsLocation().y)) {
                if (!InGame.wonLevelShown) {
                    stopSimulation();
                    InGame.wonLevelShown = true;
                    gui.nifty.createPopupWithId("popupWon", "popupWon");
                    gui.nifty.showPopup(gui.nifty.getCurrentScreen(), "popupWon", null);
                }
            }
        }
    }
    
    public void simpleRender(RenderManager rm) {
    }
    
    public void startSimulation() {
        bulletAppState.setEnabled(true);
        chaseCam.setEnabled(true);
    }
    
    public void stopSimulation() {
        bulletAppState.setEnabled(false);
        chaseCam.setEnabled(false);
    }
}
