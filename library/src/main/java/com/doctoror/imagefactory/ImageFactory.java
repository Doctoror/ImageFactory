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
import android.graphics.Movie;
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
                is.mark(Integer.MAX_VALUE);
                final int animatedGifLoopCount = getAnimatedGifLoopCount(new StreamReader(is));
                is.reset();
                if (animatedGifLoopCount != -1) {
                    return decodeAnimatedGif(res, is, animatedGifLoopCount);
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
        final int animatedGifLoopCount = getAnimatedGifLoopCountCaught(
                new SequentialByteArrayReader(data));
        if (animatedGifLoopCount != -1) {
            return decodeAnimatedGif(res, data, animatedGifLoopCount);
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
    public static Drawable decodeAnimatedGifDeprecated(@NonNull final Resources res,
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

    /**
     * Returns decoded {@link GifDrawable}. Returns null on error.
     *
     * @param res  Resources
     * @param data InputStream for image
     * @return decoded {@link GifDrawable} or null on error
     */
    @Nullable
    public static Drawable decodeAnimatedGif(@NonNull final Resources res,
            @NonNull final InputStream data,
            final int animatedGifLoopCount) {
        //final GifDecoder2 gifDecoder = new GifDecoder2();
        //try {
//            data.mark(Integer.MAX_VALUE);
//            final int status = gifDecoder.read(data, data.available());
//            data.reset();
//            if (!checkGifDecoderStatus(status)) {
//                return null;
//            }
        final Movie movie = Movie.decodeStream(data);
        if (movie == null || movie.width() <= 0 || movie.height() <= 0) {
            return null;
        }
        return new GifDrawable3(res, movie, animatedGifLoopCount);
//        } catch (IOException e) {
//            Log.w(TAG, "decodeAnimatedGif: " + e);
//            return null;
//        }
    }

    @Nullable
    public static Drawable decodeAnimatedGifDeprecated(@NonNull final Resources res,
            @NonNull final byte[] data) {
        final GifDecoder gifDecoder = new GifDecoder();
        final int status = gifDecoder.read(data);
        if (!checkGifDecoderStatus(status)) {
            return null;
        }
        return new GifDrawable(res, gifDecoder);
    }

    @Nullable
    public static Drawable decodeAnimatedGif(@NonNull final Resources res,
            @NonNull final byte[] data,
            final int animatedGifLoopCount) {
//        final GifDecoder2 gifDecoder = new GifDecoder2();
//        final int status = gifDecoder.read(data);
//        if (!checkGifDecoderStatus(status)) {
//            return null;
//        }
        final Movie movie = Movie.decodeByteArray(data, 0, data.length);
        if (movie == null || movie.width() <= 0 || movie.height() <= 0) {
            return null;
        }
        return new GifDrawable3(res, movie, animatedGifLoopCount);
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
            Log.w(TAG, "checkGifDecoderStatus() failed: status not OK: " + status);
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
    public static int getAnimatedGifLoopCountCaught(@NonNull final SequentialReader reader) {
        try {
            return getAnimatedGifLoopCount(reader);
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Detects animated GIF.
     *
     * @param reader Reader pointing to data to analyze
     * @return -1 if not animated or not a GIF, otherwise returns NETSCAPE2.0 loop count
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static int getAnimatedGifLoopCount(@NonNull final SequentialReader reader)
            throws IOException {
        final byte h1 = reader.getByte();
        final byte h2 = reader.getByte();
        final byte h3 = reader.getByte();

        //False inspection. Why?
        //noinspection ConstantConditions
        if (h1 != 'G' || h2 != 'I' || h3 != 'F') {
            return -1;
        }

        final byte v1 = reader.getByte();
        final byte v2 = reader.getByte();
        final byte v3 = reader.getByte();

        if (v1 != '8' || (v2 != '7' && v2 != '9') || v3 != 'a') {
            return -1;
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

        final byte[] block = new byte[256];
        while (true) {
            int code = reader.getByte() & 0xff;
            switch (code) {
                case 0x2c:
                    // an image block
                    return -1;

                case 0x21:
                    // extension
                    code = reader.getByte() & 0xff;
                    switch (code) {
                        case 0xf9:
                            return 1;

                        case 0xff: // application extension
                            readBlock(reader, block);
                            final String app = new String(block, 0, 11);
                            if (app.equals("NETSCAPE2.0")) {
                                return readNetscapeExt(reader, block);
                            } else {
                                return 1;
                            }

                        case 0xfe:// comment extension
                            skip(reader);
                            break;

                        case 0x01:// plain text extension
                            skip(reader);
                            break;

                        default: // uninteresting extension
                            skip(reader);
                            break;
                    }
                    break;

                case 0x3b: // terminator
                default:
                    return -1;
            }
        }
    }

    /**
     * Skips variable length blocks up to and including next zero length block.
     */

    private static void skip(@NonNull final SequentialReader reader) throws IOException {
        int blockSize;
        do {
            blockSize = reader.getByte() & 0xff;
            if (blockSize > 0) {
                try {
                    int n = 0;
                    int count;
                    while (n < blockSize) {
                        count = blockSize - n;
                        reader.skip(count);
                        n += count;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error Reading Block", e);
                    throw new IOException("Format error " + e);
                }
            }
        } while ((blockSize > 0));
    }

    /**
     * Reads next variable length block from input.
     */
    private static int readBlock(@NonNull final SequentialReader reader,
            @NonNull final byte[] block) throws IOException {
        final int blockSize = reader.getByte() & 0xff;
        int n = 0;
        if (blockSize > 0) {
            try {
                int count;
                while (n < blockSize) {
                    count = blockSize - n;
                    final byte[] read = reader.getBytes(count);
                    System.arraycopy(read, 0, block, n, count);

                    n += count;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error Reading Block", e);
                throw new IOException("Error Reading Block");
            }
        }
        return blockSize;
    }

    /**
     * Reads Netscape extension to obtain iteration count
     *
     * @return loop count
     */
    private static int readNetscapeExt(@NonNull final SequentialReader reader,
            @NonNull final byte[] block) throws IOException {
        int blockSize;
        do {
            blockSize = readBlock(reader, block);
            if (block[0] == 1) {
                // loop count sub-block
                int b1 = ((int) block[1]) & 0xff;
                int b2 = ((int) block[2]) & 0xff;
                return (b2 << 8) | b1;
            }
        } while (blockSize > 0);
        return 0;
    }
}
