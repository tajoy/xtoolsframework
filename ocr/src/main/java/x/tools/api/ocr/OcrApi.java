package x.tools.api.ocr;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import x.tools.framework.XContext;
import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.PIntDef;
import x.tools.framework.annotation.PName;
import x.tools.framework.api.AbstractApi;
import x.tools.framework.error.InitializeError;
import x.tools.framework.error.XError;

import static com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_BLOCK;
import static com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_PARA;
import static com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_SYMBOL;
import static com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE;
import static com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel.RIL_WORD;

public class OcrApi extends AbstractApi {
    @Override
    public String getNamespace() {
        return "ocr";
    }

    @Override
    public String[] getDependenceApis() {
        return new String[]{
                "x.tools.framework.api.screencap.ScreencapApi",
        };
    }

    private TessBaseAPI baseApi = null;

    @Override
    public boolean initialize(XContext xContext) throws XError {
        if (!super.initialize(xContext)) return false;

        baseApi = new TessBaseAPI();
        baseApi.setDebug(false);

        String tessDataDir = xContext.getPathData("tessdata");
        try {
            if (!xContext.copyAssetsToDataDir("tessdata")) return false;
        } catch (IOException e) {
            throw new InitializeError(e);
        }

        String[] files = new File(tessDataDir).list(
                (dir, filename) ->
                        dir.getAbsolutePath().equals(tessDataDir)
                                && filename.endsWith(".traineddata")
        );
        StringJoiner stringJoiner = new StringJoiner("+");
        for (int i = 0; i < files.length; i++) {
            stringJoiner.add(files[i].replaceAll("\\.traineddata", ""));
        }

        String language = stringJoiner.toString();
        debug("initialize TessBaseAPI with language: %s", language);

        // 初始化BaseApi
        if (!baseApi.init(tessDataDir, language))
            return false;

        return true;
    }

    @Api
    public String detectText(
            @PName(name = "bitmap")
                    Bitmap bitmap
    ) {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap);
        baseApi.setImage(newBitmap);
        String text = baseApi.getUTF8Text();
        baseApi.clear();
        if (!newBitmap.isRecycled())
            newBitmap.recycle();
        return text;
    }

    @Api
    public List<OcrResult> detectTextEx(
            @PName(name = "bitmap")
                    Bitmap bitmap,

            @PIntDef({RIL_BLOCK, RIL_PARA, RIL_TEXTLINE, RIL_WORD, RIL_SYMBOL})
            @TessBaseAPI.PageIteratorLevel.Level
                    int level
    ) {
        List<OcrResult> results = new ArrayList<>();

        Bitmap newBitmap = Bitmap.createBitmap(bitmap);
        baseApi.setImage(newBitmap);
        String text = baseApi.getUTF8Text();
        if (TextUtils.isEmpty(text)) {
            return results;
        }
        ResultIterator iterator = baseApi.getResultIterator();

        iterator.begin();
        do {
            OcrResult result = new OcrResult(
                    iterator.getUTF8Text(level),
                    iterator.confidence(level),
                    iterator.getBoundingRect(level),
                    iterator.getSymbolChoicesAndConfidence()
            );
            results.add(result);
        } while (iterator.next(level));
        iterator.delete();
        baseApi.clear();
        if (!newBitmap.isRecycled())
            newBitmap.recycle();
        return results;
    }

}
