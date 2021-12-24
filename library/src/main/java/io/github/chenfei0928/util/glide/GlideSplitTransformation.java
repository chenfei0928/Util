package io.github.chenfei0928.util.glide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.bumptech.glide.util.Util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import androidx.annotation.NonNull;

/**
 * Created by MrFeng on 2018/3/5.
 */
public class GlideSplitTransformation extends BitmapTransformation {
    private static final Paint DEFAULT_PAINT = new Paint(TransformationUtils.PAINT_FLAGS);
    private static final String ID =
            "io.github.chenfei0928.util.GlideSplitTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final int page;
    private final int pageHeight;

    public GlideSplitTransformation(int page, int pageHeight) {
        this.page = page;
        this.pageHeight = pageHeight;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap.Config config = toTransform.getConfig();
        int currentPageStartY = page * pageHeight;
        int currentPageHeight = Math.min(toTransform.getHeight() - currentPageStartY, pageHeight);
        Bitmap result = pool.get(toTransform.getWidth(), currentPageHeight, config);
        result.setHasAlpha(true);

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(toTransform, 0, currentPageStartY, DEFAULT_PAINT);
        canvas.setBitmap(null);

        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);

        byte[] radiusData = ByteBuffer.allocate(4).putInt(page).putInt(pageHeight).array();
        messageDigest.update(radiusData);
    }

    @Override
    public int hashCode() {
        return Util.hashCode(ID.hashCode(),
                Util.hashCode(page, Util.hashCode(pageHeight)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GlideSplitTransformation) {
            GlideSplitTransformation o = (GlideSplitTransformation) obj;
            return this.page == o.page
                    && this.pageHeight == o.pageHeight;
        }
        return false;
    }
}
