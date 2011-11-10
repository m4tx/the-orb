package theorb;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author m4tx1
 */
public class FileListing {

    public static String[] listFiles(String directory, boolean listDirs, boolean listFiles) {
        File folder = new File(directory);

        if (listDirs && listFiles) {
            return folder.list();
        } else if (listDirs) {
            return folder.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            });
        } else if (listFiles) {
            return folder.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile();
                }
            });
        } else {
            return null;
        }
    }
}
