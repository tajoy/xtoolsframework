//
// Created by Jacky Pro on 2018/6/26.
//

#include "ImageApi.h"

#include <math.h>

#include <android/log.h>
#include <android/bitmap.h>

#include <vector>
#include <functional>

#define  LOG_TAG    "ImageUtils-native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

typedef std::function<bool(int x, int y)> Foreach;

template <typename T>
static inline T MakeGlobalRefOrDie(JNIEnv* env, T in) {
    jobject res = env->NewGlobalRef(in);
    assert(res != NULL);
    return static_cast<T>(res);
}

static inline
jclass FindClassOrDie(JNIEnv* env, const char* class_name) {
    jclass clazz = env->FindClass(class_name);
    assert(clazz != NULL);
    return clazz;
}

static inline
jfieldID GetFieldIDOrDie(JNIEnv* env, jclass clazz,
                         const char* field_name,
                         const char* field_signature) {
    jfieldID res = env->GetFieldID(clazz, field_name, field_signature);
    assert(res != NULL);
    return res;
}

static inline
jmethodID GetMethodIDOrDie(JNIEnv* env, jclass clazz,
                           const char* method_name,
                           const char* method_signature) {
    jmethodID res = env->GetMethodID(clazz, method_name, method_signature);
    assert(res != NULL);
    return res;
}

#define GET_CLASS(var, env, clsName) if (var == NULL) var = MakeGlobalRefOrDie(env, FindClassOrDie(env, clsName))
#define GET_METHOD(var, env, cls, name, sig) if (var == NULL) var = GetMethodIDOrDie(env, cls, name, sig)
#define GET_FIELD(var, env, cls, name, sig) if (var == NULL) var = GetFieldIDOrDie(env, cls, name, sig)

class PointColor {
public:
    int x;
    int y;
    int color;

    PointColor(int x, int y, int color)
    : x(x)
    , y(y)
    , color(color)
    {
    }
};

class ABGR {
public:
    uint8_t alpha, red, green, blue;

    ABGR (uint32_t colorInt)
            : alpha ((colorInt >> 24) & 0xff)
            , blue ((colorInt >> 16) & 0xff)
            , green ((colorInt >> 8) & 0xff)
            , red (colorInt & 0xff)
    {
    }

    ABGR (uint8_t alpha, uint8_t red, uint8_t green, uint8_t blue)
            : red (red)
            , green (green)
            , blue (blue)
            , alpha (alpha)
    {
    }
};

class RGBA {
public:
    uint8_t alpha, red, green, blue;

    RGBA (uint32_t colorInt)
            : alpha ((colorInt >> 24) & 0xff)
            , red ((colorInt >> 16) & 0xff)
            , green ((colorInt >> 8) & 0xff)
            , blue (colorInt & 0xff)
    {
    }

    RGBA (uint8_t alpha, uint8_t red, uint8_t green, uint8_t blue)
            : red (red)
            , green (green)
            , blue (blue)
            , alpha (alpha)
    {
    }
};

class ARGB {
public:
    uint8_t alpha, red, green, blue;

    ARGB (uint32_t colorInt)
    : alpha ((colorInt >> 24) & 0xff)
    , red ((colorInt >> 16) & 0xff)
    , green ((colorInt >> 8) & 0xff)
    , blue (colorInt & 0xff)
    {
    }

    ARGB (uint8_t alpha, uint8_t red, uint8_t green, uint8_t blue)
            : red (red)
            , green (green)
            , blue (blue)
            , alpha (alpha)
    {
    }

    ARGB (const RGBA &rgba)
            : red (rgba.red)
            , green (rgba.green)
            , blue (rgba.blue)
            , alpha (rgba.alpha)
    {
    }

    ARGB (const ABGR &abgr)
            : red (abgr.red)
            , green (abgr.green)
            , blue (abgr.blue)
            , alpha (abgr.alpha)
    {
    }

    uint32_t toColorInt() {
        return (this->alpha) | (this->red << 24) | (this->green << 16) | (this->blue << 8);
    }

    bool cmp(ARGB &other, float similarity = 1.0) {
        float th = (1.0 - similarity) * 255.0f;

        return fabs(((float) this->red) - ((float) other.red)) <= th
               && fabs(((float) this->green) - ((float) other.green)) <= th
               && fabs(((float) this->blue) - ((float) other.blue)) <= th
               && fabs(((float) this->alpha) - ((float) other.alpha)) <= th
                ;
    }
};

static jclass classMap = NULL;
static jclass classSet = NULL;
static jclass classPoint = NULL;
static jclass classBitmap = NULL;
static jclass classInteger = NULL;
static jclass classRect = NULL;
static jmethodID method_Point_$II = NULL;
static jmethodID method_Map_keySet = NULL;
static jmethodID method_Map_get = NULL;
static jmethodID method_Set_toArray = NULL;
static jmethodID method_Bitmap_getPixels = NULL;
static jmethodID method_Integer_intValue = NULL;
static jfieldID field_Point_x = NULL;
static jfieldID field_Point_y = NULL;
static jfieldID field_Rect_left = NULL;
static jfieldID field_Rect_top = NULL;
static jfieldID field_Rect_right = NULL;
static jfieldID field_Rect_bottom = NULL;

static inline
bool cmpColor(ARGB argb1, ARGB argb2, jfloat similarity) {
    return argb1.cmp(argb2, similarity);
}

static inline
void getRect(JNIEnv *env, const AndroidBitmapInfo &info, jobject rect, int &x, int &y, int &w, int &h) {
    GET_CLASS(classRect, env, "android/graphics/Rect");
    GET_FIELD(field_Rect_left  , env, classRect, "left"  , "I");
    GET_FIELD(field_Rect_top   , env, classRect, "top"   , "I");
    GET_FIELD(field_Rect_right , env, classRect, "right" , "I");
    GET_FIELD(field_Rect_bottom, env, classRect, "bottom", "I");
    if (rect == NULL) {
        x = 0;
        y = 0;
        w = info.width;
        h = info.height;
    } else {
        jint left = env->GetIntField(rect, field_Rect_left);
        jint top = env->GetIntField(rect, field_Rect_top);
        jint right = env->GetIntField(rect, field_Rect_right);
        jint bottom = env->GetIntField(rect, field_Rect_bottom);
        x = left;
        y = top;
        w = right - left;
        h = bottom - top;
    }
}

//java map<Point, @ColorInt int> 转 std::map<const char*, Point>
static inline
std::vector<PointColor> jMap2PointColorVector(JNIEnv *env, jobject jobj) {
    std::vector<PointColor> ret;
    GET_CLASS(classMap, env, "java/util/Map");
    GET_CLASS(classSet, env, "java/util/Set");
    GET_CLASS(classPoint, env, "android/graphics/Point");
    GET_CLASS(classInteger, env, "java/lang/Integer");
    GET_METHOD(method_Map_keySet, env, classMap, "keySet", "()Ljava/util/Set;");
    GET_METHOD(method_Map_get, env, classMap, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
    GET_METHOD(method_Set_toArray, env, classSet, "toArray", "()[Ljava/lang/Object;");
    GET_METHOD(method_Integer_intValue, env, classInteger, "intValue", "()I");
    GET_FIELD(field_Point_x, env, classPoint, "x", "I");
    GET_FIELD(field_Point_y, env, classPoint, "y", "I");

    jobject jKeySet = env->CallObjectMethod(jobj, method_Map_keySet);
    jobjectArray jObjArray = (jobjectArray) env->CallObjectMethod(jKeySet, method_Set_toArray);
    if (jObjArray == NULL) {
        LOGD("param is NULL");
        return ret;
    }
    jsize size = env->GetArrayLength(jObjArray);
    int i = 0;
    for (i = 0; i < size; i++) {
        jstring jKey = (jstring) env->GetObjectArrayElement(jObjArray, i);
        jstring jValue = (jstring) env->CallObjectMethod(jobj, method_Map_get, jKey);
        int x = env->GetIntField(jKey, field_Point_x);
        int y = env->GetIntField(jKey, field_Point_y);
        int color = env->CallIntMethod(jValue, method_Integer_intValue);
        if (x >= 0 && y >= 0) {
            ret.push_back(PointColor(x, y, color));
        }
    }
    return ret;
}


static inline
bool
foreach(int startX, int endX, int startY, int endY, FindDirection direction, Foreach callback) {
    switch (direction) {
        default:
        case TOP_LEFT:
            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    if (!callback(x, y))
                        return false;
                }
            }
            break;
        case TOP_RIGHT:
            for (int y = startY; y < endY; y++) {
                for (int x = endX - 1; x >= startX; x--) {
                    if (!callback(x, y))
                        return false;
                }
            }
            break;
        case BOTTOM_LEFT:
            for (int y = endY - 1; y >= startY; y--) {
                for (int x = startX; x < endX; x++) {
                    if (!callback(x, y))
                        return false;
                }
            }
            break;
        case BOTTOM_RIGHT:
            for (int y = endY - 1; y >= startY; y--) {
                for (int x = endX - 1; x >= startX; x--) {
                    if (!callback(x, y))
                        return false;
                }
            }
            break;
        case LEFT_TOP:
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    if (!callback(x, y))
                        return false;
                }
            }
            break;
        case LEFT_BOTTOM:
            for (int x = startX; x < endX; x++) {
                for (int y = endY - 1; y >= startY; y--) {
                    if (!callback(x, y))
                        return false;
                }
            }
            break;
        case RIGHT_TOP:
            for (int x = endX - 1; x >= startX; x--) {
                for (int y = startY; y < endY; y++) {
                    if (!callback(x, y))
                        return false;
                }
            }
            break;
        case RIGHT_BOTTOM:
            for (int x = endX - 1; x >= startX; x--) {
                for (int y = endY - 1; y >= startY; y--) {
                    if (!callback(x, y))
                        return false;
                }
            }
            break;
    }
    return true;
}

/*
 * Class:     x_tools_framework_api_image_ImageApi
 * Method:    nativeFindImage
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Rect;Landroid/graphics/Bitmap;IF)Landroid/graphics/Point;
 */
JNIEXPORT jobject JNICALL Java_x_tools_framework_api_image_ImageApi_nativeFindImage
        (JNIEnv *env, jclass selfClass, jobject source, jobject sourceRect, jobject target, jint direction, jfloat similarity) {
    AndroidBitmapInfo sourceInfo;
    AndroidBitmapInfo targetInfo;
    int ret;

    if ((ret = AndroidBitmap_getInfo(env, source, &sourceInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo(source) failed ! error=%d", ret);
        return NULL;
    }

    if ((ret = AndroidBitmap_getInfo(env, target, &targetInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo(target) failed ! error=%d", ret);
        return NULL;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap of sourceInfo format is not RGBA_8888 !");
        return NULL;
    }

    if (targetInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap of targetInfo format is not RGBA_8888 !");
        return NULL;
    }

    int sw = sourceInfo.width;
    int sh = sourceInfo.height;
    int tw = targetInfo.width;
    int th = targetInfo.height;

    // 要查找的目标图片比源图片还大, 肯定不对啦
    if (tw > sw || th > sh) {
        LOGE("target width > source width OR target height > source height!");
        return NULL;
    }

    GET_CLASS(classPoint, env, "android/graphics/Point");
    GET_CLASS(classBitmap, env, "android/graphics/Bitmap");
    GET_METHOD(method_Point_$II, env, classPoint, "<init>", "(II)V");
    GET_METHOD(method_Bitmap_getPixels, env, classBitmap, "getPixels", "([IIIIIII)V");


    void *temp;
    if ((ret = AndroidBitmap_lockPixels(env, source, &temp)) < 0) {
        LOGE("AndroidBitmap_lockPixels() source failed ! error=%d", ret);
        return NULL;
    }

    uint32_t *sourcePixels = static_cast<uint32_t *>(malloc(sizeof(uint32_t) * sw * sh));
    memcpy(sourcePixels, temp, sizeof(uint32_t) * sw * sh);
    AndroidBitmap_unlockPixels(env, source);

    if ((ret = AndroidBitmap_lockPixels(env, target, &temp)) < 0) {
        AndroidBitmap_unlockPixels(env, source);
        LOGE("AndroidBitmap_lockPixels() target failed ! error=%d", ret);
        free(sourcePixels);
        return NULL;
    }
    uint32_t *targetPixels = static_cast<uint32_t *>(malloc(sizeof(uint32_t) * tw * th));
    memcpy(targetPixels, temp, sizeof(uint32_t) * tw * th);
    AndroidBitmap_unlockPixels(env, target);


    int retX = -1;
    int retY = -1;

    int x, y, w, h;
    getRect(env, sourceInfo, sourceRect, x, y, w, h);

    if (!foreach(x, x + w - tw, y, y + h - th, (FindDirection) direction,
                 [=, &retX, &retY](int sx, int sy) {
                     if (sx < 0 || sx >= sw)
                         return true;
                     if (sy < 0 || sy >= sh)
                         return true;
                     retX = sx;
                     retY = sy;
                     if (foreach(0, tw, 0, th, (FindDirection) direction,
                                 [=, &retX, &retY](int tx, int ty) {
                                     if (tx < 0 || tx >= tw)
                                         return true;
                                     if (ty < 0 || ty >= th)
                                         return true;
                                     int x = tx + sx;
                                     int y = ty + sy;
                                     if (x < 0 || x >= sw)
                                         return true;
                                     if (y < 0 || y >= sh)
                                         return true;
                                     uint32_t colorSource = sourcePixels[y * sw + x];
                                     uint32_t colorTarget = targetPixels[ty * tw + tx];
                                     ABGR argbSource(colorSource);
                                     ABGR argbTarget(colorTarget);
//                                     LOGI("find image [(%d, %d), (%d, %d), %0.2f] => source: (A:%X, R:%X, G:%X, B:%X)  target: (A:%X, R:%X, G:%X, B:%X)",
//                                          sx, sy, tx, ty, similarity,
//                                          argbSource.alpha, argbSource.red, argbSource.green, argbSource.blue,
//                                          argbTarget.alpha, argbTarget.red, argbTarget.green, argbTarget.blue
//                                     );
                                     return cmpColor(argbSource, argbTarget, similarity);
                                 })) {
                         return false; // 跳出遍历
                     }
                     return true;
                 })) {
        jobject point = env->NewObject(classPoint, method_Point_$II, retX, retY);
        free(sourcePixels);
        free(targetPixels);
        return point;
    }

    free(sourcePixels);
    free(targetPixels);
    return NULL;
}

/*
 * Class:     x_tools_framework_api_image_ImageApi
 * Method:    nativeFindColor
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Rect;IIF)Landroid/graphics/Point;
 */
JNIEXPORT jobject JNICALL Java_x_tools_framework_api_image_ImageApi_nativeFindColor
        (JNIEnv *env, jclass selfClass, jobject source, jobject sourceRect, jint color, jint direction, jfloat similarity) {

    AndroidBitmapInfo sourceInfo;
    int ret;

    if ((ret = AndroidBitmap_getInfo(env, source, &sourceInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo(source) failed ! error=%d", ret);
        return NULL;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap of sourceInfo format is not RGBA_8888 !");
        return NULL;
    }

    GET_CLASS(classPoint, env, "android/graphics/Point");
    GET_CLASS(classBitmap, env, "android/graphics/Bitmap");
    GET_METHOD(method_Point_$II, env, classPoint, "<init>", "(II)V");
    GET_METHOD(method_Bitmap_getPixels, env, classBitmap, "getPixels", "([IIIIIII)V");

    int sw = sourceInfo.width;
    int sh = sourceInfo.height;

    void *temp;
    if ((ret = AndroidBitmap_lockPixels(env, source, &temp)) < 0) {
        LOGE("AndroidBitmap_lockPixels() source failed ! error=%d", ret);
        return NULL;
    }
    uint32_t *sourcePixels = static_cast<uint32_t *>(malloc(sizeof(uint32_t) * sw * sh));
    memcpy(sourcePixels, temp, sizeof(uint32_t) * sw * sh);
    AndroidBitmap_unlockPixels(env, source);

    int retX = -1;
    int retY = -1;

    int x, y, w, h;
    getRect(env, sourceInfo, sourceRect, x, y, w, h);

    if (!foreach(x, x + w, y, y + h, (FindDirection) direction, [&](int sx, int sy) {
        if (sx < 0 || sx >= sw)
            return true;
        if (sy < 0 || sy >= sh)
            return true;
        uint32_t colorSource = sourcePixels[sy * sw + sx];
        retX = sx;
        retY = sy;
        ABGR argbSource(colorSource);
        RGBA argbTarget(color);
//        LOGI("find color [%d, %d, %0.2f] => source: (A:%X, R:%X, G:%X, B:%X)  target: (A:%X, R:%X, G:%X, B:%X)",
//             sx, sy, similarity,
//             argbSource.alpha, argbSource.red, argbSource.green, argbSource.blue,
//             argbTarget.alpha, argbTarget.red, argbTarget.green, argbTarget.blue
//        );
        return !cmpColor(argbSource, argbTarget, similarity);
    })) {
        jobject point = env->NewObject(classPoint, method_Point_$II, retX, retY);
        free(sourcePixels);
        sourcePixels = NULL;
        return point;
    }

    free(sourcePixels);
    sourcePixels = NULL;
    return NULL;
}


/*
 * Class:     x_tools_framework_api_image_ImageApi
 * Method:    nativeFindMultiColor
 * Signature: (Landroid/graphics/Bitmap;Landroid/graphics/Rect;Ljava/util/Map;IF)Landroid/graphics/Point;
 */
JNIEXPORT jobject JNICALL Java_x_tools_framework_api_image_ImageApi_nativeFindMultiColor
        (JNIEnv *env, jclass selfClass, jobject source, jobject sourceRect, jobject jMultiColor, jint direction, jfloat similarity) {
    AndroidBitmapInfo sourceInfo;
    int ret;

    if ((ret = AndroidBitmap_getInfo(env, source, &sourceInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo(source) failed ! error=%d", ret);
        return NULL;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap of sourceInfo format is not RGBA_8888 !");
        return NULL;
    }


    GET_CLASS(classPoint, env, "android/graphics/Point");
    GET_CLASS(classBitmap, env, "android/graphics/Bitmap");
    GET_METHOD(method_Point_$II, env, classPoint, "<init>", "(II)V");
    GET_METHOD(method_Bitmap_getPixels, env, classBitmap, "getPixels", "([IIIIIII)V");

    int sw = sourceInfo.width;
    int sh = sourceInfo.height;

    void *temp;
    if ((ret = AndroidBitmap_lockPixels(env, source, &temp)) < 0) {
        LOGE("AndroidBitmap_lockPixels() source failed ! error=%d", ret);
        return NULL;
    }
    uint32_t *sourcePixels = static_cast<uint32_t *>(malloc(sizeof(uint32_t) * sw * sh));
    memcpy(sourcePixels, temp, sizeof(uint32_t) * sw * sh);
    AndroidBitmap_unlockPixels(env, source);

    int retX = -1;
    int retY = -1;

    int x, y, w, h;
    getRect(env, sourceInfo, sourceRect, x, y, w, h);

    std::vector<PointColor> multiColor = jMap2PointColorVector(env, jMultiColor);
    if (!foreach(x, x + w, y, y + h, (FindDirection) direction, [=, &retX, &retY](int sx, int sy) {
        retX = sx;
        retY = sy;
        for (PointColor p: multiColor) {
            int color = p.color;
            int tx = sx + p.x;
            int ty = sy + p.y;
            if (tx < 0 || tx >= sw)
                return true;
            if (ty < 0 || ty >= sh)
                return true;
            uint32_t colorSource = sourcePixels[ty * sw + tx];
            ABGR argbSource(colorSource);
            RGBA argbTarget(color);
//            LOGI("find multi color [(%d+%d)=%d, (%d+%d)=%d, %0.2f] => source: (A:%X, R:%X, G:%X, B:%X)  target: (A:%X, R:%X, G:%X, B:%X)",
//                 sx, p.x, tx, sy, p.y, ty, similarity,
//                 argbSource.alpha, argbSource.red, argbSource.green, argbSource.blue,
//                 argbTarget.alpha, argbTarget.red, argbTarget.green, argbTarget.blue
//            );
            if (!cmpColor(argbSource, argbTarget, similarity)) {
                return true;
            }
        }
        return false;
    })) {
        jobject point = env->NewObject(classPoint, method_Point_$II, retX, retY);
        free(sourcePixels);
        return point;
    }

    free(sourcePixels);
    return NULL;
}