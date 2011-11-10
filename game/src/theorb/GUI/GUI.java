package theorb.GUI;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.input.InputManager;
import com.jme3.renderer.ViewPort;

import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author m4tx1
 */
public class GUI implements ScreenController {

    public Nifty nifty;
    private Screen screen;

    public GUI(AssetManager assetManager, InputManager inputManager, AudioRenderer audioRenderer, ViewPort vp, ScreenController screenController) {
        init(assetManager, inputManager, audioRenderer, vp, screenController);
    }

    private void init(AssetManager assetManager, InputManager inputManager, AudioRenderer audioRenderer, ViewPort vp, ScreenController screenController) {
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, vp);
        /** Create a new NiftyGUI object */
        nifty = niftyDisplay.getNifty();
        /** Read your XML and initialize your custom ScreenController */
        if (screenController == null) {
            nifty.fromXml("Interface/interface.xml", "start", this);
        } else {
            nifty.fromXml("Interface/interface.xml", "start", screenController);
        }
        // attach the Nifty display to the gui view port as a processor
        vp.addProcessor(niftyDisplay);
    }

    public void gotoScreen(String id) {
        nifty.gotoScreen(id);
    }

    public String getCurrentScreenId() {
        return nifty.getCurrentScreen().getScreenId();
    }

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
