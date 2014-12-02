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

package com.musenkishi.wally.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.musenkishi.wally.R;

/**
 * A Custom GridView where you can set the default width of a cell
 * and it will set the numbers of columns accordingly.
 * Created by Musenkishi on 2014-03-04 21:08.
 */
public class AutoGridView extends GridRecyclerView {

    private int defaultCellWidth;

    public AutoGridView(Context context) {
        super(context);
    }

    public AutoGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutoGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.AutoGridView,
                    0, 0);
            if (typedArray != null) {
                try {
                    defaultCellWidth = (int) typedArray.getDimension(R.styleable.AutoGridView_defaultCellWidth, 0);
                } finally {
                    typedArray.recycle();
                }
            }
        }
    }

    public int getDefaultCellWidth(){
        return defaultCellWidth;
    }

    public void setDefaultCellWidth(int width){
        this.defaultCellWidth = width;
    }

}
