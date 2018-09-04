package x.tools.api.ocr;

import android.graphics.Rect;
import android.util.Pair;

import java.util.List;

public class OcrResult {
    public final String text;
    public final float confidence;
    public final Rect rect;
    public final List<Pair<String, Double>> symbolChoicesAndConfidence;

    public OcrResult(String text, float confidence, Rect rect, List<Pair<String, Double>> symbolChoicesAndConfidence) {
        this.text = text;
        this.confidence = confidence;
        this.rect = rect;
        this.symbolChoicesAndConfidence = symbolChoicesAndConfidence;
    }

    @Override
    public String toString() {
        return "OcrResult{" +
                "text='" + text + '\'' +
                ", confidence=" + confidence +
                ", rect=" + rect +
                ", symbolChoicesAndConfidence=" + symbolChoicesAndConfidence +
                '}';
    }
}
