package x.tools.framework;
public enum XStatus {
    OK,
    NOT_INIT,
    INIT_FAIL,
    API_NOT_OK,
    ;


    public boolean isOk() {
        return this.equals(OK);
    }
}
