/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package theorb;

import java.math.BigDecimal;

/**
 *
 * @author m4tx1
 */
public class GameTime {

    public long minutes;
    public byte seconds, hundredths;

    public GameTime(float seconds) {
        BigDecimal bd = new BigDecimal(seconds);

        // Round to 2 decimal places
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

        long secsTemp = bd.longValue();
        minutes = secsTemp / 60;
        this.seconds = (byte) (secsTemp - minutes * 60);
        hundredths = (byte) ((bd.floatValue() - secsTemp)*100);
    }

    @Override
    public String toString() {
        return force2digits(Long.toString(minutes)) + ":"
                + force2digits(Byte.toString(seconds)) + ":"
                + force2digits(Byte.toString(hundredths));
    }

    private String force2digits(String string) {
        if (string.length() == 1) {
            return "0" + string;
        } else {
            return string;
        }
    }
}
