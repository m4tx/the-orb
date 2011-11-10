package orbmaped;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 *
 * @author m4tx1
 */
public class Map {

    private byte[][] map = new byte[256][256];
    private Vector2b spawnpoint = new Vector2b();
    private byte theme;

    public Map() {
        for (short x = 0; x < 256; x++) {
            for (short y = 0; y < 256; y++) {
                map[x][y] = -1;
            }
        }
    }

    public void load(String filename) throws IOException, ParseException {
        FileInputStream in = null;
        char[] buf;
        try {
            in = new FileInputStream(filename);
            int c;
            int currentChar = 0;
            buf = new char[in.available()];

            // Read file
            while ((c = in.read()) != -1) {
                buf[currentChar] = (char) c;
                currentChar++;
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        // Parse file
        // Header
        if (!new String(buf).substring(0, 5).matches("TOMap")) {
            throw new ParseException("File header does not match \"TOMap\"", 0);
        }
        // Version
        if (buf[5] != 1) {
            throw new ParseException("File type version is not 1", 5);
        }
        // Spawnpoint
        spawnpoint.x = (byte) buf[6];
        spawnpoint.y = (byte) buf[7];
        // Theme
        theme = (byte) buf[8];
        // Blocks
        for (int i = 9; i < buf.length; i += 3) {
            if (buf[i] < 255 && buf[i + 1] < 255 && buf[i + 2] < 255
                    && buf[i] >= 0 && buf[i + 1] >= 0 && buf[i + 1] >= 0) {
                map[(int) buf[i]][(int) buf[i + 1]] = (byte) buf[i + 2];
            }
        }
    }

    public void save(String filename) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);

            out.write("TOMap".getBytes()); // Header
            out.write(1); // Version
            out.write(spawnpoint.x);
            out.write(spawnpoint.y); // Spawnpoint
            out.write(theme); // Theme

            for (short x = 0; x < 256; x++) {
                for (short y = 0; y < 256; y++) {
                    if (map[x][y] != -1) {
                        out.write(x);
                        out.write(y);
                        out.write(map[x][y]);
                    }
                }
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public byte getBlock(short x, short y) {
        return map[x][y];
    }

    public Vector2b getSpawnpoint() {
        return spawnpoint;
    }

    public byte getTheme() {
        return theme;
    }

    public void setBlock(short x, short y, byte id) {
        map[x][y] = id;
    }

    public void setSpawnpoint(short x, short y) {
        spawnpoint.x = (byte) x;
        spawnpoint.y = (byte) y;
    }

    public void setTheme(byte id) {
        theme = id;
    }
}