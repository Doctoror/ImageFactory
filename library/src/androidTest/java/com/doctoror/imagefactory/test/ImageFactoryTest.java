/*
 * Copyright (C) 2015 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doctoror.imagefactory.test;

import com.doctoror.imagefactory.ImageFactory;

import android.content.Context;
import android.content.res.AssetManager;
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
        final InputStream is1 = context.getAssets()
                .open("w3c_home.gif", AssetManager.ACCESS_RANDOM);
        try {
            assertFalse(ImageFactory.isAnimatedGif(new BufferedInputStream(is1)));
        } finally {
            is1.close();
        }

        final InputStream is = context.getAssets().open("w3c_home.gif", AssetManager.ACCESS_RANDOM);
        try {
            assertTrue(ImageFactory
                    .decodeStream(context.getResources(), is) instanceof BitmapDrawable);
        } finally {
            is.close();
        }
    }

    public void testStaticGifAsByteArray() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets()
                .open("w3c_home.gif", AssetManager.ACCESS_RANDOM);
        final byte[] data;
        try {
            data = toByteArray(is1);
        } finally {
            is1.close();
        }

        assertFalse(
                ImageFactory
                        .isAnimatedGif(new BufferedInputStream(new ByteArrayInputStream(data))));
        assertTrue(ImageFactory
                .decodeByteArray(context.getResources(), data) instanceof BitmapDrawable);
    }

    public void testLoopOnceAnimatedGifAsAssetInputStream() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets()
                .open("loop_once.gif", AssetManager.ACCESS_RANDOM);
        try {
            assertTrue(ImageFactory.isAnimatedGif(new BufferedInputStream(is1)));
        } finally {
            is1.close();
        }

        final InputStream is = context.getAssets()
                .open("loop_once.gif", AssetManager.ACCESS_RANDOM);
        final Drawable result;
        try {
            result = ImageFactory.decodeStreamOrThrow(context.getResources(), is, null, null);
        } finally {
            is.close();
        }
        assertTrue(result instanceof GifDrawable);
        assertTrue(((GifDrawable) result).getLoopCount() == 1);
    }

    public void testLoopOnceAnimatedGifAsByteArray() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets()
                .open("loop_once.gif", AssetManager.ACCESS_RANDOM);
        final byte[] data;
        try {
            data = toByteArray(is1);
        } finally {
            is1.close();
        }

        assertTrue(ImageFactory
                .isAnimatedGif(new BufferedInputStream(new ByteArrayInputStream(data))));
        final Drawable result = ImageFactory.decodeByteArray(context.getResources(), data);
        assertTrue(result instanceof GifDrawable);
        assertTrue(((GifDrawable) result).getLoopCount() == 1);
    }

    public void testLoopedAnimatedGifAsAssetInputStream() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets()
                .open("w3c_home_animation.gif", AssetManager.ACCESS_RANDOM);
        try {
            assertTrue(ImageFactory.isAnimatedGif(new BufferedInputStream(is1)));
        } finally {
            is1.close();
        }

        final InputStream is = context.getAssets()
                .open("w3c_home_animation.gif", AssetManager.ACCESS_RANDOM);
        final Drawable result;
        try {
            result = ImageFactory.decodeStream(context.getResources(), is);
        } finally {
            is.close();
        }
        assertTrue(result instanceof GifDrawable);
        assertTrue(((GifDrawable) result).getLoopCount() == 0);
    }

    public void testLoopedAnimatedGifAsByteArray() throws Throwable {
        final Context context = getInstrumentation().getContext();
        final InputStream is1 = context.getAssets()
                .open("w3c_home_animation.gif", AssetManager.ACCESS_RANDOM);
        final byte[] data;
        try {
            data = toByteArray(is1);
        } finally {
            is1.close();
        }

        assertTrue(ImageFactory
                .isAnimatedGif(new BufferedInputStream(new ByteArrayInputStream(data))));

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
