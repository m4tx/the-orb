/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package theorb.GUI;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author m4tx1
 */
public class GUIScreen implements ScreenController {
    public Nifty nifty;
    public Screen screen;

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
