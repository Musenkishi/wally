/*
 * Copyright (C) 2014 Evelio Tarazona Caceres
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.musenkishi.wally.R;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * {@link android.widget.LinearLayout} but with foreground/overlay drawable capability
 */
public class OverlayLinearLayout extends LinearLayout {
    private Drawable mOverlayDrawable;

    public OverlayLinearLayout(Context context) {
        super(context);
        initSelf(context, null, 0, 0);
    }

    public OverlayLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSelf(context, attrs, 0, 0);
    }

    public OverlayLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSelf(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OverlayLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initSelf(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initSelf(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OverlayLinearLayout);
            setOverlayDrawable(a.getDrawable(R.styleable.OverlayLinearLayout_overlay));
            a.recycle();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mOverlayDrawable != null) {
            mOverlayDrawable.draw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mOverlayDrawable != null) {
            mOverlayDrawable.setBounds(0, 0, w, h);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        Drawable d = mOverlayDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mOverlayDrawable);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mOverlayDrawable != null) {
            mOverlayDrawable.jumpToCurrentState();
        }
    }

    @Override
    @TargetApi(LOLLIPOP)
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (mOverlayDrawable != null) {
            mOverlayDrawable.setHotspot(x, y);
        }
    }

    private void setOverlayDrawable(Drawable overlayDrawable) {
        if (mOverlayDrawable != null) {
            mOverlayDrawable.setCallback(null);
            unscheduleDrawable(mOverlayDrawable);
        }
        mOverlayDrawable = overlayDrawable;
        if (mOverlayDrawable != null) {
            setWillNotDraw(false);
            mOverlayDrawable.setCallback(this);
            if (mOverlayDrawable.isStateful()) {
                mOverlayDrawable.setState(getDrawableState());
            }
        } else {
            setWillNotDraw(true);
        }
    }

}
