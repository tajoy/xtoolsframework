package x.tools.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import x.tools.framework.XContext;
import x.tools.framework.XStatus;
import x.tools.framework.api.image.ImageApi;
import x.tools.framework.api.screencap.ScreencapApi;
import x.tools.framework.error.XError;
import x.tools.framework.log.Loggable;
import x.tools.framework.script.lua.LuaScript;

public class MainActivity extends AppCompatActivity implements Loggable {
    private XContext xContext;
    private final LuaScript luaScript = new LuaScript();

    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

    };

    private static final int REQUEST_PERMISSION = 0x123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText("");

        List<String> requestPermissions = new ArrayList<>();

        for (String perm : PERMISSIONS) {
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(perm);
            }
        }

        if (requestPermissions.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    requestPermissions.toArray(new String[requestPermissions.size()]),
                    REQUEST_PERMISSION
            );
        }

        Context context = this.getApplicationContext();
        try {
            xContext = new XContext.Builder(context)
                    .script(luaScript)
                    .api(ScreencapApi.getInstance(context))
                    .api(new ImageApi())
                    .build();
            xContext.copyAssetsToScriptDir("script");
            xContext.initialize();
        } catch (XError xError) {
            xError.printStackTrace();
            return;
        }
        luaScript.addEventListener("ping", this::onEvent);
        findViewById(R.id.button).setOnClickListener(v -> {
            XStatus status = xContext.checkStatus();
            if (XStatus.OK.equals(status)) {
                this.startScript();
            } else {
                Toast.makeText(this, xContext.statusDescription(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startScript() {
        try {
            luaScript.runScriptFile("main.lua");
        } catch (XError e) {
            e.printStackTrace();
        }
    }

    public Object onEvent(String name, Object ...args) throws XError {
        info("%s -> %s", name, Arrays.toString(args));
        luaScript.dispatchEvent("pong", luaScript.createValue("1"), luaScript.createValue("2"), luaScript.createValue("3"));
        return "ok";
    }
}
