package x.tools.framework.api.screencap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class GrantCapturePermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant_capture_permission);
        ScreencapApi.getInstance(this).init(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ScreencapApi.getInstance(this).onActivityResult(this, requestCode, resultCode, data);
        this.finish();
    }
}
