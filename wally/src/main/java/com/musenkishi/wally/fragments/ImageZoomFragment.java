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

package com.musenkishi.wally.fragments;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.musenkishi.wally.R;
import com.musenkishi.wally.anim.interpolator.LinearOutSlowInInterpolator;
import com.musenkishi.wally.observers.FileChangeReceiver;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Fragment used for letting the user zoom in on the image provided
 * Created by Musenkishi on 2014-03-29.
 */
public class ImageZoomFragment extends DialogFragment {

    public static final String TAG = "com.musenkishi.wally.fragments.ImageZoomFragment";
    private static final String STATE_BITMAP = TAG + ".bitmap";
    private static final String STATE_TOOLBAR_VISIBILITY = TAG + ".toolBarVisibility";
    private static final String STATE_URI_FILE = TAG + ".fileUri";
    private static final String STATE_URI_CONTENT = TAG + ".contentUri";

    private PhotoView zoomableImageView;
    private PhotoViewAttacher photoViewAttacher;
    private Bitmap bitmap;
    private ViewGroup toolBar;
    private View progressBar;
    private int toolBarVisibility = View.GONE;
    private Uri fileUri;
    private Uri contentUri;
    private Rect rect;
    private int statusBarHeightCorrection;
    private int position;

    public static ImageZoomFragment newInstance(Bitmap bitmap){
        ImageZoomFragment fragment = new ImageZoomFragment(bitmap);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static ImageZoomFragment newInstance(Bitmap bitmap, Rect rect) {
        ImageZoomFragment fragment = new ImageZoomFragment(bitmap, rect);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static ImageZoomFragment newInstance(Uri fileUri){
        ImageZoomFragment fragment = new ImageZoomFragment(fileUri);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static ImageZoomFragment newInstance(Uri fileUri, int position) {
        ImageZoomFragment fragment = new ImageZoomFragment(fileUri, position);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ImageZoomFragment() {
    }

    public ImageZoomFragment(Bitmap bitmap, Rect rect) {
        this.bitmap = bitmap;
        this.rect = rect;
    }

    public ImageZoomFragment(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public ImageZoomFragment(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public ImageZoomFragment(Uri fileUri, int position) {
        this.fileUri = fileUri;
        this.position = position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_BITMAP)){
                bitmap = savedInstanceState.getParcelable(STATE_BITMAP);
            }
            if (savedInstanceState.containsKey(STATE_TOOLBAR_VISIBILITY)){
                toolBarVisibility = savedInstanceState.getInt(STATE_TOOLBAR_VISIBILITY, View.GONE);
            }
            if (savedInstanceState.containsKey(STATE_URI_FILE)){
                fileUri = savedInstanceState.getParcelable(STATE_URI_FILE);
            }
            if (savedInstanceState.containsKey(STATE_URI_CONTENT)){
                contentUri = savedInstanceState.getParcelable(STATE_URI_CONTENT);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // safety check
        if (getDialog() == null) {
            return;
        }

        // set the animations to use on showing and hiding the dialog
        getDialog().getWindow().setWindowAnimations(
                R.style.dialog_animation_fade);
        // alternative way of doing it
        //getDialog().getWindow().getAttributes().
        //    windowAnimations = R.style.dialog_animation_fade;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() != null) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.activity_image_zoom);
            dialog.getWindow().setBackgroundDrawableResource(R.color.Transparent);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(layoutParams);
            initToolbar(dialog);
            progressBar = dialog.findViewById(R.id.zoom_loader);
            zoomableImageView = (PhotoView) dialog.findViewById(R.id.image_zoom_photoview);
            if (bitmap != null) {
                if (rect != null) {
                    animateIn(dialog);
                }
            } else if (fileUri != null){
                showLoader();
                Glide.with(getActivity())
                        .load(fileUri)
                        .fitCenter()
                        .listener(new RequestListener<Uri, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                hideLoader();
                                return false;
                            }
                        })
                        .into(zoomableImageView);
            } else {
                dismiss();
            }
            photoViewAttacher = new PhotoViewAttacher(zoomableImageView);
            photoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    dismiss();
                }
            });
            return dialog;
        } else {
            return null;
        }
    }

    private void hideLoader() {
        progressBar.setVisibility(View.GONE);
    }

    private void showLoader() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_TOOLBAR_VISIBILITY, toolBarVisibility);
        if (bitmap != null) {
            outState.putParcelable(STATE_BITMAP, bitmap);
        }
        if (fileUri != null) {
            outState.putParcelable(STATE_URI_FILE, fileUri);
        }
        if (contentUri != null) {
            outState.putParcelable(STATE_URI_CONTENT, contentUri);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void dismiss() {
        if (rect != null) {
            animateOut();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissParent();
                }
            }, 500);
        } else {
            dismissParent();
        }
    }

    private void dismissParent() {
        super.dismiss();
    }

    private void animateIn(final Dialog dialog) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) zoomableImageView.getLayoutParams();
        params.width = rect.right;
        params.height = rect.bottom;
        zoomableImageView.setLayoutParams(params);

        zoomableImageView.setX(rect.left);
        zoomableImageView.setY(rect.top - statusBarHeightCorrection);
        zoomableImageView.setAlpha(0.0f);
        zoomableImageView.setImageBitmap(bitmap);

        WindowManager win = getActivity().getWindowManager();
        Display d = win.getDefaultDisplay();
        int displayWidth = d.getWidth(); // Width of the actual device
        int displayHeight = d.getHeight() + statusBarHeightCorrection;

        ValueAnimator animWidth = ValueAnimator.ofInt(rect.right, displayWidth);
        animWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = zoomableImageView.getLayoutParams();
                layoutParams.width = val;
                zoomableImageView.setLayoutParams(layoutParams);
            }
        });
        animWidth.setDuration(500);
        animWidth.setInterpolator(new LinearOutSlowInInterpolator());
        animWidth.start();

        ValueAnimator animHeight = ValueAnimator.ofInt(rect.bottom, displayHeight);
        animHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = zoomableImageView.getLayoutParams();
                layoutParams.height = val;
                zoomableImageView.setLayoutParams(layoutParams);
            }
        });
        animHeight.setDuration(500);
        animHeight.setInterpolator(new LinearOutSlowInInterpolator());

        animHeight.start();

        if (statusBarHeightCorrection > 0) {
            zoomableImageView.animate()
                    .y(0.0f)
                    .setDuration(300)
                    .start();
        }

        ValueAnimator animDim = ValueAnimator.ofFloat(0.0f, 0.5f);
        animDim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.dimAmount = (Float) valueAnimator.getAnimatedValue();
                dialog.getWindow().setAttributes(layoutParams);
            }
        });
        animDim.setDuration(300);
        animDim.setStartDelay(300);
        animDim.start();
        zoomableImageView.animate().alpha(1.0f).setDuration(300).start();
    }

    private void animateOut() {
        ValueAnimator animWidth = ValueAnimator.ofInt(
                zoomableImageView.getMeasuredWidth(), rect.right
        );
        animWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = zoomableImageView.getLayoutParams();
                layoutParams.width = val;
                zoomableImageView.setLayoutParams(layoutParams);
            }
        });
        animWidth.setDuration(500);
        animWidth.start();

        ValueAnimator animHeight = ValueAnimator.ofInt(
                zoomableImageView.getMeasuredHeight(), rect.bottom
        );
        animHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = zoomableImageView.getLayoutParams();
                layoutParams.height = val;
                zoomableImageView.setLayoutParams(layoutParams);
            }
        });
        animHeight.setDuration(500);
        animHeight.start();
        if (statusBarHeightCorrection > 0) {
            zoomableImageView.animate()
                    .y(-statusBarHeightCorrection)
                    .setDuration(300)
                    .start();
        }
        zoomableImageView.animate().alpha(0.0f).setDuration(500).start();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    private void initToolbar(Dialog dialog) {
        toolBar = (ViewGroup) dialog.findViewById(R.id.zoom_toolbar);
        toolBar.setVisibility(toolBarVisibility);
        if (fileUri != null) {
            Button setAsButton = (Button) toolBar.findViewById(R.id.toolbar_set_as);
            setAsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setImageAsWallpaperPicker(fileUri);
                }
            });
        }
        if (contentUri != null){
            Button deleteButton = (Button) toolBar.findViewById(R.id.toolbar_delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String quantityString = getResources().getQuantityString(R.plurals.wallpapers, 1);
                    String title = String.format(getString(R.string.dialog_delete_title), quantityString);

                    FragmentManager fragmentManager = getFragmentManager();

                    if (fragmentManager != null) {

                        final MaterialDialogFragment materialDialogFragment = new MaterialDialogFragment();
                        materialDialogFragment.setPrimaryColor(getResources().getColor(R.color.Dialog_Button_Delete));
                        materialDialogFragment.setTitle(title);
                        materialDialogFragment.setPositiveButton(R.string.dialog_delete_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                delete(contentUri);
                            }
                        });
                        materialDialogFragment.setNegativeButton(R.string.dialog_delete_negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                materialDialogFragment.dismiss();
                            }
                        });
                        materialDialogFragment.show(fragmentManager, MaterialDialogFragment.TAG);
                    }
                }
            });
        }
    }

    public void showFileOptions(Uri fileUri, Uri contentUri) {
        toolBarVisibility = View.VISIBLE;
        this.fileUri = fileUri;
        this.contentUri = contentUri;
    }

    private void setImageAsWallpaperPicker(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setType("image/*");

        MimeTypeMap map = MimeTypeMap.getSingleton();
        String mimeType = map.getMimeTypeFromExtension("png");
        intent.setDataAndType(fileUri, mimeType);
        intent.putExtra("mimeType", mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, getString(R.string.action_set_as)));
    }

    private void delete(Uri contentUri) {
        getActivity().getContentResolver().delete(contentUri, null, null);

        //Not for SavedImagesFragment, but for the others to know that they should update their content (heart/unheart tiles)
        getActivity().sendBroadcast(new Intent(FileChangeReceiver.FILES_CHANGED));

        dismiss();
    }

}
