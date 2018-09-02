package x.tools.framework.api.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import x.tools.framework.XContext;
import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.ApiConstant;
import x.tools.framework.annotation.PEnumInt;
import x.tools.framework.annotation.PFloatRange;
import x.tools.framework.annotation.PIntRange;
import x.tools.framework.annotation.PName;
import x.tools.framework.annotation.PNonNull;
import x.tools.framework.annotation.PNullable;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.log.Loggable;

public class ImageApi extends AbstractApi implements Loggable {

    @Override
    public String getNamespace() {
        return "image";
    }

    @Override
    public String[] getDependenceApis() {
        return new String[]{
                "x.tools.framework.api.screencap.ScreencapApi",
        };
    }

    @ApiConstant
    public final int TOP_LEFT = FindDirection.TOP_LEFT.ordinal();

    @ApiConstant
    public final int TOP_RIGHT = FindDirection.TOP_RIGHT.ordinal();

    @ApiConstant
    public final int BOTTOM_LEFT = FindDirection.BOTTOM_LEFT.ordinal();

    @ApiConstant
    public final int BOTTOM_RIGHT = FindDirection.BOTTOM_RIGHT.ordinal();

    @ApiConstant
    public final int LEFT_TOP = FindDirection.LEFT_TOP.ordinal();

    @ApiConstant
    public final int LEFT_BOTTOM = FindDirection.LEFT_BOTTOM.ordinal();

    @ApiConstant
    public final int RIGHT_TOP = FindDirection.RIGHT_TOP.ordinal();

    @ApiConstant
    public final int RIGHT_BOTTOM = FindDirection.RIGHT_BOTTOM.ordinal();

    @Api
    public String bitmapToBase64(
            @PName(name = "bitmap")
            @PNullable
                    Bitmap bitmap
    ) {
        if (bitmap == null)
            return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    @Api
    public Bitmap base64ToBitmap(
            @PName(name = "base64")
            @PNullable
                    String base64
    ) {
        if (base64 == null)
            return null;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Throwable t) {
            return null;
        }
    }


    @Api
    public boolean saveImage(
            @PName(name = "bitmap")
            @PNullable
                    Bitmap bitmap,

            @PName(name = "path")
            @PNullable
                    String path
    ) {
        if (bitmap == null || path == null) {
            return false;
        }
        FileOutputStream out = null;
        try {
            File file = new File(xContext.getPathData() + File.separator + path);
            File parentFile = file.getParentFile();
            if (!parentFile.isDirectory()) {
                parentFile.mkdirs();
            }
            out = new FileOutputStream(file.getAbsoluteFile());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }
        return true;
    }

    @Api
    public int argb(
            @PName(name = "alpha")
            @PIntRange(from = 0, to = 255)
                    int alpha,

            @PName(name = "red")
            @PIntRange(from = 0, to = 255)
                    int red,

            @PName(name = "green")
            @PIntRange(from = 0, to = 255)
                    int green,

            @PName(name = "blue")
            @PIntRange(from = 0, to = 255)
                    int blue
    ) {
        return Color.argb(alpha, red, green, blue);
    }

    @Api
    public Point findImage(
            @PName(name = "source")
            @PNonNull
                    Bitmap source,

            @PName(name = "sourceRect")
            @PNullable
                    Rect sourceRect,

            @PName(name = "target")
            @PNonNull
                    Bitmap target,

            @PName(name = "direction")
            @PEnumInt(target = FindDirection.class)
                    int direction,

            @PName(name = "similarity")
            @PFloatRange(from = 0.0f, to = 1.0f)
                    float similarity
    ) {
        return nativeFindImage(source, sourceRect, target, direction, similarity);
    }

    @Api
    public Point findColor(
            @PName(name = "source")
            @PNonNull
                    Bitmap source,

            @PName(name = "sourceRect")
            @PNullable
                    Rect sourceRect,

            @PName(name = "color")
                    int color,

            @PName(name = "direction")
            @PEnumInt(target = FindDirection.class)
                    int direction,

            @PName(name = "similarity")
            @PFloatRange(from = 0.0f, to = 1.0f)
                    float similarity
    ) {
        return nativeFindColor(source, sourceRect, color, direction, similarity);
    }

    @Api
    public Point findMultiColor(
            @PName(name = "source")
            @PNonNull
                    Bitmap source,

            @PName(name = "sourceRect")
            @PNullable
                    Rect sourceRect,

            @PName(name = "multiColor")
            @PNonNull
                    Map<Point, Integer> multiColor,

            @PName(name = "direction")
            @PEnumInt(target = FindDirection.class)
                    int direction,

            @PName(name = "similarity")
            @PFloatRange(from = 0.0f, to = 1.0f)
                    float similarity
    ) {
        return nativeFindMultiColor(source, sourceRect, multiColor, direction, similarity);
    }


    static {
        System.loadLibrary("ImageApi");
    }

    private native static Point nativeFindImage(Bitmap source, Rect sourceRect, Bitmap target, int direction, float similarity);

    private native static Point nativeFindColor(Bitmap source, Rect sourceRect, int color, int direction, float similarity);

    private native static Point nativeFindMultiColor(Bitmap source, Rect sourceRect, Map<Point, Integer> multiColor, int direction, float similarity);

}
