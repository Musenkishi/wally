/*
 * Copyright (C) 2014 Freddie (Musenkishi) Lust-Hed
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

package com.musenkishi.wally.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.LruCache;

/**
 * Class for loading custom fonts for spannable strings.
 *
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-04-09.
 */
public class TypefaceSpan extends MetricAffectingSpan {

    private static LruCache<String, Typeface> typefaceCache =
            new LruCache<String, Typeface>(12);

    private Typeface typeface;

    public TypefaceSpan(Context context, String typefaceName) {
        typeface = typefaceCache.get(typefaceName);

        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(), String.format("fonts/%s", typefaceName));

            // Cache the loaded Typeface
            typefaceCache.put(typefaceName, typeface);
        }
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(typeface);

        // Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(typeface);

        // Note: This flag is required for proper typeface rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

}
