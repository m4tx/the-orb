package theorb.GUI.Screens;

import de.lessvoid.nifty.EndNotify;
import theorb.GUI.GUIScreen;
import theorb.Game;
import theorb.Managers.MapManager;

/**
 *
 * @author m4tx1
 */
public class InGameMenu extends GUIScreen {
    // 0 - exit, 1 - main menu

    int popupAction;

    public void backToTheGame() {
        nifty.gotoScreen("in-game");
    }

    public void mainMenu() {
        popupAction = 1;
        nifty.createPopupWithId("popupExit", "popupExit");
        nifty.showPopup(screen, "popupExit", null);
    }

    public void quit() {
        popupAction = 0;
        nifty.createPopupWithId("popupExit", "popupExit");
        nifty.showPopup(screen, "popupExit", null);
    }

    public void popupExit(final String exit) {
        nifty.closePopup("popupExit", new EndNotify() {

            public void perform() {
                if ("yes".equals(exit)) {
                    if (popupAction == 0) {
                        nifty.exit();
                        System.exit(0);
                    } else if (popupAction == 1) {
                        nifty.gotoScreen("main-menu");
                    }

                    Game.musics[MapManager.map.getTheme()].stop();
                }
            }
        });
    }
}
