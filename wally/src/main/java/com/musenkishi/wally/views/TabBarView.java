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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.musenkishi.wally.R;

/**
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-05-26.
 * Based on Cyril Mottier's TabBarView at
 * https://plus.google.com/118417777153109946393/posts/Jz7mBBuDoNk
 */
public class TabBarView extends LinearLayout {

    private static final int STRIP_HEIGHT = 6;

    public final Paint paint;

    private int stripHeight;
    private float offset;
    private int selectedTab = -1;

    private OnTabClickedListener onTabClickedListener;

    public interface OnTabClickedListener{
        void onTabClicked(int index);
    }

    public TabBarView(Context context) {
        this(context, null);
    }

    public TabBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();

        paint.setColor(Color.WHITE);
        stripHeight = (int) (STRIP_HEIGHT * getResources().getDisplayMetrics().density + .5f);
        setAttributes(context, attrs);
    }

    private void setAttributes(Context context, AttributeSet attrs){
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(attrs, R.styleable.TabBarView, 0, 0);
            if (typedArray != null) {

                int n = typedArray.getIndexCount();

                for (int i = 0; i < n; i++){
                    int attr = typedArray.getIndex(i);

                    switch (attr){
                        case R.styleable.TabBarView_tabBarColor:
                            int color = typedArray.getColor(i, Color.WHITE);
                            paint.setColor(color);
                            break;
                        case R.styleable.TabBarView_tabBarSize:
                            int size = typedArray.getDimensionPixelSize(i, (int) (STRIP_HEIGHT * getResources().getDisplayMetrics().density + .5f));
                            stripHeight = size;
                            break;
                    }
                }
                typedArray.recycle();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        setupTabViews();
    }

    private void setupTabViews() {
        if (getChildCount() > 0){
            for (int i = 0; i < getChildCount(); i++){
                TabView child = (TabView) getChildAt(i);
                if (child != null) {
                    child.setTag(i);
                    child.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int index = (Integer) v.getTag();
                            if (onTabClickedListener != null) {
                                onTabClickedListener.onTabClicked(index);
                            }
                        }
                    });
                }
            }
        }
    }

    public void setStripColor(int color) {
        if (paint.getColor() != color) {
            paint.setColor(color);
            invalidate();
        }
    }

    public void setStripHeight(int height) {
        if (stripHeight != height) {
            stripHeight = height;
            invalidate();
        }
    }

    public void setSelectedTab(int tabIndex) {
        if (tabIndex < 0) {
            tabIndex = 0;
        }
        final int childCount = getChildCount();
        if (tabIndex >= childCount) {
            tabIndex = childCount - 1;
        }
        if (selectedTab != tabIndex) {
            selectedTab = tabIndex;
            invalidate();
        }
    }

    public void setOffset(float offset) {
        if (this.offset != offset) {
            this.offset = offset;
            invalidate();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // Draw the strip manually
        final TabView child = (TabView) getChildAt(selectedTab);
        if (child != null) {
            int left = child.getLeft();
            int right = child.getRight();
            if (offset > 0) {
                final TabView nextChild = (TabView) getChildAt(selectedTab + 1);
                if (nextChild != null) {
                    left = (int) (child.getLeft() + offset * (nextChild.getLeft() - child.getLeft()));
                    right = (int) (child.getRight() + offset * (nextChild.getRight() - child.getRight()));
                }
            }
            canvas.drawRect(left, getHeight() - stripHeight, right, getHeight(), paint);
        }
    }

    public void setOnTabClickedListener(OnTabClickedListener onTabClickedListener) {
        this.onTabClickedListener = onTabClickedListener;
    }

    public TabView getTab(int position) {
        return (TabView) getChildAt(position);
    }

}
