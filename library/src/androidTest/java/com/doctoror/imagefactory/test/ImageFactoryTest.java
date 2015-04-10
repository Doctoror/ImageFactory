package com.doctoror.imagefactory.test;

import com.doctoror.imagefactory.ImageFactory;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.test.InstrumentationTestCase;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifDrawable;

public final class ImageFactoryTest extends InstrumentationTestCase {

    public void testStaticGifAsAssetInputStream() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets().open("w3c_home.gif");
        assertFalse(ImageFactory.isAnimatedGif(new BufferedInputStream(is1)));
        is1.close();

        final InputStream is = context.getAssets().open("w3c_home.gif");
        assertTrue(ImageFactory.decodeStream(context.getResources(), is) instanceof BitmapDrawable);
    }

    public void testStaticGifAsByteArray() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets().open("w3c_home.gif");
        final byte[] data = toByteArray(is1);
        is1.close();

        assertFalse(
                ImageFactory
                        .isAnimatedGif(new BufferedInputStream(new ByteArrayInputStream(data))));
        is1.close();

        assertTrue(ImageFactory
                .decodeByteArray(context.getResources(), data) instanceof BitmapDrawable);
    }

    public void testLoopOnceAnimatedGifAsAssetInputStream() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets().open("loop_once.gif");
        assertTrue(ImageFactory.isAnimatedGif(new BufferedInputStream(is1)));
        is1.close();

        final InputStream is = context.getAssets().open("loop_once.gif");
        final Drawable result = ImageFactory.decodeStream(context.getResources(), is);
        assertTrue(result instanceof GifDrawable);
        assertTrue(((GifDrawable) result).getLoopCount() == 1);
    }

    public void testLoopOnceAnimatedGifAsByteArray() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets().open("loop_once.gif");
        final byte[] data = toByteArray(is1);
        is1.close();

        assertTrue(
                ImageFactory
                        .isAnimatedGif(new BufferedInputStream(new ByteArrayInputStream(data))));
        is1.close();

        final Drawable result = ImageFactory.decodeByteArray(context.getResources(), data);
        assertTrue(result instanceof GifDrawable);
        assertTrue(((GifDrawable) result).getLoopCount() == 1);
    }

    public void testLoopedAnimatedGifAsAssetInputStream() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets().open("w3c_home_animation.gif");
        assertTrue(ImageFactory.isAnimatedGif(new BufferedInputStream(is1)));
        is1.close();

        final InputStream is = context.getAssets().open("w3c_home_animation.gif");
        final Drawable result = ImageFactory.decodeStream(context.getResources(), is);
        assertTrue(result instanceof GifDrawable);
        assertTrue(((GifDrawable) result).getLoopCount() == 0);
    }

    public void testLoopedAnimatedGifAsByteArray() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets().open("w3c_home_animation.gif");
        final byte[] data = toByteArray(is1);
        is1.close();

        assertTrue(
                ImageFactory
                        .isAnimatedGif(new BufferedInputStream(new ByteArrayInputStream(data))));
        is1.close();

        final Drawable result = ImageFactory.decodeByteArray(context.getResources(), data);
        assertTrue(result instanceof GifDrawable);
        assertTrue(((GifDrawable) result).getLoopCount() == 0);
    }

    private byte[] toByteArray(final InputStream is) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(
                is.available() > 0 ? is.available() : 10240);
        final byte[] buffer = new byte[10240];
        int read;
        while ((read = is.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }

}
