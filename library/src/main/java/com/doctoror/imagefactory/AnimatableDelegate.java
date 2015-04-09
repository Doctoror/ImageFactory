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

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Helps starting and stopping {@link Animatable} from {@link View}
 */
public final class AnimatableDelegate {

    private WeakReference<Drawable> mBackground;
    private WeakReference<Drawable> mImage;

    private boolean mAttachedToWindow;

    @Nullable
    private Drawable getBackground() {
        if (mBackground != null) {
            return mBackground.get();
        }
        return null;
    }

    @Nullable
    private Drawable getDrawable() {
        if (mImage != null) {
            return mImage.get();
        }
        return null;
    }

    public void onDraw(@NonNull final Drawable.Callback callback) {
        final Drawable drawable = getDrawable();
        if (drawable instanceof Animatable) {
            if (drawable.getCallback() != callback) {
                drawable.setCallback(callback);
            }
        }

        final Drawable background = getBackground();
        if (background instanceof Animatable) {
            if (background.getCallback() != callback) {
                background.setCallback(callback);
            }
        }
    }

    public void onAttachedToWindow() {
        mAttachedToWindow = true;

        final Drawable background = getBackground();
        if (background instanceof Animatable) {
            ((Animatable) background).start();
        }

        final Drawable drawable = getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((GifDrawable3) drawable).restart();
                }
            }, 11000l);
        }
    }

    public void onDetachedFromWindow() {
        mAttachedToWindow = false;

        final Drawable background = getBackground();
        if (background instanceof Animatable) {
            ((Animatable) background).stop();
        }

        final Drawable drawable = getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).stop();
        }
    }

    /**
     * Should be called from {@link ImageView#setImageDrawable(Drawable)}
     *
     * @param drawable Drawable to set
     */
    public void setImageDrawable(final Drawable drawable) {
        final Drawable prev = getDrawable();
        if (prev != drawable && prev instanceof Animatable) {
            ((Animatable) prev).stop();
            prev.setCallback(null);
        }
        mImage = new WeakReference<>(drawable);
        if (mAttachedToWindow && drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    public void setBackground(final Drawable drawable) {
        final Drawable prev = getBackground();
        if (prev != drawable && prev instanceof Animatable) {
            ((Animatable) prev).stop();
            prev.setCallback(null);
        }
        mBackground = new WeakReference<>(drawable);
        if (mAttachedToWindow && drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }
}
