package x.tools.framework.api.image;

/**
 * 搜寻方向, 遍历坐标点时的优先循序
 * 原点为屏幕左上角, X 轴正方向向右, Y 轴正方向向下
 */
public enum FindDirection {
    /**
     * 从左到右, 从上到下
     */
    TOP_LEFT,

    /**
     * 从右到左, 从上到下
     */
    TOP_RIGHT,

    /**
     * 从左到右, 从下到上
     */
    BOTTOM_LEFT,

    /**
     * 从右到左, 从下到上
     */
    BOTTOM_RIGHT,

    /**
     * 从上到下, 从左到右
     */
    LEFT_TOP,

    /**
     * 从下到上, 从左到右
     */
    LEFT_BOTTOM,

    /**
     * 从上到下, 从右到左
     */
    RIGHT_TOP,

    /**
     * 从下到上, 从右到左
     */
    RIGHT_BOTTOM,
}
