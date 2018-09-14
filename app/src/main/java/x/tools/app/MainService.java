package x.tools.app;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import x.tools.eventbus.Event;
import x.tools.eventbus.rpc.RpcFactory;
import x.tools.framework.XUtils;
import x.tools.framework.error.XError;
import x.tools.eventbus.annotation.EventSubscriber;
import x.tools.framework.log.Loggable;

import static x.tools.app.MainApplication.getXContext;

public class MainService extends Service implements Loggable, IServiceProxy {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void updateNotification() {
        Notification notification;
        Notification.Builder builder;
        builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentTitle("正常运行中 ...");
        notification = builder.build();
        startForeground(R.id.NOTIFICATION_STATUS, notification);
    }

    public static void bootService(Context context) {
        context.startService(new Intent(context, MainService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateNotification();
        getXContext().subscribe(this);
        RpcFactory.registerProxyHost(IServiceProxy.class, this, "MainService");
        info("onCreate");
//        new Handler().postDelayed(this::changeValue, XUtils.randomRange(100, 200));
    }


//    private void changeValue() {
//        Status.getInst().setStatus(XUtils.randomText(32));
//        new Handler().postDelayed(this::changeValue, XUtils.randomRange(100, 200));
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getXContext().unsubscribe(this);
        RpcFactory.unregisterProxyHost(IServiceProxy.class, this,"MainService");
        info("onDestroy");
    }

    @EventSubscriber(name = "ping")
    public void onPing(Event event) throws XError, JSONException {
        info("%s -> %s", event.getName(), event.getData());
        JSONObject inner = new JSONObject();
        inner.put("1", 1);
        inner.put("2", 2);
        inner.put("3", 3);
        inner.put("4", 4);
        JSONArray array = new JSONArray();
        array.put(1).put(2).put(3).put(4);
        JSONObject data = new JSONObject();
        data.put("1", 1);
        data.put("2", 2);
        data.put("3", 3);
        data.put("4", inner);
        data.put("5", array);
        getXContext().triggerRaw("pong", data);
    }

    @Override
    public void call_1() {
        debug("call_1");
    }

    @Override
    public void call_2(int arg1, long arg2, double arg3, String arg4) {
        debug("call_2: %d, %d, %f, %s", arg1, arg2, arg3, arg4);
    }

    @Override
    public boolean call_3(int[] arg1) {
        debug("call_3: %s", Arrays.toString(arg1));
        return true;
    }

    @Override
    public int call_4(long[] arg1) {
        debug("call_4: %s", Arrays.toString(arg1));
        return 1;
    }

    @Override
    public String call_5(double[] arg1) {
        debug("call_5: %s", Arrays.toString(arg1));
        return "call_5";
    }

    @Override
    public boolean call_6(List<Integer> arg1) {
        debug("call_6: %s", arg1);
        return true;
    }

    @Override
    public int call_7(List<Long> arg1) {
        debug("call_7: %s", arg1);
        return 2;
    }

    @Override
    public String call_8(Map<String, String> arg1) {
        debug("call_8: %s", arg1);
        return "call_8";
    }

    @Override
    public Map<String, String> call_9(Map<String, Map<String, String>> arg1) {
        debug("call_9: %s", arg1);
        return arg1.get("call_9");
    }

    @Override
    public Data call_10(Data arg1) {
        debug("call_10: %s", arg1);
        return arg1;
    }

    @Override
    public Map<String, Data> call_11(Map<String, Data> arg1) {
        debug("call_11: %s", arg1);
        return arg1;
    }

    @Override
    public Object call_12(Object... args) {
        return args;
    }
}
