package theorb.GUI.Screens;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.tools.SizeValue;
import java.util.Arrays;
import theorb.FileListing;
import theorb.GUI.GUIScreen;
import theorb.Game;
import theorb.Managers.MapManager;
import theorb.Map.MapList;

/**
 *
 * @author m4tx1
 */
public class MainMenu extends GUIScreen {

    private String currentDir = "";
    private int popupExitTodo;
    
    public static int currentLevel = 0;

    @Override
    public void onStartScreen() {
        removeAllMenuItems();
        addMenuItem("Single player", "singlePlayer", "singlePlayer()");
        addMenuItem("Credits", "credits", "credits()");
        addMenuItem("Quit", "quit", "quit()");

        refreshMenu();
    }

    public void singlePlayer() {
        removeAllMenuItems();
        addMenuItem("New game", "newGame", "newGame()");
        if (!Game.config.getContentOne("/config/userProgress/lastWonLevel").equals("0")) {
            addMenuItem("Load game", "loadGame", "loadGame()");
        }
        addMenuItem("Choose level", "chooseLevel", "chooseLevel()");
        addMenuItem("Back", "back", "onStartScreen()");

        refreshMenu();
    }

    public void newGame() {
        popupExitTodo = 1;
        nifty.createPopupWithId("popupExit", "popupExit");
        nifty.showPopup(screen, "popupExit", null);
    }

    public void loadGame() {
        runMap(Game.config.getContentOne("/config/userProgress/lastWonLevel"));
    }

    public void chooseLevel() {
        removeAllMenuItems();
        addMenuItem("Back", "back", "singlePlayer()");
        addMenuItem("Other", "other", "chooseMapOtherMain()");

        for (int i = 0; i < Integer.parseInt(Game.config.getContentOne("/config/userProgress/lastWonLevel")) + 1; i++) {
            addMenuItem(Integer.toString(i + 1) + " - " + MapList.map[i], Integer.toString(i), "runMap(" + Integer.toString(i) + ")");
        }

        nifty.executeEndOfFrameElementActions();
    }

    public void credits() {
        nifty.gotoScreen("outro");
    }

    public void quit() {
        popupExitTodo = 0;
        nifty.createPopupWithId("popupExit", "popupExit");
        nifty.showPopup(screen, "popupExit", null);
    }

    public void chooseMapOtherMain() {
        chooseMapOther("");
    }

    public void chooseMapOther(String id) {
        if (id.equals("-1")) {
            currentDir = currentDir.substring(0, currentDir.lastIndexOf("/"));
        } else if (!id.equals("")) {
            String temp = nifty.getCurrentScreen().findElementByName("dir" + id).getRenderer(TextRenderer.class).getOriginalText();
            currentDir += "/" + temp.substring(1, temp.length() - 1);
        }

        removeAllMenuItems();

        if (currentDir.equals("")) {
            addMenuItem("Back", "back", "chooseLevel()");
        } else {
            addMenuItem("[..]", "dir-1", "chooseMapOther(-1)");
        }

        String[] temp = FileListing.listFiles("./Data/Maps/Other/" + currentDir, true, false);
        Arrays.sort(temp);
        for (int i = 0; i < temp.length; i++) {
            addMenuItem("[" + temp[i] + "]", "dir" + Integer.toString(i), "chooseMapOther(" + Integer.toString(i) + ")");
        }

        temp = FileListing.listFiles("./Data/Maps/Other/" + currentDir, false, true);
        Arrays.sort(temp);
        for (int i = 0; i < temp.length; i++) {
            addMenuItem(temp[i].substring(0, temp[i].length() - 6) /* Remove .tomap */, "map" + Integer.toString(i), "runMapOther(" + Integer.toString(i) + ")");
        }

        nifty.executeEndOfFrameElementActions();
    }

    public void runMapOther(String id) {
        InGame.mapType = 1;
        MapManager.loadMap("./Data/Maps/Other/"
                + nifty.getCurrentScreen().findElementByName("map" + id).getRenderer(TextRenderer.class).getOriginalText()
                + ".tomap");
    }

    public void runMap(String id) {
        InGame.mapType = 0;
        MapManager.loadMap("./Data/Maps/" + MapList.mapFilename[Integer.parseInt(id)]);
        
        currentLevel = Integer.parseInt(id);
    }

    private void addMenuItem(String text, String id, String onClick) {
        // Create item
        TextBuilder temp = new TextBuilder();
        temp.text(text);
        temp.id(id);
        temp.style("menu-item");
        temp.interactOnClick(onClick);
        // Add it to menu
        temp.build(nifty, screen, screen.findElementByName("menu-panel"));

        Element panel = screen.findElementByName("menu-panel-parent");
        panel.setConstraintHeight(new SizeValue(Integer.toString(panel.getConstraintHeight().getValueAsInt(0) + 51) + "px"));
        screen.layoutLayers();
    }

    private void removeAllMenuItems() {
        for (Element child : screen.findElementByName("menu-panel").getElements()) {
            child.markForRemoval();
        }
        screen.findElementByName("menu-panel-parent").setConstraintHeight(new SizeValue("0px"));
        screen.layoutLayers();
    }

    private void refreshMenu() {
        nifty.executeEndOfFrameElementActions();
        screen.layoutLayers();

    }

    public void popupExit(final String exit) {
        nifty.closePopup("popupExit", new EndNotify() {

            public void perform() {
                if ("yes".equals(exit)) {
                    if (popupExitTodo == 0) {
                        nifty.setAlternateKey("fade");
                        nifty.exit();

                        System.exit(0);
                    } else if (popupExitTodo == 1) {
                        Game.config.setContentOne("/config/userProgress/lastWonLevel", "0");
                        runMap("0");
                    }
                }
            }
        });
    }
}
