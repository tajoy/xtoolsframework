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

import java.util.Timer;

import x.tools.framework.XUtils;
import x.tools.framework.error.XError;
import x.tools.framework.event.Event;
import x.tools.framework.event.annotation.EventSubscriber;
import x.tools.framework.event.annotation.SyncValue;
import x.tools.framework.event.sync.SyncBooleanValue;
import x.tools.framework.event.sync.SyncStringValue;
import x.tools.framework.log.Loggable;

import static x.tools.app.MainApplication.getXContext;

public class MainService extends Service implements Loggable {
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
        info("onCreate");
        new Handler().postDelayed(this::changeValue, XUtils.randomRange(100, 200));
    }


    private void changeValue() {
        Status.getInst().setStatus(XUtils.randomText(32));
        new Handler().postDelayed(this::changeValue, XUtils.randomRange(100, 200));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getXContext().unsubscribe(this);
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
}
