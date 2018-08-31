package x.tools.api.accessibility;

public enum RootSource {
    DIRECT,
    EVENT,
    WINDOWS;


    private WindowDetermineCallback windowDetermineCallback = null;

    public WindowDetermineCallback getWindowDetermineCallback() {
        return windowDetermineCallback;
    }

    public RootSource setWindowDetermineCallback(WindowDetermineCallback windowDetermineCallback) {
        this.windowDetermineCallback = windowDetermineCallback;
        return this;
    }
}
