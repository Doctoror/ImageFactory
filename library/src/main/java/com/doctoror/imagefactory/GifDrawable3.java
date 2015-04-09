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

import java.util.Locale;

/**
 * {@link AnimationDrawable} that draws animated GIF from {@link GifDecoder2}
 */
public class GifDrawable3 extends AnimationDrawable {

    private static final String TAG = "GifDrawable2";

    private static final boolean LOG_I = false;

    private static final int MIN_DELAY = 10;
    private static final int DELAY = 20;

    private final Movie mMovie;

    private final int mMovieDuration;
    private final int mMovieHeight;
    private final int mMovieWidth;
    private final Bitmap mTmpBitmap;
    private final BitmapDrawable mTmpDrawable;
    private final Canvas mTmpCanvas;

    private final int mLoopCount;

    private boolean mAnimationEnded;

    private final int mMaxMovieTime;

    private int mMovieTime;

    private boolean mAdvanceOnDraw;

    public GifDrawable3(@NonNull final Resources res, @NonNull final Movie movie, final int loopCount) {
        mMovie = movie;
        mLoopCount = loopCount;
        mMovieDuration = movie.duration();
        mMaxMovieTime = mMovieDuration * loopCount;

        //System.out.println("c duration: " + mMovieDuration);
        //System.out.println("m duration: " + movie.duration());

        mMovieHeight = movie.height();
        mMovieWidth = movie.width();

        mTmpBitmap = Bitmap.createBitmap(mMovieWidth, mMovieHeight, Bitmap.Config.ARGB_8888);
        mTmpDrawable = new BitmapDrawable(res, mTmpBitmap);
        mTmpCanvas = new Canvas(mTmpBitmap);

        addFrame(mTmpDrawable, DELAY);
    }

    @Override
    public void start() {
        // Don't ever call super.start()!
        if (!mAnimationEnded) {
            run();
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
            mMovieTime = 0;
            // Don't call start() here!
            System.out.println("restarted");
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
            mAdvanceOnDraw = true;
            invalidateSelf();
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        if (mMovie.width() == 0 || mMovie.height() == 0) {
            return; // nothing to draw
        }

        boolean drew = false;

        //System.out.println("draw");
        if (mAdvanceOnDraw && !mAnimationEnded) {
            mAdvanceOnDraw = false;
            //System.out.println("advancing");
            final long start = SystemClock.uptimeMillis();

            if (mLoopCount != 0 && mMovieTime >= mMaxMovieTime) {
                mAnimationEnded = true;
            } else {
                if (mMovieTime > mMovieDuration) {
                    if (LOG_I) {
                        System.out.println(
                                String.format(Locale.US, "Adjusting movie time from '%d' to '%d'",
                                        mMovieTime, mMovieTime -= mMovieDuration));
                    }
                    mMovieTime -= mMovieDuration;
                } else if (mMovieTime < 0) {
                    mMovieTime = 0;
                }

                mTmpBitmap.eraseColor(Color.TRANSPARENT);
                mMovie.setTime(mMovieTime);
                mMovie.draw(mTmpCanvas, 0, 0);
                mTmpDrawable.draw(canvas);
                drew = true;
                //System.out.println("Drew with time " + mMovieTime);

                final long now = SystemClock.uptimeMillis();
                final long drawTime = SystemClock.uptimeMillis() - start;
                final long calculatedDelay = Math.max(MIN_DELAY, DELAY - drawTime);
                mMovieTime += DELAY;
                //System.out.println("Scheduled " + calculatedDelay);
                scheduleSelf(this, now + calculatedDelay);
            }
        }
        if (!drew) {
            mTmpDrawable.draw(canvas);
        }
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
