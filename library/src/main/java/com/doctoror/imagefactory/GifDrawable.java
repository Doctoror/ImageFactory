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
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;

/**
 * {@link AnimationDrawable} that draws animated GIF from {@link GifDecoder}
 */
public class GifDrawable extends AnimationDrawable {

    /**
     * Minimum delay
     */
    private static final int MIN_DELAY = 10;

    private final GifDecoder mGifDecoder;

    private final BitmapDrawable mCurrentDrawable;

    private final int mMaxFrames;

    private int mFramesDrawn;

    private boolean mAnimationEnded;

    public GifDrawable(@NonNull final Resources res, final GifDecoder gifDecoder) {
        // One shot stops on wrong frame here, so set it always to false and use our own method
        setOneShot(false);
        mGifDecoder = gifDecoder;
        mGifDecoder.advance();
        mCurrentDrawable = new BitmapDrawable(res, mGifDecoder.getNextFrame());
        mFramesDrawn = 1;
        mMaxFrames = gifDecoder.loopCount * gifDecoder.frameCount;
        for (int i = 0; i < mGifDecoder.frameCount; i++) {
            addFrame(mCurrentDrawable, Math.max(MIN_DELAY, mGifDecoder.getDelay(i)));
        }
    }

    @Override
    public void start() {
        // Don't ever call super.start()!
        if (!mAnimationEnded) {
            scheduleSelf(this, SystemClock.uptimeMillis() + Math
                    .max(MIN_DELAY, mGifDecoder.getDelay(mGifDecoder.framePointer)));
        }
    }

    /**
     * If loop count has reached this will force-restart.
     * Restarts only if called after {@link #isAnimationEnded()} returned true.
     * Ignored if loop has not ended yet.
     */
    public void restart() {
        if (mAnimationEnded) {
            mAnimationEnded = false;
            mFramesDrawn = 0;
            // Don't call start() here!
            run();
        }
    }

    /**
     * Returns true if animation has ended (e.g. loop count reached)
     *
     * @return true if animation has ended, false if not started or not ended yet.
     */
    public boolean isAnimationEnded() {
        return mAnimationEnded;
    }

    @Override
    public void run() {
        if (!mAnimationEnded) {
            invalidateSelf();
            if (mGifDecoder.loopCount != 0 && mFramesDrawn >= mMaxFrames) {
                mAnimationEnded = true;
                unscheduleSelf(this);
            } else {
                final long start = SystemClock.uptimeMillis();
                final int frameDelay = mGifDecoder.getDelay(mGifDecoder.framePointer);
                mGifDecoder.advance();
                mGifDecoder.getNextFrame();
                final long uptime = SystemClock.uptimeMillis();
                final long drawFrameTime = uptime - start;
                scheduleSelf(this, uptime + Math.max(MIN_DELAY, frameDelay - drawFrameTime));
            }
            mFramesDrawn++;
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        mCurrentDrawable.draw(canvas);
    }
}
