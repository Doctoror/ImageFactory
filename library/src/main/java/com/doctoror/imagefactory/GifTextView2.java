package com.doctoror.imagefactory;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import pl.droidsonroids.gif.GifTextView;

/**
 * {@link GifTextView} that works with {@link GifDrawable2}
 */
public class GifTextView2 extends GifTextView {

    public GifTextView2(final Context context) {
        super(context);
    }

    public GifTextView2(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public GifTextView2(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GifTextView2(final Context context, final AttributeSet attrs, final int defStyle,
            final int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setBackground(final Drawable background) {
        final Drawable prev = getBackground();
        if (prev instanceof GifDrawable2) {
            ((GifDrawable2) prev).removeCallback(this);
        }
        super.setBackground(background);
        if (background instanceof GifDrawable2) {
            ((GifDrawable2) background).addCallback(this);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBackgroundDrawable(final Drawable background) {
        final Drawable prev = getBackground();
        if (prev instanceof GifDrawable2) {
            ((GifDrawable2) prev).removeCallback(this);
        }
        super.setBackgroundDrawable(background);
        if (background instanceof GifDrawable2) {
            ((GifDrawable2) background).addCallback(this);
        }
    }

    @Override
    public void setCompoundDrawables(final Drawable left, final Drawable top, final Drawable right,
            final Drawable bottom) {
        final Drawable[] drawables = getCompoundDrawables();
        for (int i = 0; i < drawables.length; i++) {
            final Drawable prev = drawables[i];
            if (prev instanceof GifDrawable2) {
                ((GifDrawable2) prev).removeCallback(this);
            }
        }
        super.setCompoundDrawables(left, top, right, bottom);
        drawables[0] = left;
        drawables[1] = top;
        drawables[2] = right;
        drawables[3] = bottom;
        for (int i = 0; i < drawables.length; i++) {
            final Drawable drawable = drawables[i];
            if (drawable instanceof GifDrawable2) {
                ((GifDrawable2) drawable).addCallback(this);
            }
        }
    }
}
