package x.tools.framework.api.screencap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class GrantCapturePermissionActivity extends AppCompatActivity {
    private ScreencapApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant_capture_permission);
        Bundle extras = getIntent().getExtras();
        if (extras == null) return;
        api = (ScreencapApi) extras.get("ScreencapApi");
        if (api == null) return;
        api.init(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (api == null) return;
        api.onActivityResult(this, requestCode, resultCode, data);
        this.finish();
    }
}
