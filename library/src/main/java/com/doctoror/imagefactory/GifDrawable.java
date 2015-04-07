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
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;

/**
 * {@link AnimationDrawable} that draws animated GIF from {@link GifDecoder}
 */
// TODO handle loop count if not isOneShot
public class GifDrawable extends AnimationDrawable {

    /**
     * scheduleSelf executes with a slight delay, usually close to 12 ms
     */
    private static final int SCHEDULE_DELAY = 12;

    /**
     * Minimum delay
     */
    private static final int MIN_DELAY = 10;

    private final GifDecoder mGifDecoder;

    public GifDrawable(@NonNull final Resources res, final GifDecoder gifDecoder) {
        setOneShot(gifDecoder.getLoopCount() == 1);
        mGifDecoder = gifDecoder;
        mGifDecoder.advance();
        final BitmapDrawable frame = new BitmapDrawable(res, mGifDecoder.getNextFrame());
        final int frameCount = mGifDecoder.getFrameCount();
        for (int i = 0; i < frameCount; i++) {
            addFrame(frame, Math.max(MIN_DELAY, mGifDecoder.getDelay(i) - SCHEDULE_DELAY));
        }
    }

    @Override
    public void run() {
        mGifDecoder.advance();
        mGifDecoder.getNextFrame();
        super.run();
    }
}
