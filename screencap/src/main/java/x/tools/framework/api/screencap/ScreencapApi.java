package x.tools.framework.api.screencap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import x.tools.framework.XContext;
import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.PName;
import x.tools.framework.annotation.PNullable;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiStatus;
import x.tools.framework.error.XError;

import static x.tools.framework.XUtils.delay;
import static x.tools.framework.XUtils.normalizeId;

public final class ScreencapApi extends AbstractApi {

    private static final int REQUEST_MEDIA_PROJECTION = normalizeId(R.id.REQUEST_MEDIA_PROJECTION);

    private Context context;
    private MediaProjectionManager mpManager = null;
    private WindowManager windowManager = null;
    private int resultCode;
    private Intent resultData;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private int windowWidth;
    private int windowHeight;
    private int screenDensity;

    private final AtomicBoolean isGrantPermission = new AtomicBoolean(false);

    public ScreencapApi(Context context) {
        this.context = context;
    }

    @Override
    public String getNamespace() {
        return "screencap";
    }

    @Override
    public boolean initialize(XContext xContext) throws XError {
        if (!super.initialize(xContext)) return false;

        this.context.startActivity(
                new Intent(
                        this.context,
                        GrantCapturePermissionActivity.class
                )
        );
        return true;
    }

    @Override
    public ApiStatus checkStatus() {
        if (!isInitialize) {
            return ApiStatus.NOT_INIT;
        }
        if (!isGrantPermission.get()) {
            return ApiStatus.NEED_PERMISSION;
        }
        return ApiStatus.OK;
    }

    public void init(Activity activity) {
        mpManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        activity.startActivityForResult(
                mpManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION
        );

        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        windowWidth = displayMetrics.widthPixels;
        windowHeight = displayMetrics.heightPixels;
        screenDensity = displayMetrics.densityDpi;
    }

    public void onActivityResult(Activity activity, int requestCode, int code, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (code != Activity.RESULT_OK) {
                debug("Grant screen capture permission failed!");
                Toast.makeText(activity, "Grant screen capture permission failed!!", Toast.LENGTH_SHORT).show();
                return;
            }

            isGrantPermission.set(true);
            resultCode = code;
            resultData = data;
            setUpMediaProjection();
            setUpVirtualDisplay();
        }
    }

    private void setUpMediaProjection() {
        mediaProjection = mpManager.getMediaProjection(resultCode, resultData);
    }

    private void setUpVirtualDisplay() {
        imageReader = ImageReader.newInstance(windowWidth, windowHeight, PixelFormat.RGBA_8888, 5);
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "AlipayToolsScreenCapture",
                windowWidth, windowHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,
                null
        );
    }

    private void setUpNewVirtualDisplay() {
        VirtualDisplay vd = virtualDisplay;
        ImageReader ir = imageReader;

        if (vd != null) {
            vd.release();
        }
        if (ir != null) {
            ir.close();
        }

        imageReader = null;
        virtualDisplay = null;
        setUpVirtualDisplay();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    @Api
    public Bitmap capture(
            @PName(name = "rect")
            @PNullable
                    Rect rect
    ) {
        Image image = null;
        for (int i = 0; i < 6; i++) {
            if (imageReader == null) {
                error("imageReader is null.");
                setUpNewVirtualDisplay();
            }

            try {
                image = imageReader.acquireLatestImage();
            } catch (IllegalStateException e) {
                if (e.getMessage().contains("maxImages")) {
                    setUpNewVirtualDisplay();
                } else {
                    warn(e, "screencap.capture");
                }
                image = null;
            } catch (Throwable t) {
                warn(t, "screencap.capture");
                image = null;
            }
            if (image != null) {
                break;
            }
            delay(100);
        }

        if (image == null) {
            error("image is null.");
            return null;
        }

        try {
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            if (rect != null) {
                Bitmap newBitmap = Bitmap.createBitmap(bitmap,
                        clamp(rect.left, 0, windowWidth),
                        clamp(rect.top, 0, windowHeight),
                        clamp(rect.width(), 0, windowWidth),
                        clamp(rect.height(), 0, windowHeight)
                );
                bitmap.recycle();
                bitmap = newBitmap;
            }
            return bitmap;
        } catch (Throwable t) {
            warn(t, "screencap.capture");
            return null;
        } finally {
            image.close();
        }
    }

    @Api
    public int getWindowWidth() {
        return windowWidth;
    }

    @Api
    public int getWindowHeight() {
        return windowHeight;
    }

}
