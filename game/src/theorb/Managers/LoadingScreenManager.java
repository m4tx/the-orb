package theorb.Managers;

import com.jme3.app.Application;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.tools.SizeValue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author m4tx1
 */
public class LoadingScreenManager {

    private static Element descriptionText;
    private static Element progressBarElement;
    private static TextRenderer descriptionTextRenderer;
    private static Application app;
    public static Nifty nifty;
    private static Future loadFuture = null;
    private static boolean load = false;
    private static boolean isDone = false;
    private static ScheduledThreadPoolExecutor exec = null;
    private static Callable<Void> loadCallable;
    private static Callable<Void> postLoadCallable = null;

    public static void init(Application application, Nifty niftyGui) {
        descriptionText = niftyGui.getScreen("loading").findElementByName("loadingtext");
        descriptionTextRenderer = descriptionText.getRenderer(TextRenderer.class);
        progressBarElement = niftyGui.getScreen("loading").findElementByName("progressbar");

        nifty = niftyGui;

        if (application != null) {
            app = application;
        }
    }

    public static void setLoadCallable(Callable<Void> callable) {
        loadCallable = callable;
    }

    public static void setPostLoadCallable(Callable<Void> callable) {
        postLoadCallable = callable;
    }

    public static void updateLoadState() {
        if (load) {
            if (loadFuture == null) {
                if (nifty.getCurrentScreen().getScreenId().equals("loading")) {
                    //if we have not started loading yet, submit the Callable to the executor
                    loadFuture = exec.submit(loadCallable);
                }
            } else if (loadFuture.isDone()) {
                try {
                    if (postLoadCallable != null) {
                        postLoadCallable.call();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(LoadingScreenManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                isDone = true;
            }
        }
    }

    public static void enable() {
        load = true;
        exec = new ScheduledThreadPoolExecutor(2);
        nifty.gotoScreen("loading");
    }

    public static boolean getLoad() {
        return load;
    }

    public static boolean getIsDone() {
        return isDone;
    }

    public static void disable() {
        loadFuture = null;
        exec = null;

        isDone = false;
        load = false;
        
        postLoadCallable = null;
    }

    public static void setProgress(final float progress, final String loadingText) {
        //since this method is called from another thread, we enqueue the changes to the progressbar to the update loop thread
        app.enqueue(new Callable() {

            public Object call() throws Exception {
                final int MIN_WIDTH = 32;
                int pixelWidth = (int) (MIN_WIDTH + (progressBarElement.getParent().getWidth() - MIN_WIDTH) * progress);
                progressBarElement.setConstraintWidth(new SizeValue(pixelWidth + "px"));
                progressBarElement.getParent().layoutElements();

                descriptionTextRenderer.setText(loadingText);
                return null;
            }
        });
    }
}
