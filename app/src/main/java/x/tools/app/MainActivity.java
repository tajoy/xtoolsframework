package x.tools.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import x.tools.framework.XContext;
import x.tools.framework.XStatus;
import x.tools.framework.api.image.ImageApi;
import x.tools.framework.api.screencap.ScreencapApi;
import x.tools.framework.error.XError;
import x.tools.framework.event.Event;
import x.tools.framework.event.annotation.EventSubscriber;
import x.tools.framework.log.Loggable;
import x.tools.framework.script.lua.LuaScript;

public class MainActivity extends AppCompatActivity implements Loggable {
    private XContext xContext;
    private final LuaScript luaScript = new LuaScript();
    private final Gson gson = new Gson();

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
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        findViewById(R.id.button).setOnClickListener(v -> {
            XStatus status = xContext.checkStatus();
            if (status.isOk()) {
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

    @EventSubscriber(name = "ping")
    public void onPing(Event event) throws XError, JSONException {
        info("%s -> %s", event.getName(), event.getData());
        JSONObject inner = new JSONObject();
        inner.put("1", 1);
        inner.put("2", 2);
        inner.put("3", 3);
        inner.put("4", 4);
        JSONObject data = new JSONObject();
        inner.put("1", 1);
        inner.put("2", 2);
        inner.put("3", 3);
        inner.put("4", inner);
        xContext.triggerRaw("pong", data);
    }
}
