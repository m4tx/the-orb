package theorb.Managers;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import theorb.Game;
import theorb.Map.BlockId;
import theorb.Map.Map;
import theorb.RigidBodyControlZLock;
import theorb.Vector2b;

/**
 *
 * @author m4tx1
 */
public class MapManager {

    public static Map map;
    protected static Node rootNode;
    private static BulletAppState bulletAppState;
    private static String mapFilename;
    private static ArrayList<Spatial> spatials = new ArrayList<>();
    private static ArrayList<Vector2b> endOfMapBlocks = new ArrayList<>();

    public static void init(Node node, BulletAppState bulletAppSt) {
        rootNode = node;
        bulletAppState = bulletAppSt;
    }

    public static void loadMap(String filename) {
        //rootNode.getChildren().clear();
        for (int i = 0; i < spatials.size(); i++) {
            rootNode.detachChild(spatials.get(i));
        }
        spatials.clear();
        endOfMapBlocks.clear();
        LoadingScreenManager.setLoadCallable(loadingCallable);
        LoadingScreenManager.setPostLoadCallable(postLoadCallable);
        mapFilename = filename;
        LoadingScreenManager.enable();

    }
    private static Callable<Void> loadingCallable = new Callable<Void>() {

        public Void call() {
            LoadingScreenManager.setProgress(0, "Initializing memory for a map...");
            map = new Map();
            bulletAppState.getPhysicsSpace().destroy();
            bulletAppState.getPhysicsSpace().create();
            LoadingScreenManager.setProgress(0.33f, "Loading map file...");
            try {
                map.load(mapFilename);
            } catch (IOException ex) {
                Logger.getLogger(MapManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(MapManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            LoadingScreenManager.setProgress(0.66f, "Loading map...");
            drawMap();
            addOrb();
            getEndOfMapBlocks();
            LoadingScreenManager.setProgress(1, "Loading complete.");

            return null;
        }
    };
    private static Callable<Void> postLoadCallable = new Callable<Void>() {

        public Void call() {
            // Clear a rootNode


            // Add blocks
            for (int i = 0; i < spatials.size(); i++) {
                rootNode.attachChild(spatials.get(i));
            }
            //spatials.clear();

            // Add skybox
            rootNode.attachChild(Game.skies[map.getTheme()]);

            // Play a music
            Game.musics[map.getTheme()].play();

            // Go to in-game screen
            LoadingScreenManager.nifty.gotoScreen("in-game");

            return null;
        }
    };

    private static void drawMap() {
        byte blockId;
        for (short x = 0; x < 255; x++) {
            for (short y = 0; y < 255; y++) {
                if ((blockId = map.getBlock(x, y)) != -1) {
                    Spatial tempBlock = Game.block[BlockId.getModelId(blockId)].clone();
                    tempBlock.setMaterial(Game.blockMaterial[BlockId.getTextureId(blockId)]);
                    tempBlock.move(x * 2, -y * 2, 0);
                    spatials.add(tempBlock);

                    // Create a appropriate physical shape for it
                    CollisionShape blockShape;
                    if (BlockId.getModelId(blockId) == 0) {
                        blockShape = new BoxCollisionShape(new Vector3f(1.f, 1.f, 1.f));
                    } else if (BlockId.getModelId(blockId) == 1 || BlockId.getModelId(blockId) == 2) {
                        blockShape = CollisionShapeFactory.createMeshShape(Game.collisionShape[BlockId.getModelId(blockId) - 1]);
                    } else {
                        blockShape = CollisionShapeFactory.createMeshShape(tempBlock);
                    }
                    RigidBodyControl blockPhys = new RigidBodyControl(blockShape, 0f);
                    // Attach physical properties to model and PhysicsSpace
                    tempBlock.addControl(blockPhys);
                    bulletAppState.getPhysicsSpace().add(blockPhys);
                }
            }
        }
    }

    private static void getEndOfMapBlocks() {
        byte tempBlockId;
        for (short x = 0; x < 256; x++) {
            for (short y = 0; y < 256; y++) {
                if ((tempBlockId = map.getBlock(x, y)) >= 11 && tempBlockId <= 32) {
                    Vector2b temp = new Vector2b();
                    temp.x = (byte) x;
                    temp.y = (byte) y;
                    endOfMapBlocks.add(temp);
                }
            }
        }
    }

    public static boolean isOrbInEndOfMapBlock(float orbPosX, float orbPosY) {
        for (int i = 0; i < endOfMapBlocks.size(); i++) {
            if (orbPosX > endOfMapBlocks.get(i).x * 2 - 0.1 && orbPosX < (endOfMapBlocks.get(i).x + 1) * 2 + 0.1
                    && orbPosY > (-endOfMapBlocks.get(i).y - 1) * 2 - 0.1 && orbPosY < -endOfMapBlocks.get(i).y * 2 + 0.1) {
                return true;
            }
        }
        return false;
    }

    private static void addOrb() {
        SphereCollisionShape orbShape = new SphereCollisionShape(0.999f);
        Game.orbPhys = new RigidBodyControlZLock(1.5f);
        Game.orbPhys.setCollisionShape(orbShape);
        Game.orbPhys.setFriction(10);
        Game.orb.setLocalTranslation(map.getSpawnpoint().x * 2, -map.getSpawnpoint().y * 2, 0);
        // Attach physical properties to model and PhysicsSpace
        Game.orb.addControl(Game.orbPhys);
        bulletAppState.getPhysicsSpace().add(Game.orbPhys);
    }
}
