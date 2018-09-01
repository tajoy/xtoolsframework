package x.tools.api.accessibility;

public class TapType {

    public enum TapTypeEnum {
        CLICK,
        LONG_CLICK,
        PARENT_CLICK,
        PARENT_LONG_CLICK,
        GESTURE,
    }

    private final TapTypeEnum type;
    private int parentLevel = 0;
    private long startTime = 100;
    private long duration = 200;

    private TapType(TapTypeEnum type) {
        this.type = type;
    }

    public int getParentLevel() {
        return parentLevel;
    }

    public static TapType Click() {
        return new TapType(TapTypeEnum.CLICK);
    }

    public static TapType LongClick() {
        return new TapType(TapTypeEnum.LONG_CLICK);
    }

    public static TapType ParentClick(int parentLevel) {
        return new TapType(TapTypeEnum.PARENT_CLICK).setParentLevel(parentLevel);
    }

    public static TapType ParentLongClick(int parentLevel) {
        return new TapType(TapTypeEnum.PARENT_LONG_CLICK).setParentLevel(parentLevel);
    }

    public static TapType ParentClick() {
        return new TapType(TapTypeEnum.PARENT_CLICK);
    }

    public static TapType ParentLongClick() {
        return new TapType(TapTypeEnum.PARENT_LONG_CLICK);
    }

    public static TapType Gesture() {
        return new TapType(TapTypeEnum.GESTURE);
    }

    public static TapType Gesture(long duration) {
        return new TapType(TapTypeEnum.GESTURE).setDuration(duration);
    }

    public static TapType Gesture(long startTime, long duration) {
        return new TapType(TapTypeEnum.GESTURE).setStartTime(startTime).setDuration(duration);
    }

    public TapTypeEnum getType() {
        return type;
    }

    public TapType setParentLevel(int parentLevel) {
        assert parentLevel >= 0;
        this.parentLevel = parentLevel;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public TapType setStartTime(long startTime) {
        assert startTime > 0;
        this.startTime = startTime;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public TapType setDuration(long duration) {
        assert duration > 0;
        this.duration = duration;
        return this;
    }
}
