package x.tools.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import x.tools.framework.XStatus;
import x.tools.framework.XUtils;
import x.tools.framework.error.XError;
import x.tools.framework.log.Loggable;

import static x.tools.app.MainApplication.getXContext;

public class MainActivity extends AppCompatActivity implements Loggable {

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
        getXContext().subscribe(this);


        findViewById(R.id.button).setOnClickListener(v -> {
            XStatus status = getXContext().checkStatus();
            if (status.isOk()) {
                this.startScript();
            } else {
                Toast.makeText(this, getXContext().statusDescription(), Toast.LENGTH_LONG).show();
            }
        });
        new Handler().postDelayed(this::changeValue, XUtils.randomRange(100, 200));
    }

    private void changeValue() {
        Status.getInst().setStatus(XUtils.randomText(32));
        new Handler().postDelayed(this::changeValue, XUtils.randomRange(100, 200));
    }

    private void startScript() {
        try {
            getXContext().getScript().runScriptFile("main.lua");
        } catch (XError e) {
            e.printStackTrace();
        }
    }

}
