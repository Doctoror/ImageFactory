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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Used for decoding regular images or animated GIF into a {@link Drawable}
 * The interface is much like {@link BitmapFactory}.
 */
@SuppressWarnings("UnusedDeclaration")
public final class ImageFactory {

    private ImageFactory() {
        // Private constructor, do not instantiate
    }

    private static final String TAG = "ImageFactory";

    /**
     * Decodes image from byte array.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res  Resources to use if creating a BitmapDrawable
     * @param data byte array of compressed image data
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException if the data byte array is null
     */
    @Nullable
    public static Drawable decodeByteArray(@NonNull final Resources res,
            final byte[] data) {
        return decodeByteArray(res, data, null);
    }

    /**
     * Decodes image from byte array.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res     Resources to use if creating a BitmapDrawable
     * @param data    byte array of compressed image data
     * @param options optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException if the data byte array is null
     */
    @Nullable
    public static Drawable decodeByteArray(@Nullable final Resources res,
            final byte[] data,
            @Nullable final BitmapFactory.Options options) {
        try {
            return decodeByteArrayOrThrow(res, data, options);
        } catch (IOException e) {
            Log.w(TAG, "decodeByteArray() " + e);
            return null;
        }
    }

    /**
     * Decodes image from byte array.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res     Resources to use if creating a BitmapDrawable
     * @param data    byte array of compressed image data
     * @param options optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable}
     * @throws IOException          on error
     * @throws NullPointerException if the data byte array is null
     */
    @NonNull
    public static Drawable decodeByteArrayOrThrow(@Nullable final Resources res,
            final byte[] data,
            @Nullable final BitmapFactory.Options options) throws IOException {
        if (data == null) {
            throw new NullPointerException("data byte array must not be null");
        }
        final boolean animated = isAnimatedGif(
                new BufferedInputStream(new ByteArrayInputStream(data)));
        if (animated) {
            return new GifDrawable(data);
        } else {
            final Bitmap decoded = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (decoded == null) {
                throw new IOException("BitmapFactory returned null");
            }
            return new BitmapDrawable(res, decoded);
        }
    }

    /**
     * Decodes image from InputStream.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res Resources to use if creating a BitmapDrawable
     * @param is  The input stream that holds the raw data to be decoded into a Drawable
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException if the InputStream is null
     */
    @Nullable
    public static Drawable decodeStream(@Nullable final Resources res, final InputStream is) {
        return decodeStream(res, is, null, null);
    }

    /**
     * Decodes image from InputStream.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res        Resources to use if creating a BitmapDrawable
     * @param is         The input stream that holds the raw data to be decoded into a drawable
     * @param outPadding optional outPadding if an image will be decoded to a Bitmap
     * @param options    optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException if the InputStream is null
     */
    @Nullable
    public static Drawable decodeStream(@Nullable final Resources res,
            final InputStream is,
            @Nullable final Rect outPadding,
            @Nullable final BitmapFactory.Options options) {
        try {
            return decodeStreamOrThrow(res, is, outPadding, options);
        } catch (IOException e) {
            Log.w(TAG, "decodeStream: " + e);
            return null;
        }
    }

    /**
     * Decodes image from InputStream.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     *
     * @param res        Resources to use if creating a BitmapDrawable
     * @param is         The input stream that holds the raw data to be decoded into a drawable
     * @param outPadding optional outPadding if an image will be decoded to a Bitmap
     * @param options    optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable}
     * @throws IOException          on error
     * @throws NullPointerException if the InputStream is null
     */
    @NonNull
    public static Drawable decodeStreamOrThrow(@Nullable final Resources res,
            final InputStream is,
            @Nullable final Rect outPadding,
            @Nullable final BitmapFactory.Options options) throws IOException {
        if (is == null) {
            throw new NullPointerException("InputStream must not be null");
        }

        // android-gif-drawable gives libc crash or not decoding properly if it's not a BufferedInputStream.
        final BufferedInputStream bis;
        if (is instanceof BufferedInputStream) {
            bis = (BufferedInputStream) is;
        } else {
            bis = new BufferedInputStream(is);
        }
        bis.mark(Integer.MAX_VALUE);
        final boolean animated = isAnimatedGif(bis);
        bis.reset();
        if (animated) {
            return new GifDrawable(bis);
        } else {
            final Bitmap decoded = BitmapFactory.decodeStream(is, outPadding, options);
            if (decoded == null) {
                throw new IOException("BitmapFactory returned null");
            }
            return new BitmapDrawable(res, decoded);
        }
    }

    /**
     * Decodes image from file path.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res      Resources to use if creating a BitmapDrawable
     * @param filePath complete path for the file to be decoded.
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException     if the file path is null
     * @throws IllegalArgumentException if the file path is empty
     */
    @Nullable
    public static Drawable decodeFile(@Nullable final Resources res, final String filePath) {
        return decodeFile(res, filePath, null);
    }

    /**
     * Decodes image from file path.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res      Resources to use if creating a BitmapDrawable
     * @param filePath complete path for the file to be decoded.
     * @param options  optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException     if the file path is null
     * @throws IllegalArgumentException if the file path is empty
     */
    @Nullable
    public static Drawable decodeFile(@Nullable final Resources res,
            final String filePath,
            @Nullable final BitmapFactory.Options options) {
        try {
            return decodeFileOrThrow(res, filePath, options);
        } catch (IOException e) {
            Log.w(TAG, "decodeFile: " + e);
            return null;
        }
    }

    /**
     * Decodes image from file path.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     *
     * @param res      Resources to use if creating a BitmapDrawable
     * @param filePath complete path for the file to be decoded.
     * @param options  optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable}
     * @throws IOException              on error
     * @throws NullPointerException     if the file path is null
     * @throws IllegalArgumentException if the file path is empty
     */
    @NonNull
    public static Drawable decodeFileOrThrow(@Nullable final Resources res,
            final String filePath,
            @Nullable final BitmapFactory.Options options) throws IOException {
        if (filePath == null) {
            throw new NullPointerException("filePath must not be null");
        }
        if (filePath.isEmpty()) {
            throw new IllegalArgumentException("filePath must not be empty");
        }
        return decodeStreamOrThrow(res, new FileInputStream(filePath), null, options);
    }

    /**
     * Decodes image from FileDescriptor.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res Resources to use if creating a BitmapDrawable
     * @param fd  The file descriptor containing the data to decode
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException if FileDescriptor is null
     */
    @Nullable
    public static Drawable decodeFileDescriptor(@Nullable final Resources res,
            final FileDescriptor fd) {
        return decodeFileDescriptor(res, fd, null, null);
    }

    /**
     * Decodes image from FileDescriptor.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res        Resources to use if creating a BitmapDrawable
     * @param fd         The file descriptor containing the data to decode
     * @param outPadding optional outPadding if an image will be decoded as Bitmap
     * @param options    optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException if FileDescriptor is null
     */
    @Nullable
    public static Drawable decodeFileDescriptor(@Nullable final Resources res,
            final FileDescriptor fd,
            @Nullable final Rect outPadding,
            @Nullable final BitmapFactory.Options options) {
        try {
            return decodeFileDescriptorOrThrow(res, fd, outPadding, options);
        } catch (IOException e) {
            Log.w(TAG, "decodeFileDescriptor() " + e);
            return null;
        }
    }

    /**
     * Decodes image from FileDescriptor.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     *
     * @param res        Resources to use if creating a BitmapDrawable
     * @param fd         The file descriptor containing the data to decode
     * @param outPadding optional outPadding if an image will be decoded as Bitmap
     * @param options    optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable}
     * @throws IOException          on error
     * @throws NullPointerException if FileDescriptor is null
     */
    @NonNull
    public static Drawable decodeFileDescriptorOrThrow(@Nullable final Resources res,
            final FileDescriptor fd,
            @Nullable final Rect outPadding,
            @Nullable final BitmapFactory.Options options) throws IOException {
        if (fd == null) {
            throw new NullPointerException("FileDescriptor must not be null");
        }
        return decodeStreamOrThrow(res, new FileInputStream(fd), outPadding, options);
    }

    /**
     * Decodes image from resource.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res The resources object containing the image data
     * @param id  The resource id of the image data
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException        if Resources is null
     * @throws Resources.NotFoundException if resource under the given id does not exist
     */
    @Nullable
    public static Drawable decodeResource(final Resources res, @DrawableRes @RawRes final int id) {
        return decodeResource(res, id, null);
    }

    /**
     * Decodes image from resource.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     * Returns null on error.
     *
     * @param res     The resources object containing the image data
     * @param id      The resource id of the image data
     * @param options optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable} or null on error
     * @throws NullPointerException        if Resources is null
     * @throws Resources.NotFoundException if resource under the given id does not exist
     */
    @Nullable
    public static Drawable decodeResource(final Resources res, @DrawableRes @RawRes final int id,
            @Nullable final BitmapFactory.Options options) {
        try {
            return decodeResourceOrThrow(res, id, options);
        } catch (IOException e) {
            Log.w(TAG, "decodeResource(): " + e);
            return null;
        }
    }

    /**
     * Decodes image from resource.
     * Returns {@link GifDrawable} if the image is an animated GIF.
     * Returns {@link BitmapDrawable} if the image is s valid static image {@link BitmapFactory}
     * can decode.
     *
     * @param res     The resources object containing the image data
     * @param id      The resource id of the image data
     * @param options optional options if an image will be decoded to a Bitmap
     * @return decoded {@link Drawable}
     * @throws IOException                 on error
     * @throws NullPointerException        if Resources is null
     * @throws Resources.NotFoundException if resource under the given id does not exist
     */
    @NonNull
    public static Drawable decodeResourceOrThrow(final Resources res,
            @DrawableRes @RawRes final int id,
            @Nullable final BitmapFactory.Options options) throws IOException {
        if (res == null) {
            throw new NullPointerException("Resources must not be null");
        }
        return decodeStreamOrThrow(res, res.openRawResource(id), null, options);
    }

    /**
     * Detects animated GIF.
     *
     * @param is InputStream pointing to data to analyze
     * @return true, if the reader's content is an animated gif. False if not a gif or not animated
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isAnimatedGif(@NonNull final BufferedInputStream is)
            throws IOException {
        final byte h1 = (byte) is.read();
        final byte h2 = (byte) is.read();
        final byte h3 = (byte) is.read();

        //False inspection. Why?
        //noinspection ConstantConditions
        if (h1 != 'G' || h2 != 'I' || h3 != 'F') {
            return false;
        }

        final byte v1 = (byte) is.read();
        final byte v2 = (byte) is.read();
        final byte v3 = (byte) is.read();

        if (v1 != '8' || (v2 != '7' && v2 != '9') || v3 != 'a') {
            return false;
        }

        is.skip(2); // logical screen width
        is.skip(2); // logical screen height

        // read as unsigned int 8
        final short flags = (short) (is.read() & 0xFF);

        // First three bits = (BPP - 1)
        final int colorTableSize = 1 << ((flags & 7) + 1);

        // 89a
//            if (v2 == '9') {
//                boolean isColorTableSorted = (flags & 8) != 0;
//            }

        //final int bitsPerPixel = ((flags & 0x70) >> 4) + 1;
        final boolean hasGlobalColorTable = (flags & 0xf) != 0;

        is.skip(1); // background color index
        is.skip(1); // aspect ratio byte

        if (hasGlobalColorTable) {
            is.skip(colorTableSize * 3);
        }

        while (true) {
            int code = is.read() & 0xff;
            switch (code) {
                case 0x2c:
                    // an image block
                    return false;

                case 0x21:
                    // extension
                    code = is.read() & 0xff;
                    switch (code) {
                        case 0xf9:
                            return true;

                        case 0xff: // application extension
                            return true;

                        case 0xfe:// comment extension
                            skip(is);
                            break;

                        case 0x01:// plain text extension
                            skip(is);
                            break;

                        default: // uninteresting extension
                            skip(is);
                            break;
                    }
                    break;

                case 0x3b: // terminator
                default:
                    return false;
            }
        }
    }

    /**
     * Skips variable length blocks up to and including next zero length block.
     */

    private static void skip(@NonNull final BufferedInputStream reader) throws IOException {
        int blockSize;
        do {
            blockSize = reader.read() & 0xff;
            if (blockSize > 0) {
                try {
                    int n = 0;
                    int count;
                    while (n < blockSize) {
                        count = blockSize - n;
                        n += reader.skip(count);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error Reading Block", e);
                    throw new IOException("Format error " + e);
                }
            }
        } while ((blockSize > 0));
    }
}
