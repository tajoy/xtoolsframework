package x.tools.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import x.tools.eventbus.rpc.RpcFactory;
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
            new Thread(() -> {
                try {
                    test();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }).start();
//            XStatus status = getXContext().checkStatus();
//            if (status.isOk()) {
//                this.startScript();
//            } else {
//                Toast.makeText(this, getXContext().statusDescription(), Toast.LENGTH_LONG).show();
//            }
        });
//        new Handler().postDelayed(this::changeValue, XUtils.randomRange(100, 200));
//        new Handler().postDelayed(() -> new Thread(this::test).start(), 3000);
    }

    private void test() throws Throwable {
        String process = BuildConfig.APPLICATION_ID + ":main_service";
        IServiceProxy serviceProxy = RpcFactory.getProxy(IServiceProxy.class, process, "MainService");
        if (serviceProxy == null) {
            error("serviceProxy == null");
            return;
        }
        serviceProxy.call_1();

        serviceProxy.call_2(1, 2L, 3.0, "");

        if (!serviceProxy.call_3(new int[]{1, 2, 3, 4}))
            throw new AssertionError();

        if (1 != serviceProxy.call_4(new long[]{1L, 2L, 3L, 4L}))
            throw new AssertionError();

        if (!"call_5".equals(serviceProxy.call_5(new double[]{1.0, 2.0, 4.0, 5.0})))
            throw new AssertionError();

        List<Integer> arg_call_6 = new ArrayList<>();
        arg_call_6.add(1);
        arg_call_6.add(2);
        arg_call_6.add(3);
        if (!serviceProxy.call_6(arg_call_6)) throw new AssertionError();

        List<Long> arg_call_7 = new ArrayList<>();
        arg_call_7.add(1L);
        arg_call_7.add(2L);
        arg_call_7.add(3L);
        if (2 != serviceProxy.call_7(arg_call_7)) throw new AssertionError();

        Map<String, String> arg_call_8 = new HashMap<>();
        arg_call_8.put("1", "1");
        arg_call_8.put("2", "2");
        arg_call_8.put("3", "3");
        if (!"call_8".equals(serviceProxy.call_8(arg_call_8))) throw new AssertionError();

        Map<String, Map<String, String>> arg_call_9 = new HashMap<>();
        arg_call_9.put("call_9", arg_call_8);
        if (!arg_call_9.get("call_9").equals(serviceProxy.call_9(arg_call_9)))
            throw new AssertionError();

        IServiceProxy.Data arg_call_10 = new IServiceProxy.Data(1, 2, 3, true, "arg_call_10");
        if (!arg_call_10.equals(serviceProxy.call_10(arg_call_10)))
            throw new AssertionError();

        // cannot pass
        Map<String, IServiceProxy.Data> arg_call_11 = new HashMap<>();
        arg_call_11.put("data", arg_call_10);
        if (!arg_call_11.get("data").equals(serviceProxy.call_11(arg_call_11).get("data")))
            throw new AssertionError();

        Object ret = serviceProxy.call_12(1, 2, 3, 4, 5, 6);
        Object expected = new int[]{1, 2, 3, 4, 5, 6};
        // cannot pass
        if (!Objects.deepEquals(expected, ret))
            throw new AssertionError();
    }

    private void changeValue() {
//        Status.getInst().setStatus(XUtils.randomText(32));
//        new Handler().postDelayed(this::changeValue, XUtils.randomRange(100, 200));
    }

    private void startScript() {
        try {
            getXContext().getScript().runScriptFile("main.lua");
        } catch (XError e) {
            e.printStackTrace();
        }
    }

}
