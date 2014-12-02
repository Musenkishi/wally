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

package com.musenkishi.wally.base;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.musenkishi.wally.BuildConfig;
import com.musenkishi.wally.R;
import com.musenkishi.wally.dataprovider.SharedPreferencesDataProvider;
import com.musenkishi.wally.fragments.MaterialDialogFragment;
import com.musenkishi.wally.util.TypefaceSpan;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A base class where you can put logic that is needed in all activities.
 * Created by Musenkishi on 2014-03-07 14:54.
 */
public abstract class BaseActivity extends ActionBarActivity {

    private static final int FADE_CROSSOVER_TIME_MILLIS = 300;

    private Toolbar toolbar;

    private String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);
    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleReceivedIntent(context, intent);
        }
    };

    protected abstract void handleReceivedIntent(Context context, Intent intent);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String title = getResources().getString(R.string.app_name);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_source);
            int primaryColor = getResources().getColor(R.color.Actionbar_TopList_Background);
            setTaskDescription(new ActivityManager.TaskDescription(title, bitmap, primaryColor));
        }

        if (WallyApplication.shouldShowCrashLoggingPermission()){
            showCrashLoggingPermissionDialog();
            WallyApplication.setShouldShowCrashLoggingPermission(false);
        }

        if (BuildConfig.DEBUG && isDevicePluggedIn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private boolean isDevicePluggedIn() {
        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert intent != null;
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(downloadCompleteReceiver);
        super.onPause();
    }

    private void showCrashLoggingPermissionDialog() {

        final MaterialDialogFragment permissionDialogFragment = new MaterialDialogFragment();
        permissionDialogFragment.setPrimaryColor(getResources().getColor(R.color.Actionbar_TopList_Background));
        permissionDialogFragment.setTitle(R.string.dialog_crashlogging_title);
        permissionDialogFragment.setMessage(R.string.dialog_crashlogging_message);
        permissionDialogFragment.setPositiveButton(R.string.dialog_crashlogging_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                WallyApplication.getDataProviderInstance()
                        .getSharedPreferencesDataProviderInstance()
                        .setUserApprovedCrashLogging(SharedPreferencesDataProvider.CRASH_LOGGING_APPROVED);
                WallyApplication.startCrashlytics(getApplicationContext());
            }
        });
        permissionDialogFragment.setNegativeButton(R.string.dialog_crashlogging_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                WallyApplication.getDataProviderInstance()
                        .getSharedPreferencesDataProviderInstance()
                        .setUserApprovedCrashLogging(SharedPreferencesDataProvider.CRASH_LOGGING_NOT_APPROVED);
                permissionDialogFragment.dismiss();
            }
        });
        permissionDialogFragment.onCancel(new DialogInterface() {
            @Override
            public void cancel() {
                WallyApplication.getDataProviderInstance()
                        .getSharedPreferencesDataProviderInstance()
                        .setUserApprovedCrashLogging(SharedPreferencesDataProvider.CRASH_LOGGING_NOT_APPROVED);
            }

            @Override
            public void dismiss() {
            }
        });
        permissionDialogFragment.show(getSupportFragmentManager(), MaterialDialogFragment.TAG);
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
        setSupportActionBar(this.toolbar);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    protected void setTitle(String title) {
        if (getToolbar() != null) {
            SpannableString s = new SpannableString(title);
            s.setSpan(new TypefaceSpan(this, "Lobster_1.3.otf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Update the action bar title with the TypefaceSpan instance
            getToolbar().setTitle(s);
        }
    }

    public void colorizeActionBar(int color) {
        int oldColor = getResources().getColor(R.color.Actionbar_TopList_Background);

        if (getToolbar() != null) {
            Drawable toolbarDrawable = getToolbar().getBackground();
            if (toolbarDrawable != null && toolbarDrawable instanceof ColorDrawable) {
                oldColor = ((ColorDrawable) toolbarDrawable).getColor();
            }
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, color);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (Integer) valueAnimator.getAnimatedValue();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(darkenColor(color));
                }
                getToolbar().setBackgroundColor(color);
            }
        });
        colorAnimation.setDuration(FADE_CROSSOVER_TIME_MILLIS);
        colorAnimation.start();
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static boolean isTablet(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 13) { // Honeycomb 3.2
                Configuration con = context.getResources().getConfiguration();
                Field fSmallestScreenWidthDp = con.getClass().getDeclaredField("smallestScreenWidthDp");
                return fSmallestScreenWidthDp.getInt(con) >= 600;
            } else if (android.os.Build.VERSION.SDK_INT >= 11) { // Honeycomb 3.0
                Configuration con = context.getResources().getConfiguration();
                Method mIsLayoutSizeAtLeast = con.getClass().getMethod("isLayoutSizeAtLeast", int.class);
                return (Boolean) mIsLayoutSizeAtLeast.invoke(con, Configuration.SCREENLAYOUT_SIZE_XLARGE);
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @SuppressLint("RtlHardcoded")
    public void startHeartPopoutAnimation(View originalView, int maskingColor) {
        int[] heartCoords = new int[2];
        originalView.getLocationInWindow(heartCoords);

        final ImageView view = new ImageView(getApplicationContext());

        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        Drawable heart = getResources().getDrawable(R.drawable.ic_action_saved);
        heart.setColorFilter(maskingColor, PorterDuff.Mode.MULTIPLY);
        view.setImageDrawable(heart);

        final ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.addView(view);

        FrameLayout.LayoutParams viewParams = (FrameLayout.LayoutParams) view.getLayoutParams();

        viewParams.topMargin = heartCoords[1];
        viewParams.height = originalView.getHeight();
        viewParams.width= originalView.getWidth();
        viewParams.gravity = Gravity.LEFT;
        viewParams.leftMargin = heartCoords[0];
        view.setLayoutParams(viewParams);

        view.animate()
                .scaleX(3.0f)
                .scaleY(3.0f)
                .alpha(0.0f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator(0.75f))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        decorView.removeView(view);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                })
                .start();
    }
}
