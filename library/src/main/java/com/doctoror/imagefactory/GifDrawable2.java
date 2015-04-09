package com.doctoror.imagefactory;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;

/**
 * {@link AnimationDrawable} that draws animated GIF from {@link GifDecoder2}
 */
public class GifDrawable2 extends AnimationDrawable {

    /**
     * Minimum delay
     */
    private static final int MIN_DELAY = 10;

    private final GifDecoder2 mGifDecoder;

    private final Movie mMovie;

    private final int mMovieDuration;
    private final int mMovieHeight;
    private final int mMovieWidth;
    private final Bitmap mTmpBitmap;
    private final BitmapDrawable mTmpDrawable;
    private final Canvas mTmpCanvas;

    private final int mMaxFrames;

    private int mFramesDrawn;

    private boolean mAnimationEnded;

    private int mMovieTime;

    public GifDrawable2(@NonNull final Resources res, @NonNull final GifDecoder2 gifDecoder,
            @NonNull final Movie movie) {
        mGifDecoder = gifDecoder;
        mMaxFrames = gifDecoder.loopCount * gifDecoder.frameCount;
        mMovie = movie;
        gifDecoder.advance();

        int movieDuration = movie.duration();
        if (movieDuration <= 0) {
            for (int i = 0; i < gifDecoder.frameCount; i++) {
                movieDuration += gifDecoder.getDelay(i);
            }
        }
        mMovieDuration = movieDuration;

        //System.out.println("c duration: " + mMovieDuration);
        //System.out.println("m duration: " + movie.duration());

        mMovieHeight = movie.height();
        mMovieWidth = movie.width();

        mTmpBitmap = Bitmap.createBitmap(mMovieWidth, mMovieHeight, Bitmap.Config.ARGB_8888);
        mTmpDrawable = new BitmapDrawable(res, mTmpBitmap);
        mTmpCanvas = new Canvas(mTmpBitmap);

        addFrame(mTmpDrawable, gifDecoder.getDelay(0));
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
        unscheduleSelf(this);
        if (!mAnimationEnded) {
            final long start = SystemClock.uptimeMillis();
            invalidateSelf();
            if (mGifDecoder.loopCount != 0 && mFramesDrawn >= mMaxFrames) {
                mAnimationEnded = true;
            } else {
                mGifDecoder.advance();
                final int frameDelay = mGifDecoder.getDelay(mGifDecoder.framePointer);
                final long now = SystemClock.uptimeMillis();
                final long drawTime = SystemClock.uptimeMillis() - start;
                final long calculatedDelay = Math.max(MIN_DELAY, frameDelay - drawTime);
                mMovieTime += frameDelay;
                scheduleSelf(this, now + calculatedDelay);
            }
            mFramesDrawn++;
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        if (mMovie.width() == 0 || mMovie.height() == 0) {
            return; // nothing to draw
        }

        if (mMovieTime > mMovieDuration) {
            mMovieTime -= mMovieDuration;
        }

        mMovie.setTime(mMovieTime);
        mTmpBitmap.eraseColor(Color.TRANSPARENT);
        mMovie.draw(mTmpCanvas, 0, 0);
        mTmpDrawable.draw(canvas);
    }

    @Override
    public int getIntrinsicWidth() {
        return mMovieWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mMovieHeight;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public void stop() {
        unscheduleSelf(this);
    }

    @Override
    public boolean isRunning() {
        return !mAnimationEnded;
    }
}
