/**
 * Copyright (c) 2013 Xcellent Creations, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.doctoror.imagefactory;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Reads frame data from a GIF image source and decodes it into individual frames
 * for animation purposes.  Image data can be read from either and InputStream source
 * or a byte[].
 */
//TODO make package-local
public final class GifDecoder2 {

    private static final String TAG = GifDecoder2.class.getSimpleName();

    /**
     * File read status: No errors.
     */
    public static final int STATUS_OK = 0;
    /**
     * File read status: Error decoding file (may be partially decoded)
     */
    public static final int STATUS_FORMAT_ERROR = 1;
    /**
     * File read status: Unable to open source.
     */
    public static final int STATUS_OPEN_ERROR = 2;
    /**
     * max decoder pixel stack size
     */
    protected static final int MAX_STACK_SIZE = 4096;

    /**
     * Global status code of GIF data parsing
     */
    protected int status;

    //Global File Header values and parsing flags
    protected int width; // full image width
    protected int height; // full image height
    protected boolean gctFlag; // global color table used
    protected int gctSize; // size of global color table
    protected int loopCount = 1; // iterations; 0 = repeat forever
    protected int[] gct; // global color table
    protected int bgIndex; // background color index
    protected int lctSize; // local color table size

    // Raw GIF data from input source
    protected ByteBuffer rawData;

    // Raw data read working array
    protected byte[] block = new byte[256]; // current data block
    protected int blockSize = 0; // block size last graphic control extension info

    // LZW decoder working arrays
    protected short[] prefix;

    protected ArrayList<GifFrame> frames; // frames read from current file
    protected GifFrame currentFrame;

    protected int framePointer;
    protected int frameCount;

    /**
     * Inner model class housing metadata for each frame
     */
    private static class GifFrame {

        public int ix, iy, iw, ih;
        public int delay;
        public int bufferFrameStart;

    }

    /**
     * Move the animation frame counter forward
     */
    public void advance() {
        framePointer = (framePointer + 1) % frameCount;
    }

    /**
     * Gets display duration for specified frame.
     *
     * @param n int index of frame
     * @return delay in milliseconds
     */
    public int getDelay(int n) {
        int delay = -1;
        if ((n >= 0) && (n < frameCount)) {
            delay = frames.get(n).delay;
        }
        return delay;
    }

    /**
     * Gets the number of frames read from file.
     *
     * @return frame count
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Gets the "Netscape" iteration count, if any. A count of 0 means repeat indefinitiely.
     *
     * @return iteration count if one was specified, else 1.
     */
    public int getLoopCount() {
        return loopCount;
    }

    /**
     * Reads GIF image from stream
     *
     * @param is containing GIF file.
     * @return read status code (0 = no errors)
     */
    public int read(InputStream is, int contentLength) {
        if (is != null) {
            try {
                int capacity = (contentLength > 0) ? (contentLength + 4096) : 4096;
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(capacity);
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();

                read(buffer.toByteArray());
            } catch (IOException e) {
                Log.w(TAG, "Error reading data from stream", e);
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                    Log.w(TAG, "Error closing stream", e);
                }
            }
        } else {
            status = STATUS_OPEN_ERROR;
        }

        return status;
    }

    public int getStatus() {
        return status;
    }

    /**
     * Reads GIF image from byte array
     *
     * @param data containing GIF file.
     * @return read status code (0 = no errors)
     */
    public int read(byte[] data) {
        init();
        if (data != null) {
            //Initiliaze the raw data buffer
            rawData = ByteBuffer.wrap(data);
            rawData.rewind();
            rawData.order(ByteOrder.LITTLE_ENDIAN);

            readHeader();
            if (!err()) {
                readContents();
                if (frameCount < 0) {
                    status = STATUS_FORMAT_ERROR;
                }
            }
        } else {
            status = STATUS_OPEN_ERROR;
        }

        return status;
    }

    /**
     * Skips bitmap data
     */
    protected void skipBitmapData(GifFrame frame) {
        if (frame != null) {
            //Jump to the frame start position
            rawData.position(frame.bufferFrameStart);
        }

        int nullCode = -1;
        int npix = (frame == null) ? width * height : frame.iw * frame.ih;
        int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits,
                code, count, i, datum, data_size, top, bi;

        // Initialize GIF data stream decoder.
        data_size = read();
        clear = 1 << data_size;
        end_of_information = clear + 1;
        available = clear + 2;
        old_code = nullCode;
        code_size = data_size + 1;
        code_mask = (1 << code_size) - 1;

        if (prefix == null) {
            prefix = new short[MAX_STACK_SIZE];
        }

        // Decode GIF pixel stream.
        datum = bits = count = top = bi = 0;
        for (i = 0; i < npix; ) {
            if (top == 0) {
                if (bits < code_size) {
                    // Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = readBlock();
                        if (count <= 0) {
                            break;
                        }
                        bi = 0;
                    }
                    datum += (((int) block[bi]) & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                // Get the next code.
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;
                // Interpret the code
                if ((code > available) || (code == end_of_information)) {
                    break;
                }
                if (code == clear) {
                    // Reset decoder.
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    available = clear + 2;
                    old_code = nullCode;
                    continue;
                }
                if (old_code == nullCode) {
                    old_code = code;
                    continue;
                }
                in_code = code;
                if (code == available) {
                    code = old_code;
                }
                while (code > clear) {
                    code = prefix[code];
                }
                // Add a new string to the string table,
                if (available >= MAX_STACK_SIZE) {
                    break;
                }
                prefix[available] = (short) old_code;
                available++;
                if (((available & code_mask) == 0) && (available < MAX_STACK_SIZE)) {
                    code_size++;
                    code_mask += available;
                }
                old_code = in_code;
            }
            // Pop a pixel off the pixel stack.
            top--;
            i++;
        }
    }

    /**
     * Returns true if an error was encountered during reading/decoding
     */
    protected boolean err() {
        return status != STATUS_OK;
    }

    /**
     * Initializes or re-initializes reader
     */
    protected void init() {
        status = STATUS_OK;
        frameCount = 0;
        framePointer = -1;
        frames = new ArrayList<>();
        gct = null;
    }

    /**
     * Reads a single byte from the input stream.
     */
    protected int read() {
        int curByte = 0;
        try {
            curByte = (rawData.get() & 0xFF);
        } catch (Exception e) {
            Log.w(TAG, "read() error ", e);
            status = STATUS_FORMAT_ERROR;
        }
        return curByte;
    }

    /**
     * Reads next variable length block from input.
     *
     * @return number of bytes stored in "buffer"
     */
    protected int readBlock() {
        blockSize = read();
        int n = 0;
        if (blockSize > 0) {
            try {
                int count;
                while (n < blockSize) {
                    count = blockSize - n;
                    rawData.get(block, n, count);

                    n += count;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error Reading Block", e);
                status = STATUS_FORMAT_ERROR;
            }
        }
        return n;
    }

    /**
     * Skip color table
     */
    protected void skipColorTable(final int ncolors) {
        skip(3 * ncolors);
    }

    private void skip(final int num) {
        try {
            rawData.position(rawData.position() + num);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "skip() reached EOF");
        }
    }

    /**
     * Main file parser. Reads GIF content blocks.
     */
    protected void readContents() {
        // read GIF file content blocks
        boolean done = false;
        while (!(done || err())) {
            int code = read();
            switch (code) {
                case 0x2C: // image separator
                    skipFrameImage();
                    break;
                case 0x21: // extension
                    code = read();
                    switch (code) {
                        case 0xf9: // graphics control extension
                            //Start a new frame
                            currentFrame = new GifFrame();
                            readGraphicControlExt();
                            break;
                        case 0xff: // application extension
                            readBlock();
                            String app = "";
                            for (int i = 0; i < 11; i++) {
                                app += (char) block[i];
                            }
                            if (app.equals("NETSCAPE2.0")) {
                                readNetscapeExt();
                            } else {
                                skip(); // don't care
                            }
                            break;
                        case 0xfe:// comment extension
                            skip();
                            break;
                        case 0x01:// plain text extension
                            skip();
                            break;
                        default: // uninteresting extension
                            skip();
                    }
                    break;
                case 0x3b: // terminator
                    done = true;
                    break;
                case 0x00: // bad byte, but keep going and see what happens break;
                default:
                    Log.w(TAG, "readContents() FORMAT_ERROR");
                    status = STATUS_FORMAT_ERROR;
            }
        }
    }

    /**
     * Reads GIF file header information.
     */
    protected void readHeader() {
        String id = "";
        for (int i = 0; i < 6; i++) {
            id += (char) read();
        }
        if (!id.startsWith("GIF")) {
            Log.w(TAG, "readHeader() header is not a GIF");
            status = STATUS_FORMAT_ERROR;
            return;
        }
        readLSD();
        if (gctFlag && !err()) {
            skipColorTable(gctSize);
        }
    }

    /**
     * Reads Graphics Control Extension values
     */
    protected void readGraphicControlExt() {
        read(); // block size
        read(); // packed fields
        currentFrame.delay = readShort() * 10; // delay in milliseconds
        read(); // transparent color index
        read(); // block terminator
    }

    /**
     * Skips next frame image
     */
    protected void skipFrameImage() {
        currentFrame.ix = readShort(); // (sub)image position & size
        currentFrame.iy = readShort();
        currentFrame.iw = readShort();
        currentFrame.ih = readShort();

        final int packed = read();
        final boolean lctFlag = (packed & 0x80) != 0; // 1 - local color table flag interlace
        lctSize = (int) Math.pow(2, (packed & 0x07) + 1);
        if (lctFlag) {
            skipColorTable(lctSize);
        }

        currentFrame.bufferFrameStart = rawData.position(); //Save this as the decoding position pointer

        skipBitmapData(null); // false decode pixel data to advance buffer
        skip();
        if (err()) {
            return;
        }

        frameCount++;
        frames.add(currentFrame); // add image to frame
    }

    /**
     * Reads Logical Screen Descriptor
     */
    protected void readLSD() {
        // logical screen size
        width = readShort();
        height = readShort();
        // packed fields
        int packed = read();
        gctFlag = (packed & 0x80) != 0; // 1 : global color table flag
        // 2-4 : color resolution
        // 5 : gct sort flag
        gctSize = 2 << (packed & 7); // 6-8 : gct size
        bgIndex = read(); // background color index
        read(); // pixel aspect ratio
    }

    /**
     * Reads Netscape extension to obtain iteration count
     */
    protected void readNetscapeExt() {
        do {
            readBlock();
            if (block[0] == 1) {
                // loop count sub-block
                int b1 = ((int) block[1]) & 0xff;
                int b2 = ((int) block[2]) & 0xff;
                loopCount = (b2 << 8) | b1;
            }
        } while ((blockSize > 0) && !err());
    }

    /**
     * Reads next 16-bit value, LSB first
     */
    protected int readShort() {
        // read 16-bit value
        return rawData.getShort();
    }

    /**
     * Skips variable length blocks up to and including next zero length block.
     */
    protected void skip() {
        do {
            readBlock();
        } while ((blockSize > 0) && !err());
    }
}