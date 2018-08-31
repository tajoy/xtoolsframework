package x.tools.framework.script.lua;

import org.luaj.vm2.lib.jse.JseBaseLib;

import java.io.InputStream;

import x.tools.framework.XContext;

public class LuaBaseLib extends JseBaseLib {
    private final XContext xContext;

    public LuaBaseLib(XContext xContext) {
        this.xContext = xContext;
    }

    @Override
    public InputStream findResource(String filename) {
        InputStream is = this.xContext.findResource(filename);
        if (is != null) {
            return is;
        }
        return super.findResource(filename);
    }
}
