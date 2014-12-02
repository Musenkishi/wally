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

package com.musenkishi.wally.activities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.musenkishi.wally.R;
import com.musenkishi.wally.anim.interpolator.EaseInOutBezierInterpolator;
import com.musenkishi.wally.base.BaseActivity;
import com.musenkishi.wally.base.WallyApplication;
import com.musenkishi.wally.dataprovider.DataProvider;
import com.musenkishi.wally.dataprovider.FileManager;
import com.musenkishi.wally.dataprovider.models.DataProviderError;
import com.musenkishi.wally.dataprovider.models.SaveImageRequest;
import com.musenkishi.wally.models.Author;
import com.musenkishi.wally.models.Image;
import com.musenkishi.wally.models.ImagePage;
import com.musenkishi.wally.models.Size;
import com.musenkishi.wally.observers.FileChangeReceiver;
import com.musenkishi.wally.util.Blur;
import com.musenkishi.wally.util.PaletteRequest;
import com.musenkishi.wally.views.ObservableScrollView;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.musenkishi.wally.views.ObservableScrollView.ScrollViewListener;

/**
 * Activity for showing image and any other information about the image.
 * Created by Musenkishi on 2014-03-05 20:23.
 */
public class ImageDetailsActivity extends BaseActivity implements Handler.Callback {

    private static final int MSG_GET_PAGE = 130;
    private static final int MSG_PAGE_FETCHED = 417892;
    private static final int MSG_PAGE_ERROR = 417891;
    private static final int MSG_IMAGE_REQUEST_READY_FOR_SETTING = 987484;
    private static final int MSG_IMAGE_REQUEST_SAVED = 987483;
    private static final int MSG_IMAGE_REQUEST_SAVING = 987482;
    private static final int MSG_SAVE_TO_FILE = 987486;
    private static final int MSG_RENDER_PALETTE = 987487;
    private static final int MSG_SET_IMAGE_AND_PALETTE = 987488;
    private static final int MSG_SCROLL_UP_SCROLLVIEW = 987489;


    public static final String TAG = "com.musenkishi.wally.ImageDetailsActivity";
    private static final String STATE_IMAGE_PAGE = "ImageDetailsActivity.ImagePage";

    public static final String INTENT_EXTRA_IMAGE = TAG + ".Intent.Image";

    private Handler uiHandler;
    private Handler backgroundHandler;

    private ObservableScrollView scrollView;
    private PhotoView photoView;
    private PhotoViewAttacher photoViewAttacher;
    private ImageButton buttonFullscreen;
    private ProgressBar loader;
    private Uri pageUri;
    private ShareActionProvider shareActionProvider;
    private TextView textViewUploader;
    private TextView textViewUploadDate;
    private TextView textViewSource;
    private TextView textViewResolution;
    private TextView textViewCategory;
    private TextView textViewRating;
    private Button buttonSetAs;
    private Button buttonSave;
    private ImagePage imagePage;
    private ViewGroup imageHolder;
    private ViewGroup photoLayoutHolder;

    private int currentHandlerCode;
    private Palette palette;
    private ViewGroup toolbar;
    private ViewGroup specsLayout;
    private Size imageSize;

    private boolean isInFullscreen = false;
    private View detailsViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        setToolbar((Toolbar) findViewById(R.id.toolbar));

        if (getToolbar() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getToolbar().setPadding(0, getStatusBarHeight(), 0, 0);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        final Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            pageUri = Uri.parse(intent.getDataString());
            if ("wally".equalsIgnoreCase(pageUri.getScheme())){
                pageUri = pageUri.buildUpon().scheme("http").build();
            }
        }

        setupViews();
        setupHandlers();

        Size size = new Size(16,9);

        if (intent.hasExtra(INTENT_EXTRA_IMAGE)) {
            final Image image = intent.getParcelableExtra(INTENT_EXTRA_IMAGE);
            final Bitmap thumbBitmap = WallyApplication.getBitmapThumb();

            if (thumbBitmap != null) {

                size = fitToWidthAndKeepRatio(image.getWidth(), image.getHeight());

                photoView.getLayoutParams().width = size.getWidth();
                photoView.getLayoutParams().height = size.getHeight();

                Bitmap blurBitMap;
                try {
                    blurBitMap = Blur.apply(imageHolder.getContext(), thumbBitmap);
                } catch (ArrayIndexOutOfBoundsException e) {
                    //Blur couldn't be applied. Show regular thumbnail instead.
                    blurBitMap = thumbBitmap;
                }
                photoView.setImageBitmap(blurBitMap);
            }
        }
        setupPaddings(size, false);

        if (savedInstanceState == null) {
            getPage(pageUri.toString());
        } else if (savedInstanceState.containsKey(STATE_IMAGE_PAGE)){
            imagePage = savedInstanceState.getParcelable(STATE_IMAGE_PAGE);
        }

        if (imagePage != null) {
            Message msgObj = uiHandler.obtainMessage();
            msgObj.what = MSG_PAGE_FETCHED;
            msgObj.obj = imagePage;
            uiHandler.sendMessage(msgObj);
        } else {
            getPage(pageUri.toString());
        }

    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void setupPaddings(final Size size, boolean animate) {

        int animationDuration = animate ? 300 : 0;

        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoViewAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);

        final int sidePadding = getResources().getDimensionPixelSize(R.dimen.activity_details_scrollview_side_padding);
        int fabPadding = getResources().getDimensionPixelSize(R.dimen.fab_padding_positive);

        int minimumAllowedHeight = fabPadding;

        if (size.getHeight() < minimumAllowedHeight) {
            size.setHeight(size.getHeight());
            ValueAnimator valueAnimator = ValueAnimator.ofInt(photoLayoutHolder.getPaddingTop());
            valueAnimator.setInterpolator(new EaseInOutBezierInterpolator());
            valueAnimator.setDuration(animationDuration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    photoLayoutHolder.setPadding(
                            0,
                            (Integer) valueAnimator.getAnimatedValue(),
                            0,
                            0
                    );
                }
            });
            valueAnimator.start();
        } else {
            photoLayoutHolder.setPadding(
                    0,
                    0,
                    0,
                    0
            );
        }

        scrollView.setPadding(
                0,
                0,
                0,
                -fabPadding
        );
        specsLayout.setPadding(0, 0, 0, fabPadding);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(detailsViewGroup.getPaddingTop(), size.getHeight());
        valueAnimator.setInterpolator(new EaseInOutBezierInterpolator());
        valueAnimator.setDuration(animationDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                detailsViewGroup.setPadding(sidePadding,
                        (Integer) valueAnimator.getAnimatedValue(),
                        sidePadding,
                        detailsViewGroup.getPaddingBottom());
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imagePage != null) {
            outState.putParcelable(STATE_IMAGE_PAGE, imagePage);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_details, menu);

        MenuItem shareMenuItem = menu.findItem(R.id.action_share);

        if (shareMenuItem != null) {
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, pageUri.toString());
            shareActionProvider.setShareIntent(shareIntent);
            shareMenuItem.setIcon(R.drawable.ic_action_share);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                return false;
            case R.id.action_open:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, pageUri);
                startActivity(browserIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        Drawable heart = getResources().getDrawable(R.drawable.ic_action_saved);
        heart.clearColorFilter();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Glide.clear(photoView);
        backgroundHandler.removeCallbacksAndMessages(null);
        uiHandler.removeCallbacksAndMessages(null);
        backgroundHandler.getLooper().quit();
        super.onDestroy();
    }

    private void setupViews() {
        scrollView = (ObservableScrollView) findViewById(R.id.image_details_scrollview);
        imageHolder = (ViewGroup) findViewById(R.id.image_details_imageview_holder);
        photoView = (PhotoView) findViewById(R.id.image_details_imageview);
        buttonFullscreen = (ImageButton) findViewById(R.id.image_details_button_fullscreen);
        loader = (ProgressBar) findViewById(R.id.image_details_loader);
        textViewUploader = (TextView) findViewById(R.id.image_details_uploader);
        textViewUploadDate = (TextView) findViewById(R.id.image_details_upload_date);
        textViewSource = (TextView) findViewById(R.id.image_details_source);
        textViewResolution = (TextView) findViewById(R.id.image_details_resolution);
        textViewCategory = (TextView) findViewById(R.id.image_details_category);
        textViewRating = (TextView) findViewById(R.id.image_details_rating);
        buttonSetAs = (Button) findViewById(R.id.toolbar_set_as);
        buttonSave = (Button) findViewById(R.id.toolbar_save);
        toolbar = (ViewGroup) findViewById(R.id.image_details_toolbar);
        photoLayoutHolder = (ViewGroup) findViewById(R.id.image_details_photo_layout_holder);
        specsLayout = (ViewGroup) findViewById(R.id.image_details_specs);
        detailsViewGroup = findViewById(R.id.image_details_layout);

        specsLayout.setAlpha(0.0f);

        int sidePadding = getResources().getDimensionPixelSize(R.dimen.activity_details_scrollview_side_padding);
        int fabPadding = getResources().getDimensionPixelSize(R.dimen.fab_padding_positive);
        scrollView.setPadding(0, 0, 0, -fabPadding);
        specsLayout.setPadding(0, 0, 0, fabPadding);
        photoLayoutHolder.setPadding(0, 0, 0, 0);
        detailsViewGroup.setPadding(
                sidePadding,
                detailsViewGroup.getPaddingTop(),
                sidePadding,
                detailsViewGroup.getPaddingBottom()
        );

        photoViewAttacher = new PhotoViewAttacher(photoView);
        photoViewAttacher.setZoomable(false);
        photoViewAttacher.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    @Override
    public void onBackPressed() {
        if (isInFullscreen()) {
            toggleZoomImage();
        } else {
            super.onBackPressed();
        }
    }

    private Size fitToWidthAndKeepRatio(int width, int height) {
        WindowManager win = getWindowManager();
        Display d = win.getDefaultDisplay();
        int displayWidth = d.getWidth(); // Width of the actual device

        int fittedHeight = height;
        int fittedWidth = width;

        fittedHeight = displayWidth * fittedHeight / fittedWidth;
        fittedWidth = displayWidth;

        return new Size(fittedWidth, fittedHeight);
    }

    private void enableParallaxEffect(ObservableScrollView scrollView, final View parallaxingView) {
        scrollView.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView,
                                        int x, int y, int oldx, int oldy) {

                WindowManager win = getWindowManager();
                Display d = win.getDefaultDisplay();
                int displayHeight = d.getHeight(); // Height of the actual device

                if (imageSize.getHeight() > displayHeight) {
                    float[] values = new float[9];
                    photoViewAttacher.getDrawMatrix().getValues(values);
                    float imageHeight = imageSize.getHeight();

                    float diff = imageHeight/displayHeight;

                    if (y > oldy) {
                        diff = -diff;
                    }

                    photoViewAttacher.onDrag(0, diff);

                } else {
                    float pY = -(y / 3.0f);
                    parallaxingView.setTranslationY(pY);
                }

            }
        });
    }

    private void saveImage(boolean notifyUser) {
        if (notifyUser) {
            uiHandler.sendEmptyMessage(MSG_IMAGE_REQUEST_SAVING);
        }
        Message msgObj = backgroundHandler.obtainMessage();
        msgObj.what = MSG_SAVE_TO_FILE;
        msgObj.arg1 = MSG_IMAGE_REQUEST_SAVED;
        backgroundHandler.sendMessage(msgObj);
    }

    private void setImageAsWallpaperPicker(Uri path) {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setType("image/*");

        MimeTypeMap map = MimeTypeMap.getSingleton();
        String mimeType = map.getMimeTypeFromExtension("png");
        intent.setDataAndType(path, mimeType);
        intent.putExtra("mimeType", mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, getString(R.string.action_set_as)));
    }

    private void getPage(String url) {
        Message msgGetPage = backgroundHandler.obtainMessage();
        msgGetPage.what = MSG_GET_PAGE;
        msgGetPage.obj = url;
        backgroundHandler.sendMessage(msgGetPage);
    }

    private void setupHandlers() {
        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper(), this);
        uiHandler = new Handler(getMainLooper(), this);
    }

    private void renderColors(Bitmap bitmap) {
        Message msgObj = backgroundHandler.obtainMessage();
        msgObj.what = MSG_RENDER_PALETTE;
        msgObj.obj = bitmap;
        msgObj.arg1 = MSG_IMAGE_REQUEST_SAVED;
        backgroundHandler.sendMessage(msgObj);
    }

    private void setColors(Palette palette){
        this.palette = palette;
        hideLoader();

        Palette.Swatch swatch = PaletteRequest.getBestSwatch(palette, palette.getDarkMutedSwatch());
        if (swatch != null) {
            photoLayoutHolder.setBackgroundColor(swatch.getRgb());
            findViewById(R.id.image_details_root).setBackgroundColor(swatch.getRgb());

            Drawable floatingButtonBackground = getResources()
                    .getDrawable(R.drawable.floating_action_button);
            Drawable floatingButtonIcon = getResources().getDrawable(R.drawable.ic_mask_fullscreen);

            Palette.Swatch fabSwatch = PaletteRequest.getBestSwatch(palette,
                    palette.getVibrantSwatch());
            if (fabSwatch != null) {
                floatingButtonBackground.setColorFilter(fabSwatch.getRgb(),
                        PorterDuff.Mode.MULTIPLY);
                floatingButtonIcon.setColorFilter(fabSwatch.getBodyTextColor(),
                        PorterDuff.Mode.MULTIPLY);
            }

            buttonFullscreen.setBackgroundDrawable(floatingButtonBackground);
            buttonFullscreen.setImageDrawable(floatingButtonIcon);
            buttonFullscreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleZoomImage();
                }
            });

            toolbar.setBackgroundColor(swatch.getRgb());
            buttonSetAs.setTextColor(swatch.getBodyTextColor());
            buttonSave.setTextColor(swatch.getBodyTextColor());
            setToolbarClickListeners();

            animateToolbar(View.VISIBLE);
        }

    }

    /**
     * Animations animations animations.
     * @param visibility if VISIBLE, expands toolbar.
     */
    private void animateToolbar(int visibility) {
        float from;
        float to;
        int toolbarOffset;
        int fabOffset;
        if (visibility == View.VISIBLE) {
            from = 0.0f;
            to = 1.0f;
            fabOffset = 200;
            toolbarOffset = 0;
        } else {
            from = 1.0f;
            to = 0.0f;
            fabOffset = 0;
            toolbarOffset = 200;
        }

        buttonFullscreen.animate()
                .scaleX(to)
                .scaleY(to)
                .setDuration(400)
                .setStartDelay(fabOffset)
                .setInterpolator(new EaseInOutBezierInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        buttonFullscreen.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                })
                .start();

        int toolbarFrom;
        int toolbarTo;

        if (from > 0.0f) {
            toolbarFrom = getResources().getDimensionPixelSize(R.dimen.details_toolbar_height);
            toolbarTo = 0;
        } else {
            toolbarFrom = 0;
            toolbarTo = getResources().getDimensionPixelSize(R.dimen.details_toolbar_height);
        }

        ValueAnimator valueAnimator = ValueAnimator.ofInt(toolbarFrom, toolbarTo);
        valueAnimator.setDuration(400);
        valueAnimator.setStartDelay(toolbarOffset);
        valueAnimator.setInterpolator(new EaseInOutBezierInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams toolbarParams =
                        (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
                toolbarParams.height = val;
                toolbar.setLayoutParams(toolbarParams);
            }
        });
        valueAnimator.start();

    }

    private void setToolbarClickListeners() {
        buttonSetAs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msgObj = backgroundHandler.obtainMessage();
                msgObj.what = MSG_SAVE_TO_FILE;
                msgObj.arg1 = MSG_IMAGE_REQUEST_READY_FOR_SETTING;
                backgroundHandler.sendMessage(msgObj);
            }
        });
        updateSaveButton();
    }

    private void updateSaveButton() {
        if (imagePage != null) {
            FileManager fileManager = new FileManager();
            boolean imageExists = fileManager.fileExists(imagePage.imageId());
            if (imageExists) {
                buttonSave.setClickable(false);
                buttonSave.setText(R.string.saved);
                buttonSave.setAlpha(0.5f);
            } else {
                if (!buttonSave.isClickable()){
                    buttonSave.setClickable(true);
                    buttonSave.setText(R.string.action_save);
                }
                buttonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setClickable(false);
                        saveImage(true);
                    }
                });
            }
        }
    }

    private void saveToFile(final int handlerCode) {
        currentHandlerCode = handlerCode;
        SaveImageRequest saveImageRequest = WallyApplication
                .getDataProviderInstance()
                .downloadImageIfNeeded(
                        imagePage.imagePath(),
                        pageUri.getLastPathSegment(),
                        getResources().getString(R.string.notification_title_image_saving));

        if (saveImageRequest.getDownloadID() != null){
            WallyApplication.getDownloadIDs().put(saveImageRequest.getDownloadID(), pageUri.getLastPathSegment());
        } else {
            handleSavedImageData(saveImageRequest.getFilePath());
        }
    }

    @Override
    protected void handleReceivedIntent(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
        if (WallyApplication.getDownloadIDs().containsKey(id)){
            WallyApplication.getDownloadIDs().remove(id);
            updateSaveButton();
            if (palette != null && palette.getVibrantSwatch() != null) {
                startHeartPopoutAnimation(buttonSave, palette.getVibrantSwatch().getBodyTextColor());
            } else {
                startHeartPopoutAnimation(buttonSave, Color.WHITE);
            }
            String filename = pageUri.getLastPathSegment();
            handleSavedImageData(WallyApplication.getDataProviderInstance().getFilePath(filename));
        }
    }

    private void handleSavedImageData(Uri filePath) {
        if (filePath != null && filePath.getPath() != null) {
            Message msgObj = uiHandler.obtainMessage();
            msgObj.what = currentHandlerCode;
            msgObj.obj = filePath;
            uiHandler.sendMessage(msgObj);
            MediaScannerConnection.scanFile(getApplicationContext(),
                    new String[]{filePath.getPath()},
                    null,
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {

                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            getApplication().sendBroadcast(new Intent(FileChangeReceiver.FILES_CHANGED));
                        }
                    }
            );
        }
    }

    private void showLoader() {
        loader.animate().alpha(1.0f).setDuration(300).start();
    }

    private void hideLoader() {
        loader.animate().alpha(0.0f).setDuration(300).start();
    }

    /**
     * Returns true if fullscreen mode is currently active.
     */
    private boolean isInFullscreen() {
        return isInFullscreen;
    }

    /**
     * Animations animations animations.
     */
    private void toggleZoomImage() {

        int animationDuration = 400;

        if (isInFullscreen()){
            scrollView.smoothScrollTo(0, (Integer) scrollView.getTag());
            photoViewAttacher.setScale(1.0f, true);
        } else {
            scrollView.setTag(scrollView.getScrollY());
            scrollView.smoothScrollTo(0, 0);
        }

        if (getSupportActionBar() != null) {
            getToolbar().animate()
                    .translationY(isInFullscreen() ? 0.0f : -getToolbar().getMeasuredHeight())
                    .alpha(isInFullscreen() ? 1.0f : 0.0f)
                    .setDuration(500)
                    .setInterpolator(new EaseInOutBezierInterpolator())
                    .start();
        }

        findViewById(R.id.image_details_protective_shadow).animate()
                .alpha(isInFullscreen() ? 1.0f : 0.0f)
                .setDuration(500)
                .setInterpolator(new EaseInOutBezierInterpolator())
                .start();

        int minimumAllowedHeight = getToolbar().getMeasuredHeight()
                + getResources().getDimensionPixelSize(R.dimen.fab_padding_positive);

        if (imageSize.getHeight() < minimumAllowedHeight) {
            int topFrom;
            int topTo;
            if (isInFullscreen()) {
                topFrom = 0;
                topTo = getToolbar().getMeasuredHeight();
            } else {
                topFrom = photoLayoutHolder.getPaddingTop();
                topTo = 0;
            }
            ValueAnimator topValueAnimator = ValueAnimator.ofInt(topFrom, topTo);
            topValueAnimator.setDuration(animationDuration);
            topValueAnimator.setInterpolator(new EaseInOutBezierInterpolator());
            topValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    photoLayoutHolder.setPadding(
                            photoLayoutHolder.getPaddingLeft(),
                            val,
                            photoLayoutHolder.getPaddingRight(),
                            photoLayoutHolder.getPaddingBottom()
                    );
                }
            });
            topValueAnimator.start();
        }



        if (photoLayoutHolder.getTranslationY() > 0.0f) {
            photoLayoutHolder.animate()
                    .translationY(0.0f)
                    .setInterpolator(new EaseInOutBezierInterpolator())
                    .setDuration(animationDuration)
                    .start();
        }

        WindowManager win = getWindowManager();
        Display d = win.getDefaultDisplay();

        int from = photoView.getMeasuredHeight();
        int to = isInFullscreen() ? imageSize.getHeight() : d.getHeight();

        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.setDuration(animationDuration);
        valueAnimator.setInterpolator(new EaseInOutBezierInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams toolbarParams =
                        (RelativeLayout.LayoutParams) photoView.getLayoutParams();
                toolbarParams.height = val;
                photoView.setLayoutParams(toolbarParams);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}
            @Override
            public void onAnimationEnd(Animator animator) {
                photoViewAttacher.setZoomable(true);
                photoView.setZoomable(true);
                photoViewAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                    @Override
                    public void onPhotoTap(View view, float v, float v2) {
                        toggleZoomImage();
                    }
                });
            }
            @Override
            public void onAnimationCancel(Animator animator) {}
            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        valueAnimator.start();

        int scrollTo = isInFullscreen() ? 0 : d.getHeight();

        scrollView.animate()
                .y(scrollTo)
                .setDuration(animationDuration)
                .setInterpolator(new EaseInOutBezierInterpolator())
                .start();

        isInFullscreen = !isInFullscreen;
    }

    private void scrollUpToolbarIfNeeded() {
        Display thisDisplay = getWindowManager().getDefaultDisplay();
        int screenHeight = thisDisplay.getHeight();
        int emptySpace = detailsViewGroup.getPaddingTop();
        int targetHeight = emptySpace - ((screenHeight/3)*2);
        int navBarHeight = 0;
        if (imageSize.getHeight() > (screenHeight - navBarHeight)) {
            Message msgObj = uiHandler.obtainMessage();
            msgObj.what = MSG_SCROLL_UP_SCROLLVIEW;
            msgObj.arg1 = targetHeight;
            uiHandler.sendMessageDelayed(msgObj, 500);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {

            case MSG_GET_PAGE:
                String url = (String) msg.obj;
                if (url != null) {
                    WallyApplication.getDataProviderInstance().getPageData(
                            url,
                            new DataProvider.OnPageReceivedListener() {
                                @Override
                                public void onPageReceived(ImagePage imagePage) {
                                    Message msgObj = uiHandler.obtainMessage();
                                    msgObj.what = MSG_PAGE_FETCHED;
                                    msgObj.obj = imagePage;
                                    uiHandler.sendMessage(msgObj);
                                }

                                @Override
                                public void onError(DataProviderError error) {
                                    Message msgObj = uiHandler.obtainMessage();
                                    msgObj.what = MSG_PAGE_ERROR;
                                    msgObj.obj = error;
                                    uiHandler.sendMessage(msgObj);
                                }
                            });
                }
                break;

            case MSG_SAVE_TO_FILE:
                int what = msg.arg1;
                saveToFile(what);
                break;

            case MSG_PAGE_FETCHED:
                imagePage = (ImagePage) msg.obj;
                if (imagePage != null) {
                    textViewUploader.setText(imagePage.uploader());
                    textViewUploadDate.setText(imagePage.uploadDate());
                    textViewSource.setText(imagePage.author().name());
                    if (imagePage.author().page() != Uri.EMPTY) {
                        textViewSource.setTextColor(getResources()
                                        .getColor(R.color.Holo_Blue_Dark)
                        );
                        textViewSource.setTag(imagePage.author());
                        textViewSource.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Author author = (Author) v.getTag();
                                if (author.page() != Uri.EMPTY) {
                                    Intent browserIntent = new Intent(
                                            Intent.ACTION_VIEW,
                                            author.page()
                                    );
                                    startActivity(browserIntent);
                                }
                            }
                        });
                    }
                    textViewResolution.setText(imagePage.resolution());
                    textViewCategory.setText(imagePage.category());
                    textViewRating.setText(imagePage.rating());
                    final String imageUrl = imagePage.imagePath().toString();

                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .placeholder(photoViewAttacher.getImageView().getDrawable())
                            .fitCenter()
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e,
                                                           String model,
                                                           Target<GlideDrawable> target,
                                                           boolean isFirstResource) {
                                    hideLoader();
                                    //TODO: maybe show a retry button?
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource,
                                                               String model,
                                                               Target<GlideDrawable> target,
                                                               boolean isFromMemoryCache,
                                                               boolean isFirstResource) {

                                    photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    photoViewAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                    renderColors(
                                            ((GlideBitmapDrawable) resource.getCurrent())
                                                    .getBitmap()
                                    );
                                    photoView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            toggleZoomImage();
                                        }
                                    });
                                    hideLoader();

                                    Size size = fitToWidthAndKeepRatio(
                                            imagePage.getImageWidth(),
                                            imagePage.getImageHeight()
                                    );

                                    imageSize = size;

                                    setupPaddings(size, true);

                                    enableParallaxEffect(scrollView, photoLayoutHolder);

                                    scrollUpToolbarIfNeeded();

                                    return false;
                                }
                            })
                            .into(photoView);

                    specsLayout.setAlpha(1.0f);
                    specsLayout.scheduleLayoutAnimation();

                }
                break;

            case MSG_PAGE_ERROR:
                DataProviderError dataProviderError = (DataProviderError) msg.obj;
                if (dataProviderError != null) {
                    Toast.makeText(ImageDetailsActivity.this, dataProviderError.getHttpStatusCode()
                            + ": "
                            + dataProviderError.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ImageDetailsActivity.this,
                            getString(R.string.toast_error_default),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case MSG_IMAGE_REQUEST_READY_FOR_SETTING:
                Uri savedImageDataForWallpaper = (Uri) msg.obj;
                if (savedImageDataForWallpaper != null) {
                    setImageAsWallpaperPicker(savedImageDataForWallpaper);
                }
                break;

            case MSG_IMAGE_REQUEST_SAVED:
                updateSaveButton();
                break;

            case MSG_IMAGE_REQUEST_SAVING:
                if (buttonSave != null) {
                    buttonSave.setText(getString(R.string.saving));
                    buttonSave.setAlpha(0.50f);
                }
                break;

            case MSG_RENDER_PALETTE:

                Bitmap bitmap1 = (Bitmap) msg.obj;
                Palette colorScheme = Palette.generate(bitmap1);

                Message msgObj = uiHandler.obtainMessage();
                msgObj.what = MSG_SET_IMAGE_AND_PALETTE;
                msgObj.obj = colorScheme;
                uiHandler.sendMessage(msgObj);

                break;

            case MSG_SET_IMAGE_AND_PALETTE:
                Palette renderedPalette = (Palette) msg.obj;
                setColors(renderedPalette);
                break;

            case MSG_SCROLL_UP_SCROLLVIEW:
                int targetScroll = msg.arg1;
                scrollView.smoothScrollTo(0, targetScroll);
                break;

        }
        return false;
    }
}
