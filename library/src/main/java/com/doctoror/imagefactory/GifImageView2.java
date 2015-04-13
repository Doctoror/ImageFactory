package com.doctoror.imagefactory;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import pl.droidsonroids.gif.GifImageView;

/**
 * {@link GifImageView} that supports {@link GifDrawable2}
 */
public class GifImageView2 extends GifImageView {

    public GifImageView2(final Context context) {
        super(context);
    }

    public GifImageView2(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public GifImageView2(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GifImageView2(final Context context, final AttributeSet attrs, final int defStyle,
            final int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
    }

    @Override
    public void setImageDrawable(final Drawable drawable) {
        final Drawable prev = getDrawable();
        if (prev instanceof GifDrawable2) {
            ((GifDrawable2) prev).removeCallback(this);
        }
        super.setImageDrawable(drawable);
        if (drawable instanceof GifDrawable2) {
            ((GifDrawable2) drawable).addCallback(this);
        }
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
}

