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

package com.doctoror.imagefactory.sample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extended {@link BaseAdapter} which contains items {@link List}, {@link Context} and {@link
 * LayoutInflater}.
 */
public abstract class BaseAdapter2<T> extends BaseAdapter {

    private final ArrayList<T> mItems = new ArrayList<>();

    @NonNull
    private final Context mContext;

    @NonNull
    private final LayoutInflater mLayoutInflater;

    public BaseAdapter2(@NonNull final Context context) {
        this(context, (List<T>) null);
    }

    public BaseAdapter2(@NonNull final Context context, @Nullable final T[] items) {
        this(context, items != null ? Arrays.asList(items) : null);
    }

    public BaseAdapter2(@NonNull final Context context, @Nullable final List<T> items) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        if (items != null) {
            mItems.addAll(items);
        }
    }

    protected final List<T> getItems() {
        return mItems;
    }

    public void updateData(@Nullable final List<T> data) {
        if (data == null && mItems.isEmpty() || data != null && mItems.equals(data)) {
            return;
        }
        mItems.clear();
        if (data != null) {
            mItems.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    protected final Context getContext() {
        return mContext;
    }

    @NonNull
    protected final LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    @Override
    public T getItem(final int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }
}
