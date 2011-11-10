package theorb;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author m4tx1
 */
public class XML {

    private AutoPilot ap;
    private VTDNav vn;
    private XMLModifier xm;
    
    private String filename;

    public void load(String filename) {
        this.filename = filename;
        
        VTDGen vg = new VTDGen();
        vg.parseFile(filename, false);
        vn = vg.getNav();
        ap = new AutoPilot();
        ap.bind(vn);
        xm = new XMLModifier();
        try {
            xm.bind(vn);
        } catch (ModifyException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void save() {
        try {
            xm.output(filename);
        } catch (IOException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ModifyException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TranscodeException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getContentOne(String path) {
        try {
            return vn.toNormalizedString(getIndexOne(path));
        } catch (NavException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String setContentOne(String path, String content) {
        try {
            xm.updateToken(getIndexOne(path), content);
        } catch (ModifyException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private int getIndexOne(String path) {
        try {
            ap.selectXPath(path);
            ap.evalXPath();
            return vn.getText();
        } catch (XPathEvalException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NavException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathParseException ex) {
            Logger.getLogger(XML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }
}
