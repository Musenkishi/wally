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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.graphics.Palette;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.collections4.map.LRUMap;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class that can apply colors to Views lazily.
 * Can be used in ListViews.
 * <p/>
 * Created by Freddie (Musenkishi) Lust-Hed on 2014-10-21.
 */
public class PaletteLoader {

    private static final int MSG_RENDER_PALETTE = 4194;
    private static final int MSG_DISPLAY_PALETTE = 4195;

    private static final int MAX_ITEMS_IN_CACHE = 100;
    private static final int MAX_CONCURRENT_THREADS = 5;

    private static int TRUE = 1;
    private static int FALSE = 0;

    private static Handler uiHandler;
    private static Handler backgroundHandler;

    private static Map<String, Palette> paletteCache;
    private static ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_THREADS);

    private PaletteLoader() {
        //Don't use
    }

    public static PaletteBuilder with(Context context, String id) {
        if (paletteCache == null) {
            paletteCache = Collections.synchronizedMap(
                    new LRUMap<String, Palette>(MAX_ITEMS_IN_CACHE)
            );
        }
        if (uiHandler == null || backgroundHandler == null) {
            setupHandlers(context);
        }
        return new PaletteBuilder(id);
    }

    private static void setupHandlers(Context context) {
        HandlerThread handlerThread = new HandlerThread("palette-loader-background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper(), sCallback);
        uiHandler = new Handler(context.getMainLooper(), sCallback);
    }

    private static Handler.Callback sCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {

                case MSG_RENDER_PALETTE:
                    Pair<Bitmap, PaletteTarget> pair = (Pair<Bitmap, PaletteTarget>) message.obj;
                    if (pair != null && !pair.first.isRecycled()) {
                        Palette palette = Palette.generate(pair.first);

                        paletteCache.put(pair.second.getId(), palette);

                        Message uiMessage = uiHandler.obtainMessage();
                        uiMessage.what = MSG_DISPLAY_PALETTE;
                        uiMessage.obj = new Pair<Palette, PaletteTarget>(palette, pair.second);
                        uiMessage.arg1 = FALSE;

                        uiHandler.sendMessage(uiMessage);
                    }
                    break;

                case MSG_DISPLAY_PALETTE:
                    Pair<Palette, PaletteTarget> pairDisplay = (Pair<Palette, PaletteTarget>) message.obj;
                    boolean fromCache = message.arg1 == TRUE;
                    applyColorToView(pairDisplay.second, pairDisplay.first, fromCache);
                    callListener(pairDisplay.first, pairDisplay.second.getListener());

                    break;

            }

            return false;
        }
    };

    public static class PaletteBuilder {

        private String id;
        private Bitmap bitmap;
        private boolean maskDrawable;
        private int fallbackColor = Color.TRANSPARENT;
        private PaletteRequest paletteRequest = new PaletteRequest(PaletteRequest.SwatchType.REGULAR_VIBRANT, PaletteRequest.SwatchColor.BACKGROUND);
        private Palette palette;
        private OnPaletteRenderedListener onPaletteRenderedListener;

        public PaletteBuilder(String id) {
            this.id = id;
        }

        public PaletteBuilder load(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public PaletteBuilder load(Palette colorScheme) {
            this.palette = colorScheme;
            return this;
        }

        public PaletteBuilder mask() {
            maskDrawable = true;
            return this;
        }

        public PaletteBuilder fallbackColor(int fallbackColor) {
            this.fallbackColor = fallbackColor;
            return this;
        }

        public PaletteBuilder setPaletteRequest(PaletteRequest paletteRequest) {
            this.paletteRequest = paletteRequest;
            return this;
        }

        public PaletteBuilder setListener(OnPaletteRenderedListener onPaletteRenderedListener) {
            this.onPaletteRenderedListener = onPaletteRenderedListener;
            return this;
        }

        public void into(View view) {
            final PaletteTarget paletteTarget = new PaletteTarget(id, paletteRequest, view, maskDrawable, fallbackColor, onPaletteRenderedListener);
            if (palette != null) {
                paletteCache.put(paletteTarget.getId(), palette);
                applyColorToView(paletteTarget, palette, false);
                callListener(palette, onPaletteRenderedListener);
            } else {
                if (paletteCache.get(id) != null) {
                    Palette palette = paletteCache.get(id);
                    applyColorToView(paletteTarget, palette, true);
                    callListener(palette, onPaletteRenderedListener);
                } else {
                    if (Build.VERSION.SDK_INT >= 21) {
                        executorService.submit(new PaletteRenderer(bitmap, paletteTarget));
                    } else {
                        Message bgMessage = backgroundHandler.obtainMessage();
                        bgMessage.what = MSG_RENDER_PALETTE;
                        bgMessage.obj = new Pair<Bitmap, PaletteTarget>(bitmap, paletteTarget);
                        backgroundHandler.sendMessage(bgMessage);
                    }
                }
            }
        }

    }

    private static void applyColorToView(PaletteTarget target, Palette palette, boolean fromCache) {
        if (!isViewRecycled(target)) {
            applyColorToView(target, target.getPaletteRequest().getColor(palette), fromCache);
        }
    }

    private static void applyColorToView(final PaletteTarget target, int color, boolean fromCache) {
        if (target.getView() instanceof TextView) {
            applyColorToView((TextView) target.getView(), color, fromCache);
            return;
        }
        if (fromCache) {
            if (target.getView() instanceof ImageView && target.shouldMaskDrawable()) {
                ImageView imageView = ((ImageView) target.getView());
                if (imageView.getDrawable() != null) {
                    imageView.getDrawable().mutate()
                            .setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                } else if (imageView.getBackground() != null) {
                    imageView.getBackground().mutate()
                            .setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                }
            } else {
                target.getView().setBackgroundColor(color);
            }
        } else {
            if (target.getView() instanceof ImageView && target.shouldMaskDrawable()) {

                final ImageView imageView = ((ImageView) target.getView());

                Integer colorFrom;
                ValueAnimator.AnimatorUpdateListener imageAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        if (imageView.getDrawable() != null) {
                            imageView.getDrawable().mutate()
                                    .setColorFilter((Integer) valueAnimator
                                            .getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                        } else if (imageView.getBackground() != null) {
                            imageView.getBackground().mutate()
                                    .setColorFilter((Integer) valueAnimator
                                            .getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                        }
                    }
                };
                ValueAnimator.AnimatorUpdateListener animatorUpdateListener;

                PaletteTag paletteTag = (PaletteTag) target.getView().getTag();
                animatorUpdateListener = imageAnimatorUpdateListener;
                colorFrom = paletteTag.getColor();
                target.getView().setTag(new PaletteTag(paletteTag.getId(), color));

                Integer colorTo = color;
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.addUpdateListener(animatorUpdateListener);
                colorAnimation.setDuration(300);
                colorAnimation.start();
            } else {

                Drawable preDrawable;

                if (target.getView().getBackground() == null) {
                    preDrawable = new ColorDrawable(Color.TRANSPARENT);
                } else {
                    preDrawable = target.getView().getBackground();
                }

                TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
                        preDrawable,
                        new ColorDrawable(color)
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    target.getView().setBackground(transitionDrawable);
                } else {
                    target.getView().setBackgroundDrawable(transitionDrawable);
                }
                transitionDrawable.startTransition(300);
            }
        }
    }

    private static void applyColorToView(final TextView textView, int color, boolean fromCache) {
        if (fromCache) {
            textView.setTextColor(color);
        } else {
            Integer colorFrom = textView.getCurrentTextColor();
            Integer colorTo = color;
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    textView.setTextColor((Integer) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();
        }
    }

    /**
     * Is it null? Is that null? What about that one? Is that null too? What about this one?
     * And this one? Is this null? Is null even null? How can null be real if our eyes aren't real?
     * <p/>
     * <p>Checks whether the view has been recycled or not.</p>
     *
     * @param target A {@link com.musenkishi.wally.util.PaletteLoader.PaletteTarget} to check
     * @return true is view has been recycled, otherwise false.
     */
    private static boolean isViewRecycled(PaletteTarget target) {

        if (target != null && target.getId() != null && target.getView() != null
                && target.getView().getTag() != null) {
            if (target.getView().getTag() instanceof PaletteTag) {
                return !target.getId().equals(((PaletteTag) target.getView().getTag()).getId());
            } else {
                throw new NoPaletteTagFoundException("PaletteLoader couldn't determine whether" +
                        " a View has been reused or not. PaletteLoader uses View.setTag() and " +
                        "View.getTag() for keeping check if a View has been reused and it's " +
                        "recommended to refrain from setting tags to Views PaletteLoader is using."
                );
            }
        } else {
            return false;
        }
    }

    private static class PaletteRenderer implements Runnable {

        private Bitmap bitmap;
        private PaletteTarget paletteTarget;

        private PaletteRenderer(Bitmap bitmap, PaletteTarget paletteTarget) {
            this.bitmap = bitmap;
            this.paletteTarget = paletteTarget;
        }

        @Override
        public void run() {
            if (bitmap != null && !bitmap.isRecycled()) {
                Palette colorScheme = Palette.generate(bitmap);
                paletteCache.put(paletteTarget.getId(), colorScheme);

                PalettePresenter palettePresenter = new PalettePresenter(
                        paletteTarget,
                        colorScheme,
                        false
                );
                uiHandler.post(palettePresenter);
            }
        }
    }

    private static class PalettePresenter implements Runnable {

        private PaletteTarget paletteTarget;
        private Palette palette;
        private boolean fromCache;

        private PalettePresenter(PaletteTarget paletteTarget, Palette palette, boolean fromCache) {
            this.paletteTarget = paletteTarget;
            this.palette = palette;
            this.fromCache = fromCache;
        }

        @Override
        public void run() {
            applyColorToView(paletteTarget, palette, fromCache);
            callListener(palette, paletteTarget.getListener());
        }
    }

    private static class PaletteTarget {
        private String id;
        private PaletteRequest paletteRequest;
        private View view;
        private boolean maskDrawable;
        private OnPaletteRenderedListener onPaletteRenderedListener;

        private PaletteTarget(String id, PaletteRequest paletteRequest, View view, boolean maskDrawable, int fallbackColor, OnPaletteRenderedListener onPaletteRenderedListener) {
            this.id = id;
            this.paletteRequest = paletteRequest;
            this.view = view;
            this.view.setTag(new PaletteTag(this.id, fallbackColor));
            this.maskDrawable = maskDrawable;
            this.onPaletteRenderedListener = onPaletteRenderedListener;
        }

        public String getId() {
            return id;
        }

        public PaletteRequest getPaletteRequest() {
            return paletteRequest;
        }

        public View getView() {
            return view;
        }

        public boolean shouldMaskDrawable() {
            return maskDrawable;
        }

        public OnPaletteRenderedListener getListener() {
            return onPaletteRenderedListener;
        }
    }

    private static class PaletteTag {
        private String id;
        private Integer color;

        private PaletteTag(String id, Integer color) {
            this.id = id;
            this.color = color;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getColor() {
            return color;
        }

        public void setColor(Integer color) {
            this.color = color;
        }
    }

    public static class NoPaletteTagFoundException extends NullPointerException {
        public NoPaletteTagFoundException() {
            super();
        }

        public NoPaletteTagFoundException(String message) {
            super(message);
        }
    }

    public interface OnPaletteRenderedListener {
        abstract void onRendered(Palette palette);
    }

    private static void callListener(Palette palette, OnPaletteRenderedListener onPaletteRenderedListener) {
        if (onPaletteRenderedListener != null) {
            onPaletteRenderedListener.onRendered(palette);
        }
    }

//    /**
//     * Will return a {@link android.support.v7.graphics.Palette} if it's available in the cache
//     * @param id The URL or ID that was used to render the {@link android.support.v7.graphics.Palette}.
//     * @return The desired {@link android.support.v7.graphics.Palette} if available, otherwise null.
//     */
//    public static Palette getPaletteFromCache(String id) {
//        if (paletteCache.containsKey(id)){
//            return paletteCache.get(id);
//        } else {
//            return null;
//        }
//    }

}
