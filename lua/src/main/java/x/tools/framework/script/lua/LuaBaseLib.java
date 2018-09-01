package x.tools.framework.script.lua;

import org.luaj.vm2.lib.jse.JseBaseLib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import x.tools.framework.XContext;

public class LuaBaseLib extends JseBaseLib {
    private final XContext xContext;

    public LuaBaseLib(XContext xContext) {
        this.xContext = xContext;
    }

    @Override
    public InputStream findResource(String filename) {
        if (filename == null) return null;
        File f = new File(xContext.getPathScript(filename));
        try {
            if (f.exists())
                return new FileInputStream(f);
        } catch (IOException ioe) {
            return null;
        }
        return super.findResource(filename);
    }
}
