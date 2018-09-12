package x.tools.eventbus.log;

import android.util.Log;

public class DefaultLogger implements ILogger {
    private final String name;

    public DefaultLogger(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void log(LogLevel logLevel, String msg) {
        switch (logLevel) {
            case TRACE:
                Log.v(this.name, msg);
                break;
            case DEBUG:
                Log.d(this.name, msg);
                break;
            case INFO:
                Log.i(this.name, msg);
                break;
            case WARN:
                Log.w(this.name, msg);
                break;
            case ERROR:
                Log.e(this.name, msg);
                break;
        }
    }

    @Override
    public void log(LogLevel logLevel, Throwable t, String msg) {
        switch (logLevel) {
            case TRACE:
                Log.v(this.name, msg, t);
                break;
            case DEBUG:
                Log.d(this.name, msg, t);
                break;
            case INFO:
                Log.i(this.name, msg, t);
                break;
            case WARN:
                Log.w(this.name, msg, t);
                break;
            case ERROR:
                Log.e(this.name, msg, t);
                break;
        }
    }
}
