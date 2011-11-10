package theorb.GUI.Screens;

import de.lessvoid.nifty.controls.dynamic.CustomControlCreator;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import theorb.GUI.GUIScreen;
import theorb.Game;

/**
 *
 * @author m4tx1
 */
public class Outro extends GUIScreen implements KeyInputHandler {
    @Override
    public final void onStartScreen() {
        Element theEndLabel = screen.findElementByName("theEndLabel");
        if (theEndLabel != null) {
            theEndLabel.startEffect(EffectEventId.onCustom);
            theEndLabel.show();
        }

        Element myScrollStuff = screen.findElementByName("myScrollStuff");
        if (myScrollStuff != null) {
            CustomControlCreator endScroller = new CustomControlCreator("outro-content");
            endScroller.create(nifty, screen, myScrollStuff);
            myScrollStuff.startEffect(EffectEventId.onCustom);
        }
        
        Game.outroMusic.play();
    }

    public void returnToMainMenu() {
        Game.outroMusic.stop();
        nifty.gotoScreen("main-menu");
    }

    @Override
    public void onEndScreen() {
    }

    public boolean keyEvent(final NiftyInputEvent inputEvent) {
        if (inputEvent == NiftyInputEvent.Escape) {
            returnToMainMenu();
            return true;
        }
        return false;
    }
}
