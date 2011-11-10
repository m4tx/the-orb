package theorb.GUI.Screens;

import de.lessvoid.nifty.EndNotify;
import theorb.GUI.GUIScreen;
import theorb.Game;
import theorb.Managers.MapManager;
import theorb.Map.MapList;

/**
 *
 * @author m4tx1
 */
public class InGame extends GUIScreen {

    public static boolean wonLevelShown = false;
    public static int mapType;

    public void popupWon(final String exit) {
        nifty.closePopup("popupWon", new EndNotify() {

            public void perform() {
                wonLevelShown = false;
                if (mapType == 1 || MainMenu.currentLevel == 13) {
                    nifty.gotoScreen("main-menu");
                } else if (mapType == 0) {
                    if (MainMenu.currentLevel == Integer.parseInt(Game.config.getContentOne("/config/userProgress/lastWonLevel"))) {
                        Game.config.setContentOne("/config/userProgress/lastWonLevel", Integer.toString(Integer.parseInt(Game.config.getContentOne("/config/userProgress/lastWonLevel")) + 1));
                        Game.config.save();
                        Game.config.load("./Data/config.xml");
                    }
                    MainMenu.currentLevel++;
                    MapManager.loadMap("./Data/Maps/" + MapList.mapFilename[MainMenu.currentLevel]);
                    Game.musics[MapManager.map.getTheme()].stop();
                }
            }
        });
    }
}
