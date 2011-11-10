package orbmaped;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Utils {

    public static BufferedImage getTile(Image source, int x, int y, int width, int height) {
	BufferedImage tile = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	BufferedImage bufSource = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	Graphics2D g1 = bufSource.createGraphics();
	g1.drawImage(source, 0, 0, null);
	g1.dispose();
	Graphics2D g2 = tile.createGraphics();
	g2.drawImage(bufSource.getSubimage(x, y, width, height), 0, 0, null);
	g2.dispose();
	return tile;
    }

}
