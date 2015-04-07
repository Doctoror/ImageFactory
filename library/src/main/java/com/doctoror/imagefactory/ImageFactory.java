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

package com.doctoror.imagefactory;

import com.drew.lang.SequentialByteArrayReader;
import com.drew.lang.SequentialReader;
import com.drew.lang.StreamReader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Used for decoding regular images or animated GIF
 */
public final class ImageFactory {

    private ImageFactory() {

    }

    private static final String TAG = "ImageFactory";

    /**
     * Decodes image from InputStream.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can
     * decode.
     * Returns {@link AnimationDrawable} if the image is an animated GIF.
     * Returns null on error.
     */
    @Nullable
    public static Drawable decodeImage(@NonNull final Resources res,
            @NonNull final InputStream is) {
        try {
            if (is.markSupported()) {
                is.mark(-1);
                final boolean isAnimatedGif = isAnimatedGif(new StreamReader(is));
                is.reset();
                if (isAnimatedGif) {
                    return decodeAnimatedGif(res, is);
                } else {
                    final Bitmap decoded = BitmapFactory.decodeStream(is);
                    return decoded == null ? null : new BitmapDrawable(res, decoded);
                }
            } else {
                // Mark not supported, read everything to bytes and then proceed
                final int available = is.available();
                ByteArrayOutputStream os = new ByteArrayOutputStream(
                        available > 0 ? available : 4096);
                final byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                    os.write(buffer, 0, read);
                }
                final byte[] data = os.toByteArray();
                // Free-up ByteArrayOutputStream data clone
                //noinspection UnusedAssignment
                os = null;
                return decodeImage(res, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decodes image from byte array.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can
     * decode.
     * Returns {@link AnimationDrawable} if the image is an animated GIF.
     * Returns null on error.
     */
    @Nullable
    public static Drawable decodeImage(@NonNull final Resources res, @Nullable final byte[] data) {
        if (data == null) {
            return null;
        }
        final boolean isAnimatedGif = isAnimatedGifCaught(new SequentialByteArrayReader(data));
        if (isAnimatedGif) {
            return decodeAnimatedGif(res, data);
        } else {
            final Bitmap decoded = BitmapFactory.decodeByteArray(data, 0, data.length);
            return decoded == null ? null : new BitmapDrawable(res, decoded);
        }
    }

    /**
     * Returns decoded {@link GifDrawable}. Returns null on error.
     *
     * @param res  Resources
     * @param data InputStream for image
     * @return decoded {@link GifDrawable} or null on error
     */
    @Nullable
    private static Drawable decodeAnimatedGif(@NonNull final Resources res,
            @NonNull final InputStream data) {
        final GifDecoder gifDecoder = new GifDecoder();
        try {
            final int status = gifDecoder.read(data, data.available());
            if (!checkGifDecoderStatus(status)) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return new GifDrawable(res, gifDecoder);
    }

    @Nullable
    private static Drawable decodeAnimatedGif(@NonNull final Resources res,
            @NonNull final byte[] data) {
        final GifDecoder gifDecoder = new GifDecoder();
        final int status = gifDecoder.read(data);
        if (!checkGifDecoderStatus(status)) {
            return null;
        }
        return new GifDrawable(res, gifDecoder);
    }

    private static boolean checkGifDecoderStatus(final int status) {
        switch (status) {
            case GifDecoder.STATUS_OPEN_ERROR:
                Log.w(TAG, "checkGifDecoderStatus() failed: STATUS_OPEN_ERROR");
                return false;

            case GifDecoder.STATUS_FORMAT_ERROR:
                Log.w(TAG, "checkGifDecoderStatus() failed: STATUS_FORMAT_ERROR");
                return false;
        }
        if (status != GifDecoder.STATUS_OK) {
            Log.w(TAG, "checkGifDecoderStatus() failed: statis not OK: " + status);
            return false;
        }
        return true;
    }

    /**
     * Detects animated GIF.
     *
     * @param reader Reader pointing to data to analyze
     * @return true, if animated gif detected. False if is not a gif, not animated or an error
     * occurred.
     */
    public static boolean isAnimatedGifCaught(@NonNull final SequentialReader reader) {
        try {
            return isAnimatedGif(reader);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Detects animated GIF.
     *
     * @param reader Reader pointing to data to analyze
     * @return true, if animated gif detected. False if is not a gif or not animated.
     */
    public static boolean isAnimatedGif(@NonNull final SequentialReader reader) throws IOException {
        final byte h1 = reader.getByte();
        final byte h2 = reader.getByte();
        final byte h3 = reader.getByte();

        //False inspection. Why?
        //noinspection ConstantConditions
        if (h1 != 'G' || h2 != 'I' || h3 != 'F') {
            return false;
        }

        final byte v1 = reader.getByte();
        final byte v2 = reader.getByte();
        final byte v3 = reader.getByte();

        if (v1 != '8' || (v2 != '7' && v2 != '9') || v3 != 'a') {
            return false;
        }

        reader.skip(2); // logical screen width
        reader.skip(2); // logical screen height

        final short flags = reader.getUInt8();

        // First three bits = (BPP - 1)
        final int colorTableSize = 1 << ((flags & 7) + 1);

        // 89a
//            if (v2 == '9') {
//                boolean isColorTableSorted = (flags & 8) != 0;
//            }

        //final int bitsPerPixel = ((flags & 0x70) >> 4) + 1;
        final boolean hasGlobalColorTable = (flags & 0xf) != 0;

        reader.skip(1); // background color index
        reader.skip(1); // aspect ratio byte

        if (hasGlobalColorTable) {
            reader.skip(colorTableSize * 3);
        }

        final byte imageBlockByte = reader.getByte();
        if (imageBlockByte == 0x2c) {
            // Is an image block
            return false;
        }
        if (imageBlockByte != 0x21) {
            // If not an image block then 0x21. If not 0x21 then it's something unexpected.
            return false;
        }
        final byte appExtensionOrGraphicBlock = reader.getByte();
        // Return true if is Application Extension
        return appExtensionOrGraphicBlock == -1;
    }
}
