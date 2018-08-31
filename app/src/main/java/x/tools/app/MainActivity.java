package x.tools.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import x.tools.framework.XContext;
import x.tools.framework.api.image.ImageApi;
import x.tools.framework.api.screencap.ScreencapApi;
import x.tools.framework.error.XError;
import x.tools.framework.script.lua.LuaScript;

public class MainActivity extends AppCompatActivity {
    private XContext xContext;
    private final LuaScript luaScript = new LuaScript();

    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

    };

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void copyAssets(String from, String to) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(from);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(to, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

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
        xContext = new XContext.Builder(context)
                .script(luaScript)
                .api(new ScreencapApi(context))
                .api(new ImageApi())
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        UpdateManager.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            copyAssets("script", xContext.getPathScript());
            try {
                xContext.initialize();
            } catch (XError xError) {
                xError.printStackTrace();
            }
            new Thread(this::startScript).start();
        }
    }

    private void startScript() {
        try {
            luaScript.runScriptFile("main.lua");
        } catch (XError xError) {
            xError.printStackTrace();
        }
    }
}
