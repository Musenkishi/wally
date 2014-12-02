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
package com.musenkishi.wally.views.swipeclearlayout;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.musenkishi.wally.R;

/**
 * The SwipeRefreshLayout should be used whenever the user can refresh the
 * contents of a view via a vertical swipe gesture. The activity that
 * instantiates this view should add an OnRefreshListener to be notified
 * whenever the swipe to refresh gesture is completed. The SwipeRefreshLayout
 * will notify the listener each and every time the gesture is completed again;
 * the listener is responsible for correctly determining when to actually
 * initiate a refresh of its content. If the listener determines there should
 * not be a refresh, it must call setRefreshing(false) to cancel any visual
 * indication of a refresh. If an activity wishes to show just the progress
 * animation, it should call setRefreshing(true). To disable the gesture and
 * progress animation, call setEnabled(false) on the view.
 * <p>
 * This layout should be made the parent of the view that will be refreshed as a
 * result of the gesture and can only support one direct child. This view will
 * also be made the target of the gesture and will be forced to match both the
 * width and the height supplied in this layout. The SwipeRefreshLayout does not
 * provide accessibility events; instead, a menu item must be provided to allow
 * refresh of the content wherever this gesture is used.
 * </p>
 */
public class SwipeClearLayout extends RelativeLayout {
    private static final long RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 300;
    private static final float ACCELERATE_INTERPOLATION_FACTOR = 1.5f;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final float MAX_SWIPE_DISTANCE_FACTOR = .6f;
    private static final int REFRESH_TRIGGER_DISTANCE = 120;
    private static final int CIRCLE_SIZE = 48;
    private static final int CIRCLE_DEFAULT_COLOR = Color.MAGENTA;
    private static final int DEFAULT_ANIMATION_DURATION = 300*2;

    private View circle;
    private int circleTopMargin = 0;
    private int circleColor;
    private int duration = DEFAULT_ANIMATION_DURATION;

    private ProgressBar progressBar;

    private View target; //the content that gets pulled down
    private int originalOffsetTop;
    private OnRefreshListener listener;
    private OnSwipeListener onSwipeListener;
    private MotionEvent downEvent;
    private int from;
    private boolean refreshing = false;
    private int touchSlop;
    private float distanceToTriggerSync = -1;
    private float prevY;
    private int mediumAnimationDuration;
    private float currPercentage = 0;
    private int currentTargetOffsetTop;
    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private boolean returningToStart;
    private final DecelerateInterpolator decelerateInterpolator;
    private final AccelerateInterpolator accelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[] {
            android.R.attr.enabled
    };

    private final Animation animateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            if (from != originalOffsetTop) {
                targetTop = (from + (int)((originalOffsetTop - from) * interpolatedTime));
            }
            int offset = targetTop - circle.getTop();
            final int currentTop = circle.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            setTargetOffsetTopAndBottom(offset);
        }
    };

    private final AnimationListener returnToStartPositionListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            // Once the target content has returned to its start position, reset
            // the target offset to 0
            currentTargetOffsetTop = 0;
        }
    };

    private final Runnable returnToStartPosition = new Runnable() {

        @Override
        public void run() {
            returningToStart = true;
            animateOffsetToStartPosition(currentTargetOffsetTop + getPaddingTop(),
                    returnToStartPositionListener);
        }

    };

    // Cancel the refresh gesture and animate everything back to its original state.
    private final Runnable cancel = new Runnable() {

        @Override
        public void run() {
            returningToStart = true;
            // Timeout fired since the user last moved their finger; animate the
            // trigger to 0 and put the target back at its original position
            animateOffsetToStartPosition(currentTargetOffsetTop + getPaddingTop(),
                    returnToStartPositionListener);
        }

    };
    private View filledView;

    /**
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     * @param context
     */
    public SwipeClearLayout(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     * @param context
     * @param attrs
     */
    public SwipeClearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        circle = generateCircle(context, attrs, metrics);
        progressBar = new ProgressBar(context, attrs);

        decelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        accelerateInterpolator = new AccelerateInterpolator(ACCELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        initAttrs(context, attrs);
        a.recycle();
    }

    private void initAttrs(Context context, AttributeSet attrs){
        final Resources.Theme theme = context.getTheme();
        if (theme != null) {
            final TypedArray typedArray = theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.SwipeClearLayout,
                    0, 0);
            if (typedArray != null) {
                try {
                    circleTopMargin = (int) typedArray.getDimension(R.styleable.SwipeClearLayout_circleTopMargin, 0);
                    circleColor = typedArray.getColor(R.styleable.SwipeClearLayout_circleColor, CIRCLE_DEFAULT_COLOR);
                    duration = typedArray.getInteger(R.styleable.SwipeClearLayout_duration, DEFAULT_ANIMATION_DURATION);
                } finally {
                    typedArray.recycle();
                }
            }
        }
    }

    private View generateCircle(Context context, AttributeSet attrs, DisplayMetrics metrics){
        ImageView view = new ImageView(context, attrs);
        GradientDrawable circle = (GradientDrawable) getResources().getDrawable(R.drawable.circle);
        circle.setColor(CIRCLE_DEFAULT_COLOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            view.setBackground(circle);
        } else {
            view.setBackgroundDrawable(circle);
        }
        int size = (int) (metrics.density * CIRCLE_SIZE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        view.setLayoutParams(params);
        view.setImageResource(R.drawable.clip_random);
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setRotation(90.0f);
        return view;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(cancel);
        removeCallbacks(returnToStartPosition);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(returnToStartPosition);
        removeCallbacks(cancel);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        this.from = from;
        animateToStartPosition.reset();
        animateToStartPosition.setDuration(mediumAnimationDuration);
        animateToStartPosition.setAnimationListener(listener);
        animateToStartPosition.setInterpolator(decelerateInterpolator);
//        target.startAnimation(animateToStartPosition);
        circle.startAnimation(animateToStartPosition);
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    /**
     * Set the listener to be notified when a swipe is triggered by the user.
     */
    public void setOnSwipeListener(OnSwipeListener listener) {
        onSwipeListener = listener;
    }

    private void setTriggerPercentage(float percent) {
        if (percent == 0f) {
            // No-op. A null trigger means it's uninitialized, and setting it to zero-percent
            // means we're trying to reset state, so there's nothing to reset in this case.
            currPercentage = 0;
            return;
        }
        currPercentage = percent;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing(boolean refreshing) {
        if (this.refreshing != refreshing) {
            ensureTarget();
            currPercentage = 0;
            this.refreshing = refreshing;
            if (this.refreshing) {
                animateCircle();
                progressBar.animate().alpha(1.0f).setStartDelay(duration /2).setDuration(duration /2).start();
            } else {
                progressBar.animate().alpha(0.0f).setStartDelay(0).setDuration(200).start();
                getChildAt(0).setAlpha(1.0f);
            }
        }
    }

    /**
     * Set the color for the swipe circle.
     * @param colorResId Color resource.
     */
    public void setCircleColorResourceId(int colorResId){
        ensureTarget();
        final Resources resources = getResources();
        circleColor = resources.getColor(colorResId);
        ((GradientDrawable) circle.getBackground()).setColor(circleColor);
    }

    /**
     * Set the color for the swipe circle.
     * @param color Color.
     */
    public void setCircleColor(int color){
        ensureTarget();
        circleColor = color;
        ((GradientDrawable) circle.getBackground()).setColor(circleColor);
    }

    /**
     * Set the top margin of the circle.
     */
    public void setCircleTopMargin(int topMargin){
        circleTopMargin = topMargin;
    }

    public ProgressBar getProgressBar(){
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    /**
     * @return Whether the SwipeClearWidget is actively showing refresh
     *         progress.
     */
    public boolean isRefreshing() {
        return refreshing;
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid out yet.
        if (target == null) {
            if (getChildCount() > 4 && !isInEditMode()) {
                throw new IllegalStateException(
                        "SwipeClearLayout can host only one direct child");
            }
            target = getChildAt(0);
            originalOffsetTop = circle.getTop() + getPaddingTop();
        }
        if (distanceToTriggerSync == -1) {
            if (getParent() != null && ((View)getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                distanceToTriggerSync = (int) Math.min(
                        ((View) getParent()) .getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        REFRESH_TRIGGER_DISTANCE * metrics.density);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int width =  getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        final View child = getChildAt(0);
        final int childLeft = getPaddingLeft();
        final int childTop = currentTargetOffsetTop + getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);

        if (getChildAt(1) == null){
            final DisplayMetrics metrics = getResources().getDisplayMetrics();
            int size = (int) (metrics.density * CIRCLE_SIZE);
            @SuppressLint("DrawAllocation") final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            //The circle should start to grow from the top of SwipeClearLayout, hence "-(size/2)".
//            params.topMargin = circleTopMargin - size;
            params.addRule(CENTER_HORIZONTAL, TRUE);
            circle.setLayoutParams(params);
            addView(circle, 1);
            circle.setScaleX(1.0f);
            circle.setScaleY(1.0f);
            ((GradientDrawable) circle.getBackground()).setColor(circleColor);
            circle.setTranslationY(-size);
        }

        if (getChildAt(2) == null){
            addView(progressBar, 2);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) progressBar.getLayoutParams();
            params.addRule(CENTER_IN_PARENT, TRUE);
            progressBar.setLayoutParams(params);
            progressBar.setAlpha(0.0f);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() > 4 && !isInEditMode()) {
            throw new IllegalStateException("SwipeClearLayout can host only one direct child");
        }
        if (getChildCount() > 0) {
            getChildAt(0).measure(
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredHeight() - getPaddingTop() - getPaddingBottom(),
                            MeasureSpec.EXACTLY));
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     *         scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        if (Build.VERSION.SDK_INT < 14) {
            if (target instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) target;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return target.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(target, -1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        boolean handled = false;
        if (returningToStart && ev.getAction() == MotionEvent.ACTION_DOWN) {
            returningToStart = false;
        }
        if (isEnabled() && !returningToStart && !canChildScrollUp()) {
            handled = onTouchEvent(ev);
        }
        return !handled ? super.onInterceptTouchEvent(ev) : handled;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                currPercentage = 0;
                downEvent = MotionEvent.obtain(event);
                prevY = downEvent.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (downEvent != null && !returningToStart) {
                    final float eventY = event.getY();
                    float yDiff = eventY - downEvent.getY();
                    if (yDiff > touchSlop) {
                        // User velocity passed min velocity; trigger a refresh
                        if (yDiff > distanceToTriggerSync) {
                            // User movement passed distance; trigger a refresh
                            startRefresh();
                            handled = true;
                            break;
                        } else {
                            // Just track the user's movement
                            setTriggerPercentage(
                                    accelerateInterpolator.getInterpolation(
                                            yDiff / distanceToTriggerSync));
                            float offsetTop = yDiff;
                            if (prevY > eventY) {
                                offsetTop = yDiff - touchSlop;
                            }
                            updateContentOffsetTop((int) (offsetTop));
//                            if (prevY > eventY && (target.getTop() < touchSlop)) {
                            if (prevY > eventY && (circle.getTop() < touchSlop)) {
                                // If the user puts the view back at the top, we
                                // don't need to. This shouldn't be considered
                                // cancelling the gesture as the user can restart from the top.
                                removeCallbacks(cancel);
                            } else {
                                updatePositionTimeout();
                            }
                            prevY = event.getY();
                            handled = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (downEvent != null) {
                    downEvent.recycle();
                    downEvent = null;
                }
                break;
        }
        return handled;
    }

    private void startRefresh() {
        removeCallbacks(cancel);
        returnToStartPosition.run();
        setRefreshing(true);
        listener.onRefresh();
    }

    private void animateCircle() {
        float currentScale = circle.getScaleX();

        if (currentScale == 0.0f){
            circle.setScaleX(1.0f);
            circle.setScaleY(1.0f);
            currentScale = 1.0f;
        }

        int currentPixelSize = (int) (circle.getHeight() * currentScale);

        if (currentPixelSize == 0) {
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int hypotenuse = (int) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));

        float goalScale = (hypotenuse / currentPixelSize) * 2;

        circle.setAlpha(1.0f);
        circle.animate().scaleX(goalScale).scaleY(goalScale).setDuration(duration / 2).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                if (isRefreshing()) {
                    getChildAt(0).setAlpha(0.0f); //list should be invisible until it stops refreshing
                }

                if (getChildAt(3) == null) {
                    filledView = new View(getContext());
                    ColorDrawable colorDrawable = new ColorDrawable(circleColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        filledView.setBackground(colorDrawable);
                    } else {
                        filledView.setBackgroundDrawable(colorDrawable);
                    }
                    addView(filledView, 3);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) filledView.getLayoutParams();
                    params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                    filledView.setLayoutParams(params);
                }
                filledView.setVisibility(VISIBLE);
                filledView.setAlpha(1.0f);
                filledView.animate().alpha(0.0f).setDuration(duration / 2).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
//                        removeViewAt(3);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                }).start();
                circle.setScaleX(1.0f);
                circle.setScaleY(1.0f);
                circle.setAlpha(1.0f);
                circle.setTranslationY(-circle.getHeight());
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }

    private void updateContentOffsetTop(int targetTop) {
        final int currentTop = circle.getTop();
        if (targetTop > distanceToTriggerSync) {
            targetTop = (int) distanceToTriggerSync;
        } else if (targetTop < 0) {
            targetTop = 0;
        }
        setTargetOffsetTopAndBottom(targetTop - currentTop);
    }

    private void setTargetOffsetTopAndBottom(int offset) {
        circle.offsetTopAndBottom(offset);
        currentTargetOffsetTop = circle.getTop();
        int percent = (int) ((currentTargetOffsetTop / distanceToTriggerSync) * 100);
        if (onSwipeListener != null) {
            onSwipeListener.onSwipe(percent, currentTargetOffsetTop);
        }
        ViewCompat.setElevation(circle, percent);
        ImageView imageView = (ImageView) circle;
        ClipDrawable clipDrawable = (ClipDrawable) imageView.getDrawable();

        if (percent < 50) {
            clipDrawable.setLevel(0);
        } else {
            clipDrawable.setLevel((percent - 50) * 2 * 100);
        }
    }

    private void updatePositionTimeout() {
        removeCallbacks(cancel);
        postDelayed(cancel, RETURN_TO_ORIGINAL_POSITION_TIMEOUT);
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        public void onRefresh();
    }

    /**
     * Classes that wish to be notified how much there's left until a
     * refresh is triggered should implement this interface.
     */
    public interface OnSwipeListener {
        /**
         * Called when user starts pulling the layout for a refresh.
         * @param progress How much is left (in percent) until a refresh is triggered.
         * @param pixels How much the list has moved in pixels
         */
        abstract void onSwipe(int progress, int pixels);
    }

    /**
     * Simple AnimationListener to avoid having to implement unneeded methods in
     * AnimationListeners.
     */
    private class BaseAnimationListener implements AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}