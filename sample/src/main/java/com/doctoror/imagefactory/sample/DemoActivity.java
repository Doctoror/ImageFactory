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

import com.doctoror.imagefactory.ImageFactory;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class DemoActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        final GridView grid = (GridView) findViewById(R.id.activity_demo_grid);
        grid.setAdapter(new DemoAdapter(this, generateImageInfo()));
    }

    @NonNull
    private List<Drawable> generateImageInfo() {
        final String[] names = new String[]{
                "Rotating_earth_(large).gif",
                "Static_earth.gif",
                "LoopOnce.gif",
                "small.gif",
                "smallest.gif",
                "ru9gag.gif",
                "ru9gag1.gif",
                "ru9gag3.gif",
                "ru9gag4.gif"
        };
        final List<Drawable> list = new ArrayList<>(names.length);
        for (final String name : names) {
            InputStream is = null;
            try {
                is = getAssets().open(name);
                final Drawable item = ImageFactory.decodeStream(getResources(), is);
                if (item != null) {
                    list.add(item);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return list;
    }

    private static final class DemoAdapter extends BaseAdapter2<Drawable> {

        private DemoAdapter(@NonNull final Context context,
                @Nullable final List<Drawable> items) {
            super(context, items);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.grid_item_demo, parent, false);
                imageView = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(imageView);
            } else {
                imageView = (ImageView) convertView.getTag();
            }

            imageView.setImageDrawable(getItem(position));
            return convertView;
        }
    }
}
