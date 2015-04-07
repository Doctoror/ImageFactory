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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Uses {@link AnimatableDelegate} to automatically start and stop {@link Animatable} background and
 * image
 */
public class AnimatableImageView extends ImageView {

    private final AnimatableDelegate mDelegate;

    private boolean mAttachedToWindow;

    public AnimatableImageView(final Context context) {
        super(context);
        mDelegate = new AnimatableDelegate();
    }

    public AnimatableImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mDelegate = new AnimatableDelegate();
    }

    public AnimatableImageView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDelegate = new AnimatableDelegate();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatableImageView(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDelegate = new AnimatableDelegate();
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);
        mDelegate.onDraw(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDelegate.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDelegate.onDetachedFromWindow();
    }

    @Override
    public void setImageDrawable(final Drawable drawable) {
        super.setImageDrawable(drawable);
        mDelegate.setImageDrawable(drawable);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBackgroundDrawable(final Drawable drawable) {
        super.setImageDrawable(drawable);
        mDelegate.setBackground(drawable);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setBackground(final Drawable drawable) {
        super.setBackground(drawable);
        mDelegate.setBackground(drawable);
    }
}
