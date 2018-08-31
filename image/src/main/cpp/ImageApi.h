#ifndef __IMAGE_UTILS_H__
#define __IMAGE_UTILS_H__

#include <jni.h>

/**
 * 搜寻方向, 遍历坐标点时的优先循序
 * 原点为屏幕左上角, X 轴正方向向右, Y 轴正方向向下
 */
typedef enum _FindDirection {
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
} FindDirection;

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     x_tools_framework_api_Image
 * Method:    nativeFindImage
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Rect;Landroid/graphics/Bitmap;IF)Landroid/graphics/Point;
 */
JNIEXPORT jobject JNICALL Java_x_tools_framework_api_image_ImageApi_nativeFindImage
        (JNIEnv *, jclass, jobject, jobject, jobject, jint, jfloat);

/*
 * Class:     x_tools_framework_api_image_ImageApi
 * Method:    nativeFindColor
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Rect;IIF)Landroid/graphics/Point;
 */
JNIEXPORT jobject JNICALL Java_x_tools_framework_api_image_ImageApi_nativeFindColor
        (JNIEnv *, jclass, jobject, jobject, jint, jint, jfloat);

/*
 * Class:     x_tools_framework_api_image_ImageApi
 * Method:    nativeFindMultiColor
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Rect;Ljava/util/Map;IF)Landroid/graphics/Point;
 */
JNIEXPORT jobject JNICALL Java_x_tools_framework_api_image_ImageApi_nativeFindMultiColor
        (JNIEnv *, jclass, jobject, jobject, jobject, jint, jfloat);

#ifdef __cplusplus
}
#endif

#endif // end of __IMAGE_UTILS_H__