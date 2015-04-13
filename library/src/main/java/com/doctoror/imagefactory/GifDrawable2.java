package com.doctoror.imagefactory;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.HashSet;

import pl.droidsonroids.gif.GifDrawable;

/**
 * {@link GifDrawable} that stores multiple callbacks.
 * Use {@link #addCallback(Callback)} and {@link #removeCallback(Callback)} instead of {@link
 * #setCallback(Callback)}
 */
public class GifDrawable2 extends GifDrawable {

    private final HashSet<WeakReference<Callback>> mCallbacks = new HashSet<>();
    private final HashSet<WeakReference<Callback>> mRemoveBuffer = new HashSet<>();

    public GifDrawable2(@NonNull final Resources res, final int id)
            throws Resources.NotFoundException, IOException {
        super(res, id);
    }

    public GifDrawable2(@NonNull final AssetManager assets,
            @NonNull final String assetName) throws IOException {
        super(assets, assetName);
    }

    public GifDrawable2(@NonNull final String filePath) throws IOException {
        super(filePath);
    }

    public GifDrawable2(@NonNull final File file) throws IOException {
        super(file);
    }

    public GifDrawable2(@NonNull final InputStream stream) throws IOException {
        super(stream);
    }

    public GifDrawable2(@NonNull final AssetFileDescriptor afd) throws IOException {
        super(afd);
    }

    public GifDrawable2(@NonNull final FileDescriptor fd) throws IOException {
        super(fd);
    }

    public GifDrawable2(@NonNull final byte[] bytes) throws IOException {
        super(bytes);
    }

    public GifDrawable2(@NonNull final ByteBuffer buffer) throws IOException {
        super(buffer);
    }

    public GifDrawable2(@Nullable final ContentResolver resolver,
            @NonNull final Uri uri) throws IOException {
        super(resolver, uri);
    }

    public void addCallback(@NonNull final Callback callback) {
        for (final WeakReference<Callback> callbackRef : mCallbacks) {
            final Callback refCallback = callbackRef.get();
            if (refCallback == null) {
                // Remove lost references
                mRemoveBuffer.add(callbackRef);
            } else if (refCallback == callback) {
                return;
            }
        }
        mCallbacks.removeAll(mRemoveBuffer);
        mRemoveBuffer.clear();
        mCallbacks.add(new WeakReference<>(callback));
    }

    public void removeCallback(@NonNull final Callback callback) {
        for (final WeakReference<Callback> callbackRef : mCallbacks) {
            final Callback refCallback = callbackRef.get();
            if (refCallback == null || refCallback == callback) {
                mRemoveBuffer.add(callbackRef);
            }
        }
        mCallbacks.removeAll(mRemoveBuffer);
        mRemoveBuffer.clear();
    }

    @Override
    public void invalidateSelf() {
        super.invalidateSelf();
        for (final WeakReference<Callback> callbackRef : mCallbacks) {
            final Callback refCallback = callbackRef.get();
            if (refCallback == null) {
                // Remove lost references
                mRemoveBuffer.add(callbackRef);
            } else {
                refCallback.invalidateDrawable(this);
            }
        }
        mCallbacks.removeAll(mRemoveBuffer);
        mRemoveBuffer.clear();
    }

    @Override
    public void scheduleSelf(Runnable what, long when) {
        super.scheduleSelf(what, when);
        for (final WeakReference<Callback> callbackRef : mCallbacks) {
            final Callback refCallback = callbackRef.get();
            if (refCallback == null) {
                // Remove lost references
                mRemoveBuffer.add(callbackRef);
            } else {
                refCallback.scheduleDrawable(this, what, when);
            }
        }
        mCallbacks.removeAll(mRemoveBuffer);
        mRemoveBuffer.clear();
    }

    @Override
    public void unscheduleSelf(final Runnable what) {
        super.unscheduleSelf(what);
        for (final WeakReference<Callback> callbackRef : mCallbacks) {
            final Callback refCallback = callbackRef.get();
            if (refCallback == null) {
                // Remove lost references
                mRemoveBuffer.add(callbackRef);
            } else {
                refCallback.unscheduleDrawable(this, what);
            }
        }
        mCallbacks.removeAll(mRemoveBuffer);
        mRemoveBuffer.clear();
    }
}
